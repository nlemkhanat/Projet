from zeep import Client



# URL du WSDL
url = "http://127.0.0.1:8000/?wsdl"
client = Client(url)


# Appeler le service validate_cheque
result = client.service.validate_cheque("123456")
print(result)  # Exemple de sortie : Cheque 123456 validation status: VALIDATED
