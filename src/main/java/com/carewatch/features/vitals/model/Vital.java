package com.carewatch.features.vitals.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

// Model representing patient health vitals entry
public class Vital {
    private int vitalId;
    private int patientId;
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
    public Vital() {}

    public Vital(int vitalId, int patientId, int heartRate, int oxygenLevel, int systolicBp, int diastolicBp,
                 double temperature, int sugarLevel, String symptoms, String urgencyLevel, Timestamp recordedAt) {
        this.vitalId = vitalId;
        this.patientId = patientId;
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
    public int getVitalId() {
        return vitalId;
    }

    public void setVitalId(int vitalId) {
        this.vitalId = vitalId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
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

    // Helper for TableView BP column mapping: returns "systolic/diastolic"
    public String getBp() {
        return systolicBp + "/" + diastolicBp;
    }

    // Helper for TableView Date column mapping: returns formatted string
    public String getFormattedDate() {
        if (recordedAt == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(recordedAt);
    }
}
