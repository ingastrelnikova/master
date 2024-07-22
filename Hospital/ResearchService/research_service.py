from flask import Flask, jsonify, request
import psycopg2
import os
import requests
import sys

app = Flask(__name__)

# database connection
def get_db_connection():
    conn = psycopg2.connect(
        dbname=os.getenv('DB_NAME', 'research'),
        user=os.getenv('DB_USER', 'test'),
        password=os.getenv('DB_PASSWORD', 'test'),
        host=os.getenv('DB_HOST', 'research-db'),
        port=os.getenv('DB_PORT', '5432')
    )
    return conn

# method for access control by checking with OPA
def check_authorization():
    opa_url = os.getenv('OPA_URL','http://opa:8181/v1/data/authz/allow')
    input_data = {
        "input": {
            "method": request.method,
            "path": request.path
        }
    }
    response = requests.post(opa_url, json=input_data)
    response_json = response.json()
    sys.stdout.flush()
    return response_json.get("result", False)

# endpoint to retrieve data
@app.route('/data', methods=['GET'])
def get_data():
    sys.stdout.flush()
    if not check_authorization():
        sys.stdout.flush()
        return "Unauthorized", 403
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM anonymized_patients;')
        rows = cursor.fetchall()
        cursor.close()
        conn.close()
        return jsonify(rows)
    except Exception as e:
        return str(e), 500

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=3002)
