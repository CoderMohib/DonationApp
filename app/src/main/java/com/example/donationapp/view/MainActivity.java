package com.example.donationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.donationapp.R;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.WindowInsetsHelper;
import com.example.donationapp.viewmodel.AuthViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main Activity - Container for user fragments with BottomNavigationView
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private NavController navController;
    private BottomNavigationView bottomNavigation;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Apply window insets
        View rootView = findViewById(android.R.id.content);
        WindowInsetsHelper.applyWindowInsets(rootView);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Setup Navigation - Wait for fragment to be ready
        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        // Use post to ensure NavHostFragment is ready
        bottomNavigation.post(() -> {
            try {
                // Get NavHostFragment from FragmentManager
                Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                if (navHostFragment instanceof NavHostFragment) {
                    navController = ((NavHostFragment) navHostFragment).getNavController();
                } else {
                    // Fallback: try to find it directly
                    navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
                }
                
                if (navController != null && bottomNavigation != null) {
                    // Handle logout menu item separately, then setup NavigationUI for other items
                    bottomNavigation.setOnItemSelectedListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.nav_logout) {
                            handleLogout();
                            return false; // Don't select logout item
                        } else {
                            // Use NavigationUI for other items
                            return NavigationUI.onNavDestinationSelected(item, navController);
                        }
                    });
                    
                    // Ensure initial selection is correct
                    bottomNavigation.setSelectedItemId(R.id.nav_home);
                } else {
                    Log.e(TAG, "NavController or BottomNavigationView is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting up navigation", e);
            }
        });
    }

    private void handleLogout() {
        DialogHelper.showConfirmationDialog(this, "Logout", "Are you sure you want to logout?",
                () -> {
                    authViewModel.signOut();
                    Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null) {
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}

