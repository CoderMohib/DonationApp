package com.example.donationapp.fragment;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.donationapp.R;
import com.example.donationapp.adapter.CampaignAdapter;
import com.example.donationapp.model.Campaign;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.WindowInsetsHelper;
import com.example.donationapp.view.AddCampaignActivity;
import com.example.donationapp.view.CampaignDetailActivity;
import com.example.donationapp.view.EditCampaignActivity;
import com.example.donationapp.viewmodel.CampaignViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

/**
 * Admin Home Fragment - Displays campaigns with admin controls
 */
public class AdminHomeFragment extends Fragment {
    private RecyclerView campaignsRecyclerView;
    private CampaignAdapter campaignAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private FloatingActionButton addCampaignFab;
    private SearchView searchView;
    private CircularProgressIndicator searchProgressIndicator;
    private CampaignViewModel campaignViewModel;
    private ObjectAnimator searchProgressAnimator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Apply window insets
            View toolbarContainer = view.findViewById(R.id.toolbar_container);
            if (toolbarContainer != null) {
                WindowInsetsHelper.applyWindowInsetsTop(toolbarContainer);
            }

            // Initialize views
            campaignsRecyclerView = view.findViewById(R.id.campaigns_recycler_view);
            swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
            progressBar = view.findViewById(R.id.progress_bar);
            emptyStateText = view.findViewById(R.id.empty_state_text);
            addCampaignFab = view.findViewById(R.id.add_campaign_fab);
            searchView = view.findViewById(R.id.search_view);
            searchProgressIndicator = view.findViewById(R.id.search_progress_indicator);

            // Check if all required views are found
            if (campaignsRecyclerView == null || swipeRefreshLayout == null) {
                android.util.Log.e("AdminHomeFragment", "Required views not found in layout");
                return;
            }

            android.content.Context context = getContext();
            if (context == null) {
                return;
            }

            // Setup RecyclerView with long-press listener for admin actions
            campaignAdapter = new CampaignAdapter(campaign -> {
                // Handle campaign click - navigate to detail
                android.content.Context ctx = getContext();
                if (ctx != null) {
                    Intent intent = new Intent(ctx, CampaignDetailActivity.class);
                    intent.putExtra("campaign_id", campaign.getId());
                    startActivity(intent);
                }
            }, campaign -> {
                // Handle long-press - show edit/delete options
                showCampaignOptionsDialog(campaign);
            });

            campaignsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            campaignsRecyclerView.setAdapter(campaignAdapter);

            // Initialize ViewModel
            campaignViewModel = new ViewModelProvider(this).get(CampaignViewModel.class);

            // Observe ViewModel
            observeViewModel();

            // Setup SwipeRefreshLayout
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (campaignViewModel != null) {
                    campaignViewModel.loadCampaigns();
                }
            });

            // Setup FAB
            if (addCampaignFab != null) {
                addCampaignFab.setOnClickListener(v -> {
                    android.content.Context ctx = getContext();
                    if (ctx != null) {
                        Intent intent = new Intent(ctx, AddCampaignActivity.class);
                        startActivity(intent);
                    }
                });
            }

            // Setup search view
            setupSearchView();

            // Load campaigns
            campaignViewModel.startListeningToCampaigns();
        } catch (Exception e) {
            android.util.Log.e("AdminHomeFragment", "Error initializing fragment", e);
        }
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
        campaignViewModel.getCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null) {
                campaignAdapter.setCampaigns(campaigns);
                updateEmptyState(campaigns.isEmpty());
            }
            swipeRefreshLayout.setRefreshing(false);
        });

        campaignViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        campaignViewModel.getIsSearching().observe(getViewLifecycleOwner(), isSearching -> {
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

        campaignViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                android.content.Context context = getContext();
                if (context != null && swipeRefreshLayout != null) {
                    DialogHelper.showErrorDialog(context, "Error", errorMessage);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void showCampaignOptionsDialog(Campaign campaign) {
        android.content.Context context = getContext();
        if (context == null) return;
        
        String[] options = {"Edit", "Delete"};
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle(campaign.getTitle())
                .setItems(options, (dialog, which) -> {
                    android.content.Context ctx = getContext();
                    if (ctx == null) return;
                    
                    if (which == 0) {
                        // Edit
                        Intent intent = new Intent(ctx, EditCampaignActivity.class);
                        intent.putExtra("campaign_id", campaign.getId());
                        startActivity(intent);
                    } else if (which == 1) {
                        // Delete
                        DialogHelper.showDeleteConfirmationDialog(ctx, campaign.getTitle(), () -> {
                            if (campaignViewModel != null) {
                                campaignViewModel.deleteCampaign(campaign.getId(), campaign.getImageUrl());
                            }
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
    public void onDestroyView() {
        super.onDestroyView();
        if (campaignViewModel != null) {
            campaignViewModel.stopListeningToCampaigns();
        }
        if (searchProgressAnimator != null) {
            searchProgressAnimator.cancel();
        }
    }
}

