package ServiceDemandePret;



import javax.ws.rs.*;
import javax.ws.rs.core.*;

import entity.SMSRequest;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

@Path("notifications/sms")
public class SMSNotificationService {

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendSMSNotification(SMSRequest smsRequest) {
        try {
            sendSMS(smsRequest.getPhoneNumber(), smsRequest.getMessage());
            return Response.ok("SMS envoyé avec succès").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Échec de l'envoi du SMS").build();
        }
    }

    private void sendSMS(String phoneNumber, String message) throws Exception {
        String apiUrl = "https://api.smsprovider.com/send";
        String apiKey = "votre-cle-api";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);

        String jsonInputString = "{\"phoneNumber\": \"" + phoneNumber + "\", \"message\": \"" + message + "\"}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Échec de l'envoi du SMS : code d'erreur HTTP : " + responseCode);
        }
    }
}