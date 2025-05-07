import sqlite3
import os
import logging
import time
from spyne import Application, rpc, ServiceBase, Unicode
from spyne.protocol.soap import Soap11
from spyne.server.wsgi import WsgiApplication

# Connexion à la base de données
def get_db_connection():
    db_path = os.path.abspath('C:/Users/i/dtclass.db')
    conn = sqlite3.connect(db_path)
    return conn

# Service SOAP en streaming
class ValidateChequeService(ServiceBase):
    @rpc(_returns=Unicode)
    def get_cheques_status(ctx):
        conn = get_db_connection()
        cursor = conn.cursor()

        try:
            cursor.execute("SELECT ChequeNumber, ValidationStatus FROM Cheques")
            cheques = cursor.fetchall()
            response = "\n".join([f"Chèque {num}: {status}" for num, status in cheques])
        except sqlite3.Error as e:
            response = f"Erreur : {e}"
        finally:
            conn.close()

        return response

# Validation continue des chèques
def validate_cheques_streaming():
    while True:
        conn = get_db_connection()
        cursor = conn.cursor()

        try:
            cursor.execute("SELECT ChequeNumber FROM Cheques WHERE ValidationStatus IS NULL OR ValidationStatus = 'PENDING'")
            cheques = cursor.fetchall()

            for cheque in cheques:
                cheque_number = cheque[0]
                status = "VALIDATED" if int(cheque_number) % 2 == 0 else "REJECTED"

                cursor.execute("UPDATE Cheques SET ValidationStatus = ? WHERE ChequeNumber = ?", (status, cheque_number))

            conn.commit()
            logging.info(f"✅ {len(cheques)} chèques validés.")

        except sqlite3.Error as e:
            conn.rollback()
            logging.error(f"❌ Erreur de mise à jour SQL : {e}")

        finally:
            conn.close()

        time.sleep(3)  # Pause avant de re-vérifier

# Hébergement du service SOAP
application = Application(
    [ValidateChequeService],
    tns="bank.cheque.validation",
    in_protocol=Soap11(),
    out_protocol=Soap11()
)

if __name__ == "__main__":
    from wsgiref.simple_server import make_server
    logging.basicConfig(level=logging.INFO)

    # Lancer le service SOAP en parallèle
    import threading
    threading.Thread(target=validate_cheques_streaming, daemon=True).start()

    server = make_server('0.0.0.0', 8000, WsgiApplication(application))
    logging.info("🔄 Service SOAP et Streaming démarrés...")
    server.serve_forever()
