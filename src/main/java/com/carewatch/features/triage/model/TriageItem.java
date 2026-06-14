package com.carewatch.features.triage.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

// Model class representing an item in the triage queue
public class TriageItem {
    private int queueId;
    private int patientId;
    private int vitalId;
    private String urgencyLevel;
    private int priorityScore;
    private String alertMessage;
    private String status;
    private Timestamp createdAt;

    // Joined patient demographic fields for UI mapping
    private String patientName;
    private String email;
    private String phone;
    private String diseaseType;

    // Constructors
    public TriageItem() {}

    public TriageItem(int queueId, int patientId, int vitalId, String urgencyLevel, int priorityScore,
                      String alertMessage, String status, Timestamp createdAt) {
        this.queueId = queueId;
        this.patientId = patientId;
        this.vitalId = vitalId;
        this.urgencyLevel = urgencyLevel;
        this.priorityScore = priorityScore;
        this.alertMessage = alertMessage;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getVitalId() {
        return vitalId;
    }

    public void setVitalId(int vitalId) {
        this.vitalId = vitalId;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(int priorityScore) {
        this.priorityScore = priorityScore;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    // Helper to get formatted creation date
    public String getFormattedDate() {
        if (createdAt == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(createdAt);
    }
}
