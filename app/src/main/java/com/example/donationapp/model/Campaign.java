package com.example.donationapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * Campaign model class representing a donation campaign
 */
public class Campaign {
    private String id;
    private String title;
    private String description;
    private double goalAmount;
    private double collectedAmount;
    private String imageUrl;
    @ServerTimestamp
    private Timestamp createdAt;
    private String createdBy;

    // Default constructor required for Firestore
    public Campaign() {
        this.collectedAmount = 0.0;
    }

    public Campaign(String id, String title, String description, double goalAmount, String createdBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.goalAmount = goalAmount;
        this.collectedAmount = 0.0;
        this.imageUrl = "";
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getGoalAmount() {
        return goalAmount;
    }

    public void setGoalAmount(double goalAmount) {
        this.goalAmount = goalAmount;
    }

    public double getCollectedAmount() {
        return collectedAmount;
    }

    public void setCollectedAmount(double collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Calculate progress percentage
     */
    public int getProgressPercentage() {
        if (goalAmount <= 0) return 0;
        double percentage = (collectedAmount / goalAmount) * 100;
        return (int) Math.min(percentage, 100);
    }

    /**
     * Check if campaign goal is reached
     */
    public boolean isGoalReached() {
        return collectedAmount >= goalAmount;
    }

    /**
     * Convert Campaign object to Map for Firestore
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("description", description);
        map.put("goalAmount", goalAmount);
        map.put("collectedAmount", collectedAmount);
        map.put("imageUrl", imageUrl != null ? imageUrl : "");
        map.put("createdAt", createdAt);
        map.put("createdBy", createdBy);
        return map;
    }
}

