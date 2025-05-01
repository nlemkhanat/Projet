package ServiceDemandePret;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import entity.Cheque;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Path("cheques")
public class ChequeResource {

    private static Map<String, String> cheques = new HashMap<>(); // Map pour associer ChequeNumber au ValidationStatus

    static {
        try {
            Class.forName("org.sqlite.JDBC"); // Charger le pilote JDBC
            loadCheques(); // Charger les chèques dans la Map
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur lors du chargement du pilote JDBC : " + e.getMessage());
        }
    }

    private static void loadCheques() {
        String url = "jdbc:sqlite:C:/Users/i/dtclass.db";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ChequeNumber, ValidationStatus FROM Cheques")) {

            while (rs.next()) {
                String chequeNumber = rs.getString("ChequeNumber");
                String validationStatus = rs.getString("ValidationStatus");

                // Ajouter le chèque et son statut dans la map
                cheques.put(chequeNumber, validationStatus);
            }
            System.out.println("Chèques chargés : " + cheques.size());
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement des chèques : " + e.getMessage());
        }
    }

    @GET
    @Path("{chequeNumber}/status")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getChequeStatus(@PathParam("chequeNumber") String chequeNumber) {
        String validationStatus = cheques.get(chequeNumber); // Rechercher le statut dans la map

        if (validationStatus == null) {
            // Si aucun chèque trouvé
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"Cheque not found\"}").build();
        }

    

    // Retournez uniquement le statut du chèque en réponse
    return Response.ok(validationStatus.toString()).build();
    }
}
