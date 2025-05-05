package clients;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import entity.Customer;
import entity.Loan;
import entity.Loan.Status;

public class Load {
    private static Map<Long, Loan> loanRequests = new HashMap<>();
    private static Map<Long, Customer> customers = new HashMap<>();

    public static void loadLoanRequests() {
        String url = "jdbc:sqlite:C:/Users/i/dtclass.db";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM LoanRequest")) {

            while (rs.next()) {
                long id = rs.getLong("id");
                int customerId = rs.getInt("customer_id");
                double amount = rs.getDouble("amount");
                String loanType = rs.getString("loan_type");
                String status = rs.getString("status");

                Customer customer = getCustomerById(customerId);
                if (customer != null) {
                    Loan loan = new Loan(id,customer,amount,loanType, Loan.Status.valueOf(status));
                    loan.setId(id);
                    loanRequests.put(id, loan);
                } else {
                    System.err.println("Customer with ID " + customerId + " not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadCustomers() {
        String url = "jdbc:sqlite:C:/Users/i/dtclass.db";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Customer")) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");

                Customer customer = new Customer(id, name, email, phone);
                customers.put(id, customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Customer getCustomerById(int customerId) {
        return customers.get((long) customerId);
    }

    public static void main(String[] args) {
        loadCustomers();
        loadLoanRequests();

        // Display the loaded customers and loan requests in the console
        customers.forEach((id, customer) -> {
            System.out.println("Customer ID: " + id);
            System.out.println("Name: " + customer.getName());
            System.out.println("Email: " + customer.getEmail());
            System.out.println("Phone: " + customer.getPhone());
            System.out.println();
        });

        loanRequests.forEach((id, loan) -> {
            System.out.println("Loan ID: " + id);
            System.out.println("Customer ID: " + loan.getCustomer().getId());
            System.out.println("Amount: " + loan.getAmount());
            System.out.println("Loan Type: " + loan.getLoanType());
            System.out.println("Status: " + loan.getStatus());
            System.out.println();
        });
    }
}