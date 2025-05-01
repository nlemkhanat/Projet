package clients;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

public class ChequeStatusTester {

    private static final String BASE_URIA = "http://localhost:8080/MicroserviceProject/";

    public static void main(String[] args) {
        // Initialiser le client REST avec une configuration Jersey
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        // Numéro de chèque à tester
        String chequeNumber = "123456"; // Remplacez par le numéro du chèque réel

        try {
            // Appel à la fonction pour obtenir le statut du chèque
            String chequeStatus = getChequeStatus(client, chequeNumber);

            // Vérification et traitement du statut retourné
            if ("VALIDATED".equalsIgnoreCase(chequeStatus)) {
                System.out.println("Statut du chèque : VALIDÉ");
                // Étapes supplémentaires en cas de validation
            } else if ("REJECTED".equalsIgnoreCase(chequeStatus)) {
                System.out.println("Statut du chèque : REJETÉ");
                // Étapes en cas de rejet
            } else {
                System.out.println("Erreur ou statut inconnu pour le chèque.");
                System.err.println("Statut retourné : " + chequeStatus);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la requête au service REST : " + e.getMessage());
            e.printStackTrace(); // Affiche la pile d'erreurs pour le diagnostic
        } finally {
            client.close(); // Libération des ressources du client
        }
    }

    private static String getChequeStatus(Client client, String chequeNumber) {
        try {
            // Construction de l'URL pour interroger le service REST des chèques
            String url = BASE_URIA + "cheques/" + chequeNumber + "/status";
            System.out.println("Appel à l'URL : " + url); // Log pour suivre l'URL utilisée

            // Envoi de la requête GET au service REST
            Response response = client.target(url).request().get();

            // Vérification du statut HTTP de la réponse
            if (response.getStatus() == 200) {
                // Retourner le statut du chèque comme texte brut
                return response.readEntity(String.class);
            } else if (response.getStatus() == 404) {
                System.err.println("Erreur : Chèque non trouvé.");
                return "NOT_FOUND";
            } else {
                System.err.println("Erreur : Code HTTP inattendu (" + response.getStatus() + ")");
                return "UNKNOWN";
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel au service REST : " + e.getMessage());
            return "UNKNOWN"; // Retourne un statut inconnu en cas d'erreur
        }
    }
}
