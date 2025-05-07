package servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/BankLoan")
public class BanLoanServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // URL de connexion à la base de données SQLite
    private static final String DB_URL = "jdbc:sqlite:C:/Users/i/dtclass.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC"); // Charger le pilote JDBC SQLite
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Génère un identifiant de prêt aléatoire
    private int generateLoanId() {
        return (int) (Math.random() * 10000);
    }

    // Affichage du formulaire de demande de prêt
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        List<String[]> clients = loadClients();

        response.getWriter().append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<title>BAN - Demande de Prêt</title>")
            .append("<style>")
            .append("body {font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;}")
            .append("header {background-color: #003366; color: white; padding: 1em; text-align: center;}")
            .append("form {max-width: 600px; margin: 50px auto; padding: 20px; background: white; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1);}")
            .append("input, select {width: 100%; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 4px;}")
            .append("input[type='submit'] {background-color: #003366; color: white; border: none; cursor: pointer;}")
            .append("input[type='submit']:hover {background-color: #0055aa;}")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<header>")
            .append("<h1>BAN - Formulaire de Demande de Prêt</h1>")
            .append("</header>")
            .append("<form action='' method='POST'>")
            .append("<label for='customer'>Sélectionnez un client :</label>")
            .append("<select id='customer' name='customer' required>")
            .append("<option value=''>--- Choisir un client ---</option>");

        for (String[] client : clients) {
            response.getWriter().append("<option value='" + client[0] + "'>" + client[1] + " (" + client[2] + ")</option>");
        }

        response.getWriter()
            .append("</select>")
            .append("<label for='amount'>Montant du prêt (€) :</label>")
            .append("<input type='number' id='amount' name='amount' step='0.01' required placeholder='Entrez le montant'>")
            .append("<label for='loanType'>Type de prêt :</label>")
            .append("<select id='loanType' name='loanType' required>")
            .append("<option value='PERSONAL'>Personnel</option>")
            .append("<option value='COMMERCIAL'>Commercial</option>")
            .append("</select>")
            .append("<input type='submit' value='Soumettre la demande'>")
            .append("</form>")
            .append("</body>")
            .append("</html>");
    }

    // Traitement de la demande de prêt
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String customerId = request.getParameter("customer");
        String amount = request.getParameter("amount");
        String loanType = request.getParameter("loanType");

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false); // Début transaction

            int loanId = generateLoanId();
            String insertLoanSQL = "INSERT INTO LoanRequest (id, customer_id, amount, loan_type, status) VALUES (?, ?, ?, ?, 'PENDING')";
            try (PreparedStatement pstmt = conn.prepareStatement(insertLoanSQL)) {
                pstmt.setInt(1, loanId);
                pstmt.setInt(2, Integer.parseInt(customerId));
                pstmt.setBigDecimal(3, new BigDecimal(amount));
                pstmt.setString(4, loanType);
                pstmt.executeUpdate();
            }

            conn.commit(); // Valider transaction

            response.setContentType("text/html");
            response.getWriter().append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<title>Confirmation de la demande</title>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; background-color: #f0f2f5; margin: 0; padding: 0; }")
                .append("header { background-color: #1a73e8; color: white; padding: 1em; text-align: center; }")
                .append(".container { max-width: 600px; margin: 40px auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.1); }")
                .append("h2 { color: #333; }")
                .append("p { font-size: 16px; color: #444; margin: 10px 0; }")
                .append("a.button { display: inline-block; margin-top: 20px; padding: 10px 15px; background-color: #1a73e8; color: white; text-decoration: none; border-radius: 5px; }")
                .append("a.button:hover { background-color: #0b5ed7; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<header><h1>Confirmation de la Demande de Prêt</h1></header>")
                .append("<div class='container'>")
                .append("<h2>Votre demande a été enregistrée avec succès !</h2>")
                .append("<p><strong>ID du client :</strong> " + customerId + "</p>")
                .append("<p><strong>Montant du prêt :</strong> " + amount + " €</p>")
                .append("<p><strong>Type de prêt :</strong> " + (loanType.equals("PERSONAL") ? "Personnel" : "Commercial") + "</p>")
                .append("<p><strong>ID du prêt :</strong> " + loanId + "</p>")
                .append("<a class='button' href='/MicroserviceProject/loans/" + loanId + "/status'>Vérifier le statut de votre demande</a>")
                .append("</div>")
                .append("</body>")
                .append("</html>");


        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().append("<!DOCTYPE html>")
                .append("<html><body><h3>Erreur BAN - Échec de l'enregistrement :</h3>")
                .append("<p>" + e.getMessage() + "</p>")
                .append("<a href='/BanLoanServlet'>Retour au formulaire</a>")
                .append("</body></html>");
        }
    }

    // Chargement de la liste des clients depuis la base
    private List<String[]> loadClients() {
        List<String[]> clients = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, email FROM Customer")) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id"));
                String name = rs.getString("name");
                String email = rs.getString("email");
                clients.add(new String[]{id, name, email});
            }
        } catch (SQLException e) {
            System.out.println("Erreur SQL - Chargement clients BAN : " + e.getMessage());
        }
        return clients;
    }
}
