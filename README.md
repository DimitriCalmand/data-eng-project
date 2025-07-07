# Scala IoT Drone POC – Kafka / Spark / Kafka Connect

Ce dépôt contient un **Proof-of-Concept complet** qui :

1. **Simule** des données de drones et les publie en continu dans **Kafka** (`drone-simulator` – Producer).  
2. **Filtre** les messages d’alerte et les publie dans un second topic (`alert-extractor` – Consumer + Producer).  
3. **Affiche** les alertes reçues (`alert-handler` – Consumer).  
4. **Stocke** tous les messages du topic `drones` dans un fichier **JSON Lines** via **Kafka Connect** (`kafka-connect`).  
5. **Analyse** les données stockées avec **Apache Spark** en Scala (`spark-analyzer`).

Le projet est multi-module (un `build.sbt` par répertoire) et fonctionne sur **Scala 2.13.13**, **Kafka 4.0.0**, **Spark 3.5.0** et **Java 17**.

```
.
├── drone-simulator/     # Producteur Kafka (simulateur de drones)
│   ├── build.sbt
│   └── src/main/scala/DroneSimulator.scala
├── alert-extractor/     # Consommateur drones → Producteur alerts
│   ├── build.sbt
│   └── src/main/scala/AlertExtractor.scala
├── alert-handler/       # Consommateur alerts
│   ├── build.sbt
│   └── src/main/scala/AlertHandler.scala
├── kafka-connect/       # Fichiers de config Kafka Connect
│   ├── connect-standalone.properties
│   └── drones-sink.properties
├── spark-analyzer/      # Job Spark batch
│   ├── build.sbt
│   └── src/main/scala/SparkAnalyzer.scala
└── README.md            # (ce fichier)
```

---

## 1. Prérequis

| Outil               | Version testée | Commentaire                                  |
|---------------------|----------------|----------------------------------------------|
| Java JDK            | 17             | `java -version`                              |
| **Kafka** (Scala 2.13 build) | 4.0.0         | Téléchargement : <https://kafka.apache.org/> |
| **sbt**             | ≥ 1.10         | `brew install sbt` ou <https://www.scala-sbt.org> |
| **Spark**           | 3.5.0 (pré-assemblé Hadoop 3.3) | <https://spark.apache.org/downloads.html> |

> **Apple Silicon (M-series)** : Kafka 4 et Spark 3.5 tournent nativement (aarch64).  
> Sinon, lancez-les via Rosetta ou Docker.

---

## 2. Installation locale

```bash
# 1) Cloner le dépôt
git clone https://github.com/<votre-org>/drone-iot-poc.git
cd drone-iot-poc

# 2) Démarrer ZooKeeper et Kafka (2 terminaux)
$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties
$KAFKA_HOME/bin/kafka-server-start.sh     $KAFKA_HOME/config/server.properties

# 3) Créer les topics
$KAFKA_HOME/bin/kafka-topics.sh --create --topic drones  --bootstrap-server localhost:9092
$KAFKA_HOME/bin/kafka-topics.sh --create --topic alerts --bootstrap-server localhost:9092

# 4) Démarrer Kafka Connect (3ᵉ terminal)
cd kafka-connect
$KAFKA_HOME/bin/connect-standalone.sh connect-standalone.properties drones-sink.properties
```

> Le worker Connect écrit alors dans `drones_output.json` du répertoire courant.

---

## 3. Compilation & Exécution des modules Scala

Ouvrir **un terminal par module** ou utiliser `tmux`/`screen` :

```bash
# Module 1 : simulateur (produit vers topic 'drones')
cd drone-simulator
sbt run

# Module 2 : extracteur (drones -> alerts)
cd ../alert-extractor
sbt run

# Module 3 : handler (affiche les alerts)
cd ../alert-handler
sbt run
```

Laissez tourner quelques dizaines de secondes pour collecter des données.

---

## 4. Analyse Spark batch

```bash
# Lancer après avoir accumulé des messages
cd spark-analyzer
#  (optionnel) fournir le chemin absolu du fichier généré par Kafka Connect
sbt -Ddrone.file=/chemin/vers/drones_output.json run
```

### Requêtes exécutées

| Requête | Description |
|---------|-------------|
| **Q1**  | `COUNT(*)` de messages par `droneId` |
| **Q2**  | Nombre d’alertes (`status = 'alert'`) par drone |
| **Q3**  | Moyenne de `metric1`, `metric2` groupée par `status` |
| **Q4**  | Ensemble (`COLLECT_SET`) des positions uniques `(latitude, longitude)` par drone |

Les résultats sont affichés dans la console et peuvent être exportés (Parquet/CSV).

---

## 5. Configuration Kafka Connect

`kafka-connect/connect-standalone.properties`

```properties
bootstrap.servers=localhost:9092
group.id=connect-file-sink

key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.storage.StringConverter

plugin.path=/opt/kafka/libs,/opt/kafka/plugins
offset.storage.file.filename=/tmp/connect-offsets
offset.flush.interval.ms=10000
```

`kafka-connect/drones-sink.properties`

```properties
name=drones-file-sink
connector.class=FileStreamSink
tasks.max=1
topics=drones
file=drones_output.json          # chemin relatif ou absolu
```

---

## 6. Désactivation des logs verbeux (facultatif)

Ajouter dans chaque module :

```scala
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.14"
```

et placer `src/main/resources/logback.xml` :

```xml
<configuration>
  <root level="WARN">
    <appender-ref ref="STDOUT"/>
  </root>
  <logger name="org.apache.kafka" level="INFO"/>
  <logger name="org.apache.spark" level="INFO"/>
</configuration>
```

---

## 7. Aller plus loin

* Passer **Spark** en **Structured Streaming** et lire le topic `drones` directement.  
* Remplacer FileStream par un sink **HDFS**, **S3** ou **Delta Lake**.  
* Brancher l’alert-handler sur un service d’e-mail / Slack webhook.  
* Ajouter des tests ScalaTest (unitaires + intégration Kafka Embedded).  
* Dockeriser l’ensemble avec `docker-compose.yml`.

---

© 2025 – Projet pédagogique INT-Data Engineering – EPITA  
_Conçu pour illustrer la chaîne Temps réel → Stockage → Batch Analytics en Scala._
