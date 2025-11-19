package com.example.donationapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.donationapp.R;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.Validator;
import com.example.donationapp.util.WindowInsetsHelper;
import com.example.donationapp.viewmodel.AuthViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Forgot Password Activity - Handles password reset requests
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputEditText emailEditText;
    private MaterialButton resetButton;
    private CircularProgressIndicator progressIndicator;
    private TextView backToLoginLink;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Apply window insets
        View rootView = findViewById(android.R.id.content);
        WindowInsetsHelper.applyWindowInsets(rootView);

        // Initialize views
        emailLayout = findViewById(R.id.email_layout);
        emailEditText = findViewById(R.id.email_edit_text);
        resetButton = findViewById(R.id.reset_button);
        progressIndicator = findViewById(R.id.progress_indicator);
        backToLoginLink = findViewById(R.id.back_to_login_link);

        // Back to login listener
        if (backToLoginLink != null) {
            backToLoginLink.setOnClickListener(v -> finish());
        }

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe ViewModel
        observeViewModel();

        // Set click listeners
        resetButton.setOnClickListener(v -> attemptReset());

        // Real-time validation
        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateEmail();
            }
        });
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    progressIndicator.setVisibility(View.VISIBLE);
                    resetButton.setEnabled(false);
                    resetButton.setText("");
                } else {
                    progressIndicator.setVisibility(View.GONE);
                    resetButton.setEnabled(true);
                    resetButton.setText(R.string.reset_password_button);
                }
            }
        });

        authViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                DialogHelper.showErrorDialog(this, "Reset Failed", errorMessage);
            }
        });

        authViewModel.getResetPasswordSuccess().observe(this, success -> {
            if (success != null && success) {
                DialogHelper.showSuccessDialog(this, "Email Sent", 
                    "A password reset link has been sent to your email address.",
                    () -> finish());
            }
        });
    }

    private void attemptReset() {
        // Clear previous errors
        emailLayout.setError(null);

        String email = emailEditText.getText().toString().trim();

        if (!validateEmail()) {
            return;
        }

        // Sanitize input
        email = Validator.sanitizeInput(email);

        // Attempt reset
        authViewModel.resetPassword(email);
    }

    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();
        String error = Validator.getEmailError(email);
        emailLayout.setError(error);
        return error == null;
    }
}
