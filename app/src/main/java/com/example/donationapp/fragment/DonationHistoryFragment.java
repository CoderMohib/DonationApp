package com.example.donationapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.donationapp.R;
import com.example.donationapp.adapter.DonationAdapter;
import com.example.donationapp.model.Donation;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.FirebaseHelper;
import com.example.donationapp.util.WindowInsetsHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Donation History Fragment - Displays user's donation history
 */
public class DonationHistoryFragment extends Fragment {
    private RecyclerView donationsRecyclerView;
    private DonationAdapter donationAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyStateText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donation_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply window insets
        WindowInsetsHelper.applyWindowInsetsTop(view);

        // Initialize views
        donationsRecyclerView = view.findViewById(R.id.donations_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        // Setup RecyclerView
        donationAdapter = new DonationAdapter();
        donationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        donationsRecyclerView.setAdapter(donationAdapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadDonations();
        });

        // Load donations
        loadDonations();
    }

    private void loadDonations() {
        FirebaseUser currentUser = FirebaseHelper.getInstance().getCurrentUser();
        if (currentUser == null) {
            DialogHelper.showErrorDialog(requireContext(), "Error", "User not authenticated");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        FirebaseHelper.getInstance().getUserDonations(currentUser.getUid(),
                querySnapshot -> {
                    List<Donation> donations = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Donation donation = document.toObject(Donation.class);
                        if (donation != null) {
                            donation.setId(document.getId());
                            donations.add(donation);
                        }
                    }
                    donationAdapter.setDonations(donations);
                    updateEmptyState(donations.isEmpty());
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                },
                exception -> {
                    DialogHelper.showErrorDialog(requireContext(), "Error", "Failed to load donations");
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    updateEmptyState(true);
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateText.setVisibility(View.VISIBLE);
            donationsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            donationsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}


