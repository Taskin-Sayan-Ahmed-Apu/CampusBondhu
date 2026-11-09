# 🎓 CampusBondhu - Smart Campus Application

CampusBondhu is a comprehensive, fully functional Java-based Android application designed to digitize and streamline campus operations. It connects four key stakeholders of a university: **Students, Teachers, Guardians, and Administrators** into a single, secure ecosystem.

## 🚀 Key Features

### 🔐 1. Role-Based Authentication
- Secure login and registration using **Firebase Authentication**.
- Four distinct roles (Student, Teacher, Guardian, Admin) with specific permissions and dashboards.

### 📷 2. Smart QR & Face Attendance
- **Teachers** can generate real-time QR codes for attendance sessions with custom expiry times.
- **Students** scan the QR code and verify their identity using **ML Kit Face Detection** (Demo level).
- Backend validation prevents duplicate attendance and expired sessions.

### 📚 3. Digital Library
- **Admin** can add books and scan barcodes.
- **Students** can search, issue, and return books. Includes due-date calculation and My Loans history.

### 💰 4. Demo Wallet & Canteen Pre-order
- A fully functional **Demo Wallet** (No real money) to handle transactions.
- **Students** can pre-order food from the canteen and get a unique Pickup Code.
- **Admin/Canteen Staff** can verify the code and mark orders as collected.

### 🚌 5. Live Bus Tracking (Real-Time)
- Uses **Firebase Realtime Database (RTDB)** for zero-latency location updates.
- **Students** can view the live bus location on an **OpenStreetMap** (osmdroid) interface.
- **Students** can also view their own current GPS location mapped directly from their device.

### 🆘 6. Emergency SOS System
- A prominent SOS button that triggers an alert with GPS coordinates.
- Uses **Android Foreground Services** to keep the alert active with a persistent notification and alarm sound.
- Notifies the Admin and linked Guardian immediately.

### 📢 7. Notice Board & Notifications
- **Teachers/Admins** can publish notices (General, Exam, Cancellation) with an "Important" flag.
- In-app dynamic UI updates with sound and vibration alerts for important notices.

### 🛒 8. Campus Marketplace
- **Students** can list items for sale.
- **Admins** can moderate and delete inappropriate listings.

### 📊 9. CGPA Calculator & Routine
- **Admin** configures courses (including Lab credits like 0.75, 1.5).
- **Students** select grades to calculate their GPA instantly.
- Dynamic weekly class routines.

### 📖 10. Study Hub & AI Assistant
- Central repository for sharing notes and past questions (via URLs).
- **Rule-based AI Chatbot** that helps students navigate app data (Notices, Routine, Library).

---

## 🛠️ Tech Stack & Architecture

- **Language:** Java
- **UI:** XML, Material Design Components
- **Architecture:** MVVM (Model-View-ViewModel) with Repository Pattern
- **Backend & Cloud:**
  - **Firebase Authentication** (User Login/Register)
  - **Cloud Firestore** (Static app data: Users, Books, Notices, etc.)
  - **Firebase Realtime Database (RTDB)** (High-frequency Live Bus Tracking)
- **External APIs & Libraries:**
  - **OpenStreetMap (osmdroid):** Free mapping solution for Bus Tracking.
  - **Cloudinary API:** Free image storage for profile pictures (via OkHttp).
  - **ML Kit & CameraX:** Face detection during attendance.
  - **ZXing:** QR Code generation and scanning.
  - **Shimmer Layout:** Modern loading animations.

---

## 📱 Android Core Components Demonstrated

- **Multi-Activity Navigation** & Explicit/Implicit Intents.
- **View Binding:** Used across all Activities to prevent NullPointerExceptions.
- **RecyclerView & Adapters:** For dynamic list rendering (Notices, Books, Canteen).
- **Runtime Permissions:** Camera, Location, and Notifications.
- **Broadcast Receivers:** Detects Airplane Mode and Battery Low system events.
- **Foreground Services:** For continuous SOS alert tracking.
- **Content Providers:** Used to pick images from the device gallery (`MediaStore`).

---

## ⚙️ Setup Instructions

### 1. Firebase Setup
1. Create a project on [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with package name `com.campusbondhu`.
3. Download `google-services.json` and place it in the `app/` folder.
4. Enable **Authentication** (Email/Password).
5. Enable **Firestore Database** (Start in Production Mode).
6. Enable **Realtime Database** (Start in Test Mode).
7. Apply the provided `firestore.rules` and Realtime Database rules.

### 2. Cloudinary Setup (For Images)
Since Firebase Storage requires a paid plan, we use Cloudinary for free image hosting.
1. Create a free account at [Cloudinary](https://cloudinary.com/).
2. Get your `Cloud Name`.
3. Create an **Unsigned Upload Preset**.
4. Open `FaceRegisterActivity.java` and `EditProfileActivity.java`.
5. Replace `CLOUD_NAME` and `UPLOAD_PRESET` with your details.

### 3. Build and Run
1. Open the project in Android Studio.
2. Sync Gradle and wait for dependencies to download.
3. Build and Run on an Emulator or Physical Device.

---

## 👨‍💻 Developer
**Taskin Sayan Ahmed Apu**  
*Department of Computer Science & Engineering*

---
*This project was developed as a complete academic demonstration of Android development, covering UI/UX, backend integration, and core Android system components.*
