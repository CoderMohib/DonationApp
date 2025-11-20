package com.example.donationapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.donationapp.model.User;
import com.example.donationapp.util.FirebaseHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel for profile operations
 * Handles user profile updates and data loading
 */
public class ProfileViewModel extends AndroidViewModel {
    private static final String TAG = "ProfileViewModel";
    private FirebaseHelper firebaseHelper;
    
    private MutableLiveData<User> userProfile = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        firebaseHelper = FirebaseHelper.getInstance();
    }

    /**
     * Load user profile
     */
    public void loadUserProfile(String userId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        firebaseHelper.getUser(userId,
                user -> {
                    userProfile.setValue(user);
                    isLoading.setValue(false);
                },
                exception -> {
                    Log.e(TAG, "Error loading user profile", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Update user name
     */
    public void updateUserName(String userId, String name) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        updateSuccess.setValue(false);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        
        firebaseHelper.updateUser(userId, updates,
                aVoid -> {
                    Log.d(TAG, "User name updated");
                    loadUserProfile(userId); // Reload profile
                    updateSuccess.setValue(true);
                    isLoading.setValue(false);
                },
                exception -> {
                    Log.e(TAG, "Error updating user name", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Update user phone
     */
    public void updateUserPhone(String userId, String phone) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        updateSuccess.setValue(false);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("phone", phone != null ? phone : "");
        
        firebaseHelper.updateUser(userId, updates,
                aVoid -> {
                    Log.d(TAG, "User phone updated");
                    loadUserProfile(userId); // Reload profile
                    updateSuccess.setValue(true);
                    isLoading.setValue(false);
                },
                exception -> {
                    Log.e(TAG, "Error updating user phone", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Update user profile image URL
     */
    public void updateProfileImage(String userId, String imageUrl) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        updateSuccess.setValue(false);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImage", imageUrl != null ? imageUrl : "");
        
        firebaseHelper.updateUser(userId, updates,
                aVoid -> {
                    Log.d(TAG, "Profile image updated");
                    loadUserProfile(userId); // Reload profile
                    updateSuccess.setValue(true);
                    isLoading.setValue(false);
                },
                exception -> {
                    Log.e(TAG, "Error updating profile image", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Update multiple user fields at once
     */
    public void updateUserProfile(String userId, Map<String, Object> updates) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        updateSuccess.setValue(false);
        
        firebaseHelper.updateUser(userId, updates,
                aVoid -> {
                    Log.d(TAG, "User profile updated");
                    loadUserProfile(userId); // Reload profile
                    updateSuccess.setValue(true);
                    isLoading.setValue(false);
                },
                exception -> {
                    Log.e(TAG, "Error updating user profile", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    // Getters for LiveData
    public LiveData<User> getUserProfile() {
        return userProfile;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    /**
     * Reset update success flag
     */
    public void resetUpdateSuccess() {
        updateSuccess.setValue(false);
    }
}

