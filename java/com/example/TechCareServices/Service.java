package com.example.TechCareServices;

public class Service {
    private String name, category, fee, estimateTime, description, imageUrl;
    private String serviceId; // Added to store the Firestore Document ID

    public Service() {} // Required for Firestore

    public Service(String name, String category, String fee, String estimateTime, String description, String imageUrl) {
        this.name = name;
        this.category = category;
        this.fee = fee;
        this.estimateTime = estimateTime;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getFee() { return fee; }
    public String getEstimateTime() { return estimateTime; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }

    // Crucial: Getter and Setter for the Service ID
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}