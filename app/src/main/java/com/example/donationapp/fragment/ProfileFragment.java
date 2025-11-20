package com.example.donationapp.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.donationapp.R;
import com.example.donationapp.model.User;
import com.example.donationapp.util.DialogHelper;
import com.example.donationapp.util.FirebaseHelper;
import com.example.donationapp.util.ImageHelper;
import com.example.donationapp.util.Validator;
import com.example.donationapp.util.WindowInsetsHelper;
import com.example.donationapp.view.SplashActivity;
import com.example.donationapp.viewmodel.AuthViewModel;
import com.example.donationapp.viewmodel.ProfileViewModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Map;

/**
 * Profile Fragment - Displays and allows editing of user profile
 */
public class ProfileFragment extends Fragment {
    private ImageView profileImage;
    private TextView emailText;
    private TextInputLayout nameLayout;
    private TextInputLayout phoneLayout;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;
    private Button editImageButton;
    private Button saveButton;
    private CircularProgressIndicator progressBar;
    
    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;
    private Uri imageUri;
    private byte[] imageBytes;
    private boolean imageChanged = false;
    private boolean isInitialLoad = true;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply window insets
        WindowInsetsHelper.applyWindowInsetsTop(view);

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image);
        // Set default user icon initially
        profileImage.setImageResource(R.drawable.ic_profile);
        emailText = view.findViewById(R.id.email_text);
        nameLayout = view.findViewById(R.id.name_layout);
        phoneLayout = view.findViewById(R.id.phone_layout);
        // Find child views within included layouts
        nameEditText = nameLayout.findViewById(R.id.text_input_edit_text);
        phoneEditText = phoneLayout.findViewById(R.id.text_input_edit_text);
        // Configure input fields
        nameLayout.setHint(getString(R.string.name_label));
        nameEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        nameEditText.setAutofillHints("name");
        phoneLayout.setHint(getString(R.string.phone_label));
        phoneEditText.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phoneEditText.setAutofillHints("phone");
        
        editImageButton = view.findViewById(R.id.edit_image_button);
        // Find button directly (no longer using include with merge)
        saveButton = view.findViewById(R.id.primary_button);
        if (saveButton != null) {
            saveButton.setText(getString(R.string.save_button));
        } else {
            // Log error but don't crash - button functionality will be unavailable
            android.util.Log.e("ProfileFragment", "Save button not found! Button will not be functional.");
        }
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize ViewModels - safely get activity
        if (getActivity() == null) {
            return;
        }
        authViewModel = new ViewModelProvider(getActivity()).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(getActivity()).get(ProfileViewModel.class);

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
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
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> saveProfile());
        }

        // Observe ViewModels
        observeViewModels();

        // Load user data
        FirebaseUser currentUser = FirebaseHelper.getInstance().getCurrentUser();
        if (currentUser != null) {
            emailText.setText(currentUser.getEmail());
            profileViewModel.loadUserProfile(currentUser.getUid());
            // Mark that initial load is complete after a short delay
            view.postDelayed(() -> isInitialLoad = false, 500);
        }
    }

    private void observeViewModels() {
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                populateForm(user);
            }
        });

        profileViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (saveButton != null) {
                    saveButton.setEnabled(!isLoading);
                }
            }
        });

        profileViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                android.content.Context context = getContext();
                if (context != null) {
                    DialogHelper.showErrorDialog(context, "Error", errorMessage);
                }
            }
        });

        profileViewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success && !isInitialLoad) {
                android.content.Context context = getContext();
                if (context != null) {
                    DialogHelper.showSuccessDialog(context, "Success", "Profile updated successfully!", null);
                    // Reset the success flag after showing dialog
                    profileViewModel.resetUpdateSuccess();
                }
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
        android.content.Context context = getContext();
        if (context == null) return;
        
        String[] options = {"Camera", "Gallery"};
        new androidx.appcompat.app.AlertDialog.Builder(context)
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
        android.content.Context context = getContext();
        if (context == null) return;
        
        try {
            File imageFile = ImageHelper.createImageFile(context);
            imageUri = ImageHelper.getFileProviderUri(context, imageFile);
            cameraLauncher.launch(imageUri);
        } catch (Exception e) {
            DialogHelper.showErrorDialog(context, "Error", "Error opening camera");
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelection(Uri uri) {
        android.content.Context context = getContext();
        if (context == null) return;
        
        imageUri = uri;
        imageBytes = ImageHelper.uriToByteArray(context, uri);
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
            android.content.Context context = getContext();
            if (context != null) {
                DialogHelper.showErrorDialog(context, "Error", "User not authenticated");
            }
            return;
        }

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

