package com.example.donationapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.donationapp.model.User;
import com.example.donationapp.util.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel for authentication operations
 * Handles login, signup, logout, and user role checking
 */
public class AuthViewModel extends AndroidViewModel {
    private static final String TAG = "AuthViewModel";
    private FirebaseHelper firebaseHelper;
    
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isAuthenticated = new MutableLiveData<>();
    private MutableLiveData<Boolean> resetPasswordSuccess = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        firebaseHelper = FirebaseHelper.getInstance();
        checkAuthState();
    }

    /**
     * Check current authentication state
     */
    private void checkAuthState() {
        FirebaseUser firebaseUser = firebaseHelper.getCurrentUser();
        if (firebaseUser != null) {
            loadUserData(firebaseUser.getUid());
            isAuthenticated.setValue(true);
        } else {
            isAuthenticated.setValue(false);
        }
    }

    /**
     * Load user data from Firestore
     */
    private void loadUserData(String userId) {
        isLoading.setValue(true);
        firebaseHelper.getUser(userId,
                user -> {
                    currentUser.setValue(user);
                    isLoading.setValue(false);
                },
                exception -> {
                    Log.e(TAG, "Error loading user data", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Sign in with email and password
     */
    public void signIn(String email, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        firebaseHelper.signIn(email, password,
                aVoid -> {
                    FirebaseUser firebaseUser = firebaseHelper.getCurrentUser();
                    if (firebaseUser != null) {
                        loadUserData(firebaseUser.getUid());
                    }
                },
                exception -> {
                    Log.e(TAG, "Sign in failed", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Sign up with email, password, and name
     */
    public void signUp(String email, String password, String name) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        firebaseHelper.signUp(email, password, name,
                aVoid -> {
                    FirebaseUser firebaseUser = firebaseHelper.getCurrentUser();
                    if (firebaseUser != null) {
                        loadUserData(firebaseUser.getUid());
                    }
                },
                exception -> {
                    Log.e(TAG, "Sign up failed", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Send password reset email
     */
    public void resetPassword(String email) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        resetPasswordSuccess.setValue(false);

        firebaseHelper.sendPasswordResetEmail(email,
                aVoid -> {
                    isLoading.setValue(false);
                    resetPasswordSuccess.setValue(true);
                },
                exception -> {
                    Log.e(TAG, "Reset password failed", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                    resetPasswordSuccess.setValue(false);
                });
    }

    /**
     * Sign out current user
     */
    public void signOut() {
        firebaseHelper.signOut();
        currentUser.setValue(null);
        isAuthenticated.setValue(false);
    }

    /**
     * Refresh current user data
     */
    public void refreshUserData() {
        FirebaseUser firebaseUser = firebaseHelper.getCurrentUser();
        if (firebaseUser != null) {
            loadUserData(firebaseUser.getUid());
        }
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsAuthenticated() {
        return isAuthenticated;
    }

    public LiveData<Boolean> getResetPasswordSuccess() {
        return resetPasswordSuccess;
    }

    /**
     * Get current Firebase user
     */
    public FirebaseUser getFirebaseUser() {
        return firebaseHelper.getCurrentUser();
    }
}

