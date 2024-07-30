import time
import csv
import psycopg2
from psycopg2 import sql
import os
from glob import glob

DB_HOST = os.getenv('DB_HOST', 'research')
DB_PORT = os.getenv('DB_PORT', '5432')
DB_NAME = os.getenv('DB_NAME', 'research')
DB_USER = os.getenv('DB_USER', 'test')
DB_PASSWORD = os.getenv('DB_PASSWORD', 'test')

CSV_DIR_PATH = 'anonymized_patients'

def create_connection():
    conn = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD
    )
    return conn

def insert_data(conn, data):
    query = sql.SQL("""
        INSERT INTO anonymized_patients (
            anonymized_date_of_birth, anonymized_name, disease, gender, zip_code
        ) VALUES (%s, %s, %s, %s, %s)
    """)
    with conn.cursor() as cursor:
        cursor.execute(query, data)
    conn.commit()

def load_data_from_csv(csv_file_path):
    with open(csv_file_path, mode='r') as file:
        reader = csv.DictReader(file)
        for row in reader:
            yield (
                row['dateOfBirth'],
                row['name'],
                row['disease'],
                row['gender'],
                row['zipcode']
            )

def main():
    time.sleep(10)
    conn = create_connection()
    try:
        while True:
            csv_files = sorted(glob(os.path.join(CSV_DIR_PATH, '*.csv')))
            print(csv_files)
            for csv_file in csv_files:
                for data in load_data_from_csv(csv_file):
                    insert_data(conn, data)
                print(f"Completed processing file: {csv_file}")
                time.sleep(30)
    except Exception as e:
        print(f"An error occurred: {e}")
    finally:
        conn.close()

if __name__ == "__main__":
    main()
