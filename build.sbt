name := "RaspiSecScala"
//javaHome := Some(new File("C:/Program Files/Java/jdk1.8.0_111"))

version := "1.0"

scalaVersion := "2.12.1"

resolvers += Resolver.sonatypeRepo("snapshots")
mainClass in Compile := Some("raspiSec.Main")

libraryDependencies ++= Seq(
  "org.ini4j" % "ini4j" % "0.5.4",
  "org.tinylog" % "tinylog" % "1.1",
  "com.github.pengrad" % "java-telegram-bot-api" % "2.3.1"
)