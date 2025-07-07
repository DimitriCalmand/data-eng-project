// drone-simulator/src/main/scala/DroneSimulator.scala
import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import scala.util.Random
import scala.annotation.tailrec

// Case class représentant le message d'un drone
case class DroneMsg(timestamp: String, droneId: String, latitude: Double, longitude: Double,
                    metric1: Double, metric2: Double, status: String)

object DroneSimulator extends App {
  // Configuration du producteur Kafka (adresse du broker, sérialisation en String)
  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[String, String](props)

  // Fonction récursive pour générer et envoyer des messages en continu
  @tailrec
  def loop(): Unit = {
    val ts = java.time.Instant.now.toString  // timestamp ISO-8601 du message
    val msg = DroneMsg(
      timestamp = ts,
      droneId   = s"DR-${Random.nextInt(10) + 1}",  // ID du drone entre DR-1 et DR-10
      latitude  = 48.8 + Random.nextDouble() / 10,  // autour de 48.8xx
      longitude = 2.3 + Random.nextDouble() / 10,   // autour de 2.3xx
      metric1   = Random.nextDouble() * 100,        // métrique aléatoire 0-100
      metric2   = Random.nextDouble() * 50,         // métrique aléatoire 0-50
      status    = if (Random.nextDouble() < 0.1) "alert" else "ok"  // 10% d'alertes
    )
    // Construction du message JSON (on utilise ici une simple interpolation de string)
    val json = "{\"timestamp\":\"" + msg.timestamp + "\"," +
               "\"droneId\":\""   + msg.droneId   + "\"," +
               "\"latitude\":"    + msg.latitude  + "," +
               "\"longitude\":"   + msg.longitude + "," +
               "\"metric1\":"     + msg.metric1   + "," +
               "\"metric2\":"     + msg.metric2   + "," +
               "\"status\":\""    + msg.status    + "\"}"
    // Envoi du message JSON sur le topic "drones"
    producer.send(new ProducerRecord[String, String]("drones", json))
    Thread.sleep(500)  // pause de 500 ms entre deux envois
    loop()             // appel récursif (tail recursion) pour le prochain message
  }

  // Démarrage de la boucle d’envoi
  loop()
}
