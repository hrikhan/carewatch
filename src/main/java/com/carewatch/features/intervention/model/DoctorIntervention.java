package com.carewatch.features.intervention.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

// Model representing a recorded clinical action / doctor intervention
public class DoctorIntervention {
    private int interventionId;
    private int patientId;
    private int doctorUserId;
    private String actionTaken;
    private String notes;
    private Timestamp createdAt;

    // Joined fields for UI mapping
    private String patientName;
    private String doctorName;

    // Constructors
    public DoctorIntervention() {}

    public DoctorIntervention(int interventionId, int patientId, int doctorUserId, String actionTaken, String notes, Timestamp createdAt) {
        this.interventionId = interventionId;
        this.patientId = patientId;
        this.doctorUserId = doctorUserId;
        this.actionTaken = actionTaken;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getInterventionId() {
        return interventionId;
    }

    public void setInterventionId(int interventionId) {
        this.interventionId = interventionId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorUserId() {
        return doctorUserId;
    }

    public void setDoctorUserId(int doctorUserId) {
        this.doctorUserId = doctorUserId;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    // Helper for table date column binding
    public String getFormattedDate() {
        if (createdAt == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(createdAt);
    }
}
