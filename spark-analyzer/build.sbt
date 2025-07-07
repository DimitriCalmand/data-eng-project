// spark-analyzer/build.sbt
ThisBuild / scalaVersion := "2.13.10"

// --- options spécifiques Spark / JDK 17+
fork := true                               // sbt lancera une JVM séparée
javaOptions ++= Seq(
  // Spark >= 3.4 a seulement besoin de ces deux lignes pour JDK 17+
  "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
  "--add-opens=java.base/java.nio=ALL-UNNAMED"
)
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.0"
