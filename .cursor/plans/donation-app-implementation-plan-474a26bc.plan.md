<!-- 474a26bc-2272-4d32-be4e-d132bfcb8a49 667a5293-034f-4064-88b7-19560d782a7f -->
# Donation App - Complete Implementation Plan

## Project Structure

```
app/src/main/java/com/example/donationapp/
├── model/
│   ├── User.java
│   ├── Campaign.java
│   └── Donation.java
├── view/
│   ├── SplashActivity.java
│   ├── LoginActivity.java
│   ├── SignupActivity.java
│   ├── UserDashboardActivity.java
│   ├── AdminDashboardActivity.java
│   ├── CampaignDetailActivity.java
│   ├── DonateActivity.java
│   ├── AddCampaignActivity.java
│   ├── EditCampaignActivity.java
│   └── ProfileActivity.java
├── viewmodel/
│   ├── AuthViewModel.java
│   ├── CampaignViewModel.java
│   └── ProfileViewModel.java
├── adapter/
│   ├── CampaignAdapter.java
│   └── DonationAdapter.java
├── util/
│   ├── FirebaseHelper.java
│   ├── ImageHelper.java
│   ├── Validator.java
│   └── DialogHelper.java
└── MainActivity.java (redirects to SplashActivity)
```

## Implementation Steps

### 1. Firebase Setup & Dependencies

- Add Firebase dependencies to `app/build.gradle.kts`:
  - Firebase BOM (Bill of Materials) for version management
  - Firebase Auth
  - Firestore
  - Storage
  - Glide for image loading
- Add AndroidX Lifecycle dependencies:
  - lifecycle-viewmodel (for ViewModel support)
  - lifecycle-livedata (for LiveData support)
  - lifecycle-runtime (for lifecycle-aware components)
- Add RecyclerView dependency
- Update `libs.versions.toml` with Firebase versions and AndroidX Lifecycle versions
- Add permissions to AndroidManifest.xml:
  - INTERNET
  - READ_EXTERNAL_STORAGE (for Android < 13)
  - READ_MEDIA_IMAGES (for Android 13+)
  - CAMERA (for image picker)
  - WRITE_EXTERNAL_STORAGE (if needed for image saving)
- Create `google-services.json` placeholder (user will add their own)

### 2. Data Models

- **User.java**: name, email, role, profileImage, phone (optional)
- **Campaign.java**: id, title, description, goalAmount, collectedAmount, imageUrl, createdAt, createdBy
- **Donation.java**: id, campaignId, userId, amount, date

### 3. Firebase Helper & Utilities

- **FirebaseHelper.java**: Centralized Firebase operations
  - Auth methods (login, signup, logout) with error handling
  - Firestore CRUD for campaigns, users, donations with try-catch blocks
  - Storage upload/download methods with progress callbacks
  - Error code mapping to user-friendly messages
- **ImageHelper.java**: Image picker and compression utilities
  - Image compression before upload to reduce storage costs
  - Handle both camera and gallery selection
  - Proper image rotation handling
- **Validator.java**: Email, password, amount validation
  - Email format validation
  - Password strength requirements (min 6 characters)
  - Amount validation (positive numbers, decimal support, min/max limits)
  - Input sanitization to prevent injection attacks
- **DialogHelper.java**: Material dialogs for confirmations, errors, success
  - Standardized error dialogs with retry options
  - Loading dialogs for async operations
  - Success confirmation dialogs

### 4. ViewModels (MVVM)

- **AuthViewModel.java**: Handle authentication logic, role checking
  - Use LiveData for auth state, error messages, loading states
  - Expose MutableLiveData for UI observation
- **CampaignViewModel.java**: Campaign CRUD operations, real-time listeners
  - LiveData for campaigns list, loading states, error messages
  - Real-time Firestore listeners with proper cleanup
- **ProfileViewModel.java**: Profile update operations
  - LiveData for user profile data, update status, errors

### 5. Authentication Module

- **SplashActivity.java**: Check auth state, redirect by role
- **LoginActivity.java**: Email/password login with Material Design inputs
  - Loading indicator during login
  - Error handling with user-friendly messages
- **SignupActivity.java**: Registration with role="user" default, create Firestore user doc
  - Form validation
  - Password confirmation field
  - Error handling

### 6. Dashboard Activities

