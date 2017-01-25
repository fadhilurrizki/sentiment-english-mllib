name := "sentiment-english-mllib"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.poi" % "poi-ooxml" % "3.9"
libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.json4s" %% "json4s-jackson" % "3.3.0"
)