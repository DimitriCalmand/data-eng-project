# Scala IoT Bin POC – Kafka / Spark / Kafka Connect

Ce dépôt contient un **Proof-of-Concept** complet illustrant une chaîne de traitement de données IoT pour des **poubelles connectées** dans une ville.

## Ce que fait le projet

1. **Simule** des données de capteurs pour des poubelles (`bin-simulator`) et les publie dans **Kafka**.
2. **Détecte** les poubelles pleines et publie une alerte dans un topic `alerts` (`alert-extractor`).
3. **Affiche** les alertes reçues (`alert-handler`).
4. **Stocke** tous les messages du topic `bins` dans un fichier JSON Lines via **Kafka Connect**.
5. **Analyse** les données avec **Apache Spark** (`spark-analyzer`).

---

## Structure du projet

```
.
├── bin-simulator/       # Producteur Kafka (simulateur de poubelles)
├── alert-extractor/     # Consommateur bins → Producteur alerts
├── alert-handler/       # Consommateur alerts
├── kafka-connect/       # Config Kafka Connect (file sink)
├── spark-analyzer/      # Analyse Spark batch
├── docker-compose.yml   # Tout se lance ici
└── README.md
```

---

## Prérequis

* **Docker** et **Docker Compose** installés

---

## Lancement

```bash
# Clone du dépôt
git clone https://github.com/DimitriCalmand/data-eng-project.git
cd data-eng-project

# Lancer l'ensemble des services
docker-compose up --build
```

Les containers suivants seront lancés automatiquement :

* Kafka + ZooKeeper
* Kafka Connect
* Les modules Scala (`bin-simulator`, `alert-extractor`, `alert-handler`)
* Spark pour analyse (batch)

---

## Fichiers générés

Kafka Connect écrit les messages dans le fichier `bins_output.json` (format JSON Lines).

---

## Lancer l’analyse batch (facultatif)

Après quelques secondes/minutes de données :

```bash
# Depuis un terminal dans le container spark
docker exec -it spark-analyzer /bin/bash

# Lancer le job Spark
sbt -Dbin.file=/app/kafka-connect/bins_output.json run
```

### Requêtes exécutées :

| Requête | Description                                                         |
| ------- | ------------------------------------------------------------------- |
| Q1      | Nombre total de messages par `binId`                                |
| Q2      | Nombre d’alertes (poubelle pleine) par `binId`                      |
| Q3      | Moyenne de remplissage par statut                                   |
| Q4      | Liste des emplacements uniques `(latitude, longitude)` par poubelle |

---

## Pour aller plus loin

* Passer Spark en mode **streaming** pour lecture en temps réel.
* Connecter les alertes à un système de notification (e-mail, webhook).
* Utiliser un stockage type **S3**, **Delta Lake** ou **PostgreSQL**.
* Ajouter des tests automatisés avec ScalaTest.
* Déployer le tout sur Kubernetes.
