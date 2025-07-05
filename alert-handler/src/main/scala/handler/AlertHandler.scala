// src/main/scala/AlertHandler.scala
import java.time.Duration
import java.util.Properties
import org.apache.kafka.clients.consumer.{KafkaConsumer, ConsumerConfig}
import scala.jdk.CollectionConverters._

object AlertHandler extends App {
  val props = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
            "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
            "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, "alert-handler-group")
  val consumer = new KafkaConsumer[String,String](props)
  consumer.subscribe(java.util.Arrays.asList("alerts"))

  @annotation.tailrec
  def loop(): Unit = {
    val records = consumer.poll(Duration.ofSeconds(1))
    records.asScala.foreach { record =>
      println(s"ALERT reçue: ${record.value()}")  // affichage simple
    }
    loop()
  }
  loop()
}
