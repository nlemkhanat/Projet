package entity;

public class EmailRequest {
    private String to;
    private String subject;
    private String body;

    // Constructeur par défaut
    public EmailRequest() {
    }

    // Constructeur avec arguments
    public EmailRequest(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    // Getters et Setters
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}