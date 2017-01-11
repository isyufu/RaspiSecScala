package raspiSec

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util
import java.util.{Timer, TimerTask}

import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import com.pengrad.telegrambot.model.{Message, Update}
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.{TelegramBotAdapter, UpdatesListener}

import collection.JavaConverters._
import scala.collection.mutable
import sys.process._
/**
  * Created by Ivan on 20.12.2016.
  */

object TeleBot {
  var whiteList =  Main.ini.get("WhiteList")
  val alertlog = List.newBuilder[String]
  var protect = false
  val bot = TelegramBotAdapter.build(Main.ini.get("TelegramBot", "Token"))

  def start() = bot.setUpdatesListener(new UpdatesListener {
    override def process(updates: util.List[Update]) = {
      updates.asScala.foreach(x => {
        println("recive: "+x.message())
        val request:Option[SendMessage] = x.message().text() match {
          case "/ping" => checkUser(x.message(), () => {"pong"})
          case "/on" => checkUser(x.message(), () => {protect = true; "ok "})
          case "/off" => checkUser(x.message(), () => { protect = false; "ok "})
          case "/log" => checkUser(x.message(), () => {
            var s = alertlog.result().mkString("\n"); alertlog.clear(); s
          })
          case "/hello" => checkUser(x.message(), () => {"ok"}).map(_.replyMarkup(markup()))
          case "/publish" => checkUser(x.message(), () => {publish("test warning"); "ok"})
          case "/help" => checkUser(x.message(), () => {
            """
              | /ping -> pong
              | /hello - create keyboard
              | /on - alarm On
              | /off - alarm Off
              | /log - show last sensors events
              | /help - show this help
              | /publish - push 'test warning' to whitelist
              | !/adduser {username}
              | !/savechat
              | !/removeuser {username}
              | !/putsensor {sensorname} {gpio}
              | !/removesensor {sensorname} {gpio}
            """.stripMargin
          })
          case _ => None
        }
        request.foreach(bot execute _)

      })
      UpdatesListener.CONFIRMED_UPDATES_ALL
    }
  })

  def markup() = new ReplyKeyboardMarkup(
    Array("/ping","/on","/off","/log")
  )

  def checkUser(msg:Message, f:()=> String):Option[SendMessage] = {
    if(whiteList.containsKey(msg.from.username())){
      Some(new SendMessage(msg.chat().id(), f()))
    } else
      None
  }

  def publish(s:String) = {
    whiteList.asScala.foreach((kv) => {
      bot.execute(new SendMessage(kv._2.toLong, s))
    })
  }

  val dtf =  DateTimeFormatter.ISO_LOCAL_DATE_TIME
  def alert(device: String, value:Int) = {
    val text = s"${LocalDateTime.now().format(dtf)} $device $value"
    println(text)
    alertlog += text
    if (protect) {
      publish(text)
    }
  }
}

object GPIOListener {
  val sensors = Main.ini.get("GPIO")
  var statuses = mutable.HashMap[String, Int]()
  var statusesOld = mutable.Map[String, Int]()

  def start() = new Timer().schedule(new TimerTask {
    override def run() = {
      for (name <- sensors.keySet().asScala) {
        import scala.util.control.Exception.catching
        catching(classOf[Exception])
          .either(("/usr/bin/gpio read " + sensors.get(name)).!!.stripLineEnd.toInt) match {
          case Right(i) => {
            statuses.put(name, i)
            statusesOld.get(name).foreach(i2 =>  if (i2 != i) TeleBot.alert(name, i2))
            statusesOld.put(name, i)
          }
          case Left(e) => println(s"exc:${e.getLocalizedMessage} ")
        }
      }
    }
  }, 1000L, 500L)
}