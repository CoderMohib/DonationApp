package com.example.donationapp.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

/**
 * Add Campaign Activity - Allows admin to create new campaigns
 */
public class AddCampaignActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    
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

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_campaign);

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
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
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
        }
    }

    private void observeViewModel() {
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

        // Check image
        // if (imageBytes == null) {
        //     DialogHelper.showErrorDialog(this, "Error", "Please select an image for the campaign");
        //     isValid = false;
        // }

        if (!isValid) {
            return;
        }

        // Sanitize inputs
        title = Validator.sanitizeInput(title);
        description = Validator.sanitizeDescription(description);
        double goalAmount = Double.parseDouble(goalAmountStr);

        // Get current user
        FirebaseUser currentUser = FirebaseHelper.getInstance().getCurrentUser();
        if (currentUser == null) {
            DialogHelper.showErrorDialog(this, "Error", "User not authenticated");
            return;
        }

        // TODO: Uncomment when Firebase Storage is enabled
        // Original Firebase Storage upload code (commented out temporarily)
        // // Compress image
        // byte[] compressedImage = ImageHelper.compressImage(imageBytes);
        //
        // // Upload image first, then create campaign
        // String imagePath = "campaigns/" + System.currentTimeMillis() + ".jpg";
        // FirebaseHelper.getInstance().uploadImage(compressedImage, imagePath,
        //         imageUrl -> {
        //             // Image uploaded, create campaign
        //             Campaign campaign = new Campaign("", title, description, goalAmount, currentUser.getUid());
        //             campaign.setImageUrl(imageUrl);
        //             campaignViewModel.createCampaign(campaign);
        //             
        //             // Show success and finish
        //             DialogHelper.showSuccessDialog(this, "Success", "Campaign created successfully!", () -> {
        //                 finish();
        //             });
        //         },
        //         exception -> {
        //             DialogHelper.showErrorDialog(this, "Error", "Failed to upload image: " + exception.getMessage());
        //         },
        //         null);

        // Temporary fallback: Create campaign without image upload (works without Storage)
        Campaign campaign = new Campaign("", title, description, goalAmount, currentUser.getUid());
        campaign.setImageUrl(""); // Empty imageUrl when Storage is disabled
        campaignViewModel.createCampaign(campaign);
        
        // Show success and finish
        DialogHelper.showSuccessDialog(this, "Success", "Campaign created successfully!", () -> {
            finish();
        });
    }
}

