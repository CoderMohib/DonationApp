package com.example.donationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.donationapp.R;
import com.example.donationapp.model.User;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.Validator;
import com.example.donationapp.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Login Activity - Handles user authentication
 */
public class LoginActivity extends AppCompatActivity {
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private TextView signupButton;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Handle system window insets for notch/punch hole
        // The layout uses fitsSystemWindows="true" which automatically handles insets
        // This ensures content doesn't overlap with system UI elements

        // Initialize views
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe ViewModel
        observeViewModel();

        // Set click listeners
        loginButton.setOnClickListener(v -> attemptLogin());
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Real-time validation
        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateEmail();
            }
        });

        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePassword();
            }
        });
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                loginButton.setEnabled(!isLoading);
            }
        });

        authViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                DialogHelper.showErrorDialog(this, "Login Failed", errorMessage);
            }
        });

        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // Login successful, redirect based on role
                redirectToDashboard(user);
            }
        });
    }

    private void attemptLogin() {
        // Clear previous errors
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        boolean isValid = true;

        // Validate email
        String emailError = Validator.getEmailError(email);
        if (emailError != null) {
            emailLayout.setError(emailError);
            isValid = false;
        }

        // Validate password
        String passwordError = Validator.getPasswordError(password);
        if (passwordError != null) {
            passwordLayout.setError(passwordError);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Sanitize inputs
        email = Validator.sanitizeInput(email);

        // Attempt login
        authViewModel.signIn(email, password);
    }

    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();
        String error = Validator.getEmailError(email);
        emailLayout.setError(error);
        return error == null;
    }

    private boolean validatePassword() {
        String password = passwordEditText.getText().toString();
        String error = Validator.getPasswordError(password);
        passwordLayout.setError(error);
        return error == null;
    }

    private void redirectToDashboard(User user) {
        Intent intent;
        if (user.isAdmin()) {
            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

