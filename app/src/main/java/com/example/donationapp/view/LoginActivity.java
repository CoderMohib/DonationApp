package com.example.donationapp.view;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.donationapp.R;
import com.example.donationapp.model.User;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.Validator;
import com.example.donationapp.util.WindowInsetsHelper;
import com.example.donationapp.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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
    private MaterialButton loginButton;
    private TextView signupButton;
    private CircularProgressIndicator loginProgressIndicator;
    private AuthViewModel authViewModel;
    private ObjectAnimator progressAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Apply window insets for notch/punch hole camera
        View rootView = findViewById(android.R.id.content);
        WindowInsetsHelper.applyWindowInsets(rootView);

        // Initialize views
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        // Find child views within included layouts
        emailEditText = emailLayout.findViewById(R.id.text_input_edit_text);
        passwordEditText = passwordLayout.findViewById(R.id.text_input_edit_text);
        // Configure password field
        passwordLayout.setEndIconMode(com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        passwordLayout.setEndIconTintList(getColorStateList(R.color.input_hint));
        // Configure email field
        emailLayout.setHint(getString(R.string.email_label));
        emailEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEditText.setAutofillHints("email");
        // Configure password field
        passwordLayout.setHint(getString(R.string.password_label));
        passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEditText.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
        passwordEditText.setAutofillHints("password");
        
        // Find button and progress indicator within included layout
        View buttonContainer = findViewById(R.id.login_button_container);
        loginButton = buttonContainer.findViewById(R.id.action_button);
        loginProgressIndicator = buttonContainer.findViewById(R.id.progress_indicator);
        // Configure button
        loginButton.setText(getString(R.string.login_button));
        
        signupButton = findViewById(R.id.signup_button);

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
        
        // Forgot Password listener
        TextView forgotPasswordText = findViewById(R.id.forgot_password_link);
        if (forgotPasswordText != null) {
            forgotPasswordText.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

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
                if (isLoading) {
                    // Show loading indicator and hide button text
                    loginProgressIndicator.setVisibility(View.VISIBLE);
                    loginProgressIndicator.setProgress(0);
                    loginButton.setText("");
                    loginButton.setEnabled(false);
                    
                    // Animate progress from 0 to 100
                    if (progressAnimator != null) {
                        progressAnimator.cancel();
                    }
                    progressAnimator = ObjectAnimator.ofInt(loginProgressIndicator, "progress", 0, 100);
                    progressAnimator.setDuration(2500); // 2.5 seconds
                    progressAnimator.start();
                } else {
                    // Hide loading indicator and show button text
                    if (progressAnimator != null) {
                        progressAnimator.cancel();
                    }
                    loginProgressIndicator.setProgress(0);
                    loginProgressIndicator.setVisibility(View.GONE);
                    loginButton.setText(R.string.login_button);
                    loginButton.setEnabled(true);
                }
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
            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

