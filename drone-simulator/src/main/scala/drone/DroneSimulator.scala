// src/main/scala/DroneSimulator.scala
import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import scala.util.Random

case class DroneMsg(timestamp: String, droneId: String, latitude: Double, longitude: Double,
                    metric1: Double, metric2: Double, status: String)

object DroneSimulator extends App {
  // Configuration du producteur Kafka
  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
            "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
            "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[String,String](props)  // cf. :contentReference[oaicite:1]{index=1}

  // Génération récursive de messages
  def loop(): Unit = {
    val ts = java.time.Instant.now.toString
    val msg = DroneMsg(
      timestamp = ts,
      droneId   = s"DR-${Random.nextInt(10)+1}", 
      latitude  = 48.8 + Random.nextDouble()/10,
      longitude = 2.3 + Random.nextDouble()/10,
      metric1   = Random.nextDouble()*100,
      metric2   = Random.nextDouble()*50,
      status    = if (Random.nextDouble() < 0.1) "alert" else "ok"
    )
    // Construire JSON manuellement (on peut utiliser une librairie JSON pour plus de robustesse)
    val json = s"""{"timestamp":"${msg.timestamp}","droneId":"${msg.droneId}",
                  "latitude":${msg.latitude},"longitude":${msg.longitude},
                  "metric1":${msg.metric1},"metric2":${msg.metric2},
                  "status":"${msg.status}"}"""
    producer.send(new ProducerRecord[String,String]("drones", json))
    Thread.sleep(500)  // pause entre messages
    loop()  // récursion sans var ni boucle explicite (style fonctionnel)
  }

  loop()  // lancer la génération infinie de messages
}
