package com.example.donationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.donationapp.R;
import com.example.donationapp.model.Campaign;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.viewmodel.CampaignViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Campaign Detail Activity - Shows full campaign details
 */
public class CampaignDetailActivity extends AppCompatActivity {
    private String campaignId;
    
    private ImageView campaignImage;
    private TextView titleText;
    private TextView descriptionText;
    private TextView goalAmountText;
    private TextView collectedAmountText;
    private LinearProgressIndicator progressIndicator;
    private Button donateButton;
    private ProgressBar progressBar;
    
    private CampaignViewModel campaignViewModel;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign_detail);

        // Get campaign ID from intent
        campaignId = getIntent().getStringExtra("campaign_id");
        if (campaignId == null) {
            DialogHelper.showErrorDialog(this, "Error", "Campaign ID not provided");
            finish();
            return;
        }

        // Initialize views
        campaignImage = findViewById(R.id.campaign_image);
        titleText = findViewById(R.id.title_text);
        descriptionText = findViewById(R.id.description_text);
        goalAmountText = findViewById(R.id.goal_amount_text);
        collectedAmountText = findViewById(R.id.collected_amount_text);
        progressIndicator = findViewById(R.id.progress_indicator);
        donateButton = findViewById(R.id.donate_button);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize ViewModel
        campaignViewModel = new ViewModelProvider(this).get(CampaignViewModel.class);

        // Set click listener
        donateButton.setOnClickListener(v -> {
            Intent intent = new Intent(CampaignDetailActivity.this, DonateActivity.class);
            intent.putExtra("campaign_id", campaignId);
            startActivity(intent);
        });

        // Observe ViewModel
        observeViewModel();

        // Load campaign
        campaignViewModel.loadCampaign(campaignId);
    }

    private void observeViewModel() {
        campaignViewModel.getSelectedCampaign().observe(this, campaign -> {
            if (campaign != null) {
                displayCampaign(campaign);
            }
        });

        campaignViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        campaignViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                DialogHelper.showErrorDialog(this, "Error", errorMessage);
            }
        });
    }

    private void displayCampaign(Campaign campaign) {
        titleText.setText(campaign.getTitle());
        descriptionText.setText(campaign.getDescription());
        goalAmountText.setText("Goal: " + currencyFormat.format(campaign.getGoalAmount()));
        collectedAmountText.setText("Collected: " + currencyFormat.format(campaign.getCollectedAmount()));

        // Set progress
        int progress = campaign.getProgressPercentage();
        progressIndicator.setProgress(progress);

        // Load image
        if (campaign.getImageUrl() != null && !campaign.getImageUrl().isEmpty()) {
            Picasso.get().load(campaign.getImageUrl()).into(campaignImage);
        }
    }
}

