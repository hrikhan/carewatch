package com.carewatch.features.patient.model;

import java.sql.Timestamp;

// Patient profile details model
public class Patient {
    private int patientId;
    private int userId;
    private int age;
    private String gender;
    private String phone;
    private String address;
    private String diseaseType;
    private String riskLevel;
    private int assignedDoctorId;
    private String emergencyContact;
    private Timestamp createdAt;

    // Helper fields for UI TableView columns
    private String patientName;
    private String patientEmail;
    private String assignedDoctorName;

    // Constructors
    public Patient() {}

    public Patient(int patientId, int userId, int age, String gender, String phone, String address, 
                   String diseaseType, String riskLevel, int assignedDoctorId, String emergencyContact, Timestamp createdAt) {
        this.patientId = patientId;
        this.userId = userId;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.diseaseType = diseaseType;
        this.riskLevel = riskLevel;
        this.assignedDoctorId = assignedDoctorId;
        this.emergencyContact = emergencyContact;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDiseaseType() {
        return diseaseType;
    }

    public void setDiseaseType(String diseaseType) {
        this.diseaseType = diseaseType;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getAssignedDoctorId() {
        return assignedDoctorId;
    }

    public void setAssignedDoctorId(int assignedDoctorId) {
        this.assignedDoctorId = assignedDoctorId;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
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

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getAssignedDoctorName() {
        return assignedDoctorName;
    }

    public void setAssignedDoctorName(String assignedDoctorName) {
        this.assignedDoctorName = assignedDoctorName;
    }
}
