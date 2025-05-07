import requests
import json

# URL de l'endpoint GraphQL (sur ton serveur Flask)
url = "http://localhost:5000/graphql"

# Requête GraphQL que tu souhaites tester
query = """
mutation {
    confirmLoanDisbursement(loanId: "5434") {
        success
        message
        loanId
        transferredAmount
        customer {
            id
            name
            email
        }
    }
}
"""

# Payload à envoyer avec la requête
payload = {
    "query": query
}

# En-têtes pour spécifier que le corps est en JSON
headers = {
    "Content-Type": "application/json"
}

# Envoi de la requête POST
response = requests.post(url, json=payload, headers=headers)

# Affichage de la réponse du serveur
if response.status_code == 200:
    print("Réponse du serveur :")
    print(json.dumps(response.json(), indent=2))  # Affiche la réponse formatée
else:
    print(f"Erreur : {response.status_code}")
    print(response.text)
