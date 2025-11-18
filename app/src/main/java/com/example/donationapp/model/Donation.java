package com.example.donationapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * Donation model class representing a donation made by a user
 */
public class Donation {
    private String id;
    private String campaignId;
    private String userId;
    private double amount;
    @ServerTimestamp
    private Timestamp date;

    // Default constructor required for Firestore
    public Donation() {
    }

    public Donation(String id, String campaignId, String userId, double amount) {
        this.id = id;
        this.campaignId = campaignId;
        this.userId = userId;
        this.amount = amount;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    /**
     * Convert Donation object to Map for Firestore
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("campaignId", campaignId);
        map.put("userId", userId);
        map.put("amount", amount);
        map.put("date", date);
        return map;
    }
}

