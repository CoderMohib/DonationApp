package com.example.donationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.donationapp.R;
import com.example.donationapp.model.User;
import com.example.donationapp.util.FirebaseHelper;
import com.example.donationapp.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

/**
 * Splash Activity - Checks authentication state and redirects accordingly
 */
public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe authentication state
        authViewModel.getIsAuthenticated().observe(this, isAuthenticated -> {
            if (isAuthenticated != null && isAuthenticated) {
                // User is authenticated, check role and redirect
                checkUserRoleAndRedirect();
            } else {
                // User not authenticated, go to login
                redirectToLogin();
            }
        });

        // Check auth state
        FirebaseUser currentUser = FirebaseHelper.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        } else {
            // Wait a bit for user data to load, then check role
            new Handler(Looper.getMainLooper()).postDelayed(this::checkUserRoleAndRedirect, SPLASH_DELAY);
        }
    }

    private void checkUserRoleAndRedirect() {
        User currentUser = authViewModel.getCurrentUser().getValue();
        if (currentUser != null) {
            if (currentUser.isAdmin()) {
                redirectToAdminDashboard();
            } else {
                redirectToUserDashboard();
            }
        } else {
            // User data not loaded yet, wait a bit more
            authViewModel.getCurrentUser().observe(this, user -> {
                if (user != null) {
                    if (user.isAdmin()) {
                        redirectToAdminDashboard();
                    } else {
                        redirectToUserDashboard();
                    }
                } else {
                    redirectToLogin();
                }
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToUserDashboard() {
        Intent intent = new Intent(SplashActivity.this, UserDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToAdminDashboard() {
        Intent intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}

