package com.example.fyp1.Model;

import java.util.Date;

public class ManageUserModel {

    private String userId;
    private String fullName;
    private String email;
    private String role;
    private String imageUrl;
    private String dateAdded;
    private String lastSignInTime;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public ManageUserModel() {
    }

    public ManageUserModel(String userId, String fullName, String email, String role, String imageUrl, String dateAdded, String lastSignInTime) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.imageUrl = imageUrl;
        this.dateAdded = dateAdded;
        this.lastSignInTime = lastSignInTime;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getLastSignInTime() {
        return lastSignInTime;
    }

    public void setLastSignInTime(String lastSignInTime) {
        this.lastSignInTime = lastSignInTime;
    }
}