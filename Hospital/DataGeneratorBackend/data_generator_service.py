from flask import Flask, request, jsonify
from flask_cors import CORS
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.interval import IntervalTrigger
import random
import pytz
import os
import requests
import pandas as pd
import threading

app = Flask(__name__)
CORS(app)

next_patient_id = 1

# Parameters, set via frontend or random
parameters = {
    'batch_size': '60',
    'delete_size': '5',
    'diversity_level': 'random'
}
patients = {}

diseases_df = pd.read_csv('diseases.csv')
disease_pool = diseases_df['Disease'].tolist()

scheduler = BackgroundScheduler()
lock = threading.Lock()

# Method to initialize the database with some patients
def initialize_patients():
    with lock:
        initial_batch_size = random.randint(50, 70)
        initial_patients = {}
        for _ in range(initial_batch_size):
            patient = generate_patient()
            patients[patient['id']] = patient
            initial_patients[patient['id']] = patient
        send_patients_to_management_service(initial_patients)
        # print(f"Initialized with {len(initial_patients)} patient/s.")

# Method to generate one patient
def generate_patient():
    global next_patient_id

    if parameters['diversity_level'] == 'random':
        diversity_level = random.randint(0, 100)
    else:
        diversity_level = int(parameters['diversity_level'])
    diversity_index = int(len(disease_pool) * (diversity_level / 100))

    selected_diseases = random.sample(disease_pool, max(1, diversity_index))
    disease = random.choice(selected_diseases)


    patient_id = next_patient_id
    next_patient_id += 1
    patient = {
        "id": patient_id,
        "name": random.choice(["Alice", "Bob", "Charlie", "David", "Eva"]),
        # because year granularity is only supported for millenium as a last granularity
        "dateOfBirth": f"{random.randint(1950, 1999)}-{random.randint(1, 12):02}-{random.randint(1, 28):02}",
        "zipCode": f"{random.randint(10000, 11000):05}",
        "gender": random.choice(["Male", "Female"]),
        "disease": disease
    }
    return patient

# Method to send several patients to the patient management service
def send_patients_to_management_service(patients):
    patient_list = list(patients.values())
    try:
        response = requests.post("http://patient-management-service:8080/patients/createPatients", json=patient_list)
        if response.status_code != 201:
            print(f"Response: {response.text}")
    except requests.exceptions.RequestException as e:
        print(f"Error: {e}")

# Method to send one patient to the patient management service
def send_patient_to_management_service(patient):
    try:
        response = requests.post("http://patient-management-service:8080/patients/createPatient", json=patient)
        if response.status_code != 201:
            print(f"Response: {response.text}")
    except requests.exceptions.RequestException as e:
        print(f"Error: {e}")

# Method to delete patients from the database
def delete_patients_from_management_service(patient_ids):
    try:
        response = requests.post("http://patient-management-service:8080/patients/deletePatientsByIds", json=patient_ids)
        if response.status_code != 204:
            print(f"Response: {response.text}")
    except requests.exceptions.RequestException as e:
        print(f"Error: {e}")

# Method to generate a batch of patients
def generate_patients():
    with lock:
        if parameters['batch_size'] == 'random':
            batch_size = random.randint(1, 50)
        else:
            batch_size = int(parameters['batch_size'])
        new_patients = {}
        for _ in range(batch_size):
            patient = generate_patient()
            patients[patient['id']] = patient
            new_patients[patient['id']] = patient
        if len(new_patients) == 1:
            send_patient_to_management_service(list(new_patients.values())[0])
        else:
            send_patients_to_management_service(new_patients)
        # print(f"Generated {len(new_patients)} patient/s.")

# Method to delete patients
def delete_patients():
    with lock:
        if len(patients) == 0:
            print("No patients to delete.")
            return
        if parameters['delete_size'] == 'random':
            delete_size = random.randint(1, len(patients))
        else:
            delete_size = int(parameters['delete_size'])
        if delete_size > len(patients):
            patient_ids = list(patients.keys())
        else:
            patient_ids = list(patients.keys())[:delete_size]
        for patient_id in patient_ids:
            patients.pop(patient_id, None)
        if len(patient_ids) != 0:
            delete_patients_from_management_service(patient_ids)
        # print(f"Deleted {len(patient_ids)} patients IDs: {patient_ids}")

# Endpoint to set parameters
@app.route('/set_parameters', methods=['POST'])
def set_parameters():
    req_data = request.get_json()
    for param in ['batch_size', 'delete_size', 'diversity_level']:
        if param in req_data:
            parameters[param] = req_data[param]
    # print(f"Parameters updated: {parameters}")
    return jsonify({"message": "Parameters updated", "parameters": parameters}), 200

# Method to schedule the generation and deletion
def schedule_tasks():
    if not scheduler.get_jobs():
        scheduler.add_job(generate_patients, IntervalTrigger(seconds=15, timezone=pytz.utc), id='generate_patients_job')
        scheduler.add_job(delete_patients, IntervalTrigger(seconds=15, timezone=pytz.utc), id='delete_patients_job')
        scheduler.start()

if __name__ == '__main__':
    if not app.debug or os.environ.get('WERKZEUG_RUN_MAIN') == 'true':
        # time.sleep(5)
        # initialize_patients()
        schedule_tasks()
    app.run(debug=True, host='0.0.0.0', port=5000)