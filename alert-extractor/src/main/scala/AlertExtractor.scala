// alert-extractor/src/main/scala/AlertExtractor.scala
import java.time.Duration
import java.util.Properties
import org.apache.kafka.clients.consumer.{KafkaConsumer, ConsumerConfig}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import scala.jdk.CollectionConverters._
import scala.annotation.tailrec

// Même structure de message que dans BinSimulator (pour référence, ici non exploitée directement)
case class BinMsg(timestamp: String, binId: String, latitude: Double, longitude: Double,
                    metric1: Double, metric2: Double, status: String)

object AlertExtractor extends App {
  // Configuration du consommateur Kafka (lecture du topic "bins")
  val cprops = new Properties()
  cprops.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  cprops.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  cprops.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  cprops.put(ConsumerConfig.GROUP_ID_CONFIG, "alert-extractor-group")
  // Optionnel: on peut définir auto.offset.reset à "earliest" pour lire les anciens messages si nécessaires
  // cprops.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  val consumer = new KafkaConsumer[String, String](cprops)
  consumer.subscribe(java.util.Arrays.asList("bins"))

  // Configuration du producteur Kafka (écriture vers le topic "alerts")
  val pprops = new Properties()
  pprops.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  pprops.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  pprops.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[String, String](pprops)

  // Boucle de polling récursive pour consommer en continu
  @tailrec
  def loop(): Unit = {
    val records = consumer.poll(Duration.ofSeconds(1))  // récupération des messages (attente 1s max)
    records.asScala.foreach { record =>
      val json = record.value()       // le message JSON complet du bin
      // Filtrage basé sur le contenu JSON : on vérifie si "status":"alert" est présent
      if (json.contains("\"status\":\"alert\"")) {
        // Si c'est une alerte, on la réémet sur le topic "alerts"
        producer.send(new ProducerRecord[String, String]("alerts", json))
      }
    }
    loop()  // récursion tail-recursive pour continuer la boucle infinie
  }

  // Démarrage de l'extraction des alertes
  loop()
}
