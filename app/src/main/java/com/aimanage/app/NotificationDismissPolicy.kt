package com.aimanage.app

/**
 * Conservative notification-dismiss rules. The user must opt in and choose
 * exact application packages. Critical categories are never auto-dismissed.
 *
 * This policy is pure Kotlin and can be validated without Android permissions.
 */
internal object NotificationDismissPolicy {
 private val protectedPackages = setOf(
  "com.android.dialer",
  "com.google.android.dialer",
  "com.android.phone",
  "com.android.systemui",
  "com.miui.securitycenter"
 )
 private val protectedCategories = setOf(
  "call", "alarm", "sys", "service", "transport", "reminder",
  "err", "missed_call", "navigation", "location_sharing"
 )

 fun canDismiss(
  automationEnabled: Boolean,
  sourcePackage: String,
  ownPackage: String,
  allowlistedPackages: Set<String>,
  ongoing: Boolean,
  foregroundService: Boolean,
  clearable: Boolean,
  category: String?,
  hasFullScreenIntent: Boolean,
  secretVisibility: Boolean
 ): Boolean {
  if (!automationEnabled || sourcePackage.isBlank() || sourcePackage == ownPackage) return false
  if (sourcePackage in protectedPackages || sourcePackage !in allowlistedPackages) return false
  if (ongoing || foregroundService || !clearable || hasFullScreenIntent || secretVisibility) return false
  if (category in protectedCategories) return false
  return true
 }
}
