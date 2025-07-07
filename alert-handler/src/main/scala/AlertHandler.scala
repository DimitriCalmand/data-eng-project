// alert-handler/src/main/scala/AlertHandler.scala
import java.time.Duration
import java.util.Properties
import org.apache.kafka.clients.consumer.{KafkaConsumer, ConsumerConfig}
import scala.jdk.CollectionConverters._
import scala.annotation.tailrec

object AlertHandler extends App {
  // Configuration du consommateur Kafka pour le topic "alerts"
  val props = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, "alert-handler-group")
  // Par défaut, auto.offset.reset = "latest" (on lira les nouvelles alertes arrivant après le démarrage)
  val consumer = new KafkaConsumer[String, String](props)
  consumer.subscribe(java.util.Arrays.asList("alerts"))

  // Boucle infinie de consommation des alertes
  @tailrec
  def loop(): Unit = {
    val records = consumer.poll(Duration.ofSeconds(1))
    records.asScala.foreach { record =>
      // Affichage de chaque alerte reçue dans la console
      println(s"ALERTE reçue: ${record.value()}")
    }
    loop()  // continuer la boucle de lecture
  }

  // Démarrage de l'écoute des alertes
  loop()
}
