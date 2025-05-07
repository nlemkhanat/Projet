package clients;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

import entity.EmailRequest;
import entity.SMSRequest;

public class TestNotificationServices {

    private static final String EMAIL_URI = "http://localhost:8080/MicroserviceProject/notifications/email/send";
    private static final String SMS_URI = "http://localhost:8080/MicroserviceProject/notifications/sms/send";

    public static void main(String[] args) {
        Client client = ClientBuilder.newClient();

        // Test de l'envoi d'un email
        EmailRequest emailRequest = new EmailRequest(null, null, null);
        emailRequest.setTo("nassr-eddine@outlook.fr");
        emailRequest.setSubject("Test Email");
        emailRequest.setBody("Ceci est un test d'envoi d'email.");
        WebTarget emailTarget = client.target(UriBuilder.fromUri(EMAIL_URI));
        Response emailResponse = emailTarget.request(MediaType.TEXT_PLAIN).post(Entity.json(emailRequest));
        System.out.println("Email Notification Response: " + emailResponse.readEntity(String.class));

        // Test de l'envoi d'un SMS
        SMSRequest smsRequest = new SMSRequest(null, null);
        smsRequest.setPhoneNumber("0618287604");
        smsRequest.setMessage("Ceci est un test d'envoi de SMS.");
        WebTarget smsTarget = client.target(UriBuilder.fromUri(SMS_URI));
        Response smsResponse = smsTarget.request(MediaType.TEXT_PLAIN).post(Entity.json(smsRequest));
        System.out.println("SMS Notification Response: " + smsResponse.readEntity(String.class));
    }
}