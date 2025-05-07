from zeep import Client
import time

# URL du service
url = "http://127.0.0.1:8000/?wsdl"
client = Client(url)

# Fonction pour suivre les mises à jour
def stream_cheques_updates():
    while True:
        result = client.service.get_cheques_status()
        print("\n🔄 Mises à jour des chèques :\n", result)
        time.sleep(3)  # Pause avant de récupérer les nouvelles données

# Lancer le streaming
if __name__ == "__main__":
    print("🟢 Suivi des chèques en streaming...")
    stream_cheques_updates()
