# AImanage Android APK editions

## Standard (recommended for initial testing)

Artifact: `app-standard-debug.apk`. No PACKAGE_USAGE_STATS permission and
no NotificationListenerService registered. It retains local battery and thermal
diagnostics, offline safety checks, and opt-in local health monitoring.
Notification automation and per-app usage history are unavailable.

## Advanced (optional sensitive permissions)

Artifact: `app-advanced-debug.apk`. Separate application ID
`com.aimanage.app.advanced`; includes Usage Access and Notification Access
integration, both requiring explicit user action in Android Settings.

## Distribution and Play Protect

These are development/debug APKs, not Play-reviewed production releases.
Removing optional privileges may reduce risk indicators but **does not
guarantee** approval by Play Protect. If blocked, leave Play Protect enabled
and seek a security review through the normal Play Protect/developer process.
Never circumvent the warning or present debug APKs as publicly approved.
