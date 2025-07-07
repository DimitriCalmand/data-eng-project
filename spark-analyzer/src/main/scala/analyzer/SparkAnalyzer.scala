// src/main/scala/SparkAnalyzer.scala

package analyzer
import org.apache.spark.sql.SparkSession

object SparkAnalyzer extends App {
  val spark = SparkSession.builder
    .appName("BinDataAnalyzer")
    .master("local[*]")
    .getOrCreate()

  // Lire les fichiers JSON stockés (inspiré de Spark SQL JSON docs:contentReference[oaicite:7]{index=7})
  val df = spark.read.json("bins_output.json")
  df.printSchema()
  df.show(false)  
  df.createOrReplaceTempView("bins")

  // Q1: Nombre total de messages par poubelle
  val q1 = spark.sql("SELECT binId, COUNT(*) AS total_msgs FROM bins GROUP BY binId")
  // Q2: Nombre d'alertes par poubelle
  val q2 = spark.sql("SELECT binId, COUNT(*) AS total_alerts FROM bins WHERE status = 'alert' GROUP BY binId")
  // Q3: Statistiques basiques (moyenne metrics) par statut
  val q3 = spark.sql("SELECT status, AVG(metric1) AS avg1, AVG(metric2) AS avg2 FROM bins GROUP BY status")
  // Q4: Liste des positions (latitude, longitude) uniques pour chaque binId
  val q4 = spark.sql("SELECT binId, COLLECT_SET(struct(latitude, longitude)) AS coords FROM bins GROUP BY binId")

  // (Ici on se contente d'afficher les plans d'exécution)
  q1.show()
  q2.show()
  q3.show()
  q4.show()

  spark.stop()
}
