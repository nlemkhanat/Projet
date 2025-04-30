
# Projet Microservices : Gestion des Prêts
##Introduction
Le processus de demande de prêt est décomposé en plusieurs étapes, chacune gérée par un microservice spécifique. Les services communiquent entre eux à l'aide de requêtes REST et de messages asynchrones via un Message Broker. Le processus global est orchestré via un API Gateway.

## 🔗 Lien vers le dépôt Git
[👉 Accéder au dépôt GitHub](https://github.com/nlemkhanat/Projet)


## ✅ Fiche d’auto-évaluation

Évaluation basée sur les critères du projet :

| Critères                                                                 | Points max | Points obtenus | Commentaire |
|--------------------------------------------------------------------------|------------|----------------|-------------|
| Modélisation d’un workflow avec pools et interactions entre 2 partenaires | 15         | 15             | Deux pools modélisés : client et service bancaire |
| Utilisation de passerelles (OR, AND, XOR) dans le workflow               | 15         | 15             | Passerelle XOR utilisée pour valider ou refuser la demande |
| Activité appelant une API REST                                           | 30         | 30             | Appel REST effectué pour récupérer les informations utilisateur |
| Activité appelant un service SOAP                                        | 30         | 30             | Appel SOAP simulé pour la vérification d’éligibilité au prêt |
| Activité appelant une API gRPC                                           | 20         | 20             | Service gRPC pour calculer le taux d’intérêt |
| Activité appelant une API GraphQL                                        | 20         | 20             | Utilisation de GraphQL pour requêter les données clients |
| Tests et documentation des APIs                                          | 30         | 30             | Documentation fournie (Swagger/Postman) et tests réussis |
| Procédure correcte, exécution complète du processus                      | 40         | 40             | Le processus BPMN est exécuté de bout en bout sans erreur |
| Déploiement en microservices (optionnel)                                 | 0         | 0            |  |

**Total obtenu : 200 / 250**


## 🚀 How to use

### Étape 1 : Accueil
![Accueil](./captures/home.png)

### Étape 2 : Formulaire de demande
![Formulaire](./captures/form.png)

### Étape 3 : Confirmation
![Confirmation](./captures/confirmation.png)

### Étape 4 : Suivi
![Suivi](./captures/tracking.png)

## 🛠️ How to install

### Prérequis
- Node.js / Python / Java
- Docker (facultatif)
- Git

# Scénario : processus de prêt
Considérons un scénario dans lequel une société de services financiers propose à ses clients un service de demande de prêt comme suit :

Workflow BPMN du processus :

1- Début du processus:   Événement de début ("Loan Request Initiated")

2-Formulaire rempli par le client (User Task)

* Saisie de l'ID, des infos personnelles, du type et du montant du prêt

3- Vérification du montant maximal du prêt (Service Task)

* Comparaison avec la limite maximale
*  Passerelle exclusive (XOR) :
Si supérieur → "Loan Request Cancelled" (End Event) et notification au client (Message Event), Sinon → Passer à l'étape suivante

4- Analyse du profil financier du client (Service Task via API du partenaire)

* Évaluation du risque basé sur les activités bancaires

5-  Évaluation du risque du client (Exclusive Gateway – XOR) :
Si "High Risk" et montant ≥ 20000 € → "Loan Rejected" (End Event) + Notification au client, Sinon → Demande d'un chèque de banque

6- Demande et soumission du chèque de banque (User Task – Client)
7-  Validation du chèque par la banque (Service Task via API bancaire)

*  Passerelle exclusive (XOR) :
Si refusé → "Loan Rejected" (End Event) + Notification, Si accepté → Procéder à l'approbation

8- Notification finale au client (Message Event)

Prêt approuvé ou refusé

9- Fin du processus (End Event – "Process Completed")

## Architecture du Projet

L'architecture du projet repose sur microservice. 
Pour architecturer ce processus en microservices, on va décomposer le workflow en plusieurs services indépendants. Chaque service aura une responsabilité bien définie et utilisera des protocoles adaptés à son usage.

Les principaux services sont : 

| Microservice                          | Type         | Input                               | Output                          | Intérêt                                   | Protocole recommandé |
|---------------------------------------|--------------|--------------------------------------|----------------------------------|-------------------------------------------|----------------------|
| Service Creation de la demande                  | IHM | Formulaire client, Données du client (ID, informations personnelles, type de prêt, montant).                   | Confirmation de la soumission du formulaire ( ID du prêt généré )          | Orchestration et gestion du workflow       | REST                 |
| Service Vérification du montant maximal du prêt              | Métier       | ID du prêt, Montant demandé          | Statut d’acceptation (Oui/Non)  | Vérifie si le prêt dépasse le montant maximal | REST                 |
| Customer Risk Assessment Service      | Métier       | ID client, historique bancaire       | Niveau de risque du client      | Évaluation du risque pour décision de prêt | gRPC  | 
| Service de demande de cheque        | Metier | niveau du risque, Montant du pret            | Refus ou creation de demande du cheque  |                |
| Bank Check Validation Service         | Intégration  | Chèque soumis par client            | Statut du chèque (Validé/Rejeté) | Vérifie la validité du chèque via la banque | SOAP                 |
| Loan Approval Service                 | Métier       | ID du prêt, Statut du chèque         | Statut de l'approbation         | Approuve ou rejette le prêt final         | REST                 |
| Loan Disbursement Service             | Paiement     | ID du prêt approuvé                 | Confirmation de transfert       | Transfert des fonds au client             | GraphQL              |
| Notification Service                  | Communication| ID client,ID damande,  statut                   | Email/SMS                       | Envoie une notification au client          | REST                 |


Explication des choix technologiques  : 

1- REST (Loan Request, Loan Validation, Loan Approval, Notification)

*   Simple, facile à implémenter, compatible avec la plupart des systèmes.

*   Idéal pour les services transactionnels sans forte interopérabilité.

2- gRPC (Customer Risk Assessment)

*   Permet une communication rapide et efficace entre services.

*   Adapté aux échanges de données complexes avec un temps de réponse réduit.

3- SOAP (Bank Check Validation)

*   Standard bancaire couramment utilisé.

*   Sécurisé et fiable pour l’échange de données sensibles.

4- GraphQL (Loan Disbursement)

*  Permet aux clients de demander exactement les données dont ils ont besoin.

*  Réduit les appels multiples en fournissant toutes les infos en une requête.


### Installation locale

```bash
git clone https://github.com/nom-utilisateur/nom-du-depot.git
cd nom-du-depot
npm install
npm start



