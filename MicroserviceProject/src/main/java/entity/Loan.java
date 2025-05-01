package entity;

public class Loan {
    private long id;
    private Customer customer;
    private double amount;
    private String loanType;
    private Status status;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED
    }

    // Constructeur par défaut
    public Loan() {
    }

    // Constructeur avec paramètres
    public Loan(long id, Customer customer, double amount, String loanType, Status status) {
        this.id = id;
        this.customer = customer;
        this.amount = amount;
        this.loanType = loanType;
        this.status = status;
    }

    // Getters et setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}