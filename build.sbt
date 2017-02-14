name := "sentiment-english-mllib"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.poi" % "poi-ooxml" % "3.9"
libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.json4s" %% "json4s-jackson" % "3.2.10"
)

val overrideScalaVersion = "2.11.8"
val sparkVersion = "2.0.0"

//Override Scala Version to the above 2.11.8 version
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

resolvers ++= Seq(
  "All Spark Repository -> bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven/"
)

libraryDependencies ++= Seq(
  "org.apache.spark"      %%  "spark-core"      %   sparkVersion  exclude("jline", "2.12"),
  "org.apache.spark"      %% "spark-mllib"      % sparkVersion
)