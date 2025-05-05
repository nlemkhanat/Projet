
# Projet Microservices : Application demande des Prêts

## 🔗 Lien du projet vers le dépôt Git
[👉 Accéder au dépôt GitHub](https://github.com/nlemkhanat/Projet)

## Introduction
Le processus de demande de prêt est décomposé en plusieurs étapes, chacune gérée par un microservice spécifique. Les services communiquent entre eux à l'aide de requêtes REST,SOAP, GraphQL et GRPC et de messages asynchrones via un Service de notification. 

## Scénario : processus de prêt et WorkFlow BPMN 
Considérons un scénario dans lequel une société de services financiers propose à ses clients un service de demande de prêt. Le Workflow BPMN du processus:

![Processus BPMN](Processus%20Loan%20Request/Loan%20Request%20BPMN.png)


## Architecture de l'application et Bases donnees 

L'architecture du projet repose sur microservice. 
Pour architecturer ce processus en microservices, on va décomposer le workflow en plusieurs services indépendants. Chaque service aura une responsabilité bien définie et utilisera des protocoles adaptés à son usage.

Les principaux services sont : 

| Microservice                          | Type         | Input                               | Output                          | Intérêt                                   | Protocole recommandé |
|---------------------------------------|--------------|--------------------------------------|----------------------------------|-------------------------------------------|----------------------|
| Service Creation de la demande                  | IHM | Formulaire client, Données du client (ID, informations personnelles, type de prêt, montant).                   | Confirmation de la soumission du formulaire ( ID du prêt généré )          | Orchestration et gestion du workflow       | REST                 |
| Service Vérification du montant maximal du prêt              | Métier       | ID du prêt, Montant demandé          | Statut d’acceptation (Oui/Non)  | Vérifie si le prêt dépasse le montant maximal | REST  |
| Customer Risk Assessment Service      | Métier       | ID client, historique bancaire       | Niveau de risque du client      | Évaluation du risque pour décision de prêt | gRPC  | 
| Service de demande de cheque        | Metier | niveau du risque, Montant du pret            | Refus ou creation de demande du cheque  | Service verifcation dule RISK et le montant pour valider la demande  |REST
| Bank Check Validation Service         | Intégration  | Chèque soumis par client            | Statut du chèque (Validé/Rejeté) | Vérifie la validité du chèque via la banque | SOAP                 |
| Loan Approval Service                 | Métier       | ID du prêt, Statut du chèque         | Statut de l'approbation         | Approuve ou rejette le prêt final         | REST                 |
| Loan Disbursement Service             | Paiement     | ID du prêt approuvé                 | Confirmation de transfert       | Transfert des fonds au client             | GraphQL              |
| Notification Service                  | Communication| ID client,ID damande,  statut                   | Email/SMS                       | Envoie une notification au client          | REST                 |


## ✅ Fiche d’auto-évaluation
Une Auto-Évaluation basée sur les critères du projet :


| **Critères**                                                          | **Points max** | **Points obtenus** | **Commentaires**                                                                                                                                                   |
|------------------------------------------------------------------------|----------------|---------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Modélisation d’un workflow avec pools et interactions entre partenaires | 15             | 15                  | Deux pools modélisés : client et services bancaire.                                                                                                                  |
| Utilisation de passerelles (OR, AND, XOR) dans le workflow             | 15             | 15                  | Passerelle XOR utilisée pour valider ou rejeter le etapes .                                                                                                         |
| Activité appelant une API REST                                         | 30             | 30                  | Consolidation des services REST ("Vérification du montant maximal du prêt", "Création de la demande", "Approbation du prêt") dans une seule API nommée ResourceLoan, documentée dans APILoan et services de notification. |
| Activité appelant un service SOAP                                      | 30             | 30                  | Service backend SOAP utilisé pour vérifier le statut du chèque et insérer son état dans la base de données.                                                        |
| Activité appelant une API gRPC                                         | 20             | 20                  | Service gRPC pour calculer le profil de risque client et l'enregistrer dans la base de données.                                                                    |
| Activité appelant une API GraphQL                                      | 20             | 20                  | Utilisation de GraphQL pour effectuer un transfert d'argent entre la banque et le client.                                                                          |
| Tests et documentation des APIs                                        | 30             | 30                  | Documentation fournie (Swagger/Postman) et tests réalisés avec succès pour les différents services.                                                                |
| Procédure correcte, exécution complète du processus                    | 40             | 40                  | Processus BPMN exécuté intégralement et sans erreur soit par interface et par client en JAVA Qui passe par tous les etapes du processus.                                                                                                               |
| Déploiement en microservices (optionnel)                               | 0              | 0                   | Non réalisé ou non applicable.                                                                                                                                     |


**Total obtenu : 200 / 250**


## 🛠️ Prérequis et Installation
- Node.js / Python / Java JDK /Eclippse IDE/ Tomacat 9.0
- Docker (facultatif)
- Git

**Avant de lancer l'application, assurez-vous d'avoir les éléments suivants installés et configurés sur votre machine.**

### 🔹 1. Prérequis Java (pour le backend avec Tomcat)

#### a. Télécharger Eclipse IDE
Téléchargez et installez **Eclipse IDE for Java EE Developers** depuis le site officiel :  
👉[Eclipse IDE for Enterprise Java and Web Developers](https://www.eclipse.org/downloads/packages/release/2022-06/r/eclipse-ide-enterprise-java-and-web-developers)

#### b. Installer Apache Tomcat dans Eclipse
1. Téléchargez **Apache Tomcat 9.0** (ou version compatible) depuis :  
   👉 [Tomcat 9.0 ](https://tomcat.apache.org/download-90.cgi)
2. Extrayez le dossier ZIP dans un répertoire local.
3. Dans Eclipse :
   - Allez dans `Window → Show View → Other... → Servers`.
   - Clic droit dans la vue *Servers*, sélectionnez `New → Server`.
   - Choisissez `Apache → Tomcat v9.0 Server`, hôte : `localhost`.
   - Cliquez sur **Next**, puis configurez le chemin vers le dossier Tomcat extrait.
   - Terminez et démarrez le serveur.

### 🔹 2. Prérequis Python (pour les services SOAP et gRPC)

Assurez-vous que **Python 3.x** et `pip` sont installés sur votre système.  
Vous pouvez vérifier avec :
```bash
python --version
pip --version
```

### 🔹3.Résumé des Technologies utlisees 

| Type de Service         | Technologie / Outil                      |
|-------------------------|-------------------------------------------|
| IDE                     | Eclipse for Java EE Developers            |
| Serveur d'application   | Apache Tomcat 9.x                         |
| REST API                | Java (JAX-RS via Jersey / Jakarta)        |
| SOAP API                | Python avec `spyne`, `zeep`, `suds-jurko` |
| gRPC API                | Python avec `grpcio`, `grpcio-tools`      |
| GraphQL API             | (à compléter si présent)                  |


## 🛠️ How to install application 

Étape 1 : Cloner le projet
Téléchargez le dossier du projet depuis le dépôt Git en exécutant la commande suivante :

``` git clone https://github.com/nlemkhanat/Projet.git
```

L'installation  de application est via  :

Lancer les microservices en Java avec Tomcat.
Lancer les services backend en Python dans des terminaux séparés.


Étape 2 : Organisation des dossiers
Dans le dossier cloné du projet, vous trouverez trois dossiers principaux destinés à l'installation des différents services  : Microservices Project,Customer Risk Assessment Service, Bank Check Validation Service.

### Installation et exécution du dossier en java  : Microservices Project  
Ce dossier contient les principaux services Java, l'application et le client. Suivez ces étapes pour exécuter l'application dans un serveur Tomcat : 
```  1.Lancez le serveur Tomcat.
2.Faites un clic droit sur le serveur Tomcat → Sélectionnez  Add and Remove….
3.Ajoutez le projet MicroserviceProject dans le serveur.
4.Accédez à l'application depuis votre navigateur à l'adresse suivante :
http://localhost:8080/MicroserviceProject/BankLoan
5.Vérifiez que la réponse s'affiche correctement dans le navigateur.
```

###  Installation et exécution des services backend en Python : Customer Risk Assessment Service, Bank Check Validation Service.

Installation et exécution du dossier en python : 

1.Ouvrez un terminal (cmd) et naviguez jusqu'au répertoire des services backend spécifiques. Par exemple, pour lancer le service de validation des chèques, exécutez les commandes suivantes :

``` cd Desktop\Projet MicroserviceDemande du pret\Bank Check Validation Service
python server.py
```
2.Dans un autre terminal, lancez le client correspondant :

```cd Desktop\Projet MicroserviceDemande du pret\Bank Check Validation Service
python client.py
```
3.Répétez cela pour chaque service dans chaque cmd
4.pour service Customer Risk Assessment Service : Accédez à son répertoire et exécutez les scripts serveur et client de la même manière.


## 🚀 How to use

### Étape 1 : Accueil
![Accueil](./captures/home.png)

### Étape 2 : Formulaire de demande
![Formulaire](.form.png)

### Étape 3 : Confirmation
![Confirmation](.confirmation.png)

### Étape 4 : Suivi
![Suivi](./captures/tracking.png)













