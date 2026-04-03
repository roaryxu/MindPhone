# MindPhone 📱

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2023.08.00-green.svg)](https://developer.android.com/jetpack/compose)

**MindPhone** is a premium, high-performance, minimalist Android launcher designed to reclaim your focus. By stripping away distractions and focusing on a clean, search-driven interface, MindPhone turns your smartphone back into a tool, not a slot machine.

![MindPhone Mockup](https://raw.githubusercontent.com/username/MindPhone/main/assets/mockup.png)

## ✨ Features

- **Distraction-Free Home Screen**: A bold digital clock and minimalist battery indicator are all you see at the top, with notifications hidden by default (pull down to see them).
- **App Drawer**: Instant-select search bar upon tapping the drawer icon. Start typing as soon as you open the drawer and be purposeful in what you're using your phone for.
- **Persistent Favourites**: Keep your focused apps just one tap away from the default home screen. Manage your Favourites with the white dots next to each app name in the drawer.
- **Glassmorphic Quick-Dock**: Access Phone, Messages, Browser, Camera and a hold-to-exit button (allowing you to return to your default home launcher) at the bottom of the screen.
- **Privacy Only**: No telemetry, no tracking, and no internet permission required, at any time.
- **Clean Aesthetics**: Built with a "Warm Sienna" (#C48B6C) accent palette on true black.

## 🚀 Getting Started

### Prerequisites
- Android 8.0 (Oreo) or higher (API level 26+)
- Android Studio Hedgehog or newer (for building from source)

### Installation

#### 📦 Direct APK (Easiest for Mobile)
If you're viewing this page on your Android device, you can install MindPhone immediately:
1. Navigate to the [**Releases**](https://github.com/yourusername/MindPhone/releases) page.
2. Download the latest `app-debug.apk` directly to your phone.
3. Open the downloaded file. If prompted, allow "Install from Unknown Sources" for your browser.
4. Once installed, press the **Home** button on your device and select **MindPhone** as your default launcher.

#### 🛠 Build from Source
1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/MindPhone.git
   ```
2. Open the project in **Android Studio**.
3. Build the project and run it on your device/emulator.
4. Set **MindPhone** as your default launcher in Android Settings.

## 🛠 Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture with Repository Pattern
- **Persistence**: SharedPreferences (for lightweight state management)
- **Concurrency**: Kotlin Coroutines & Flow

## 🎨 Design Philosophy
MindPhone follows a "Focus-First" design philosophy:
- **No Icons on Home**: Text-based lists reduce the visual "pull" of addictive apps.
- **Haptic Feedback**: Subtle vibrates (where supported) for navigation.
- **Warm Accents**: Using `#C48B6C` (Warm Sienna) to provide a soft, premium feel without the eye strain of harsh blues or greens.

## 🤝 Contributing
Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.

---
*Completely vibe-coded and sharing for free because focus belongs to everyone.*
