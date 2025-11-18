package com.example.donationapp.util;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Input validation utility class
 * Provides methods for validating email, password, amounts, and sanitizing inputs
 */
public class Validator {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final double MIN_DONATION_AMOUNT = 0.01;
    private static final double MAX_DONATION_AMOUNT = 1000000.0;
    private static final double MIN_CAMPAIGN_GOAL = 1.0;

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Get email validation error message
     */
    public static String getEmailError(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email is required";
        }
        if (!isValidEmail(email)) {
            return "Invalid email address";
        }
        return null;
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Get password validation error message
     */
    public static String getPasswordError(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters";
        }
        return null;
    }

    /**
     * Validate password confirmation matches password
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Get password confirmation error message
     */
    public static String getPasswordConfirmError(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            return "Please confirm your password";
        }
        if (!doPasswordsMatch(password, confirmPassword)) {
            return "Passwords do not match";
        }
        return null;
    }

    /**
     * Validate name (non-empty, reasonable length)
     */
    public static boolean isValidName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 100;
    }

    /**
     * Get name validation error message
     */
    public static String getNameError(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Name is required";
        }
        String trimmed = name.trim();
        if (trimmed.length() < 2) {
            return "Name must be at least 2 characters";
        }
        if (trimmed.length() > 100) {
            return "Name is too long";
        }
        return null;
    }

    /**
     * Validate donation amount
     */
    public static boolean isValidDonationAmount(double amount) {
        return amount >= MIN_DONATION_AMOUNT && amount <= MAX_DONATION_AMOUNT;
    }

    /**
     * Validate donation amount string
     */
    public static boolean isValidDonationAmount(String amountStr) {
        if (TextUtils.isEmpty(amountStr)) {
            return false;
        }
        try {
            double amount = Double.parseDouble(amountStr);
            return isValidDonationAmount(amount);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Get donation amount validation error message
     */
    public static String getDonationAmountError(String amountStr) {
        if (TextUtils.isEmpty(amountStr)) {
            return "Amount is required";
        }
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < MIN_DONATION_AMOUNT) {
                return "Minimum donation amount is $" + String.format("%.2f", MIN_DONATION_AMOUNT);
            }
            if (amount > MAX_DONATION_AMOUNT) {
                return "Maximum donation amount is $" + String.format("%.2f", MAX_DONATION_AMOUNT);
            }
            return null;
        } catch (NumberFormatException e) {
            return "Invalid amount format";
        }
    }

    /**
     * Validate campaign goal amount
     */
    public static boolean isValidCampaignGoal(double goal) {
        return goal >= MIN_CAMPAIGN_GOAL && goal <= MAX_DONATION_AMOUNT;
    }

    /**
     * Validate campaign goal amount string
     */
    public static boolean isValidCampaignGoal(String goalStr) {
        if (TextUtils.isEmpty(goalStr)) {
            return false;
        }
        try {
            double goal = Double.parseDouble(goalStr);
            return isValidCampaignGoal(goal);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Get campaign goal validation error message
     */
    public static String getCampaignGoalError(String goalStr) {
        if (TextUtils.isEmpty(goalStr)) {
            return "Goal amount is required";
        }
        try {
            double goal = Double.parseDouble(goalStr);
            if (goal < MIN_CAMPAIGN_GOAL) {
                return "Minimum goal amount is $" + String.format("%.2f", MIN_CAMPAIGN_GOAL);
            }
            if (goal > MAX_DONATION_AMOUNT) {
                return "Maximum goal amount is $" + String.format("%.2f", MAX_DONATION_AMOUNT);
            }
            return null;
        } catch (NumberFormatException e) {
            return "Invalid amount format";
        }
    }

    /**
     * Validate campaign title
     */
    public static boolean isValidCampaignTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return false;
        }
        String trimmed = title.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 200;
    }

    /**
     * Get campaign title validation error message
     */
    public static String getCampaignTitleError(String title) {
        if (TextUtils.isEmpty(title)) {
            return "Title is required";
        }
        String trimmed = title.trim();
        if (trimmed.length() < 3) {
            return "Title must be at least 3 characters";
        }
        if (trimmed.length() > 200) {
            return "Title is too long";
        }
        return null;
    }

    /**
     * Validate campaign description
     */
    public static boolean isValidCampaignDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            return false;
        }
        String trimmed = description.trim();
        return trimmed.length() >= 10 && trimmed.length() <= 5000;
    }

    /**
     * Get campaign description validation error message
     */
    public static String getCampaignDescriptionError(String description) {
        if (TextUtils.isEmpty(description)) {
            return "Description is required";
        }
        String trimmed = description.trim();
        if (trimmed.length() < 10) {
            return "Description must be at least 10 characters";
        }
        if (trimmed.length() > 5000) {
            return "Description is too long";
        }
        return null;
    }

    /**
     * Sanitize string input to prevent XSS and injection attacks
     */
    public static String sanitizeInput(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        // Remove potentially dangerous characters
        return input.trim()
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * Sanitize string but preserve line breaks for descriptions
     */
    public static String sanitizeDescription(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        // Replace line breaks with newline character, then sanitize
        String sanitized = input.replace("\r\n", "\n").replace("\r", "\n");
        return sanitizeInput(sanitized);
    }

    /**
     * Validate phone number (optional, basic format check)
     */
    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return true; // Phone is optional
        }
        // Basic phone validation - digits, spaces, dashes, parentheses, plus sign
        Pattern phonePattern = Pattern.compile("^[+]?[(]?[0-9]{1,4}[)]?[-\\s]?[0-9]{1,4}[-\\s]?[0-9]{1,9}$");
        return phonePattern.matcher(phone.trim()).matches();
    }

    /**
     * Get phone validation error message
     */
    public static String getPhoneError(String phone) {
        if (!TextUtils.isEmpty(phone) && !isValidPhone(phone)) {
            return "Invalid phone number format";
        }
        return null;
    }
}

