package ServiceDemandePret;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.sql.*;
import java.util.*;
import entity.Customer;
import entity.Loan;

@Path("loans")
public class LoanResource {

    private static final String DB_URL = "jdbc:sqlite:C:/Users/i/dtclass.db";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private Customer getCustomerById(long customerId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Customer WHERE id = ?")) {
            stmt.setLong(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                    customerId,
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Loan> getAllLoanRequests() {
        List<Loan> loans = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM LoanRequest")) {
            while (rs.next()) {
                long id = rs.getLong("id");
                long customerId = rs.getLong("customer_id");
                Customer customer = getCustomerById(customerId);
                if (customer != null) {
                    Loan loan = new Loan(
                        id,
                        customer,
                        rs.getDouble("amount"),
                        rs.getString("loan_type"),
                        Loan.Status.valueOf(rs.getString("status"))
                    );
                    loans.add(loan);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoanRequest(@PathParam("id") long id) {
        Loan loan = null;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM LoanRequest WHERE id = ?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Customer customer = getCustomerById(rs.getLong("customer_id"));
                if (customer != null) {
                    loan = new Loan(
                        id,
                        customer,
                        rs.getDouble("amount"),
                        rs.getString("loan_type"),
                        Loan.Status.valueOf(rs.getString("status"))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (loan == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }
        return Response.ok(loan).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLoanRequest(Loan request) {
        String sql = "INSERT INTO LoanRequest (customer_id, amount, loan_type, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, request.getCustomer().getId());
            stmt.setDouble(2, request.getAmount());
            stmt.setString(3, request.getLoanType());
            stmt.setString(4, request.getStatus().toString());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return Response.serverError().entity("Creation failed").build();
            }
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                request.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
        return Response.status(Response.Status.CREATED).entity(request).build();
    }

    @PUT
    @Path("{id}/verify")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyLoanRequest(@PathParam("id") long id) {
        final double maxLoanAmount = 200000;

        String sqlSelect = "SELECT * FROM LoanRequest WHERE id = ?";
        String sqlUpdate = "UPDATE LoanRequest SET status = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(sqlSelect)) {

            selectStmt.setLong(1, id);
            ResultSet rs = selectStmt.executeQuery();

            if (!rs.next()) {
                return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
            }

            double amount = rs.getDouble("amount");
            String loanType = rs.getString("loan_type");
            long customerId = rs.getLong("customer_id");

            Customer customer = getCustomerById(customerId);
            if (customer == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Customer not found").build();
            }

            Loan.Status newStatus = (amount > maxLoanAmount) ? Loan.Status.REJECTED : Loan.Status.PENDING;

            try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
                updateStmt.setString(1, newStatus.toString());
                updateStmt.setLong(2, id);
                updateStmt.executeUpdate();
            }

            Loan loanRequest = new Loan(id, customer, amount, loanType, newStatus);
            String message = (newStatus == Loan.Status.REJECTED)
                    ? "LoanRequest refused due to exceeding maximum loan amount"
                    : "LoanRequest is under processing";

            Map<String, Object> response = new HashMap<>();
            response.put("loanRequest", loanRequest);
            response.put("message", message);

            return Response.ok(response).build();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity("Database error occurred").build();
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteLoanRequest(@PathParam("id") long id) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM LoanRequest WHERE id = ?")) {
            stmt.setLong(1, id);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
        return Response.ok("LoanRequest deleted").build();
    }

    @GET
    @Path("{id}/status")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLoanStatus(@PathParam("id") long id) {
        String status = null;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT status FROM LoanRequest WHERE id = ?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                status = rs.getString("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }

        if (status == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("LoanRequest not found").build();
        }

        return Response.ok(status).build();
    }

    @GET
    @Path("/{loanId}/risk")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRiskLevel(@PathParam("loanId") long loanId) {
        String riskLevel = "UNKNOWN";
        try (Connection conn = getConnection();
             PreparedStatement loanStmt = conn.prepareStatement("SELECT customer_id FROM LoanRequest WHERE id = ?")) {
            loanStmt.setLong(1, loanId);
            ResultSet loanRs = loanStmt.executeQuery();
            if (loanRs.next()) {
                long customerId = loanRs.getLong("customer_id");
                try (PreparedStatement riskStmt = conn.prepareStatement("SELECT risk_level FROM FinancialProfile WHERE customer_id = ?")) {
                    riskStmt.setLong(1, customerId);
                    ResultSet riskRs = riskStmt.executeQuery();
                    if (riskRs.next()) {
                        riskLevel = riskRs.getString("risk_level");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Map<String, String> response = new HashMap<>();
        response.put("riskLevel", riskLevel);
        return Response.ok(response).build();
    }
}
