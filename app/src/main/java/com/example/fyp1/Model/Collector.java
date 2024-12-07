package com.example.fyp1.Model;

public class Collector {
    private String companyName;
    private String imageUrl;
    private String locationID;
    private String province;
    private String state;

    // Default constructor required for Firebase
    public Collector() {}

    public Collector(String companyName, String imageUrl, String locationID, String province, String state) {
        this.companyName = companyName;
        this.imageUrl = imageUrl;
        this.locationID = locationID;
        this.province = province;
        this.state = state;
    }

    // Getters and setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLocationID() { return locationID; }
    public void setLocationID(String locationID) { this.locationID = locationID; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}