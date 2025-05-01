import sqlite3
import os
import logging
from spyne import Application, rpc, ServiceBase, Unicode, Integer
from spyne.protocol.soap import Soap11
from spyne.server.wsgi import WsgiApplication

# Fonction pour se connecter à la base de données
def get_db_connection():
    db_path = os.path.abspath('C:/Users/i/dtclass.db')
    logging.info(f"Database path: {db_path}")
    try:
        conn = sqlite3.connect(db_path)
        logging.info("Connected to database dtclass.db")
        return conn
    except sqlite3.Error as e:
        logging.error(f"Error connecting to database: {e}")
        raise

# Créer le service SOAP
class ValidateChequeService(ServiceBase):
    @rpc(Unicode, _returns=Unicode)
    def validate_cheque(ctx, cheque_number):
        # Connexion à la base de données
        conn = get_db_connection()
        cursor = conn.cursor()

        # Logique de validation de chèque
        if int(cheque_number) % 2 == 0:
            status = "VALIDATED"
        else:
            status = "REJECTED"

        # Insérer ou mettre à jour dans la table Cheques
        try:
            cursor.execute("""
                UPDATE Cheques
                SET ValidationStatus = ?
                WHERE ChequeNumber = ?;
            """, (status, cheque_number))

            if cursor.rowcount == 0:
                # Si le chèque n'existe pas, l'insérer
                cursor.execute("""
                    INSERT INTO Cheques (ChequeNumber, Amount, SubmissionDate, ValidationStatus, CustomerID)
                    VALUES (?, 0.00, DATETIME('now'), ?, 1);
                """, (cheque_number, status))

            conn.commit()
        except sqlite3.Error as e:
            conn.rollback()
            logging.error(f"Database error: {e}")
            return f"Error: {e}"
        finally:
            conn.close()

        return f"Cheque {cheque_number} validation status: {status}"

# Configurer l'application SOAP
application = Application(
    [ValidateChequeService],
    tns="bank.cheque.validation",
    in_protocol=Soap11(),
    out_protocol=Soap11()
)

# Héberger l'application avec un serveur WSGI
if __name__ == "__main__":
    from wsgiref.simple_server import make_server

    logging.basicConfig(level=logging.INFO)
    logging.info("Starting ValidateChequeService...")

    server = make_server('0.0.0.0', 8000, WsgiApplication(application))
    logging.info("Service is running on http://0.0.0.0:8000")
    server.serve_forever()