- **UserDashboardActivity.java**:
  - RecyclerView with CampaignAdapter
  - Campaign cards with image, title, goal, collected, description
  - "Donate Now" button per card
  - Bottom navigation: Home, Profile
  - SwipeRefreshLayout for pull-to-refresh
  - Empty state view when no campaigns
  - Loading indicator for initial load
- **AdminDashboardActivity.java**:
  - All UserDashboard features
  - FAB for "Add Campaign"
  - Long-press on cards for Edit/Delete options
  - Bottom navigation: Home, Profile
  - SwipeRefreshLayout for pull-to-refresh
  - Empty state view when no campaigns

### 7. Campaign Management (Admin)

- **AddCampaignActivity.java**:
  - Image picker (camera/gallery)
  - Form fields: title, description, goalAmount
  - Real-time form validation
  - Image compression before upload
  - Upload image to Firebase Storage with progress indicator
  - Save campaign to Firestore
  - Error handling and success feedback
- **EditCampaignActivity.java**:
  - Pre-fill form with existing data
  - Allow image replacement
  - Form validation
  - Update Firestore document
  - Error handling
- **Delete Campaign**: Confirmation dialog, delete Firestore doc + Storage image
  - Error handling if deletion fails
  - Success feedback

### 8. Donation Flow (User)

- **CampaignDetailActivity.java**: Full campaign details view
  - Display campaign image, title, description, progress bar
  - Show goal vs collected amount
  - "Donate Now" button
  - Empty state if campaign not found
  - Loading indicator
- **DonateActivity.java**:
  - Display campaign info
  - Amount input field with validation (min amount, max amount, decimal support)
  - Input sanitization
  - Loading indicator during donation processing
  - Use Firestore transaction to update collectedAmount (atomic operation)
  - Create donation log entry
  - Error handling for network failures, insufficient funds scenarios
  - Success confirmation with navigation back to dashboard

### 9. Profile Management

- **ProfileActivity.java**:
  - Display current user info
  - Edit name, phone fields
  - Profile image upload (Firebase Storage)
  - Image compression before upload
  - Update Firestore user document
  - Logout functionality
  - Loading indicators for updates
  - Error handling

### 10. UI Layouts (Material Design 3)

- All activities use MaterialToolbar, MaterialCardView, MaterialButton
- TextInputLayout for form fields with error messages
- ConstraintLayout for responsive design
- Material color scheme
- Ripple effects and proper spacing
- Progress indicators (ProgressBar/CircularProgressIndicator) for loading states
- Empty state views for RecyclerViews (no campaigns, no donations)
- SwipeRefreshLayout for pull-to-refresh on dashboards
- Skeleton screens or shimmer effects for initial loading (optional)

### 11. RecyclerView Adapters

- **CampaignAdapter.java**: Display campaigns in cards with Glide image loading
  - Click listeners for navigation
  - Long-press listeners for admin actions
  - Empty state handling
- **DonationAdapter.java**: Display user's donation history (if needed)

### 12. Navigation Flow

- SplashActivity → Check auth → LoginActivity or role-based dashboard
- Login → Fetch role from Firestore → Redirect to appropriate dashboard
- Campaign click → CampaignDetailActivity → DonateActivity
- Admin FAB → AddCampaignActivity
- Admin long-press → EditCampaignActivity or Delete dialog
- Proper back button handling
- Prevent back navigation after logout

### 13. Error Handling & User Feedback

- **Error Handling Strategy**:
  - Network error handling (no internet, timeout, server errors)
  - Firebase error code mapping (auth errors, permission denied, not found)
  - User-friendly error messages in DialogHelper
  - Retry mechanisms for failed operations
  - Logging errors for debugging (using Log, not user-facing)
- **Loading States**:
  - ProgressBar/CircularProgressIndicator for async operations
  - Disable buttons during operations to prevent double-submission
  - Skeleton screens for initial data loading (optional)
- **Empty States**:
  - Empty RecyclerView messages ("No campaigns yet", "No donations yet")
  - Illustrations or icons for empty states
- **Input Validation Feedback**:
  - Real-time validation with error messages under TextInputLayout
  - Visual feedback (red borders, error icons)
  - Disable submit buttons until form is valid

### 14. Security & Data Protection

- **Firestore Security Rules**:
  - Users can only read campaigns, write their own donations
  - Admins can create/update/delete campaigns
  - Users can only update their own profile
  - Document structure validation in rules
  - Prevent users from modifying role field
- **Input Sanitization**:
  - Sanitize all user inputs before storing in Firestore
  - Prevent XSS and injection attacks
  - Validate data types and ranges
