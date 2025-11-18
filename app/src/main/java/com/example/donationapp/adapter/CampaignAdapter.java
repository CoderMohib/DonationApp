package com.example.donationapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donationapp.R;
import com.example.donationapp.model.Campaign;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying campaigns in RecyclerView
 */
public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder> {
    private List<Campaign> campaigns;
    private OnCampaignClickListener clickListener;
    private OnCampaignLongClickListener longClickListener;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public CampaignAdapter(OnCampaignClickListener clickListener, 
                          OnCampaignLongClickListener longClickListener) {
        this.campaigns = new ArrayList<>();
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public interface OnCampaignClickListener {
        void onCampaignClick(Campaign campaign);
    }

    public interface OnCampaignLongClickListener {
        void onCampaignLongClick(Campaign campaign);
    }

    @NonNull
    @Override
    public CampaignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_campaign, parent, false);
        return new CampaignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignViewHolder holder, int position) {
        Campaign campaign = campaigns.get(position);
        holder.bind(campaign);
    }

    @Override
    public int getItemCount() {
        return campaigns.size();
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns != null ? campaigns : new ArrayList<>();
        notifyDataSetChanged();
    }

    class CampaignViewHolder extends RecyclerView.ViewHolder {
        private ImageView campaignImage;
        private TextView titleText;
        private TextView descriptionText;
        private TextView goalAmountText;
        private TextView collectedAmountText;
        private TextView progressText;
        private Button donateButton;

        public CampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            campaignImage = itemView.findViewById(R.id.campaign_image);
            titleText = itemView.findViewById(R.id.title_text);
            descriptionText = itemView.findViewById(R.id.description_text);
            goalAmountText = itemView.findViewById(R.id.goal_amount_text);
            collectedAmountText = itemView.findViewById(R.id.collected_amount_text);
            progressText = itemView.findViewById(R.id.progress_text);
            donateButton = itemView.findViewById(R.id.donate_button);

            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onCampaignClick(campaigns.get(position));
                }
            });

            // Set long click listener
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onCampaignLongClick(campaigns.get(position));
                    return true;
                }
                return false;
            });

            // Donate button click
            donateButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onCampaignClick(campaigns.get(position));
                }
            });
        }

        public void bind(Campaign campaign) {
            if (campaign == null) {
                return;
            }

            // Set text fields with null safety
            String title = campaign.getTitle();
            titleText.setText(title != null ? title : "");

            String description = campaign.getDescription();
            descriptionText.setText(description != null ? description : "");

            goalAmountText.setText("Goal: " + currencyFormat.format(campaign.getGoalAmount()));
            collectedAmountText.setText("Collected: " + currencyFormat.format(campaign.getCollectedAmount()));
            
            int progress = campaign.getProgressPercentage();
            progressText.setText(progress + "%");

            // Load image with error handling
            String imageUrl = campaign.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(campaignImage);
            } else {
                campaignImage.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}

