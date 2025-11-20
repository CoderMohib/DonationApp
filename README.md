# 💝 Donation App

A modern, feature-rich Android donation application built with Firebase backend, Material Design 3, and MVVM architecture pattern. This app enables organizations to create donation campaigns and allows users to browse and contribute to causes they care about.

![Android](https://img.shields.io/badge/Android-24%2B-green.svg)
![Java](https://img.shields.io/badge/Java-11-orange.svg)
![Firebase](https://img.shields.io/badge/Firebase-33.7.0-yellow.svg)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-blue.svg)
![License](https://img.shields.io/badge/License-Educational-purple.svg)

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Screenshots](#-screenshots)
- [Prerequisites](#-prerequisites)
- [Setup Instructions](#-setup-instructions)
- [Project Structure](#-project-structure)
- [Key Components](#-key-components)
- [Firebase Configuration](#-firebase-configuration)
- [Security Rules](#-security-rules)
- [Dependencies](#-dependencies)
- [Building and Running](#-building-and-running)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### 🔐 Authentication
- **Email/Password Authentication** - Secure user registration and login
- **Password Recovery** - Forgot password functionality with email reset
- **Role-Based Access Control** - Separate dashboards for users and admins
- **Session Management** - Automatic authentication state handling
- **Profile Management** - Update name, phone, and profile picture

### 📢 Campaign Management (Admin)
- **Create Campaigns** - Add new donation campaigns with images, goals, and descriptions
- **Edit Campaigns** - Update existing campaign details
- **Delete Campaigns** - Remove campaigns with confirmation dialogs
- **Real-time Updates** - Campaign list updates automatically using Firestore listeners
- **Image Upload** - Support for campaign images with compression

### 💰 Donations (Users)
- **Browse Campaigns** - View all available donation campaigns
- **Campaign Details** - See full campaign information with progress tracking
- **Make Donations** - Contribute to campaigns with amount validation
- **Transaction Safety** - Atomic updates ensure data consistency
- **Progress Tracking** - Visual progress indicators for each campaign

### 🎨 User Experience
- **Material Design 3** - Modern, beautiful UI following Material Design guidelines
- **Bottom Navigation** - Easy navigation between Home, Donation History, and Profile
- **Fragment-based Architecture** - Smooth transitions and efficient memory usage
- **Swipe to Refresh** - Pull-to-refresh functionality for campaign lists
- **Empty States** - Helpful messages when no data is available
- **Error Handling** - User-friendly error messages throughout the app
- **Loading Indicators** - Clear feedback during async operations

---

## 🏗️ Architecture

This project follows the **MVVM (Model-View-ViewModel)** architecture pattern with **Navigation Components** and **Fragment-based UI**, ensuring separation of concerns and maintainability.

```
┌─────────────────────────────────────────────────────────────┐
│                         View Layer                          │
│  (MainActivity, AdminMainActivity, Fragments, Adapters)     │
│  - Bottom Navigation with Fragment transitions             │
│  - Observes ViewModel LiveData                             │
│  - Handles UI rendering and user interactions              │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ Observes
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                        │
│  (AuthViewModel, CampaignViewModel, ProfileViewModel)      │
│  - Business logic and state management                     │
│  - Exposes LiveData for UI observation                     │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ Uses
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                      Repository Layer                       │
│  (FirebaseHelper)                                           │
│  - Centralized Firebase operations                         │
│  - Handles authentication, Firestore, and Storage          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ Communicates with
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                      Firebase Backend                       │
│  - Authentication, Firestore, Storage                      │
└─────────────────────────────────────────────────────────────┘
```

### Key Principles:
- **Separation of Concerns**: UI, business logic, and data access are clearly separated
- **Navigation Components**: Uses AndroidX Navigation for fragment transitions
- **Reactive Programming**: Uses LiveData for reactive UI updates
- **Single Source of Truth**: Firebase serves as the central data source
- **Testability**: ViewModels can be tested independently of UI

---

## 📸 Screenshots

> **Note**: Add screenshots of your app here to showcase the UI

### Authentication Screens
- Splash Screen
- Login Screen
- Signup Screen
- Forgot Password Screen

### User Screens (Bottom Navigation)
- Home Fragment - Browse donation campaigns
- Donation History Fragment - View past donations
- Profile Fragment - Manage profile

### Admin Screens (Bottom Navigation)
- Admin Home Fragment - Manage campaigns
- Profile Fragment - Admin profile

### Other Screens
- Campaign Details
- Donation Form
- Add/Edit Campaign (Admin)

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio** - Hedgehog (2023.1.1) or later
- **JDK 11** or later
- **Android SDK** - API 24 (Android 7.0) minimum
- **Firebase Account** - For backend services
- **Git** - For version control

---

## 🚀 Setup Instructions

### Step 1: Clone the Repository

```bash
git clone https://github.com/CoderMohib/DonationApp.git
cd DonationApp
```

### Step 2: Firebase Configuration

1. **Create Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Add project" and follow the setup wizard
   - Enable Google Analytics (optional)

2. **Add Android App**
   - In Firebase Console, click "Add app" → Android
   - Package name: `com.example.donationapp`
   - Download `google-services.json`
   - Place it in `app/` directory (replace the existing file)

3. **Enable Firebase Services**
   - **Authentication**: Enable Email/Password sign-in method
   - **Firestore Database**: Create database in test mode (or production with security rules)
   - **Storage**: Create storage bucket (optional, for image uploads)

### Step 3: Firestore Database Setup

1. Navigate to **Firestore Database** in Firebase Console
2. Create database (choose test mode for development)
3. The following collections will be created automatically:
   - `users` - User profiles and roles
   - `campaigns` - Donation campaigns
   - `donations` - Donation records

### Step 4: Configure Security Rules

See [Security Rules](#-security-rules) section below for detailed Firestore and Storage rules.

### Step 5: Set Admin Role

To create an admin user:

1. Register a user through the app
2. Go to Firestore Console → `users` collection
3. Find the user document (by Firebase Auth UID)
4. Edit the document and set `role` field to `"admin"`

Alternatively, use Firebase Admin SDK for programmatic role assignment.

### Step 6: Build and Run

1. Open the project in Android Studio
2. Sync Gradle files (File → Sync Project with Gradle Files)
3. Connect an Android device or start an emulator
4. Click "Run" (▶️) or press `Shift + F10`

---

## 📁 Project Structure

```
app/src/main/java/com/example/donationapp/
├── model/              # Data models
│   ├── User.java       # User model with role and profile info
│   ├── Campaign.java   # Campaign model with goal tracking
│   └── Donation.java   # Donation model
│
├── view/               # UI Activities
│   ├── SplashActivity.java          # App entry point
│   ├── LoginActivity.java           # User authentication
│   ├── SignupActivity.java          # User registration
│   ├── ForgotPasswordActivity.java  # Password recovery
│   ├── MainActivity.java            # User dashboard with bottom navigation
│   ├── AdminMainActivity.java       # Admin dashboard with bottom navigation
│   ├── CampaignDetailActivity.java  # Campaign details view
│   ├── DonateActivity.java          # Donation form
│   ├── AddCampaignActivity.java     # Create campaign (admin)
│   ├── EditCampaignActivity.java    # Edit campaign (admin)
│   ├── ProfileActivity.java         # Full-screen profile management
│   ├── AdminDashboardActivity.java  # Legacy admin activity
│   └── UserDashboardActivity.java   # Legacy user activity
│
├── fragment/           # UI Fragments
│   ├── HomeFragment.java            # User home with campaigns list
│   ├── ProfileFragment.java         # User profile management
│   ├── DonationHistoryFragment.java # User donation history
│   └── AdminHomeFragment.java       # Admin campaign management
│
├── viewmodel/          # ViewModels (MVVM)
│   ├── AuthViewModel.java      # Authentication logic
│   ├── CampaignViewModel.java  # Campaign operations
│   └── ProfileViewModel.java   # Profile management
│
├── adapter/            # RecyclerView adapters
│   ├── CampaignAdapter.java   # Campaign list adapter
│   └── DonationAdapter.java   # Donation history adapter
│
└── util/               # Utility classes
    ├── FirebaseHelper.java  # Firebase operations wrapper
    ├── Validator.java       # Input validation
    ├── ImageHelper.java     # Image processing
    ├── DialogHelper.java    # Dialog utilities
    └── WindowInsetsHelper.java  # Window insets handling
```

---

## 🔑 Key Components

### Models

#### User Model
- Stores user information: name, email, role, profile image, phone
- Role-based access: `"user"` or `"admin"`
- Helper method: `isAdmin()` for role checking

#### Campaign Model
- Campaign details: title, description, goal amount, collected amount
- Progress calculation: `getProgressPercentage()`
- Goal tracking: `isGoalReached()`

#### Donation Model
- Links user, campaign, and donation amount
- Timestamp tracking for donation history

### ViewModels

#### AuthViewModel
- Handles sign in, sign up, and sign out
- Manages authentication state
- Loads user profile data

#### CampaignViewModel
- Real-time campaign list updates via Firestore listeners
- CRUD operations for campaigns
- Campaign loading and selection

#### ProfileViewModel
- User profile loading and updates
- Profile image management
- Name and phone updates

### Utilities

#### FirebaseHelper
- Singleton pattern for Firebase operations
- Centralized error handling
- User-friendly error messages
- Transaction support for atomic operations

#### Validator
- Input validation for forms
- Email, password, amount validation
- Input sanitization for security

#### ImageHelper
- Image compression and resizing
- EXIF rotation handling
- URI to byte array conversion

---

## 🔒 Security Rules

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      // Anyone authenticated can read user profiles
      allow read: if request.auth != null;
      
      // Users can only write their own profile
      allow write: if request.auth != null && request.auth.uid == userId;
      
      // Prevent role modification by users
      allow update: if request.auth != null && request.auth.uid == userId 
                    && !request.resource.data.diff(resource.data).affectedKeys().hasAny(['role']);
    }
    
    // Campaigns collection
    match /campaigns/{campaignId} {
      // Anyone authenticated can read campaigns
      allow read: if request.auth != null;
      
      // Only admins can create or delete campaigns
      allow create: if request.auth != null && 
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
      allow delete: if request.auth != null && 
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
      
      // Admins can update any field, authenticated users can only update collectedAmount
      allow update: if request.auth != null && (
                    // Admin can update anything
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin' ||
                    // Regular users can only update collectedAmount field (for donations)
                    (request.resource.data.diff(resource.data).affectedKeys().hasOnly(['collectedAmount']) &&
                     request.resource.data.collectedAmount is number &&
                     request.resource.data.collectedAmount >= (resource.data.collectedAmount == null ? 0.0 : resource.data.collectedAmount))
                   );
    }
    
    // Donations collection
    match /donations/{donationId} {
      // Users can read their own donations, admins can read all
      allow read: if request.auth != null && 
                  (resource.data.userId == request.auth.uid || 
                   get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin');
      
      // Users can only create donations for themselves
      allow create: if request.auth != null && 
                    request.resource.data.userId == request.auth.uid;
    }
  }
}
```

### Firebase Storage Security Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Campaign images - read for all authenticated users, write for admins only
    match /campaigns/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
                   firestore.get(/databases/(default)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // Profile images - users can read/write their own images
    match /profiles/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## 📦 Dependencies

### Core Android Libraries
- **AndroidX AppCompat** - Backward compatibility
- **Material Design 3** - Modern UI components
- **ConstraintLayout** - Flexible layouts
- **RecyclerView** - Efficient list rendering
- **SwipeRefreshLayout** - Pull-to-refresh functionality

### Architecture Components
- **Lifecycle ViewModel** - MVVM architecture support
- **Lifecycle LiveData** - Reactive data observation
- **Lifecycle Runtime** - Lifecycle-aware components

### Firebase
- **Firebase BOM 33.7.0** - Bill of Materials for version management
- **Firebase Authentication** - User authentication
- **Cloud Firestore** - NoSQL database
- **Firebase Storage** - File storage (optional)

### Image Loading
- **Glide** - Image loading and caching
- **Picasso** - Alternative image loading library

### Testing
- **JUnit** - Unit testing
- **Espresso** - UI testing

See `gradle/libs.versions.toml` for exact versions.

---

## 🛠️ Building and Running

### Build Requirements
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Java Version**: 11

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Running on Device

1. Enable **Developer Options** on your Android device
2. Enable **USB Debugging**
3. Connect device via USB
4. Run the app from Android Studio or install the APK

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit your changes** (`git commit -m 'Add some AmazingFeature'`)
4. **Push to the branch** (`git push origin feature/AmazingFeature`)
5. **Open a Pull Request**

### Code Style Guidelines
- Follow Java naming conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Keep methods focused and small
- Follow MVVM architecture patterns

---

## 📝 License

This project is for **educational purposes** only. Feel free to use it as a learning resource or starting point for your own projects.

---

## 🙏 Acknowledgments

- Firebase team for excellent backend services
- Material Design team for beautiful UI guidelines
- Android community for continuous improvements

---

## 📞 Support

For issues, questions, or suggestions:
- **GitHub Issues**: [Create an issue](https://github.com/CoderMohib/DonationApp/issues)
- Check existing issues for solutions
- Review [Firebase documentation](https://firebase.google.com/docs) for backend questions
- Contact: CoderMohib

---

**Made with ❤️ for the community**
