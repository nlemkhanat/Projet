package entity;


import javax.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "Cheques")
public class Cheque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChequeID")
    private Long chequeId;

    @Column(name = "ChequeNumber", nullable = false)
    private String chequeNumber;

    @Column(name = "Amount", nullable = false)
    private Double amount;

    @Column(name = "SubmissionDate", nullable = false)
    private Timestamp submissionDate;

    @Column(name = "ValidationStatus", nullable = false)
    private String validationStatus; // Statuts possibles : PENDING, VALIDATED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerID", nullable = false)
    private Customer customer;

  


    // Parameterized constructor
    public Cheque(Long chequeId, String chequeNumber, Double amount, Timestamp submissionDate, String validationStatus, Customer customer) {
        this.chequeId = chequeId;
        this.chequeNumber = chequeNumber;
        this.amount = amount;
        this.submissionDate = submissionDate;
        this.validationStatus = validationStatus;
        this.customer = customer;
    }

    public Cheque(long chequeId2, String chequeNumber2, double amount2, Timestamp submissionDate2,
			String validationStatus2, Customer customer2) {
		// TODO Auto-generated constructor stub
	}

	// Getters and Setters
    public Long getChequeId() {
        return chequeId;
    }

    public void setChequeId(Long chequeId) {
        this.chequeId = chequeId;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Timestamp getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Timestamp submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

	

	
}

