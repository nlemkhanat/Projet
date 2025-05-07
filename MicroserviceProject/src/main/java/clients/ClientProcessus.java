package Clients;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import entity.EmailRequest;
import entity.Loan;
import entity.SMSRequest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientProcessus {
    private static final String BASE_URI = "http://localhost:8080/MicroserviceProject/loans";
    private static final String EMAIL_URI = "http://localhost:8080/MicroserviceProject/notifications/email/send";
    private static final String SMS_URI = "http://localhost:8080/MicroserviceProject/notifications/sms/send";
    private static final String DB_URL = "jdbc:sqlite:C:/Users/i/dtclass.db";
    private static final String BASE_URIA = "http://localhost:8080/MicroserviceProject/";
    private static final String GRAPHQL_URI = "http://localhost:5000/graphql";
    public static void main(String[] args) {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(UriBuilder.fromUri(BASE_URI));
        Scanner scanner = new Scanner(System.in);

        // Étape 2: Acceder LoanRequest par le client
        System.out.print("Entrez l'ID du prêt: ");
        long loanId = scanner.nextLong();
        scanner.nextLine(); // Consommer la nouvelle ligne

        // Étape 3: Récupérer les détails de la demande de prêt
        WebTarget loanTarget = client.target(UriBuilder.fromUri(BASE_URI)).path(String.valueOf(loanId));
        Response loanResponse = loanTarget.request(MediaType.APPLICATION_JSON).get();
        if (loanResponse.getStatus() == 200) {
            Loan loan = loanResponse.readEntity(Loan.class);
            System.out.println("Détails de la demande de prêt:");
            System.out.println("ID du prêt: " + loan.getId());
            System.out.println("ID du client: " + loan.getCustomer().getId());
            System.out.println("Nom du client: " + loan.getCustomer().getName());
            System.out.println("Email du client: " + loan.getCustomer().getEmail());
            System.out.println("Téléphone du client: " + loan.getCustomer().getPhone());
            System.out.println("Montant du prêt: " + loan.getAmount());
            System.out.println("Type de prêt: " + loan.getLoanType());
            System.out.println("Statut du prêt: " + loan.getStatus());

            // Étape 4: Vérification du montant maximal du prêt...
            System.out.println("Vérification du montant maximal du prêt...");
            System.out.println("Appel à l'URL : " + BASE_URI + "/" + loan.getId() + "/verify"); // Log pour suivre l'URL utilisée
            WebTarget verifyTarget = client.target(UriBuilder.fromUri(BASE_URI)).path(loan.getId() + "/verify");
            Response verifyResponse = verifyTarget.request(MediaType.APPLICATION_JSON).put(Entity.json(""));  // Lire la réponse JSON sous forme de Map
            Map<String, Object> verifyResult = verifyResponse.readEntity(new GenericType<Map<String, Object>>() {});
            String message = (String) verifyResult.get("message"); // Lecture du statut mis à jour depuis la sous-carte "loanRequest"
            Map<String, Object> loanMap = (Map<String, Object>) verifyResult.get("loanRequest");
            String newStatus = (String) loanMap.get("status");
            System.out.println("Message de vérification : " + message);
            System.out.println("Nouveau statut du prêt : " + newStatus);
            if ("REJECTED".equalsIgnoreCase(newStatus)) {
                updateLoanStatusInDatabase(loanId, Loan.Status.REJECTED);
                System.out.println("Notifier Client Pour la refus et cloture de la demande du pret ");
                
                sendSMSNotification(client, loan.getCustomer().getPhone(),
                    "Votre demande de prêt a été annulée car le montant dépasse la limite maximale.");
                return; }

            
            // Étape 5: Analyse du profil financier du client
            if ("PENDING".equalsIgnoreCase(newStatus)) { 
            System.out.println("Analyse du profil financier du client...");
            System.out.println("Appel à l'URL : " + BASE_URI + "/" + loan.getId() + "/risk");
            String riskLevel = getRiskLevel(client, loanId);
            System.out.println("Niveau de risque du client: " + riskLevel);
          
           
            // Étape 6: Évaluation du risque du client
            if ("HIGH".equals(riskLevel) && loan.getAmount() >= 20000) {
                System.out.println("Loan Request Rejected: High risk level and amount ≥ 20000.");
                updateLoanStatusInDatabase(loanId, Loan.Status.REJECTED);
                System.out.println("Notifier Client Pour la refus et cloture de la demande du pret ");
                sendEmailNotification(client, loan.getCustomer().getEmail(), "Loan Request Rejected", "Votre demande de prêt a été refusée en raison d'un niveau de risque élevé et d'un montant supérieur ou égal à 20000.");
                sendSMSNotification(client, loan.getCustomer().getPhone(), "Votre demande de prêt a été refusée en raison d'un niveau de risque élevé et d'un montant supérieur ou égal à 20000.");
                return;
            }
            
            
            // Étape 7: Demande d'un chèque de banque
            System.out.println("Demande d'un chèque de banque..."); 
            System.out.println("Notifier Client Pour poser un cheque a la banque "); 
            System.out.println("Cheque numero 123456 est deposer...");
            // Numéro de chèque à tester
               String chequeNumber = "123456"; // Remplacez par le numéro du chèque réel
               try {
                   // Appel à la fonction pour obtenir le statut du chèque
                   String chequeStatus = getChequeStatus(client, chequeNumber);

                   // Vérification et traitement du statut retourné
                   if ("VALIDATED".equalsIgnoreCase(chequeStatus)) {
                       System.out.println("Statut du chèque : " + chequeStatus);
                       updateLoanStatusInDatabase(loanId, Loan.Status.APPROVED);
                       disburseLoanViaGraphQL(loanId);
                       // Étapes supplémentaires en cas de validation
                   } else if ("REJECTED".equalsIgnoreCase(chequeStatus)) {
                       System.out.println("Statut du chèque : " + chequeStatus);
                       updateLoanStatusInDatabase(loanId, Loan.Status.REJECTED);
                       // Étapes en cas de rejet
                   } else {
                       System.out.println("Erreur ou statut inconnu pour le chèque.");
                       System.err.println("Statut retourné : " + chequeStatus);
                   }
               } catch (Exception e) {
                   System.err.println("Erreur lors de la requête au service REST : " + e.getMessage());
                   e.printStackTrace(); // Affiche la pile d'erreurs pour le diagnostic
               } }
            
            // Étape 10: Récupérer le statut mis à jour de la demande de prêt
            WebTarget statusTarget = client.target(UriBuilder.fromUri(BASE_URI)).path(loan.getId() + "/status");
            Response updatedStatusResponse = statusTarget.request(MediaType.TEXT_PLAIN).get();
            System.out.println("Statut mis à jour de la demande de prêt: " + updatedStatusResponse.readEntity(String.class));   
            
        } else {
            System.out.println("Erreur: Demande de prêt non trouvée.");
        }
}
    
    
    
    

    private static String getRiskLevel(Client client, long loanId) {
        WebTarget riskTarget = client.target(UriBuilder.fromUri(BASE_URI)).path(loanId + "/risk");
        Response riskResponse = riskTarget.request(MediaType.APPLICATION_JSON).get();
        if (riskResponse.getStatus() == 200) {
            Map<String, String> riskData = riskResponse.readEntity(new GenericType<Map<String, String>>() {});
            return riskData.get("riskLevel");
        } else {
            return "UNKNOWN";
        }
    }

    private static void updateLoanStatusInDatabase(long loanId, Loan.Status newStatus) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "UPDATE LoanRequest SET status = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus.name());
                pstmt.setLong(2, loanId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void sendEmailNotification(Client client, String to, String subject, String body) {
        EmailRequest emailRequest = new EmailRequest(null, null, null);
        emailRequest.setTo(to);
        emailRequest.setSubject(subject);
        emailRequest.setBody(body);
        WebTarget emailTarget = client.target(UriBuilder.fromUri(EMAIL_URI));
        Response emailResponse = emailTarget.request(MediaType.TEXT_PLAIN).post(Entity.json(emailRequest));
        System.out.println("Email Notification Response: " + emailResponse.readEntity(String.class));
    }

    private static void sendSMSNotification(Client client, String phoneNumber, String message) {
        SMSRequest smsRequest = new SMSRequest(null, null);
        smsRequest.setPhoneNumber(phoneNumber);
        smsRequest.setMessage(message);
        WebTarget smsTarget = client.target(UriBuilder.fromUri(SMS_URI));
        Response smsResponse = smsTarget.request(MediaType.TEXT_PLAIN).post(Entity.json(smsRequest));
        System.out.println("SMS Notification Response: " + smsResponse.readEntity(String.class));
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
    
    public static void disburseLoanViaGraphQL(long loanId) throws IOException {
        String graphqlEndpoint = "http://localhost:5000/graphql";

        // Étape 1: Construction de la requête GraphQL
        String payload = String.format("{ \"query\": \"mutation { confirmLoanDisbursement(loanId: %d) { loanId success message transferredAmount customer { id name email } } }\" }", loanId);
        System.out.println("Renvoir de la requete GraphQL : " + payload);
        // Étape 2: Connexion HTTP POST
        HttpURLConnection conn = (HttpURLConnection) new URL(graphqlEndpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        

        // Étape 3: Envoi du payload
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        // Étape 4: Lecture de la réponse et extraction du message
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                conn.getResponseCode() < 400 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8))) {
            
            String response = br.lines().reduce("", (acc, line) -> acc + line);
            System.out.println("Réponse GraphQL : " + response);

            // Extraction du champ "message"
            String message = extractMessageFromResponse(response);
            if (message != null) {
                System.out.println("📨 Message du serveur : " + message);
            } else {
                System.out.println("⚠️ Champ 'message' non trouvé.");
            }
        }
    }

    // Méthode utilitaire pour extraire le message à l'aide d'une expression régulière
    private static String extractMessageFromResponse(String response) {
        // Amélioration de l'expression régulière pour prendre en compte les espaces et retours à la ligne
        Pattern pattern = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(response);
        return matcher.find() ? matcher.group(1) : null;
    }
    

 
  

}
