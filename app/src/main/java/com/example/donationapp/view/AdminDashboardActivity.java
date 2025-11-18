package com.example.donationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.donationapp.R;
import com.example.donationapp.adapter.CampaignAdapter;
import com.example.donationapp.model.Campaign;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.viewmodel.CampaignViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Admin Dashboard Activity - Displays campaigns with admin controls
 */
public class AdminDashboardActivity extends AppCompatActivity {
    private RecyclerView campaignsRecyclerView;
    private CampaignAdapter campaignAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton addCampaignFab;
    private CampaignViewModel campaignViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views
        campaignsRecyclerView = findViewById(R.id.campaigns_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateText = findViewById(R.id.empty_state_text);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        addCampaignFab = findViewById(R.id.add_campaign_fab);

        // Setup RecyclerView with long-press listener for admin actions
        campaignAdapter = new CampaignAdapter(campaign -> {
            // Handle campaign click - navigate to detail
            Intent intent = new Intent(AdminDashboardActivity.this, CampaignDetailActivity.class);
            intent.putExtra("campaign_id", campaign.getId());
            startActivity(intent);
        }, campaign -> {
            // Handle long-press - show edit/delete options
            showCampaignOptionsDialog(campaign);
        });

        campaignsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        campaignsRecyclerView.setAdapter(campaignAdapter);

        // Initialize ViewModel
        campaignViewModel = new ViewModelProvider(this).get(CampaignViewModel.class);

        // Observe ViewModel
        observeViewModel();

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            campaignViewModel.loadCampaigns();
        });

        // Setup FAB
        addCampaignFab.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AddCampaignActivity.class);
            startActivity(intent);
        });

        // Setup bottom navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(AdminDashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Load campaigns
        campaignViewModel.startListeningToCampaigns();
    }

    private void observeViewModel() {
        campaignViewModel.getCampaigns().observe(this, campaigns -> {
            if (campaigns != null) {
                campaignAdapter.setCampaigns(campaigns);
                updateEmptyState(campaigns.isEmpty());
            }
            swipeRefreshLayout.setRefreshing(false);
        });

        campaignViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        campaignViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                DialogHelper.showErrorDialog(this, "Error", errorMessage);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showCampaignOptionsDialog(Campaign campaign) {
        String[] options = {"Edit", "Delete"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(campaign.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Edit
                        Intent intent = new Intent(AdminDashboardActivity.this, EditCampaignActivity.class);
                        intent.putExtra("campaign_id", campaign.getId());
                        startActivity(intent);
                    } else if (which == 1) {
                        // Delete
                        DialogHelper.showDeleteConfirmationDialog(this, campaign.getTitle(), () -> {
                            campaignViewModel.deleteCampaign(campaign.getId(), campaign.getImageUrl());
                        });
                    }
                })
                .show();
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateText.setVisibility(View.VISIBLE);
            campaignsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            campaignsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        campaignViewModel.stopListeningToCampaigns();
    }
}

