package com.example.donationapp.model;

import java.util.HashMap;
import java.util.Map;

/**
 * User model class representing a user in the donation app
 */
public class User {
    private String id;
    private String name;
    private String email;
    private String role; // "user" or "admin"
    private String profileImage;
    private String phone;

    // Default constructor required for Firestore
    public User() {
    }

    public User(String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role != null ? role : "user"; // Default to "user"
        this.profileImage = "";
        this.phone = "";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Convert User object to Map for Firestore
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("email", email);
        map.put("role", role);
        map.put("profileImage", profileImage != null ? profileImage : "");
        map.put("phone", phone != null ? phone : "");
        return map;
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}

