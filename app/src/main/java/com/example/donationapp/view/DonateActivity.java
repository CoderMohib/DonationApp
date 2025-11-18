package com.example.donationapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.donationapp.R;
import com.example.donationapp.model.Campaign;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.FirebaseHelper;
import com.example.donationapp.util.Validator;
import com.example.donationapp.viewmodel.CampaignViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Donate Activity - Allows users to make donations
 */
public class DonateActivity extends AppCompatActivity {
    private String campaignId;
    private Campaign campaign;
    
    private TextView campaignTitleText;
    private TextView campaignGoalText;
    private TextInputLayout amountLayout;
    private TextInputEditText amountEditText;
    private Button donateButton;
    private ProgressBar progressBar;
    
    private CampaignViewModel campaignViewModel;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        // Get campaign ID from intent
        campaignId = getIntent().getStringExtra("campaign_id");
        if (campaignId == null) {
            DialogHelper.showErrorDialog(this, "Error", "Campaign ID not provided");
            finish();
            return;
        }

        // Initialize views
        campaignTitleText = findViewById(R.id.campaign_title_text);
        campaignGoalText = findViewById(R.id.campaign_goal_text);
        amountLayout = findViewById(R.id.amount_layout);
        amountEditText = findViewById(R.id.amount_edit_text);
        donateButton = findViewById(R.id.donate_button);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize ViewModel
        campaignViewModel = new ViewModelProvider(this).get(CampaignViewModel.class);

        // Set click listener
        donateButton.setOnClickListener(v -> processDonation());

        // Observe ViewModel
        observeViewModel();

        // Load campaign
        campaignViewModel.loadCampaign(campaignId);
    }

    private void observeViewModel() {
        campaignViewModel.getSelectedCampaign().observe(this, campaign -> {
            if (campaign != null) {
                this.campaign = campaign;
                displayCampaignInfo(campaign);
            }
        });

        campaignViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                donateButton.setEnabled(!isLoading);
            }
        });

        campaignViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                DialogHelper.showErrorDialog(this, "Error", errorMessage);
            }
        });
    }

    private void displayCampaignInfo(Campaign campaign) {
        campaignTitleText.setText(campaign.getTitle());
        campaignGoalText.setText("Goal: " + currencyFormat.format(campaign.getGoalAmount()));
    }

    private void processDonation() {
        // Clear error
        amountLayout.setError(null);

        String amountStr = amountEditText.getText().toString().trim();

        // Validate amount
        String amountError = Validator.getDonationAmountError(amountStr);
        if (amountError != null) {
            amountLayout.setError(amountError);
            return;
        }

        double amount = Double.parseDouble(amountStr);

        // Get current user
        FirebaseUser currentUser = FirebaseHelper.getInstance().getCurrentUser();
        if (currentUser == null) {
            DialogHelper.showErrorDialog(this, "Error", "User not authenticated");
            return;
        }

        // Create donation using transaction
        FirebaseHelper.getInstance().createDonation(campaignId, amount, currentUser.getUid(),
                aVoid -> {
                    // Success
                    DialogHelper.showSuccessDialog(this, "Success", 
                            "Thank you for your donation of " + currencyFormat.format(amount) + "!",
                            () -> {
                                finish();
                            });
                },
                exception -> {
                    DialogHelper.showErrorDialog(this, "Error", 
                            FirebaseHelper.getInstance().getFirestoreErrorMessage(exception));
                });
    }
}

