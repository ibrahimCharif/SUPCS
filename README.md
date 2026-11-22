# SPUCS — Version 2 (Android classroom demo)

**Smart Phone Usage Control System — SWE 401, Group 11**

Repository URL: [Insert clickable GitHub repository URL]

## Team Members

| Name | Student ID | Role |
|------|------------|------|
| Omar Mohamed Awadh | 1052433 | Face recognition |
| Youssef Noureldin Mohamed Elsayed Elseby | 1091341 | Usage control, project setup |
| ibrahim Khalil Charif | 1097811 | Account, login and main screen |

## Problem

Parents need a way to control and limit a child's smartphone usage to minimize child brain from roting , and to be sure the right child is the one using the device
 
## Key Features

- **Log In and Create Account** — new first screen of Version 2.
- **Camera** — enrolling and identifying a child uses the front camera, with a blink check (liveness).
- **AI** — the MobileFaceNet face-recognition model runs on the phone and recognises the child.
- **Everything else works as in Version 1:** add child, consent, daily limit, warnings, restriction, extra-time requests, parent lock and Next Day.
- **In-memory data** — closing the app clears the accounts you created, the children and the usage.

### What is new in the code

| Type | New or changed | What it does |
|------|----------------|--------------|
| `LoginActivity` | New screen | Log In or Create Account, then opens `MainActivity`. |
| `AccountManager` | New | `register(name, email, password, pin)`, `login(email, password)`, `logout()`, `getLoggedInParent()`. Checks the email, a password of at least 6 characters and a 4-digit PIN. |
| `FaceCaptureActivity` | New screen | Front camera. ML Kit finds one face and waits for a blink, then the face is cut out at 112 × 112 and sent back. |
| `OnDeviceFaceRecognitionAPI` | New (the AI) | Implements `FaceRecognitionAPI` with MobileFaceNet: the model turns a face into 128 numbers; two faces match when the cosine similarity is at least cos(74.18°). |
| `FaceRecognitionAPI` | Changed | The image is now a `Bitmap`; `match` gets the references of the paired parent's children. |
| `FaceRecognitionService` | Changed | Gets the API in its constructor; `enrollFace` and `identifyChild` take the camera picture. |
| `FaceTemplate` | Changed | Keeps only the reference and the time. The face numbers stay in the AI class; no photo is kept. |
| `ChildDevice` | Changed | `captureFace(screen, requestCode)` opens the camera screen. |
| `MainActivity` | Changed | Save Child opens the camera to scan the new child's face (with consent); Enroll Face (Camera) scans again later; Scan Face (Camera); Log Out. The PIN is the logged-in parent's PIN. |

`User`, `Account`, `Parent`, `Child`, `DeviceLock`, `DailyUsageRecord`, `ExtraTimeRequest` and `UsageController` are the same as in Version 1.

## Technologies

| Item | Version |
|------|---------|
| Android Studio | Rabbit 2, 2026.2.2 Patch 1 |
| Android Gradle Plugin / Gradle | 9.5.1 / 9.6.0 |
| Java language level | 17 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 29 (Android 10) |
| CameraX | 1.7.0 (camera picture) |
| ML Kit Face Detection | 16.1.7 (finds the face and the blink) |
| LiteRT | 2.1.0 (runs the AI model) |
| AI model | `app/src/main/assets/mobile_facenet.tflite` (MobileFaceNet, Qualcomm AI Hub, Apache-2.0) |

## Prerequisites

- Android Studio (Rabbit 2, 2026.2.2 Patch 1) with JDK 17 support.
- An Android 10 (API 29) or newer device or emulator **with a front camera**.
- Internet connection for the first Gradle sync (it downloads the libraries listed above).

## Configuration

- **Emulator camera:** in **Device Manager**, edit the virtual device and set the **front camera to `Webcam0`** so the emulator uses your PC's webcam.
- **Demo account** (or create your own in the app):

  | Email | Password | PIN |
  |-------|----------|-----|
  | parent@spucs.demo | demo1234 | 1234 |

- The AI model is already included at `app/src/main/assets/mobile_facenet.tflite`; no extra setup is needed.

## How to Run

1. Select the emulator (with webcam configured) or a physical Android device.
2. Press **Run** in Android Studio.
3. The app opens on the **Log In / Create Account** screen.

> Data is kept in memory only. Closing the app clears accounts, children and usage.

## How to Test

Follow this walkthrough:

1. **Create Account:** name, email, password (at least 6 characters), 4-digit PIN. You are logged in.
2. **Add Child** with your PIN and consent ticked, then **Save Child**. The camera opens at once to scan the child's face: look at the camera, blink once, tap **Capture**.
   - Without consent, the child is saved with no scan.
   - If the scan is cancelled or fails, the child stays saved and **Enroll Face (Camera)** scans the face later.
3. **Set Daily Limit** for the child.
4. **Child Device > Scan Face (Camera):** blink, tap **Capture**. The child becomes the active child.
   - A face that was not enrolled leaves no child active; **Select Child** with the PIN is the fallback.
5. Use the **1, 5 and 15 Minutes** buttons, request more time and approve it, as in Version 1.
6. **Log Out**, then log in again: your children are still there while the app stays open.
