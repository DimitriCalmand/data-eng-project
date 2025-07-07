// spark-analyzer/src/main/scala/SparkAnalyzer.scala
import org.apache.spark.sql.SparkSession

object SparkAnalyzer extends App {
  // Initialiser une session Spark en local
  val spark = SparkSession.builder()
    .appName("BinDataAnalyzer")
    .master("local[*]")
    .getOrCreate()

  // Lecture du fichier JSON généré par Kafka Connect (chaque ligne = un objet JSON)
  val df = spark.read.json("bins_output.json")
  df.printSchema()        // Afficher le schéma déduit des données
  df.show(truncate = false)  // Afficher quelques lignes des données brutes

  // Créer une vue temporaire pour utiliser SQL
  df.createOrReplaceTempView("bins")

  // Q1: Nombre total de messages par bin (agrégation COUNT)
  val q1 = spark.sql("""
    SELECT binId, COUNT(*) AS total_msgs
    FROM bins
    GROUP BY binId
  """)

  // Q2: Nombre d'alertes par bin (filtre sur status = 'alert')
  val q2 = spark.sql("""
    SELECT binId, COUNT(*) AS total_alerts
    FROM bins
    WHERE status = 'alert'
    GROUP BY binId
  """)

  // Q3: Statistiques moyennes des métriques par statut (alert/ok)
  val q3 = spark.sql("""
    SELECT status, AVG(metric1) AS avg_metric1, AVG(metric2) AS avg_metric2
    FROM bins
    GROUP BY status
  """)

  // Q4: Positions uniques (latitude, longitude) reportées par chaque bin
  val q4 = spark.sql("""
    SELECT binId,
           COLLECT_SET(named_struct('lat', latitude, 'lon', longitude)) AS unique_positions
    FROM bins
    GROUP BY binId
  """)

  // Afficher les résultats de chaque requête
  println("** Nombre total de messages par bin **")
  q1.show()

  println("** Nombre d'alertes par bin **")
  q2.show()

  println("** Moyenne des métriques par statut **")
  q3.show()

  println("** Positions uniques par bin **")
  q4.show(truncate = false)

  // Arrêt de la session Spark
  spark.stop()
}
