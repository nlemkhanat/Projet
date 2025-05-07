package clients;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class LoanDisbursementClient {

    private static final String GRAPHQL_URL = "http://localhost:5000/graphql";

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            // Demande à l'utilisateur d'entrer l'ID du prêt
            System.out.print("Entrez l'ID du prêt à décaisser : ");
            int loanId = sc.nextInt();
            
            // Prépare la charge utile pour la mutation GraphQL
            String payload = buildGraphQLPayload(loanId);

            // Envoie la requête HTTP POST
            HttpURLConnection conn = createHttpConnection(payload);

            // Traite la réponse du serveur
            handleResponse(conn);
        } catch (IOException e) {
            System.err.println("Erreur de communication avec le serveur: " + e.getMessage());
        }
    }

    // Construction de la charge utile GraphQL (payload)
    private static String buildGraphQLPayload(int loanId) {
        return "{ \"query\": \"mutation { confirmLoanDisbursement(loanId: " + loanId + ") { loanId success message transferredAmount customer { id name email } } }\" }";
    }

    // Crée la connexion HTTP et envoie la requête
    private static HttpURLConnection createHttpConnection(String payload) throws IOException {
        URL url = new URL(GRAPHQL_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        // Envoie la charge utile
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return conn;
    }

    // Traite la réponse du serveur
    private static void handleResponse(HttpURLConnection conn) throws IOException {
        int statusCode = conn.getResponseCode();
        System.out.println("Code HTTP: " + statusCode);

        // Sélectionne le bon flux de lecture (succès ou erreur)
        try (InputStream is = (statusCode < 400) ? conn.getInputStream() : conn.getErrorStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            // Lit et affiche chaque ligne de la réponse
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            System.out.println("Réponse du serveur : " + response.toString());
        }
    }
}
