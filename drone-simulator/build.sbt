// drone-simulator/build.sbt
ThisBuild / scalaVersion := "2.13.10"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.5.0"

ThisBuild / javacOptions ++= Seq("--release", "17")  // évite de générer du byte-code 21 inutile
ThisBuild / scalacOptions += "-release:17"
