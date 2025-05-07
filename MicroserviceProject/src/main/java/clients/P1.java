package clients;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LoanServlet") // URL correcte pour accéder à la servlet
public class P1 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String URL = "jdbc:sqlite:C:/Users/i/dtclass.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int generateId() {
        return (int) (Math.random() * 10000); // Générer un identifiant unique
    }

    // **📝 Affichage du formulaire de demande de prêt**
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.getWriter().append("<!DOCTYPE html>")
                .append("<html lang='fr'>")
                .append("<head><meta charset='UTF-8'><title>Demande de Prêt</title>")
                .append("<link rel='stylesheet' href='css/style.css'>") // Lien CSS corrigé
                .append("</head><body>")
                .append("<div class='form-container'>")
                .append("<h2>Formulaire de demande de prêt</h2>")
                .append("<form action='LoanServlet' method='POST'>")
                .append("<label for='name'>Nom :</label>")
                .append("<input type='text' id='name' name='name' required>")

                .append("<label for='email'>Email :</label>")
                .append("<input type='email' id='email' name='email' required>")

                .append("<label for='phone'>Téléphone :</label>")
                .append("<input type='text' id='phone' name='phone' required>")

                .append("<label for='amount'>Montant du Prêt :</label>")
                .append("<input type='number' id='amount' name='amount' step='0.01' required>")

                .append("<label for='loanType'>Type de Prêt :</label>")
                .append("<select id='loanType' name='loanType' required>")
                .append("<option value='PERSONAL'>Personnel</option>")
                .append("<option value='COMMERCIAL'>Commercial</option>")
                .append("</select>")

                .append("<button type='submit'>Envoyer la demande</button>")
                .append("</form>")
                .append("</div>")
                .append("</body></html>");
    }

    // **📌 Traitement de la demande après soumission**
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String amount = request.getParameter("amount");
        String loanType = request.getParameter("loanType");

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);

            // **🔹 Insérer le client**
            int customerId = generateId();
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Customer (id, name, email, phone) VALUES (?, ?, ?, ?)")) {
                pstmt.setInt(1, customerId);
                pstmt.setString(2, name);
                pstmt.setString(3, email);
                pstmt.setString(4, phone);
                pstmt.executeUpdate();
            }

            // **🔹 Insérer la demande de prêt**
            int loanId = generateId();
            try (PreparedStatement pstmtLoan = conn.prepareStatement("INSERT INTO LoanRequest (id, customer_id, amount, loan_type, status) VALUES (?, ?, ?, ?, 'PENDING')")) {
                pstmtLoan.setInt(1, loanId);
                pstmtLoan.setInt(2, customerId);
                pstmtLoan.setBigDecimal(3, new BigDecimal(amount));
                pstmtLoan.setString(4, loanType);
                pstmtLoan.executeUpdate();
            }

            conn.commit();

            // **🔹 Affichage avancé de la confirmation**
            response.setContentType("text/html");
            response.getWriter().append("<!DOCTYPE html>")
                    .append("<html lang='fr'>")
                    .append("<head><meta charset='UTF-8'><title>Demande soumise</title>")
                    .append("<link rel='stylesheet' href='css/style.css'>") // Lien CSS corrigé
                    .append("</head><body>")
                    .append("<div class='confirmation-container'>")
                    .append("<h2>Votre demande a été envoyée avec succès !</h2>")
                    .append("<p><strong>Nom :</strong> " + name + "</p>")
                    .append("<p><strong>Email :</strong> " + email + "</p>")
                    .append("<p><strong>Téléphone :</strong> " + phone + "</p>")
                    .append("<p><strong>Montant :</strong> " + amount + " €</p>")
                    .append("<p><strong>Type de prêt :</strong> " + loanType + "</p>")
                    .append("<p><strong>Statut :</strong> <span class='pending'>En attente</span></p>")
                    
                    .append("<p><a href='http://localhost:8080/ProjectLoanRequestServices/loans/" + loanId + "/status'>Vérifiez l'état de votre demande de prêt</a></p>")
                    .append("<button onclick='window.location.href=\"index.html\"'>Retour à l'accueil</button>")
                    .append("</div></body></html>");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().append("<!DOCTYPE html><html><body><h3>❌ Erreur lors de l'enregistrement.</h3><p>" + e.getMessage() + "</p></body></html>");
        }
    }
}
