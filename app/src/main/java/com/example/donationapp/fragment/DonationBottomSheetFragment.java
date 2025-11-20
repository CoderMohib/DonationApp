package com.example.donationapp.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.donationapp.R;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.FirebaseHelper;
import com.example.donationapp.util.Validator;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Bottom Sheet Dialog Fragment for making donations
 * Uses Material Design 3 components
 */
public class DonationBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String ARG_CAMPAIGN_ID = "campaign_id";
    private static final String ARG_CAMPAIGN_TITLE = "campaign_title";
    
    private String campaignId;
    private String campaignTitle;
    
    private TextView campaignTitleText;
    private TextInputLayout amountLayout;
    private TextInputEditText amountEditText;
    private MaterialButton donateSubmitButton;
    private CircularProgressIndicator progressIndicator;
    private ObjectAnimator progressAnimator;
    
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    /**
     * Create a new instance of DonationBottomSheetFragment
     * @param campaignId The ID of the campaign to donate to
     * @param campaignTitle Optional title of the campaign for display
     * @return New instance of DonationBottomSheetFragment
     */
    public static DonationBottomSheetFragment newInstance(String campaignId, String campaignTitle) {
        DonationBottomSheetFragment fragment = new DonationBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAMPAIGN_ID, campaignId);
        if (campaignTitle != null) {
            args.putString(ARG_CAMPAIGN_TITLE, campaignTitle);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            campaignId = getArguments().getString(ARG_CAMPAIGN_ID);
            campaignTitle = getArguments().getString(ARG_CAMPAIGN_TITLE);
        }
        
        if (campaignId == null) {
            dismiss();
            return;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_donation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        campaignTitleText = view.findViewById(R.id.campaign_title_text);
        amountLayout = view.findViewById(R.id.amount_layout);
        // Find child views within included layouts
        amountEditText = amountLayout.findViewById(R.id.text_input_edit_text);
        // Configure amount field
        amountLayout.setHint(getString(R.string.donation_amount_label));
        amountLayout.setPrefixText("$");
        amountEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        // Find button and progress indicator within included layout
        View buttonContainer = view.findViewById(R.id.donate_button_container);
        donateSubmitButton = buttonContainer.findViewById(R.id.action_button);
        progressIndicator = buttonContainer.findViewById(R.id.progress_indicator);
        // Configure button
        donateSubmitButton.setText(getString(R.string.donate_button));
        
        // Set campaign title if provided
        if (campaignTitle != null && !campaignTitle.isEmpty()) {
            campaignTitleText.setText(campaignTitle);
            campaignTitleText.setVisibility(View.VISIBLE);
        } else {
            campaignTitleText.setVisibility(View.GONE);
        }
        
        // Set click listener for donate button
        donateSubmitButton.setOnClickListener(v -> processDonation());
        
        // Set up text watcher for real-time validation
        amountEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateAmount();
            }
        });
    }

    private void processDonation() {
        // Clear previous errors
        amountLayout.setError(null);
        
        // Validate amount
        String amountStr = amountEditText.getText().toString().trim();
        String amountError = Validator.getDonationAmountError(amountStr);
        
        if (amountError != null) {
            amountLayout.setError(amountError);
            return;
        }
        
        double amount = Double.parseDouble(amountStr);
        
        // Check authentication
        FirebaseUser currentUser = FirebaseHelper.getInstance().getCurrentUser();
        if (currentUser == null) {
            DialogHelper.showErrorDialog(requireContext(), "Authentication Error", 
                    "You are not authorized. Please log in to make a donation.");
            dismiss();
            return;
        }
        
        // Show loading state
        setLoadingState(true);
        
        // Create donation using transaction
        FirebaseHelper.getInstance().createDonation(campaignId, amount, currentUser.getUid(),
                aVoid -> {
                    // Success
                    setLoadingState(false);
                    DialogHelper.showSuccessDialog(requireContext(), "Success", 
                            "Thank you for your donation of " + currencyFormat.format(amount) + "!",
                            () -> {
                                dismiss();
                            });
                },
                exception -> {
                    // Error
                    setLoadingState(false);
                    String errorMessage = FirebaseHelper.getInstance().getFirestoreErrorMessage(exception);
                    // Check if it's a permission error and provide more specific guidance
                    if (exception != null && exception.getMessage() != null && 
                        exception.getMessage().toLowerCase().contains("permission")) {
                        DialogHelper.showErrorDialog(requireContext(), "Permission Error", 
                                "Unable to process donation. Please ensure your Firestore security rules allow authenticated users to update campaign collectedAmount. " +
                                "See README.md for security rules configuration.");
                    } else {
                        DialogHelper.showErrorDialog(requireContext(), "Donation Failed", errorMessage);
                    }
                });
    }

    private void validateAmount() {
        String amountStr = amountEditText.getText().toString().trim();
        String amountError = Validator.getDonationAmountError(amountStr);
        amountLayout.setError(amountError);
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            // Show loading indicator and hide button text (like login/signup)
            progressIndicator.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(0);
            donateSubmitButton.setText("");
            donateSubmitButton.setEnabled(false);
            amountEditText.setEnabled(false);
            
            // Animate progress from 0 to 100
            if (progressAnimator != null) {
                progressAnimator.cancel();
            }
            progressAnimator = ObjectAnimator.ofInt(progressIndicator, "progress", 0, 100);
            progressAnimator.setDuration(2500); // 2.5 seconds
            progressAnimator.start();
        } else {
            // Hide loading indicator and show button text
            if (progressAnimator != null) {
                progressAnimator.cancel();
            }
            progressIndicator.setProgress(0);
            progressIndicator.setVisibility(View.GONE);
            donateSubmitButton.setText(R.string.donate_button);
            donateSubmitButton.setEnabled(true);
            amountEditText.setEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
    }
}

