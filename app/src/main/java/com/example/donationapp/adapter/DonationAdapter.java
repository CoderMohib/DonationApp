package com.example.donationapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donationapp.R;
import com.example.donationapp.model.Donation;
import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying user donations in RecyclerView
 */
public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder> {
    private List<Donation> donations;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    public DonationAdapter() {
        this.donations = new ArrayList<>();
    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation, parent, false);
        return new DonationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
        Donation donation = donations.get(position);
        holder.bind(donation);
    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

    public void setDonations(List<Donation> donations) {
        this.donations = donations != null ? donations : new ArrayList<>();
        notifyDataSetChanged();
    }

    class DonationViewHolder extends RecyclerView.ViewHolder {
        private TextView amountText;
        private TextView dateText;
        private TextView campaignIdText;

        public DonationViewHolder(@NonNull View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.amount_text);
            dateText = itemView.findViewById(R.id.date_text);
            campaignIdText = itemView.findViewById(R.id.campaign_id_text);
        }

        public void bind(Donation donation) {
            amountText.setText(currencyFormat.format(donation.getAmount()));
            
            if (donation.getDate() != null) {
                Timestamp timestamp = donation.getDate();
                Date date = timestamp.toDate();
                dateText.setText(dateFormat.format(date));
            } else {
                dateText.setText("Date not available");
            }
            
            campaignIdText.setText("Campaign ID: " + donation.getCampaignId());
        }
    }
}

