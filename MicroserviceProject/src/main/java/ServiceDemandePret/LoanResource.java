package ServiceDemandePret;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import entity.Customer;
import entity.Loan;

import java.sql.*;
import java.util.*;

@Path("loans")
public class LoanResource {

    private static Map<Long, Loan> loanRequests = new HashMap<>();
    private static Map<Long, Customer> customers = new HashMap<>();
    private static long currentId = 1;

    // Chargement du driver JDBC au démarrage
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        loadCustomers();
        loadLoanRequests();
    }

    private static void loadLoanRequests() {
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
                    Loan loan = new Loan(id, customer, amount, loanType, Loan.Status.valueOf(status));
                    loanRequests.put(id, loan);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loadCustomers() {
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

    private static Customer getCustomerById(long customerId) {
        return customers.get(customerId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Loan> getAllLoanRequests() {
        return loanRequests.values();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoanRequest(@PathParam("id") Long id) {
        Loan loan = loanRequests.get(id);
        if (loan == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }
        return Response.ok(loan).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLoanRequest(Loan loan) {
        loan.setId(currentId++);
        loanRequests.put(loan.getId(), loan);
        return Response.status(Response.Status.CREATED).entity(loan).build();
    }

    @PUT
    @Path("{id}/verify")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyLoanRequest(@PathParam("id") Long id) {
        Loan loan = loanRequests.get(id);
        if (loan == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }

        double maxLoanAmount = 200000;

        if (loan.getAmount() > maxLoanAmount) {
            loan.setStatus(Loan.Status.REJECTED);
            return Response.ok(loan).entity("LoanRequest rejected due to amount exceeding limit").build();
        } else {
            loan.setStatus(Loan.Status.PENDING);
            return Response.ok(loan).entity("LoanRequest is being processed").build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateLoanRequest(@PathParam("id") Long id, Loan updatedLoan) {
        if (!loanRequests.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }

        updatedLoan.setId(id); // Assure que l'ID reste cohérent
        loanRequests.put(id, updatedLoan);
        return Response.ok(updatedLoan).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteLoanRequest(@PathParam("id") Long id) {
        if (!loanRequests.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }
        loanRequests.remove(id);
        return Response.ok("LoanRequest deleted").build();
    }

    @GET
    @Path("{id}/status")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLoanStatus(@PathParam("id") Long id) {
        Loan loan = loanRequests.get(id);
        if (loan == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }
        return Response.ok(loan.getStatus().toString()).build();
    }

    @GET
    @Path("{loanId}/risk")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRiskLevel(@PathParam("loanId") long loanId) {
        Loan loan = loanRequests.get(loanId);
        if (loan == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }

        String riskLevel = getRiskLevelFromDatabase(loan.getCustomer().getId());
        Map<String, String> response = new HashMap<>();
        response.put("riskLevel", riskLevel);
        return Response.ok(response).build();
    }

    private String getRiskLevelFromDatabase(long customerId) {
        String url = "jdbc:sqlite:C:/Users/i/dtclass.db";
        String riskLevel = "UNKNOWN";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT risk_level FROM FinancialProfile WHERE customer_id = ?")) {
            stmt.setLong(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                riskLevel = rs.getString("risk_level");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return riskLevel;
    }
}
