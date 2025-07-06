// src/main/scala/SparkAnalyzer.scala

package analyzer
import org.apache.spark.sql.SparkSession

object SparkAnalyzer extends App {
  val spark = SparkSession.builder
    .appName("DroneDataAnalyzer")
    .master("local[*]")
    .getOrCreate()

  // Lire les fichiers JSON stockés (inspiré de Spark SQL JSON docs:contentReference[oaicite:7]{index=7})
  val df = spark.read.json("drones_output.json")
  df.printSchema()
  df.show(false)  
  df.createOrReplaceTempView("drones")

  // Q1: Nombre total de messages par drone
  val q1 = spark.sql("SELECT droneId, COUNT(*) AS total_msgs FROM drones GROUP BY droneId")
  // Q2: Nombre d'alertes par drone
  val q2 = spark.sql("SELECT droneId, COUNT(*) AS total_alerts FROM drones WHERE status = 'alert' GROUP BY droneId")
  // Q3: Statistiques basiques (moyenne metrics) par statut
  val q3 = spark.sql("SELECT status, AVG(metric1) AS avg1, AVG(metric2) AS avg2 FROM drones GROUP BY status")
  // Q4: Liste des positions (latitude, longitude) uniques pour chaque droneId
  val q4 = spark.sql("SELECT droneId, COLLECT_SET(struct(latitude, longitude)) AS coords FROM drones GROUP BY droneId")

  // (Ici on se contente d'afficher les plans d'exécution)
  q1.show()
  q2.show()
  q3.show()
  q4.show()

  spark.stop()
}
