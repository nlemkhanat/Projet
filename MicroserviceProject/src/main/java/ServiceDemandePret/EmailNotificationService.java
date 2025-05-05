package ServiceDemandePret;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import entity.EmailRequest;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

@Path("notifications/email")
public class EmailNotificationService {

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendEmailNotification(EmailRequest emailRequest) {
        try {
            sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody());
            return Response.ok("Email envoyé avec succès").build();
        } catch (MessagingException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Échec de l'envoi de l'email").build();
        }
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException {
        String from = "nassr-eddine@outlook.fr";
        String host = "smtp.example.com";

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);

        Session session = Session.getDefaultInstance(properties);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}