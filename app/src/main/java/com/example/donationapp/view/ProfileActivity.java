package com.example.donationapp.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.donationapp.R;
import com.example.donationapp.model.User;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.FirebaseHelper;
import com.example.donationapp.util.ImageHelper;
import com.example.donationapp.util.Validator;
import com.example.donationapp.viewmodel.AuthViewModel;
import com.example.donationapp.viewmodel.ProfileViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Map;

/**
 * Profile Activity - Displays and allows editing of user profile
 */
public class ProfileActivity extends AppCompatActivity {
    private ImageView profileImage;
    private TextView emailText;
    private TextInputLayout nameLayout;
    private TextInputLayout phoneLayout;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;
    private Button editImageButton;
    private Button saveButton;
    private ProgressBar progressBar;
    
    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;
    private Uri imageUri;
    private byte[] imageBytes;
    private boolean imageChanged = false;
    private boolean isInitialLoad = true;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        profileImage = findViewById(R.id.profile_image);
        // Set default user icon initially
        profileImage.setImageResource(R.drawable.ic_profile);
        emailText = findViewById(R.id.email_text);
        nameLayout = findViewById(R.id.name_layout);
        phoneLayout = findViewById(R.id.phone_layout);
        // Find child views within included layouts
        nameEditText = nameLayout.findViewById(R.id.text_input_edit_text);
        phoneEditText = phoneLayout.findViewById(R.id.text_input_edit_text);
        // Configure input fields
        nameLayout.setHint(getString(R.string.name_label));
        nameEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        phoneLayout.setHint(getString(R.string.phone_label));
        phoneEditText.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        
        editImageButton = findViewById(R.id.edit_image_button);
        // Find button within included layout
        View buttonView = findViewById(R.id.save_button);
        saveButton = buttonView.findViewById(R.id.primary_button);
        saveButton.setText(getString(R.string.save_button));
        progressBar = findViewById(R.id.progress_bar);

        // Initialize ViewModels
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

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
        editImageButton.setOnClickListener(v -> showImageSourceDialog());
        saveButton.setOnClickListener(v -> saveProfile());

        // Observe ViewModels
        observeViewModels();

        // Load user data
        FirebaseUser currentUser = FirebaseHelper.getInstance().getCurrentUser();
        if (currentUser != null) {
            emailText.setText(currentUser.getEmail());
            profileViewModel.loadUserProfile(currentUser.getUid());
            // Mark that initial load is complete after a short delay
            findViewById(android.R.id.content).postDelayed(() -> isInitialLoad = false, 500);
        }
    }

    private void observeViewModels() {
        profileViewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                populateForm(user);
            }
        });

        profileViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                saveButton.setEnabled(!isLoading);
            }
        });

        profileViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                DialogHelper.showErrorDialog(this, "Error", errorMessage);
            }
        });

        profileViewModel.getUpdateSuccess().observe(this, success -> {
            if (success != null && success && !isInitialLoad) {
                DialogHelper.showSuccessDialog(this, "Success", "Profile updated successfully!", null);
                // Reset the success flag after showing dialog
                profileViewModel.resetUpdateSuccess();
            } else if (success != null && success && isInitialLoad) {
                // Reset on initial load without showing dialog
                profileViewModel.resetUpdateSuccess();
                isInitialLoad = false;
            }
        });
    }

    private void populateForm(User user) {
        nameEditText.setText(user.getName());
        phoneEditText.setText(user.getPhone());

        // Load profile image or show default icon
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Picasso.get().load(user.getProfileImage())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(profileImage);
        } else {
            // Show default user icon when no profile image
            profileImage.setImageResource(R.drawable.ic_profile);
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
            profileImage.setImageURI(uri);
            imageChanged = true;
        }
    }

    private void saveProfile() {
        // Clear errors
        nameLayout.setError(null);
        phoneLayout.setError(null);

        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        boolean isValid = true;

        // Validate name
        String nameError = Validator.getNameError(name);
        if (nameError != null) {
            nameLayout.setError(nameError);
            isValid = false;
        }

        // Validate phone (optional)
        String phoneError = Validator.getPhoneError(phone);
        if (phoneError != null) {
            phoneLayout.setError(phoneError);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Get current user
        FirebaseUser currentUser = FirebaseHelper.getInstance().getCurrentUser();
        if (currentUser == null) {
            DialogHelper.showErrorDialog(this, "Error", "User not authenticated");
            return;
        }

        // TODO: Uncomment when Firebase Storage is enabled
        // Original Firebase Storage upload code (commented out temporarily)
        // // If image changed, upload new image first
        // if (imageChanged && imageBytes != null) {
        //     byte[] compressedImage = ImageHelper.compressImage(imageBytes);
        //     String imagePath = "profiles/" + currentUser.getUid() + "_" + System.currentTimeMillis() + ".jpg";
        //     
        //     FirebaseHelper.getInstance().uploadImage(compressedImage, imagePath,
        //             imageUrl -> {
        //                 profileViewModel.updateProfileImage(currentUser.getUid(), imageUrl);
        //                 // Also update name and phone
        //                 profileViewModel.updateUserName(currentUser.getUid(), name);
        //                 if (!phone.isEmpty()) {
        //                     profileViewModel.updateUserPhone(currentUser.getUid(), phone);
        //                 }
        //             },
        //             exception -> {
        //                 DialogHelper.showErrorDialog(this, "Error", "Failed to upload image");
        //             },
        //             null);
        // } else {
        //     // No image change, just update name and phone
        //     profileViewModel.updateUserName(currentUser.getUid(), name);
        //     if (!phone.isEmpty()) {
        //         profileViewModel.updateUserPhone(currentUser.getUid(), phone);
        //     }
        // }

        // Update all fields at once using a single call to avoid multiple success dialogs
        Map<String, Object> updates = new java.util.HashMap<String, Object>();
        updates.put("name", name);
        updates.put("phone", phone != null && !phone.isEmpty() ? phone : "");
        
        // If image was changed, set profileImage to empty (image won't be uploaded without Storage)
        if (imageChanged && imageBytes != null) {
            updates.put("profileImage", ""); // Empty imageUrl when Storage is disabled
        }
        
        // Single update call for all fields
        profileViewModel.updateUserProfile(currentUser.getUid(), updates);
    }
}

