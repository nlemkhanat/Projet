package entity;

public class SMSRequest {
    private String phoneNumber;
    private String message;

    // Constructeur par défaut
    public SMSRequest() {
    }

    // Constructeur avec arguments
    public SMSRequest(String phoneNumber, String message) {
        this.phoneNumber = phoneNumber;
        this.message = message;
    }

    // Getters et Setters
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}