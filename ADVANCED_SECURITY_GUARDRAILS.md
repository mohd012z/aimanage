# AImanage Advanced: Google Play Protect and Guardrails

## Confirmed installation blocker

Google's developer guidance for **"App blocked to protect your device"**
specifies automatic blocking of internet-sideloaded packages that declare
a **Notification Listener**, SMS reading, or Accessibility access in selected
markets. AImanage Advanced declares a NotificationListenerService.
This behavior cannot legitimately be solved with an APK filename change,
different download manager, new debug key, hidden permission, or disabled
security scanning. The Standard build does not declare the listener.

Official source:
https://developers.google.com/android/play-protect/warning-dev-guidance

## Capabilities and exposure

- Standard: no Usage Access, no NotificationListenerService.
- Advanced: Usage Access and NotificationListenerService declared openly.
- Notification access requires a separate explicit Android user grant.
- Automation is disabled by default; only allowlisted app packages are eligible.
- AImanage must not collect SMS, credentials or personal notification content.
- Force stopping other apps, covert controls and network traffic interception
  are not implemented and should not be implied.

## CI security gates

The `scripts/check_apk_permissions.py` gate inspects **built APK manifests**
for unapproved permissions; it fails if Standard accidentally acquires Usage
Access/Notification Listener or if Advanced privileges silently change.
CI additionally validates debug APK signatures using SDK `apksigner`.
Passing these tests does not indicate Play Protect approval.

## Legitimate path for Advanced

1. Complete documented privacy/consent disclosures and accurate data safety
   declarations, including notification access and usage access.
2. Produce a controlled, non-debug release signed with a stable signing key.
   Keep release signing secrets outside source control.
3. Submit the sensitive Advanced functionality to Google Play developer review
   (for example, internal testing, subject to Play approval).
4. If misclassified after a full review, use Google's official Play Protect
   appeal process; attach the package ID, APK hash and exact warning.
5. Do not attempt sideload bypasses, disable Play Protect or conceal the
   declared permission. Preserve ordinary app features in Standard.

## Android policy

The notification listener is a powerful user-granted facility. Prefer
notification settings shortcuts when access to full notification content
is unnecessary. Make any future privileged workflow opt-in, comprehensible,
revocable and isolated from standard diagnostics.
