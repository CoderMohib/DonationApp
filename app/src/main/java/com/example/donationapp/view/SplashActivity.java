package com.example.donationapp.view;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.donationapp.R;
import com.example.donationapp.model.User;
import com.example.donationapp.util.FirebaseHelper;
import com.example.donationapp.viewmodel.AuthViewModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseUser;

/**
 * Splash Activity - Checks authentication state and redirects accordingly
 */
public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private AuthViewModel authViewModel;
    private CircularProgressIndicator progressBar;
    private ObjectAnimator progressAnimator;
    private boolean hasRedirected = false; // Flag to prevent multiple redirects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize progress bar
        progressBar = findViewById(R.id.progress_bar);
        
        // Start progress animation
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
            progressAnimator.setDuration(SPLASH_DELAY);
            progressAnimator.start();
        }

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Check auth state first
        FirebaseUser currentUser = FirebaseHelper.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        } else {
            // User is authenticated, observe for user data to load
            authViewModel.getCurrentUser().observe(this, user -> {
                if (user != null && !hasRedirected) {
                    redirectBasedOnRole(user);
                }
            });
            
            // Also observe authentication state as backup
            authViewModel.getIsAuthenticated().observe(this, isAuthenticated -> {
                if (isAuthenticated != null && !isAuthenticated && !hasRedirected) {
                    redirectToLogin();
                }
            });
            
            // Wait a bit for user data to load, then check role
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!hasRedirected) {
                    checkUserRoleAndRedirect();
                }
            }, SPLASH_DELAY);
        }
    }

    private void checkUserRoleAndRedirect() {
        if (hasRedirected) {
            return; // Already redirected, don't do it again
        }
        
        User currentUser = authViewModel.getCurrentUser().getValue();
        if (currentUser != null) {
            redirectBasedOnRole(currentUser);
        } else {
            // User data not loaded yet, wait a bit more or redirect to login
            // The observer will handle it when data loads
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!hasRedirected) {
                    User user = authViewModel.getCurrentUser().getValue();
                    if (user != null) {
                        redirectBasedOnRole(user);
                    } else {
                        redirectToLogin();
                    }
                }
            }, 1000);
        }
    }

    private void redirectBasedOnRole(User user) {
        if (hasRedirected) {
            return; // Already redirected
        }
        if (user.isAdmin()) {
            redirectToAdminDashboard();
        } else {
            redirectToUserDashboard();
        }
    }

    private void redirectToLogin() {
        if (hasRedirected) {
            return; // Already redirected
        }
        hasRedirected = true;
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToUserDashboard() {
        if (hasRedirected) {
            return; // Already redirected
        }
        hasRedirected = true;
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToAdminDashboard() {
        if (hasRedirected) {
            return; // Already redirected
        }
        hasRedirected = true;
        Intent intent = new Intent(SplashActivity.this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

