package com.example.donationapp.view;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

/**
 * User Dashboard Activity - Displays campaigns for regular users
 */
public class UserDashboardActivity extends AppCompatActivity {
    private RecyclerView campaignsRecyclerView;
    private CampaignAdapter campaignAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private BottomNavigationView bottomNavigation;
    private SearchView searchView;
    private CircularProgressIndicator searchProgressIndicator;
    private CampaignViewModel campaignViewModel;
    private ObjectAnimator searchProgressAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize views
        campaignsRecyclerView = findViewById(R.id.campaigns_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateText = findViewById(R.id.empty_state_text);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        searchView = findViewById(R.id.search_view);
        searchProgressIndicator = findViewById(R.id.search_progress_indicator);

        // Setup RecyclerView
        campaignAdapter = new CampaignAdapter(campaign -> {
            // Handle campaign click - navigate to detail
            Intent intent = new Intent(UserDashboardActivity.this, CampaignDetailActivity.class);
            intent.putExtra("campaign_id", campaign.getId());
            startActivity(intent);
        }, null); // No long-press for users

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

        // Setup bottom navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(UserDashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Setup search view
        setupSearchView();

        // Load campaigns
        campaignViewModel.startListeningToCampaigns();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                campaignViewModel.searchCampaigns(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                campaignViewModel.searchCampaigns(newText);
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            campaignViewModel.clearSearch();
            return false;
        });
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

        campaignViewModel.getIsSearching().observe(this, isSearching -> {
            if (isSearching != null) {
                if (isSearching) {
                    searchProgressIndicator.setVisibility(View.VISIBLE);
                    searchProgressIndicator.setProgress(0);
                    
                    // Animate progress from 0 to 100
                    if (searchProgressAnimator != null) {
                        searchProgressAnimator.cancel();
                    }
                    searchProgressAnimator = ObjectAnimator.ofInt(searchProgressIndicator, "progress", 0, 100);
                    searchProgressAnimator.setDuration(1500); // 1.5 seconds for search
                    searchProgressAnimator.start();
                } else {
                    if (searchProgressAnimator != null) {
                        searchProgressAnimator.cancel();
                    }
                    searchProgressIndicator.setProgress(0);
                    searchProgressIndicator.setVisibility(View.GONE);
                }
            }
        });

        campaignViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                DialogHelper.showErrorDialog(this, "Error", errorMessage);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
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

