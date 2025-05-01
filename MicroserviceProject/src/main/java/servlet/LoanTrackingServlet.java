package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LoanTrackingServlet")
public class LoanTrackingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        response.getWriter().append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<title>Suivi de la Demande de Prêt</title>")
            .append("<style>")
            .append("body {font-family: 'Roboto', sans-serif; margin: 0; background-color: #f0f4f8; color: #333;}")
            .append("header {background-color: #0047ab; color: white; padding: 20px; text-align: center; box-shadow: 0 2px 5px rgba(0,0,0,0.1);}")
            .append(".container {max-width: 700px; margin: auto; padding: 20px; background-color: white; border-radius: 12px; box-shadow: 0 5px 15px rgba(0,0,0,0.2);}")
            .append("form {display: flex; flex-direction: column; gap: 15px;}")
            .append("input, button {width: 100%; padding: 10px; margin-bottom: 15px; border-radius: 5px; border: 1px solid #ddd;}")
            .append("button {background-color: #0047ab; color: white; border: none; cursor: pointer; font-size: 16px;}")
            .append("button:hover {background-color: #003687;}")
            .append(".progress-container {margin-top: 20px;}")
            .append(".progress-bar {width: 100%; background-color: #eee; border-radius: 10px;}")
            .append(".progress-bar-inner {height: 20px; border-radius: 10px; text-align: center; color: white;}")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<header>")
            .append("<h1>Suivi de votre Demande de Prêt</h1>")
            .append("</header>")
            .append("<div class='container'>")
            .append("<form action='' method='POST'>")
            .append("<label for='loanId'>Entrez l'ID de votre demande de prêt :</label>")
            .append("<input type='text' id='loanId' name='loanId' required placeholder='ID de la demande de prêt'>")
            .append("<button type='submit'>Afficher le cheminement</button>")
            .append("</form>")
            .append("<div class='progress-container' id='progressContainer' style='display:none;'>")
            .append("<h2>Cheminement</h2>")
            .append("<div class='progress-bar'>")
            .append("<div class='progress-bar-inner' id='progressBar' style='width: 0%; background-color: orange;'>PENDING</div>")
            .append("</div>")
            .append("</div>")
            .append("</div>")
            .append("</body>")
            .append("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String loanId = request.getParameter("loanId");

        // Appeler le service REST pour obtenir le statut de la demande
        String status = fetchLoanStatus(loanId);

        // Générer la réponse HTML avec une barre de cheminement interactive
        response.setContentType("text/html");
        response.getWriter().append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<title>Cheminement de la Demande</title>")
            .append("<style>")
            .append("body {font-family: 'Roboto', sans-serif; margin: 0; background-color: #f4f4f8; color: #333;}")
            .append("header {background-color: #0047ab; color: white; padding: 20px; text-align: center; box-shadow: 0 2px 5px rgba(0,0,0,0.1);}")
            .append(".container {max-width: 700px; margin: auto; padding: 20px; background-color: white; border-radius: 12px; box-shadow: 0 5px 15px rgba(0,0,0,0.2);}")
            .append(".progress-container {margin-top: 20px;}")
            .append(".progress-bar {width: 100%; background-color: #eee; border-radius: 10px;}")
            .append(".progress-bar-inner {height: 30px; border-radius: 10px; text-align: center; color: white; line-height: 30px;}")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<header>")
            .append("<h1>Cheminement de la Demande</h1>")
            .append("</header>")
            .append("<div class='container'>")
            .append("<h2>Statut Actuel : " + status + "</h2>")
            .append("<div class='progress-bar'>")
            .append("<div class='progress-bar-inner' style='width: " + getStatusWidth(status) + "; background-color: " + getStatusColor(status) + ";'>" + status + "</div>")
            .append("</div>")
            .append("<p><a href='/LoanServlet'>Revenir au formulaire</a></p>")
            .append("</div>")
            .append("</body>")
            .append("</html>");
    }

    private String fetchLoanStatus(String loanId) {
        try {
            URL url = new URL("http://localhost:8080/MicroserviceProject/loans/" + loanId + "/status");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                byte[] data = inputStream.readAllBytes();
                String response = new String(data);
                return response;
            } else {
                return "UNKNOWN";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    private String getStatusWidth(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return "25%";
            case "APPROVED": return "50%";
            case "REJECTED": return "75%";
            case "CANCELLED": return "100%";
            default: return "0%";
        }
    }

    private String getStatusColor(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return "orange";
            case "APPROVED": return "green";
            case "REJECTED": return "red";
            case "CANCELLED": return "gray";
            default: return "blue";
        }
    }
}
