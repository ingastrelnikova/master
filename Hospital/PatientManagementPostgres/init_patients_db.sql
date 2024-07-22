CREATE TABLE IF NOT EXISTS patients (
                                        id SERIAL PRIMARY KEY,
                                        name VARCHAR(100),
    date_of_birth DATE,
    zip_code VARCHAR(10),
    gender VARCHAR(10),
    disease VARCHAR(100),
    created_at TIMESTAMP,
    anonymized BOOLEAN DEFAULT FALSE
    );