- **Authentication Security**:
  - Secure password requirements (enforced in Validator)
  - Session management (Firebase Auth handles this)
  - Role-based access control (check role on every sensitive operation)

### 15. UX Enhancements

- **Pull-to-Refresh**: Implement SwipeRefreshLayout on dashboards
- **Image Optimization**: Compress images before upload (in ImageHelper)
- **Offline Support**: Consider Firestore offline persistence (optional, add if needed)
- **Form Validation**: Real-time validation with visual feedback
- **Navigation**: Proper back button handling, prevent back navigation after logout
- **Progress Indicators**: Show progress for image uploads, donations, profile updates
- **Smooth Animations**: Use Material Design transitions between activities

### 16. Resource Files (Strings, Colors, Themes)

- **strings.xml** - All text resources:
  - App name, activity titles
  - Button labels (Login, Signup, Donate, Add Campaign, Edit, Delete, Logout, Save, Cancel)
  - Form labels (Email, Password, Name, Phone, Title, Description, Goal Amount, Donation Amount)
  - Error messages (Invalid email, Password too short, Amount required, Network error, etc.)
  - Success messages (Login successful, Donation successful, Campaign added, etc.)
  - Empty state messages (No campaigns, No donations)
  - Navigation labels (Home, Profile)
  - Dialog messages (Confirm delete, Are you sure?, etc.)
  - Placeholder texts for input fields
  - Validation error messages

- **colors.xml** - Material Design 3 color scheme:
  - Primary color (brand color for app)
  - Secondary color
  - Tertiary color
  - Error color (for validation errors)
  - Surface colors (background, card backgrounds)
  - On-surface colors (text on surfaces)
  - Primary container, secondary container colors
  - Material Design 3 color tokens

- **themes.xml** - Theme customizations:
  - Customize colorPrimary, colorSecondary, colorTertiary
  - Set colorScheme for Material 3
  - Customize toolbar/action bar styles
  - Set default text colors
  - Configure ripple effects colors
  - Set card view styles
  - Configure button styles

- **dimens.xml** (create if needed):
  - Standard spacing values (padding, margin)
  - Text sizes
  - Corner radius for cards and buttons
  - Icon sizes

### 17. Documentation

- **README.md**: 
  - Setup instructions
  - Firebase configuration steps
  - How to set admin role manually in Firestore
  - Dependencies list
  - Build and run instructions
  - Firestore security rules example
- **Code Comments**: 
  - Complex logic explanations
  - Firebase operation comments
  - Security considerations noted in code

## Key Files to Create/Modify

**New Files (35+):**

- 9 Activity classes
- 3 ViewModel classes
- 3 Model classes
- 2 Adapter classes
- 4 Utility classes
- 12-15 layout XML files:
  - activity_splash.xml
  - activity_login.xml
  - activity_signup.xml
  - activity_user_dashboard.xml
  - activity_admin_dashboard.xml
  - activity_campaign_detail.xml
  - activity_donate.xml
  - activity_add_campaign.xml
  - activity_edit_campaign.xml
  - activity_profile.xml
  - item_campaign.xml (RecyclerView item)
  - item_donation.xml (RecyclerView item, if needed)
  - dialog_loading.xml (optional, for custom loading dialogs)
  - layout_empty_state.xml (reusable empty state)
- Updated AndroidManifest.xml
- Updated build.gradle.kts with Firebase and AndroidX dependencies
- Firestore security rules file (firestore.rules) - to be added to Firebase console

**Modified Files:**

- `app/build.gradle.kts`: Add Firebase dependencies, AndroidX Lifecycle, RecyclerView
- `gradle/libs.versions.toml`: Add Firebase library versions, AndroidX Lifecycle versions
- `app/src/main/AndroidManifest.xml`: Add permissions, activities
- `MainActivity.java`: Redirect to SplashActivity

## Technical Decisions

- **Architecture**: MVVM pattern for separation of concerns
- **Navigation**: Activities with Intent-based navigation (simpler for this scope)
- **Image Loading**: Glide library with caching
- **Database**: Firestore (default as specified) with offline persistence (optional)
- **Storage**: Firebase Storage for images with compression before upload
- **Role Management**: Stored in Firestore users collection, checked on login and before sensitive operations
- **Error Handling**: Centralized error handling with user-friendly messages
- **State Management**: LiveData for reactive UI updates
- **Security**: Firestore security rules + input validation + sanitization

### To-dos

- [x] 