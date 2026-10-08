# AImanage
Native Android device dashboard for Xiaomi HyperOS and Android 16.

## Current implemented scope
- Jetpack Compose dashboard with bottom navigation and left drawer
- Battery percentage, charge state, thermal status and device information
- App-management and permission-settings shortcuts
- Category pages for Apps, Thermal, Network, Protection and Settings

## Important limitations
No CPU governor control, third-party force-stop, VPN, DNS filtering, or silent autostart changes are implemented. The UI labels these as planned. The Android OS owns background restrictions and thermal throttling.

## Build
Open in Android Studio with JDK 17 and Android SDK 35. Gradle wrapper is not included; generate with `gradle wrapper --gradle-version 8.10.2` if needed. Run `gradle assembleDebug`.
