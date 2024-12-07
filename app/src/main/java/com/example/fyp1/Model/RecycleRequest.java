package com.example.fyp1.Model;

public class RecycleRequest {
    private String id;
    private String date;
    private String itemName;
    private String imageUri;
    private String requestStatus;
    private String quantity;
    private String requestAddress;

    public RecycleRequest(String id, String date, String itemName, String imageUri, String requestStatus, String quantity, String requestAddress) {
        this.id = id;
        this.date = date;
        this.itemName = itemName;
        this.imageUri = imageUri;
        this.requestStatus = requestStatus;
        this.quantity = quantity;
        this.requestAddress = requestAddress;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getItemName() {
        return itemName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getRequestAddress() {
        return requestAddress;
    }
}
