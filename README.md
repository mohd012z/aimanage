# AImanage

Experimental native Android device diagnostics and opt-in automation for Android 10+ and Xiaomi HyperOS. Built with Kotlin and Jetpack Compose.

## Implemented features

- Device dashboard with battery, thermal status, device information and system-settings shortcuts
- Battery readings, battery-side charging-power estimates and display diagnostics (when Android exposes sensors)
- Usage Access-based foreground app history; **not** other apps' CPU or battery measurements
- Offline, rule-based device assistant and basic URL/phone-number screening (not a live reputation service)
- Opt-in Notification Access automation for an explicit list of app package names
- Local-only device learning samples (capped at 168 entries, minimum 15 minutes between consecutive captures)
- Optional best-effort WorkManager sampling, nominally every 30 minutes; Doze and OEM policies can delay it
- Optional battery/thermal alerts subject to permission and a two-hour cooldown

## Notification safety

Notification Access is sensitive. It must be granted manually in Android Settings. Notification auto-dismiss remains disabled unless the user enables it and explicitly adds exact package names in AImanage. The app excludes its own notifications, system/dialer/security packages, ongoing and non-clearable notifications, foreground-service notifications, full-screen notifications, secret-visibility notifications, and alarm/call/reminder/system/error/navigation categories. Do not add packages whose notifications you must retain. Disable automation immediately if unexpected dismissal occurs.

## Data and permission boundaries

The app stores its sample history and settings in on-device SharedPreferences. It does not implement external history uploads. Android automatic app-data backup is disabled via the manifest, although OEM transfer behavior can vary. Clearing app storage or uninstalling can remove history. For diagnostics, consent is required before notification or usage access; POST_NOTIFICATIONS permission is only requested when health alerts are enabled.

**Not implemented:** root-only controls, CPU governor or hardware cooling adjustment, third-party force-stop, silent autostart policy changes, per-app network blocking, DNS filtering, or VPN. Android and Xiaomi control these OS-owned features. Planned screens do not implement those capabilities.

## Build and CI

Use JDK 17, Android SDK 35, and Gradle 8.9 with Android Gradle Plugin 8.7.3. A Gradle wrapper is not committed; when working locally, install Gradle 8.9 or generate a matching wrapper.

```bash
gradle :app:testDebugUnitTest
gradle :app:lintDebug
gradle :app:assembleDebug
```

GitHub Actions runs policy unit tests and Android lint before the debug APK build and uploads the APK artifact after success. Passing these checks does **not** mean that notification behavior and OEM background policies have been verified on physical devices. Before distributing a release, manually verify permissions, notifications, clock changes, app upgrades, and real-device battery behavior.

Version: 0.1.0 (experimental).
