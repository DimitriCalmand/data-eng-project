import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

// Autorise l'accès aux classes internes nécessaires à Spark
javaOptions += "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED"
lazy val root = (project in file("."))
  .settings(
    name := "spark-analyzer",
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-clients" % "3.5.0",
      "org.apache.spark" %% "spark-sql"     % "3.5.0",
      munit % Test
    )
  )
