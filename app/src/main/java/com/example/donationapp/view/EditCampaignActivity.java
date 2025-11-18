package com.example.donationapp.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.donationapp.R;
import com.example.donationapp.model.Campaign;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.FirebaseHelper;
import com.example.donationapp.util.ImageHelper;
import com.example.donationapp.util.Validator;
import com.example.donationapp.viewmodel.CampaignViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Edit Campaign Activity - Allows admin to edit existing campaigns
 */
public class EditCampaignActivity extends AppCompatActivity {
    private String campaignId;
    private Campaign currentCampaign;
    
    private ImageView campaignImage;
    private TextInputLayout titleLayout;
    private TextInputLayout descriptionLayout;
    private TextInputLayout goalAmountLayout;
    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;
    private TextInputEditText goalAmountEditText;
    private Button selectImageButton;
    private Button saveButton;
    private ProgressBar progressBar;
    
    private CampaignViewModel campaignViewModel;
    private Uri imageUri;
    private byte[] imageBytes;
    private boolean imageChanged = false;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_campaign);

        // Get campaign ID from intent
        campaignId = getIntent().getStringExtra("campaign_id");
        if (campaignId == null) {
            DialogHelper.showErrorDialog(this, "Error", "Campaign ID not provided");
            finish();
            return;
        }

        // Initialize views
        campaignImage = findViewById(R.id.campaign_image);
        titleLayout = findViewById(R.id.title_layout);
        descriptionLayout = findViewById(R.id.description_layout);
        goalAmountLayout = findViewById(R.id.goal_amount_layout);
        titleEditText = findViewById(R.id.title_edit_text);
        descriptionEditText = findViewById(R.id.description_edit_text);
        goalAmountEditText = findViewById(R.id.goal_amount_edit_text);
        selectImageButton = findViewById(R.id.select_image_button);
        saveButton = findViewById(R.id.save_button);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize ViewModel
        campaignViewModel = new ViewModelProvider(this).get(CampaignViewModel.class);

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            handleImageSelection(uri);
                        }
                    }
                });

        // Setup camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && imageUri != null) {
                        handleImageSelection(imageUri);
                    }
                });

        // Set click listeners
        selectImageButton.setOnClickListener(v -> showImageSourceDialog());
        saveButton.setOnClickListener(v -> saveCampaign());

        // Observe ViewModel
        observeViewModel();

        // Load campaign data
        campaignViewModel.loadCampaign(campaignId);
    }

    private void observeViewModel() {
        campaignViewModel.getSelectedCampaign().observe(this, campaign -> {
            if (campaign != null) {
                currentCampaign = campaign;
                populateForm(campaign);
            }
        });

        campaignViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                saveButton.setEnabled(!isLoading);
            }
        });

        campaignViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                DialogHelper.showErrorDialog(this, "Error", errorMessage);
            }
        });
    }

    private void populateForm(Campaign campaign) {
        titleEditText.setText(campaign.getTitle());
        descriptionEditText.setText(campaign.getDescription());
        goalAmountEditText.setText(String.valueOf(campaign.getGoalAmount()));

        // Load image
        if (campaign.getImageUrl() != null && !campaign.getImageUrl().isEmpty()) {
            Picasso.get().load(campaign.getImageUrl()).into(campaignImage);
            campaignImage.setVisibility(View.VISIBLE);
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        try {
            File imageFile = ImageHelper.createImageFile(this);
            imageUri = ImageHelper.getFileProviderUri(this, imageFile);
            cameraLauncher.launch(imageUri);
        } catch (Exception e) {
            DialogHelper.showErrorDialog(this, "Error", "Error opening camera");
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelection(Uri uri) {
        imageUri = uri;
        imageBytes = ImageHelper.uriToByteArray(this, uri);
        if (imageBytes != null) {
            campaignImage.setImageURI(uri);
            campaignImage.setVisibility(View.VISIBLE);
            imageChanged = true;
        }
    }

    private void saveCampaign() {
        // Clear errors
        titleLayout.setError(null);
        descriptionLayout.setError(null);
        goalAmountLayout.setError(null);

        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String goalAmountStr = goalAmountEditText.getText().toString().trim();

        boolean isValid = true;

        // Validate title
        String titleError = Validator.getCampaignTitleError(title);
        if (titleError != null) {
            titleLayout.setError(titleError);
            isValid = false;
        }

        // Validate description
        String descriptionError = Validator.getCampaignDescriptionError(description);
        if (descriptionError != null) {
            descriptionLayout.setError(descriptionError);
            isValid = false;
        }

        // Validate goal amount
        String goalError = Validator.getCampaignGoalError(goalAmountStr);
        if (goalError != null) {
            goalAmountLayout.setError(goalError);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Sanitize inputs
        title = Validator.sanitizeInput(title);
        description = Validator.sanitizeDescription(description);
        double goalAmount = Double.parseDouble(goalAmountStr);

        // Prepare updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("description", description);
        updates.put("goalAmount", goalAmount);

        // TODO: Uncomment when Firebase Storage is enabled
        // Original Firebase Storage upload code (commented out temporarily)
        // // If image changed, upload new image first
        // if (imageChanged && imageBytes != null) {
        //     byte[] compressedImage = ImageHelper.compressImage(imageBytes);
        //     String imagePath = "campaigns/" + System.currentTimeMillis() + ".jpg";
        //     
        //     // Delete old image
        //     if (currentCampaign.getImageUrl() != null && !currentCampaign.getImageUrl().isEmpty()) {
        //         FirebaseHelper.getInstance().deleteImage(currentCampaign.getImageUrl(), null, null);
        //     }
        //     
        //     // Upload new image
        //     FirebaseHelper.getInstance().uploadImage(compressedImage, imagePath,
        //             imageUrl -> {
        //                 updates.put("imageUrl", imageUrl);
        //                 campaignViewModel.updateCampaign(campaignId, updates);
        //                 DialogHelper.showSuccessDialog(this, "Success", "Campaign updated successfully!", () -> {
        //                     finish();
        //                 });
        //             },
        //             exception -> {
        //                 DialogHelper.showErrorDialog(this, "Error", "Failed to upload image");
        //             },
        //             null);
        // } else {
        //     // No image change, just update other fields
        //     campaignViewModel.updateCampaign(campaignId, updates);
        //     DialogHelper.showSuccessDialog(this, "Success", "Campaign updated successfully!", () -> {
        //         finish();
        //     });
        // }

        // Temporary fallback: Update campaign without image upload (works without Storage)
        // If image was changed, set imageUrl to empty (image won't be uploaded)
        if (imageChanged && imageBytes != null) {
            updates.put("imageUrl", ""); // Empty imageUrl when Storage is disabled
        }
        // Update campaign with or without image change
        campaignViewModel.updateCampaign(campaignId, updates);
        DialogHelper.showSuccessDialog(this, "Success", "Campaign updated successfully!", () -> {
            finish();
        });
    }
}

