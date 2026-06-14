package com.carewatch.features.report.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

// Model representing a unified row in patient clinical reports
public class ReportRow {
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
    private String urgencyLevel;
    private String triageStatus;
    private String lastIntervention;
    private Timestamp recordedAt;

    // Constructors
    public ReportRow() {}

    public ReportRow(String patientName, String email, String phone, String diseaseType, int heartRate,
                     int oxygenLevel, int systolicBp, int diastolicBp, double temperature, int sugarLevel,
                     String urgencyLevel, String triageStatus, String lastIntervention, Timestamp recordedAt) {
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
        this.urgencyLevel = urgencyLevel;
        this.triageStatus = triageStatus;
        this.lastIntervention = lastIntervention;
        this.recordedAt = recordedAt;
    }

    // Getters & Setters
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

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getTriageStatus() {
        return triageStatus;
    }

    public void setTriageStatus(String triageStatus) {
        this.triageStatus = triageStatus;
    }

    public String getLastIntervention() {
        return lastIntervention;
    }

    public void setLastIntervention(String lastIntervention) {
        this.lastIntervention = lastIntervention;
    }

    public Timestamp getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Timestamp recordedAt) {
        this.recordedAt = recordedAt;
    }

    // Helper for BP column cell binding
    public String getBp() {
        return systolicBp + "/" + diastolicBp;
    }

    // Helper for Date column cell binding
    public String getFormattedDate() {
        if (recordedAt == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(recordedAt);
    }
}
