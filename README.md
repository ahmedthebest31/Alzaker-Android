# Alzaker - الذاكر 📿

Your daily companion for dhikr and staying connected with your worship — now a fully native Android app.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=android)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-8.0%20%2B-3DDC84?logo=android)](https://www.android.com/)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

<a href="https://play.google.com/store/apps/details?id=com.ahmedsamy.alzaker">
  <img alt="Get it on Google Play" src="https://upload.wikimedia.org/wikipedia/commons/7/78/Google_Play_Store_badge_EN.svg" width="250"/>
</a>

## About ✨

Alzaker is a comprehensive Islamic app that helps you maintain dhikr throughout the day. It features a complete adhkar library, an advanced digital tasbih, background audio playback, smart audible and silent reminders, two home-screen widgets (a rotating dhikr widget and a tasbih counter widget), and full accessibility support — all with zero ads, zero tracking, and no account required.

This is the native rewrite (Kotlin + Jetpack Compose) of the previously published Expo app, keeping the same app identity and signing key so it ships as a seamless update.

## Features 🌟

### Comprehensive Adhkar Library 📖
- **267 adhkar and supplications** across **132 categories**
- Morning and evening adhkar, sleep adhkar, food adhkar, Quranic supplications, and more
- **Category filter** for quick access to any dhikr
- Each item shows its **repeat count** and opens a dedicated detail page with a built-in counter
- One-tap **audio playback** for items with audio

### Advanced Digital Tasbih 📿
- Smart counter with a **customizable goal**
- Haptic feedback on each tap
- **Celebration alert** when the goal is reached
- Instant reset

### Background Audio Playback 🎧
- Play adhkar audio **in the background** even with the screen locked or the app dismissed
- **Lock-screen and notification controls** (play / pause / stop)
- **Floating audio overlay** shown during playback
- Uses a foreground media service for reliable playback

### Voice and Text Reminders ⏰
- **Audible reminders** with built-in audio drops (14 local audio files)
- **Silent reminders** with a fresh random dhikr notification
- Configurable **intervals** for both reminder types
- **Do Not Disturb (quiet hours)** with 12-hour AM/PM format
- Supports periods that span midnight (e.g. 10 PM - 6 AM)
- Exact-alarm scheduling that **survives reboot**, plus boot re-scheduling
- Tapping a reminder copies the dhikr to the clipboard
- **Chinese-OEM friendly**: in-app guidance for autostart, battery optimization, and the exact-alarm permission (Xiaomi / Samsung / Huawei)

### Favorites System ⭐
- Save favorite adhkar for quick access
- One-tap toggle to add/remove

### Home-Screen Widgets 🧩
- **Dhikr widget** — a rotating dhikr that changes automatically with the same cadence and fade as the Home tab, plus a **copy button** that copies the displayed dhikr to the clipboard
- **Tasbih widget** — tap to count with haptic feedback, shares the same goal as the app's tasbih tab, shows a thin progress bar and a compact "N/M" counter, the count turns gold when the goal is reached (and keeps counting), and a reset button opens a confirmation dialog
- Both widgets follow the system color theme (Material You on Android 12+) and work on any launcher (stock RemoteViews layouts)

### Themes and Visual Customization 🎨
- **5 custom themes**: Default, Midnight, Nature, Royal, High Contrast
- **Font size adjustment** with a slider
- Full RTL design supporting Arabic
- Dark theme by default with an optional dynamic color (Material You) mode

### Accessibility ♿
- Full compatibility with **TalkBack** (screen reader)
- Every button has a descriptive label and live announcements for changes
- Haptic feedback and focus-visible indicators

### Privacy First 🔒
- No ads, no analytics, no tracking, no account
- All your data stays **on-device**
- Only requested permissions: notifications, exact alarms (Android 12+), and internet (only for streaming dhikr audio)

## Tech Stack 🛠️

- **Kotlin** with Coroutines and Flow
- **Jetpack Compose** (Material 3) with full RTL support
- **Clean Architecture**: ViewModels, Repository pattern, DataStore preferences
- **AlarmManager** exact alarms with self-rescheduling chains
- **Foreground service** + platform **MediaPlayer** and **MediaSession** for background audio
- **App Widgets** for the home-screen dhikr and tasbih counter
- **minSdk 26 / targetSdk 36**, fully offline-capable builds

## Project Structure 🗂️

```
Alzaker-Android/
├── app/
│   └── src/main/
│       ├── java/com/ahmedsamy/alzaker/
│       │   ├── audio/       # Foreground audio service + media session
│       │   ├── data/        # Data sources, repositories, DataStore
│       │   ├── reminder/    # Alarm scheduler, receivers, quiet hours
│       │   ├── ui/          # Screens, navigation, theme, components
│       │   ├── util/        # Haptics, permissions, time format, etc.
│       │   └── widget/      # Home-screen widgets (dhikr + tasbih)
│       ├── res/             # Assets, raw audio, font, drawables
│       └── AndroidManifest.xml
├── gradle/                  # Version catalog (locked, offline-capable)
└── build.gradle.kts
```

### Tabs 🧭

- **Home** 🏠 — random dhikr that changes automatically with copy / share / favorite actions
- **Tasbih** 📿 — digital prayer counter with goals
- **Adhkar** 📖 — full adhkar library with category filter and audio playback
- **Favorites** ⭐ — saved adhkar
- **Settings** ⚙️ — themes, font, reminders, quiet hours, support

## Getting Started 🚀

### Prerequisites
- JDK 21 or higher
- Android SDK Command-line Tools (compileSdk 36 / targetSdk 36)
- Git

### Build

No IDE required. Use the wrapper directly:

```bash
# Debug build
gradlew.bat :app:assembleDebug

# Run unit tests
gradlew.bat :app:testDebugUnitTest

# Offline (no network) — dependencies are locked in the version catalog
gradlew.bat :app:assembleDebug --offline
```

### Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Building for Production 📦

```bash
gradlew.bat :app:assembleRelease
```

The release build is minified with R8 and signed with the app's release key. Signing is configured via `signing.properties` (gitignored — never commit it):

1. Copy the example: `signing.properties.example` → `signing.properties`
2. Place your keystore at `app/release-key.jks`
3. Fill in the store password, key alias, and key password

Without `signing.properties` the release build still succeeds but produces an unsigned APK.

## Important Links 🔗

- [Google Play Store](https://play.google.com/store/apps/details?id=com.ahmedsamy.alzaker)
- [PayPal](https://www.paypal.com/paypalme/ahmedthebest31)
- [InstaPay](https://ipn.eg/S/ahmedsamyelkhouly/instapay/1KWwcR)

## Developer 👨‍💻

**Ahmed Samy** - [@ahmedthebest31](https://github.com/ahmedthebest31)

## License 📄

This project is licensed under the GNU General Public License v3.0 (GPLv3) - see the [LICENSE](LICENSE) file for details.

## Support 💚

If you like this app, please rate it on Google Play and share the good deed. JazakAllahu Khairan.
