package raspiSec

import java.io.File
import org.ini4j.Wini
/**
  * Created by Ivan on 20.12.2016.
  */

object Main extends App{
  val ini = new Wini(new File("config.ini"))
  println("starting telegram bot...")
  TeleBot.start()
  println("starting gpio listener...")
  GPIOListener.start()
  println("working...")
}


