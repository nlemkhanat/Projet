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
                Loan loan = new Loan(id, customer, amount, loanType, Loan.Status.valueOf(status));
                loanRequests.put(id, loan);
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

    private static Customer getCustomerById(int customerId) {
        return customers.get((long) customerId);
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
        Loan request = loanRequests.get(id);
        if (request == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }
        return Response.ok(request).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLoanRequest(Loan request) {
        request.setId(currentId++);
        loanRequests.put(request.getId(), request);
        return Response.status(Response.Status.CREATED).entity(request).build();
    }
    
    @PUT
    @Path("{id}/verify")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyLoanRequest(@PathParam("id") Long id) {
        Loan request = loanRequests.get(id);
        if (request == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }

        // Supposons que le montant maximum du prêt est 100000
        double maxLoanAmount = 200000;

        if (request.getAmount() > maxLoanAmount) {
            request.setStatus(Loan.Status.REJECTED);
            return Response.ok(request).entity("LoanRequest refused due to exceeding maximum loan amount").build();
        } else {
            request.setStatus(Loan.Status.PENDING);
            return Response.ok(request).entity("LoanRequest is under processing").build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateLoanRequest(@PathParam("id") Long id, Loan updatedRequest) {
        if (!loanRequests.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }
        updatedRequest.setId(id);
        loanRequests.put(id, updatedRequest);
        return Response.ok(updatedRequest).build();
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
        Loan request = loanRequests.get(id);
        if (request == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }
        return Response.ok(request.getStatus().toString()).build();
    }
    
    @GET
    @Path("/{loanId}/risk")
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
             PreparedStatement stmt = conn.prepareStatement("SELECT risk_level FROM FinancialProfile WHERE customer_id = ?")) {
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