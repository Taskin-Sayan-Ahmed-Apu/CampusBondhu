# CampusBondhu - Smart Campus Android App

CampusBondhu is a complete, Java-based Android application designed to digitize campus operations. 

## Features
- Role-Based Access (Student, Teacher, Guardian, Admin)
- Smart QR & Face Attendance
- Notice Board
- Library Management (Book QR)
- Demo Wallet & Canteen Pre-order
- Bus Tracking (OpenStreetMap)
- Study Hub (Link-based PDFs)
- Marketplace
- CGPA Calculator & Routine
- SOS Alerts & Guardian Dashboard
- Rule-Based AI Assistant

## Setup Instructions

### 1. Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com/) and create a project.
2. Add an Android app with package name `com.campusbondhu`.
3. Download `google-services.json` and place it in the `app/` folder.
4. Enable **Authentication** (Email/Password).
5. Enable **Firestore Database** (Start in Production Mode).
6. Copy the contents of `firestore.rules` into Firestore Rules and Publish.
7. Copy the contents of `firestore.indexes.json` into Firestore Indexes and Publish.

### 2. Cloudinary Setup (For Face Images)
Since Firebase Storage requires a paid plan, we use Cloudinary for free image hosting.
1. Create a free account at [Cloudinary](https://cloudinary.com/).
2. Get your `Cloud Name`.
3. Create an **Unsigned Upload Preset**.
4. Open `FaceRegisterActivity.java` and replace `CLOUD_NAME` and `UPLOAD_PRESET` with your details.

### 3. Build and Run
1. Open the project in Android Studio.
2. Sync Gradle.
3. Build and Run on an Emulator or Physical Device.

## Demo Accounts
- **Admin:** admin@test.com / 123456
- **Teacher:** teacher@test.com / 123456
- **Student:** student@test.com / 123456
- **Guardian:** guardian@test.com / 123456
