package com.example.donationapp.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.donationapp.R;
import com.example.donationapp.util.WindowInsetsHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Admin Main Activity - Container for admin fragments with BottomNavigationView
 */
public class AdminMainActivity extends AppCompatActivity {
    private static final String TAG = "AdminMainActivity";
    private NavController navController;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Apply window insets
        View rootView = findViewById(android.R.id.content);
        WindowInsetsHelper.applyWindowInsets(rootView);

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
                    navController = Navigation.findNavController(AdminMainActivity.this, R.id.nav_host_fragment);
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
        com.example.donationapp.util.DialogHelper.showConfirmationDialog(this, "Logout", "Are you sure you want to logout?",
                () -> {
                    new androidx.lifecycle.ViewModelProvider(this).get(com.example.donationapp.viewmodel.AuthViewModel.class).signOut();
                    android.content.Intent intent = new android.content.Intent(AdminMainActivity.this, SplashActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

