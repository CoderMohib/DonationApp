package com.example.donationapp.util;

import android.util.Log;

import com.example.donationapp.model.Campaign;
import com.example.donationapp.model.Donation;
import com.example.donationapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized Firebase operations helper class
 * Handles authentication, Firestore CRUD operations, and Storage operations
 */
public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static FirebaseHelper instance;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    // ==================== Authentication Methods ====================

    /**
     * Get current Firebase user
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Sign in with email and password
     */
    public void signIn(String email, String password,
                       OnSuccessListener<Void> onSuccess,
                       OnFailureListener onFailure) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Sign in successful");
                        if (onSuccess != null) {
                            onSuccess.onSuccess(null);
                        }
                    } else {
                        String errorMessage = getAuthErrorMessage(task.getException());
                        Log.e(TAG, "Sign in failed: " + errorMessage);
                        if (onFailure != null) {
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Sign up with email and password
     */
    public void signUp(String email, String password, String name,
                      OnSuccessListener<Void> onSuccess,
                      OnFailureListener onFailure) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Create user document in Firestore
                            createUserDocument(user.getUid(), name, email, "user", onSuccess, onFailure);
                        }
                    } else {
                        String errorMessage = getAuthErrorMessage(task.getException());
                        Log.e(TAG, "Sign up failed: " + errorMessage);
                        if (onFailure != null) {
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Sign out current user
     */
    public void signOut() {
        auth.signOut();
        Log.d(TAG, "User signed out");
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email,
                                     OnSuccessListener<Void> onSuccess,
                                     OnFailureListener onFailure) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Password reset email sent");
                        if (onSuccess != null) {
                            onSuccess.onSuccess(null);
                        }
                    } else {
                        String errorMessage = getAuthErrorMessage(task.getException());
                        Log.e(TAG, "Failed to send reset email: " + errorMessage);
                        if (onFailure != null) {
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    // ==================== User Document Methods ====================

    /**
     * Create user document in Firestore
     */
    private void createUserDocument(String userId, String name, String email, String role,
                                    OnSuccessListener<Void> onSuccess,
                                    OnFailureListener onFailure) {
        User user = new User(userId, name, email, role);
        firestore.collection("users")
                .document(userId)
                .set(user.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document created");
                    if (onSuccess != null) {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user document", e);
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                });
    }

    /**
     * Get user document from Firestore
     */
    public void getUser(String userId, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                user.setId(doc.getId());
                                if (onSuccess != null) {
                                    onSuccess.onSuccess(user);
                                }
                            }
                        } else {
                            if (onFailure != null) {
                                onFailure.onFailure(new Exception("User not found"));
                            }
                        }
                    } else {
                        Log.e(TAG, "Error getting user", task.getException());
                        if (onFailure != null) {
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Update user document
     */
    public void updateUser(String userId, Map<String, Object> updates,
                          OnSuccessListener<Void> onSuccess,
                          OnFailureListener onFailure) {
        firestore.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated successfully");
                    if (onSuccess != null) {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user", e);
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                });
    }

    // ==================== Campaign Methods ====================

    /**
     * Create a new campaign
     */
    public void createCampaign(Campaign campaign,
                              OnSuccessListener<String> onSuccess,
                              OnFailureListener onFailure) {
        firestore.collection("campaigns")
                .add(campaign.toMap())
                .addOnSuccessListener(documentReference -> {
                    String campaignId = documentReference.getId();
                    // Update campaign with its ID
                    documentReference.update("id", campaignId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Campaign created with ID: " + campaignId);
                                if (onSuccess != null) {
                                    onSuccess.onSuccess(campaignId);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating campaign", e);
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                });
    }

    /**
     * Get all campaigns
     */
    public void getAllCampaigns(OnSuccessListener<QuerySnapshot> onSuccess,
                               OnFailureListener onFailure) {
        firestore.collection("campaigns")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (onSuccess != null) {
                        onSuccess.onSuccess(querySnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting campaigns", e);
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                });
    }

    /**
     * Get campaign by ID
     */
    public void getCampaign(String campaignId,
                           OnSuccessListener<Campaign> onSuccess,
                           OnFailureListener onFailure) {
        firestore.collection("campaigns")
                .document(campaignId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            Campaign campaign = doc.toObject(Campaign.class);
                            if (campaign != null) {
                                campaign.setId(doc.getId());
                                if (onSuccess != null) {
                                    onSuccess.onSuccess(campaign);
                                }
                            }
                        } else {
                            if (onFailure != null) {
                                onFailure.onFailure(new Exception("Campaign not found"));
                            }
                        }
                    } else {
                        Log.e(TAG, "Error getting campaign", task.getException());
                        if (onFailure != null) {
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Update campaign
     */
    public void updateCampaign(String campaignId, Map<String, Object> updates,
                              OnSuccessListener<Void> onSuccess,
                              OnFailureListener onFailure) {
        firestore.collection("campaigns")
                .document(campaignId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Campaign updated successfully");
                    if (onSuccess != null) {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating campaign", e);
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                });
    }

    /**
     * Delete campaign
     */
    public void deleteCampaign(String campaignId,
                              OnSuccessListener<Void> onSuccess,
                              OnFailureListener onFailure) {
        firestore.collection("campaigns")
                .document(campaignId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Campaign deleted successfully");
                    if (onSuccess != null) {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting campaign", e);
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                });
    }

    // ==================== Donation Methods ====================

    /**
     * Create donation and update campaign collected amount atomically using transaction
     */
    public void createDonation(String campaignId, double amount, String userId,
                              OnSuccessListener<Void> onSuccess,
                              OnFailureListener onFailure) {
        DocumentReference campaignRef = firestore.collection("campaigns").document(campaignId);
        DocumentReference donationRef = firestore.collection("donations").document();

        firestore.runTransaction((Transaction.Function<Void>) transaction -> {
            // Get current campaign data
            DocumentSnapshot campaignDoc = transaction.get(campaignRef);
            if (!campaignDoc.exists()) {
                throw new RuntimeException("Campaign not found");
            }

            Double currentCollected = campaignDoc.getDouble("collectedAmount");
            if (currentCollected == null) {
                currentCollected = 0.0;
            }

            // Update campaign collected amount
            transaction.update(campaignRef, "collectedAmount", currentCollected + amount);

            // Create donation document
            Donation donation = new Donation(donationRef.getId(), campaignId, userId, amount);
            transaction.set(donationRef, donation.toMap());

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Donation created successfully");
            if (onSuccess != null) {
                onSuccess.onSuccess(null);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error creating donation", e);
            if (onFailure != null) {
                onFailure.onFailure(e);
            }
        });
    }

    /**
     * Get donations by user ID
     */
    public void getUserDonations(String userId,
                                OnSuccessListener<QuerySnapshot> onSuccess,
                                OnFailureListener onFailure) {
        firestore.collection("donations")
                .whereEqualTo("userId", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (onSuccess != null) {
                        onSuccess.onSuccess(querySnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting donations", e);
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                });
    }

    // ==================== Storage Methods ====================

    /**
     * Upload image to Firebase Storage
     */
    public void uploadImage(byte[] imageBytes, String path,
                          OnSuccessListener<String> onSuccess,
                          OnFailureListener onFailure,
                          OnSuccessListener<UploadTask.TaskSnapshot> onProgress) {
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(path);

        UploadTask uploadTask = imageRef.putBytes(imageBytes);
        
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                Log.d(TAG, "Image uploaded: " + downloadUrl);
                if (onSuccess != null) {
                    onSuccess.onSuccess(downloadUrl);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error getting download URL", e);
                if (onFailure != null) {
                    onFailure.onFailure(e);
                }
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error uploading image", e);
            if (onFailure != null) {
                onFailure.onFailure(e);
            }
        });

        // Progress listener
        if (onProgress != null) {
            uploadTask.addOnProgressListener(taskSnapshot -> {
                if (onProgress != null) {
                    onProgress.onSuccess(taskSnapshot);
                }
            });
        }
    }

    /**
     * Delete image from Firebase Storage
     */
    public void deleteImage(String imageUrl,
                           OnSuccessListener<Void> onSuccess,
                           OnFailureListener onFailure) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            if (onSuccess != null) {
                onSuccess.onSuccess(null);
            }
            return;
        }

        StorageReference storageRef = storage.getReferenceFromUrl(imageUrl);
        storageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Image deleted successfully");
                    if (onSuccess != null) {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting image", e);
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                });
    }

    // ==================== Error Message Helpers ====================

    /**
     * Get user-friendly error message from Firebase Auth exception
     */
    private String getAuthErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown error occurred";
        }
        
        String errorMessage = exception.getMessage();
        if (errorMessage == null) {
            return "Unknown error occurred";
        }

        String lowerMessage = errorMessage.toLowerCase();
        
        if (lowerMessage.contains("email address is badly formatted") || 
            lowerMessage.contains("invalid-email")) {
            return "Invalid email address";
        } else if (lowerMessage.contains("password should be at least") || 
                   lowerMessage.contains("weak-password")) {
            return "Password must be at least 6 characters";
        } else if (lowerMessage.contains("no user record") || 
                   lowerMessage.contains("user-not-found")) {
            return "No account found with this email";
        } else if (lowerMessage.contains("wrong password") || 
                   lowerMessage.contains("wrong-password")) {
            return "Incorrect password";
        } else if (lowerMessage.contains("email address is already in use") || 
                   lowerMessage.contains("email-already-in-use")) {
            return "Email already registered";
        } else if (lowerMessage.contains("network") || 
                   lowerMessage.contains("network-error")) {
            return "Network error. Please check your internet connection";
        } else if (lowerMessage.contains("too-many-requests")) {
            return "Too many attempts. Please try again later";
        } else if (lowerMessage.contains("user-disabled")) {
            return "This account has been disabled";
        }
        
        return "Authentication failed. Please try again";
    }

    /**
     * Get Firestore instance (for real-time listeners)
     */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    /**
     * Get user-friendly error message from Firestore exception
     */
    public String getFirestoreErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown error occurred";
        }
        
        String errorMessage = exception.getMessage();
        if (errorMessage == null) {
            return "Unknown error occurred";
        }

        String lowerMessage = errorMessage.toLowerCase();
        
        if (lowerMessage.contains("network") || 
            lowerMessage.contains("unavailable")) {
            return "Network error. Please check your internet connection";
        } else if (lowerMessage.contains("permission") || 
                   lowerMessage.contains("permission-denied")) {
            // Check if it's related to campaign update (donation issue)
            if (lowerMessage.contains("campaign") || lowerMessage.contains("update")) {
                return "Permission denied. Firestore security rules need to allow updating campaign collectedAmount. Please update your Firestore security rules to allow authenticated users to update the collectedAmount field of campaigns.";
            }
            return "Permission denied. You don't have access to this resource";
        } else if (lowerMessage.contains("not found") || 
                   lowerMessage.contains("not-found")) {
            return "Resource not found";
        } else if (lowerMessage.contains("deadline-exceeded") || 
                   lowerMessage.contains("timeout")) {
            return "Request timed out. Please try again";
        } else if (lowerMessage.contains("already-exists")) {
            return "This resource already exists";
        } else if (lowerMessage.contains("failed-precondition")) {
            return "Operation failed. Please check your data";
        } else if (lowerMessage.contains("out-of-range")) {
            return "Invalid data provided";
        } else if (lowerMessage.contains("unauthenticated")) {
            return "Please sign in to continue";
        }
        
        return "An error occurred. Please try again";
    }
}

