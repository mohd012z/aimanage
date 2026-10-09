#!/usr/bin/env python3
"""Fail closed when Android APK privileges diverge from approved flavor policy.

Requires the Android SDK's aapt binary and both debug APKs built by Gradle.
This checks the compiled APK manifest, not only declarative source files.
"""
import os
import pathlib
import re
import subprocess
import sys

DENIED_PERMISSIONS = (
    "android.permission.READ_SMS",
    "android.permission.RECEIVE_SMS",
    "android.permission.SEND_SMS",
    "android.permission.READ_CALL_LOG",
    "android.permission.WRITE_CALL_LOG",
    "android.permission.READ_CONTACTS",
    "android.permission.RECORD_AUDIO",
    "android.permission.CAMERA",
    "android.permission.QUERY_ALL_PACKAGES",
)
USAGE = "android.permission.PACKAGE_USAGE_STATS"
LISTENER = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
LISTENER_SERVICE = "android.service.notification.NotificationListenerService"
APK_DIR = pathlib.Path("app/build/outputs/apk")
BUILD_TOOLS = pathlib.Path(os.environ.get("ANDROID_HOME", "")) / "build-tools"
candidates = sorted(BUILD_TOOLS.glob("*/aapt"))
if not candidates:
    sys.exit("GUARDRAIL RED: Android SDK aapt missing; cannot audit APK")
aapt = str(candidates[-1])


def output(*args):
    result = subprocess.run([aapt, *args], capture_output=True, text=True)
    if result.returncode:
        sys.exit(f"GUARDRAIL RED: aapt failed: {result.stderr}")
    return result.stdout


def inspect(flavor):
    paths = list((APK_DIR / flavor / "debug").glob("*.apk"))
    if len(paths) != 1:
        sys.exit(f"GUARDRAIL RED: expected one {flavor} debug APK, got {len(paths)}")
    apk = paths[0]
    permissions = output("dump", "permissions", str(apk))
    manifest = output("dump", "xmltree", str(apk), "AndroidManifest.xml")
    for permission in DENIED_PERMISSIONS:
        if permission in permissions:
            sys.exit(f"GUARDRAIL RED: unexpected dangerous permission {permission} in {flavor}")
    usage = USAGE in permissions
    listener = LISTENER_SERVICE in manifest or LISTENER in manifest
    if flavor == "standard":
        if usage or listener:
            sys.exit("GUARDRAIL RED: sensitive access leaked into Standard APK")
    else:
        if not usage or not listener:
            sys.exit("GUARDRAIL RED: Advanced capabilities changed; review manifest and documentation")
    if "android.permission.INTERNET" in permissions:
        sys.exit(f"GUARDRAIL RED: unexpected Internet permission in {flavor}")
    print(f"GUARDRAIL GREEN: {apk} — usage_access={usage}, listener={listener}, no unapproved permissions")


if __name__ == "__main__":
    for flavor_name in ("standard", "advanced"):
        inspect(flavor_name)
