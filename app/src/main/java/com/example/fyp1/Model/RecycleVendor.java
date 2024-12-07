package com.example.fyp1.Model;

public class RecycleVendor {
    private String userId;
    private String fullName;
    private String registrationNumber;
    private String email;
    private String approveStatus;

    public RecycleVendor() {
        // Default constructor required for Firebase
    }

    public RecycleVendor(String userId, String fullName, String registrationNumber, String email, String approveStatus) {
        this.userId = userId;
        this.fullName = fullName;
        this.registrationNumber = registrationNumber;
        this.email = email;
        this.approveStatus = approveStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApproveStatus() {
        return approveStatus;
    }

    public void setApproveStatus(String approveStatus) {
        this.approveStatus = approveStatus;
    }
}
