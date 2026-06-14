-- CareWatch System Database Schema
-- Target Platform: PostgreSQL / Neon PostgreSQL

-- 1. Create the Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL, -- Plaintext for prototype phase
    role VARCHAR(30) NOT NULL,      -- Expected values: 'PATIENT', 'DOCTOR_ADMIN', 'SUPER_ADMIN'
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Insert Demo Users (Clean restart state)
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

INSERT INTO users (full_name, email, password, role, status)
VALUES 
('Patient User', 'patient@gmail.com', '123456', 'PATIENT', 'ACTIVE'),
('Doctor Admin', 'doctor@gmail.com', '123456', 'DOCTOR_ADMIN', 'ACTIVE'),
('Super Admin', 'admin@gmail.com', '123456', 'SUPER_ADMIN', 'ACTIVE');

-- 3. Create the Patients Table
CREATE TABLE IF NOT EXISTS patients (
    patient_id SERIAL PRIMARY KEY,
    user_id INT UNIQUE REFERENCES users(user_id),
    age INT,
    gender VARCHAR(20),
    phone VARCHAR(30),
    address TEXT,
    disease_type VARCHAR(100),
    risk_level VARCHAR(30),
    assigned_doctor_id INT REFERENCES users(user_id),
    emergency_contact VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Demo Patients (linked to Patient User with user_id = 1, assigned to Doctor Admin with user_id = 2)
TRUNCATE TABLE patients RESTART IDENTITY CASCADE;

INSERT INTO patients (user_id, age, gender, phone, address, disease_type, risk_level, assigned_doctor_id, emergency_contact)
VALUES 
(1, 35, 'Male', '+123456789', '123 Health Ave, Medical City', 'Diabetes Type 2', 'NORMAL', 2, 'Jane Doe (+198765432)');

-- 4. Create the Vitals History Table
CREATE TABLE IF NOT EXISTS vitals_history (
    vital_id SERIAL PRIMARY KEY,
    patient_id INT REFERENCES patients(patient_id),
    heart_rate INT,
    oxygen_level INT,
    systolic_bp INT,
    diastolic_bp INT,
    temperature DECIMAL(4,1),
    sugar_level INT,
    symptoms TEXT,
    urgency_level VARCHAR(30),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Demo Vitals History (linked to patient_id = 1)
TRUNCATE TABLE vitals_history RESTART IDENTITY CASCADE;

INSERT INTO vitals_history (patient_id, heart_rate, oxygen_level, systolic_bp, diastolic_bp, temperature, sugar_level, symptoms, urgency_level)
VALUES
(1, 72, 98, 120, 80, 98.6, 95, 'No major symptoms, feeling good.', 'NORMAL'),
(1, 105, 94, 135, 85, 99.5, 120, 'Mild headache and slightly tired.', 'WARNING'),
(1, 135, 88, 150, 95, 103.2, 180, 'High fever, shortness of breath, fast heart beat.', 'CRITICAL');

-- 5. Create the Triage Queue Table
CREATE TABLE IF NOT EXISTS triage_queue (
    queue_id SERIAL PRIMARY KEY,
    patient_id INT REFERENCES patients(patient_id),
    vital_id INT REFERENCES vitals_history(vital_id),
    urgency_level VARCHAR(30),
    priority_score INT,
    alert_message TEXT,
    status VARCHAR(30) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Insert Demo Triage Queue Entries (linked to patient_id = 1, vital_id = 3)
TRUNCATE TABLE triage_queue RESTART IDENTITY CASCADE;

INSERT INTO triage_queue (patient_id, vital_id, urgency_level, priority_score, alert_message, status)
VALUES
(1, 3, 'CRITICAL', 1, 'Patient has CRITICAL vitals: Heart Rate 135 bpm, Oxygen 88%, Temp 103.2°F. High fever, shortness of breath, fast heart beat.', 'PENDING');

-- 7. Create the Doctor Interventions Table
CREATE TABLE IF NOT EXISTS doctor_interventions (
    intervention_id SERIAL PRIMARY KEY,
    patient_id INT REFERENCES patients(patient_id),
    doctor_user_id INT REFERENCES users(user_id),
    action_taken TEXT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Insert Demo Doctor Interventions (linked to patient_id = 1, doctor_user_id = 2)
TRUNCATE TABLE doctor_interventions RESTART IDENTITY CASCADE;

INSERT INTO doctor_interventions (patient_id, doctor_user_id, action_taken, notes)
VALUES
(1, 2, 'Called Patient', 'Spoke to patient regarding high heart rate and low oxygen levels. Advised to stay hydrated and monitor breathing.'),
(1, 2, 'Marked as Stable', 'Vitals stabilized back to normal after a brief period of observation.');



