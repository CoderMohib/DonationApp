# Donation App

A complete Android donation application built with Firebase backend, Material Design 3, and MVVM architecture pattern.

## Features

- **User Authentication**: Email/password authentication with Firebase Auth
- **Role-Based Access**: Separate dashboards for regular users and admins
- **Campaign Management**: Admins can create, edit, and delete donation campaigns
- **Donations**: Users can browse campaigns and make donations
- **Profile Management**: Users can update their profile information and profile picture
- **Real-time Updates**: Campaigns update in real-time using Firestore listeners
- **Image Upload**: Campaign and profile images with compression and Firebase Storage
- **Material Design 3**: Modern UI with Material Design 3 components

## Architecture

- **MVVM Pattern**: Separation of concerns with ViewModels and LiveData
- **Firebase Backend**: 
  - Firebase Authentication for user management
  - Cloud Firestore for database
  - Firebase Storage for images
- **Material Design 3**: Modern UI components and theming

## Setup Instructions

### 1. Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add an Android app to your project:
   - Package name: `com.example.donationapp`
   - Download `google-services.json`
4. Replace the placeholder `app/google-services.json` with your downloaded file

### 2. Firestore Database Setup

1. In Firebase Console, go to Firestore Database
2. Create a database in test mode (or production mode with security rules)
3. Create the following collections:
   - `users` - User profiles
   - `campaigns` - Donation campaigns
   - `donations` - Donation records

### 3. Firestore Security Rules

Add the following security rules to your Firestore database:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
      // Prevent role modification by users
      allow update: if request.auth != null && request.auth.uid == userId 
                    && !request.resource.data.diff(resource.data).affectedKeys().hasAny(['role']);
    }
    
    // Campaigns collection
    match /campaigns/{campaignId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && 
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
      allow update: if request.auth != null && 
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
      allow delete: if request.auth != null && 
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // Donations collection
    match /donations/{donationId} {
      allow read: if request.auth != null && 
                  (resource.data.userId == request.auth.uid || 
                   get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin');
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
    }
  }
}
```

### 4. Firebase Storage Setup

1. In Firebase Console, go to Storage
2. Create a storage bucket
3. Set up Storage Security Rules:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /campaigns/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
                   firestore.get(/databases/(default)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    match /profiles/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 5. Setting Admin Role

To set a user as admin:

1. Go to Firestore Database in Firebase Console
2. Navigate to `users` collection
3. Find the user document (by their Firebase Auth UID)
4. Edit the document and set the `role` field to `"admin"`

Alternatively, you can do this programmatically or through Firebase Admin SDK.

### 6. Build and Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project
4. Run on an emulator or physical device

## Dependencies

- Firebase BOM 33.7.0
- Firebase Auth
- Cloud Firestore
- Firebase Storage
- AndroidX Lifecycle (ViewModel, LiveData)
- Material Design 3
- Glide (for image loading)
- RecyclerView

## Project Structure

```
app/src/main/java/com/example/donationapp/
├── model/          # Data models (User, Campaign, Donation)
├── view/           # Activities (UI)
├── viewmodel/      # ViewModels (MVVM)
├── adapter/        # RecyclerView adapters
└── util/           # Utility classes (FirebaseHelper, Validator, etc.)
```

## Key Features Implementation

### Authentication
- Email/password authentication
- User registration with default "user" role
- Role-based dashboard redirection

### Campaign Management (Admin)
- Create campaigns with images
- Edit existing campaigns
- Delete campaigns
- Real-time campaign list updates

### Donations (Users)
- Browse all campaigns
- View campaign details
- Make donations with amount validation
- Atomic transaction updates for campaign collected amounts

### Profile Management
- Update name and phone
- Upload profile picture
- Logout functionality

## Notes

- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36
- Compile SDK: 36
- Java Version: 11

## License

This project is for educational purposes.

