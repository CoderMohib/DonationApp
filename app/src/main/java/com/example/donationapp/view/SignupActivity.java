package com.example.donationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
 * Signup Activity - Handles new user registration
 */
public class SignupActivity extends AppCompatActivity {
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private Button signupButton;
    private Button loginButton;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        nameLayout = findViewById(R.id.name_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        signupButton = findViewById(R.id.signup_button);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe ViewModel
        observeViewModel();

        // Set click listeners
        signupButton.setOnClickListener(v -> attemptSignup());
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Real-time validation
        nameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateName();
            }
        });

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

        confirmPasswordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateConfirmPassword();
            }
        });
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                signupButton.setEnabled(!isLoading);
            }
        });

        authViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                DialogHelper.showErrorDialog(this, "Signup Failed", errorMessage);
            }
        });

        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // Signup successful, redirect to user dashboard
                DialogHelper.showSuccessDialog(this, "Success", "Account created successfully!", () -> {
                    Intent intent = new Intent(SignupActivity.this, UserDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    private void attemptSignup() {
        // Clear previous errors
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        boolean isValid = true;

        // Validate name
        String nameError = Validator.getNameError(name);
        if (nameError != null) {
            nameLayout.setError(nameError);
            isValid = false;
        }

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

        // Validate password confirmation
        String confirmPasswordError = Validator.getPasswordConfirmError(password, confirmPassword);
        if (confirmPasswordError != null) {
            confirmPasswordLayout.setError(confirmPasswordError);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Sanitize inputs
        name = Validator.sanitizeInput(name);
        email = Validator.sanitizeInput(email);

        // Attempt signup
        authViewModel.signUp(email, password, name);
    }

    private boolean validateName() {
        String name = nameEditText.getText().toString().trim();
        String error = Validator.getNameError(name);
        nameLayout.setError(error);
        return error == null;
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

    private boolean validateConfirmPassword() {
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        String error = Validator.getPasswordConfirmError(password, confirmPassword);
        confirmPasswordLayout.setError(error);
        return error == null;
    }
}

