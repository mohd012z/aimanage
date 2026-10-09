# Advanced release candidate and review runbook

**Purpose:** Produce a reproducible, properly signed Advanced APK and Google Play
Android App Bundle for legitimate developer review. This does **not** bypass
Play Protect, assert approval, or promise sideload installation.

## Required private configuration

In GitHub repository Settings > Secrets and variables > Actions, set all four
repository secrets (never commit them or paste their values into logs):

- `AIMANAGE_RELEASE_KEYSTORE_BASE64`: base64-encoded binary JKS/PKCS12 keystore
- `AIMANAGE_RELEASE_STORE_PASSWORD`: keystore password
- `AIMANAGE_RELEASE_KEY_ALIAS`: signing alias
- `AIMANAGE_RELEASE_KEY_PASSWORD`: private-key password

The keystore should be generated securely and backed up by its owner. Losing
it can prevent compatible updates. A public CI debug key is **not** an acceptable
substitute for a production key. Do not send signing keys to this chat.

## Build and review

1. Verify the Android manifest matches the app's actual capabilities and
   prepare accurate privacy, notification listener, usage-access and retention
   disclosures. Confirm explicit opt-in is functioning on real devices.
2. In GitHub Actions run **Advanced release candidate (manual)**. The workflow
   fails closed when any signing secret is missing.
3. It builds `assembleAdvancedRelease` and `bundleAdvancedRelease`, validates
   APK/AAB signatures and the Advanced APK's declared permissions, and uploads
   both artifacts for a short retention period. An artifact proves only build
   integrity, not Play approval.
4. Submit the AAB using your authorized Google Play developer account through
   Play Console testing/review. Follow Google's requirements regarding
   declaration of sensitive notification/usage capabilities. Do not distribute
   the release APK as Play-approved unless Google actually approves it.
5. If enhanced fraud protection still blocks a reviewed app, follow Google's
   official developer appeal guidance and include package name
   `com.aimanage.app.advanced`, exact warning, app version, and APK SHA-256.

## Data practices

The local notification listener currently checks package names and notification
metadata to apply allowlisted dismissal rules. It should not persist or transmit
notification message text. Settings grant is explicit and revocable. Default
automation is off. Usage statistics indicate foreground time and do not establish
per-app CPU load or actual battery drain.

Do not add background control, VPN, accessibility, contacts, SMS, or cloud
data collection without separate disclosure, permission audits and security tests.

Official reference:
https://developers.google.com/android/play-protect/warning-dev-guidance
