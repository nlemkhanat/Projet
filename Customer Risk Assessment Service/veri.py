import sqlite3
import logging
import os

def get_db_connection():
    # Utiliser le chemin correct de la base de données
    db_path = os.path.abspath('C:/Users/i/dtclass.db')
    logging.info(f"Database path: {db_path}")
    try:
        conn = sqlite3.connect(db_path)
        logging.info("Connected to database dtclass.db")
        return conn
    except sqlite3.Error as e:
        logging.error(f"Error connecting to database: {e}")
        raise

def list_tables_and_schema():
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        
        # Lister toutes les tables
        cur.execute("SELECT name FROM sqlite_master WHERE type='table';")
        tables = cur.fetchall()
        if tables:
            logging.info("Tables in the database:")
            for table in tables:
                logging.info(f"Table: {table[0]}")
                
                # Afficher le schéma de chaque table
                cur.execute(f"PRAGMA table_info({table[0]});")
                schema = cur.fetchall()
                logging.info(f"Schema for table {table[0]}:")
                for column in schema:
                    logging.info(column)
        else:
            logging.error("No tables found in the database")
        
        conn.close()
    except sqlite3.Error as e:
        logging.error(f"Error accessing tables and schema: {e}")
        raise

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    list_tables_and_schema()