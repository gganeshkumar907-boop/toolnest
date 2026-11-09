# ToolNest Android app

This Android project wraps the published ToolNest website in a native Android WebView.

## Current setup
- Application ID: `com.toolnest.app`
- Version: `1.0` (versionCode 1), target API 36 (Android 16)
- Website: https://gganeshkumar907-boop.github.io/toolnest/
- Java 17, Android Gradle Plugin 8.10.1, Gradle 8.11.1
- GitHub Actions builds a debug APK and an unsigned release AAB.

## Build
Open the repository's **Actions** tab and run **ToolNest Android Build**, or push a change to the `toolnest-android-build` branch under `android/`.

When the workflow succeeds, download the `toolnest-android-build` artifact. It contains:
- A debug APK for installation/testing.
- An unsigned release AAB.

## Play Store signing
The release AAB produced by this starter workflow is not signed for Play upload. Before publishing, create and safely back up an upload key, configure the Android release signing Gradle block, and store the keystore/password values as GitHub Actions secrets. Never commit a keystore or passwords to this repository.

The Play Console registration fee and Play Console verification are separate from building/testing the app.
