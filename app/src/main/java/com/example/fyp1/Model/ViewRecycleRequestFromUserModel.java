package com.example.fyp1.Model;

public class ViewRecycleRequestFromUserModel {
    private String requestID;
    private String id;
    private String ItemType;
    private String PickUpDate;
    private int quantity;
    private String requestAddress;
    private String vendorLocationId;
    private String imageUri;
    private String requestStatus;
    private String userEmail;
    private String contactNumber;

    // Default constructor required for Firebase
    public ViewRecycleRequestFromUserModel() {
        // Default constructor
    }

    // Getter and setter for requestId
    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    // Getter and setter for id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and setter for itemType
    public String getItemType() {
        return ItemType;
    }

    public void setItemType(String itemType) {
        this.ItemType = itemType;
    }

    // Getter and setter for pickUpDate
    public String getPickUpDate() {
        return PickUpDate;
    }

    public void setPickUpDate(String pickUpDate) {
        this.PickUpDate = pickUpDate;
    }

    // Getter and setter for quantity
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Getter and setter for requestAddress
    public String getRequestAddress() {
        return requestAddress;
    }

    public void setRequestAddress(String requestAddress) {
        this.requestAddress = requestAddress;
    }

    // Getter and setter for vendorLocationId
    public String getVendorLocationId() {
        return vendorLocationId;
    }

    public void setVendorLocationId(String vendorLocationId) {
        this.vendorLocationId = vendorLocationId;
    }

    // Getter and setter for imageUri
    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    // Getter and setter for requestStatus
    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    // Getter and setter for userEmail
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // Getter and setter for contactNumber
    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
