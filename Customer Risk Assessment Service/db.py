import sqlite3
import logging
import os  # Ajoutez cette ligne


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

def check_table_exists():
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='Customer';")
        table_exists = cur.fetchone()
        if table_exists:
            logging.info("Table TransactionHistory exists")
        else:
            logging.error("Table TransactionHistory does not exist")
        conn.close()
    except sqlite3.Error as e:
        logging.error(f"Error checking table existence: {e}")
        raise

def calculate_risk(customer_id):
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT amount FROM TransactionHistory WHERE customer_id = ?", (customer_id,))
        transactions = cur.fetchall()
        total_amount = sum([t[0] for t in transactions])
        logging.info(f"Total amount for customer_id {customer_id}: {total_amount}")
        
        if total_amount > 10000:
            return "HIGH"
        elif total_amount > 5000:
            return "MEDIUM"
        else:
            return "LOW"
    except sqlite3.Error as e:
        logging.error(f"Database error: {e}")
        raise

def update_financial_profile(customer_id, risk_level):
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("""
            INSERT INTO FinancialProfile (customer_id, risk_level)
            VALUES (?, ?)
            ON CONFLICT (customer_id) DO UPDATE SET risk_level = excluded.risk_level
        """, (customer_id, risk_level))
        conn.commit()
        cur.close()
        conn.close()
        logging.info(f"Updated FinancialProfile for customer_id {customer_id} with risk_level {risk_level}")
    except sqlite3.Error as e:
        logging.error(f"Database error: {e}")
        raise

def get_all_customer_ids():
    """
    Récupère tous les IDs des clients depuis la table Customer.
    """
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT id FROM Customer")  # Assurez-vous que la table Customer existe
        customer_ids = [row[0] for row in cur.fetchall()]
        conn.close()
        return customer_ids
    except sqlite3.Error as e:
        logging.error(f"Erreur lors de la récupération des IDs des clients : {e}")
        raise
