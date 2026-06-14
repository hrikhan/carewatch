package com.carewatch.features.monitoring.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

// Model class representing a patient's profile and vital metrics join view
public class PatientVitalView {
    private int patientId;
    private String patientName;
    private String email;
    private String phone;
    private String diseaseType;
    private int heartRate;
    private int oxygenLevel;
    private int systolicBp;
    private int diastolicBp;
    private double temperature;
    private int sugarLevel;
    private String symptoms;
    private String urgencyLevel;
    private Timestamp recordedAt;

    // Constructors
    public PatientVitalView() {}

    public PatientVitalView(int patientId, String patientName, String email, String phone, String diseaseType,
                            int heartRate, int oxygenLevel, int systolicBp, int diastolicBp, double temperature,
                            int sugarLevel, String symptoms, String urgencyLevel, Timestamp recordedAt) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.email = email;
        this.phone = phone;
        this.diseaseType = diseaseType;
        this.heartRate = heartRate;
        this.oxygenLevel = oxygenLevel;
        this.systolicBp = systolicBp;
        this.diastolicBp = diastolicBp;
        this.temperature = temperature;
        this.sugarLevel = sugarLevel;
        this.symptoms = symptoms;
        this.urgencyLevel = urgencyLevel;
        this.recordedAt = recordedAt;
    }

    // Getters & Setters
    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDiseaseType() {
        return diseaseType;
    }

    public void setDiseaseType(String diseaseType) {
        this.diseaseType = diseaseType;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getOxygenLevel() {
        return oxygenLevel;
    }

    public void setOxygenLevel(int oxygenLevel) {
        this.oxygenLevel = oxygenLevel;
    }

    public int getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(int systolicBp) {
        this.systolicBp = systolicBp;
    }

    public int getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(int diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getSugarLevel() {
        return sugarLevel;
    }

    public void setSugarLevel(int sugarLevel) {
        this.sugarLevel = sugarLevel;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public Timestamp getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Timestamp recordedAt) {
        this.recordedAt = recordedAt;
    }

    // Helper to get formatted Blood Pressure: "systolic/diastolic"
    public String getBp() {
        return systolicBp + "/" + diastolicBp;
    }

    // Helper to get formatted date string
    public String getFormattedDate() {
        if (recordedAt == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(recordedAt);
    }
}
