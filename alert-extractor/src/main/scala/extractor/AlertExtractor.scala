// src/main/scala/AlertExtractor.scala
import java.time.Duration
import java.util.Properties
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.consumer.{KafkaConsumer, ConsumerConfig}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.jdk.CollectionConverters._

case class BinMsg(timestamp: String, binId: String, latitude: Double, longitude: Double,
                    metric1: Double, metric2: Double, status: String)

object AlertExtractor extends App {
  // Configuration du consommateur Kafka
  val cprops = new Properties()
  cprops.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  cprops.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
             "org.apache.kafka.common.serialization.StringDeserializer")
  cprops.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
             "org.apache.kafka.common.serialization.StringDeserializer")
  cprops.put(ConsumerConfig.GROUP_ID_CONFIG, "alert-extractor-group")
  val consumer = new KafkaConsumer[String,String](cprops)
  consumer.subscribe(java.util.Arrays.asList("bins"))

  // Configuration du producteur Kafka (pour topic "alerts")
  val pprops = new Properties()
  pprops.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  pprops.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
             "org.apache.kafka.common.serialization.StringSerializer")
  pprops.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
             "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[String,String](pprops)

  // Boucle de polling récursive
  @annotation.tailrec
  def loop(): Unit = {
    val records = consumer.poll(Duration.ofSeconds(1))
    // Iteration fonctionnelle avec foreach
    records.asScala.foreach { record =>
      val value = record.value()
      // Filtre basique sur le champ JSON "status":"alert"
      if (value.contains("\"status\":\"alert\"")) {
        producer.send(new ProducerRecord[String,String]("alerts", value))
      }
    }
    loop()
  }
  loop()
}
