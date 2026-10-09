package com.aimanage.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDismissPolicyTest {
 private val allowed = setOf("com.example.promotions")

 private fun mayDismiss(
  enabled: Boolean = true,
  source: String = "com.example.promotions",
  own: String = "com.aimanage.app",
  packages: Set<String> = allowed,
  ongoing: Boolean = false,
  foreground: Boolean = false,
  clearable: Boolean = true,
  category: String? = "promo",
  fullScreen: Boolean = false,
  secret: Boolean = false,
  groupSummary: Boolean = false
 ) = NotificationDismissPolicy.canDismiss(
  automationEnabled=enabled,
  sourcePackage=source,
  ownPackage=own,
  allowlistedPackages=packages,
  ongoing=ongoing,
  foregroundService=foreground,
  clearable=clearable,
  category=category,
  hasFullScreenIntent=fullScreen,
  secretVisibility=secret,
  groupSummary=groupSummary
 )

 @Test fun explicitOptInAndExactPackageAllowlistRequired() {
  assertTrue(mayDismiss())
  assertFalse(mayDismiss(enabled=false))
  assertFalse(mayDismiss(packages=emptySet()))
  assertFalse(mayDismiss(source="com.example.promotions.fake"))
  assertFalse(mayDismiss(source=""))
 }

 @Test fun cannotDismissOwnOrProtectedSystemPackages() {
  assertFalse(mayDismiss(source="com.aimanage.app",packages=setOf("com.aimanage.app")))
  for (name in listOf("com.android.dialer","com.google.android.dialer","com.android.phone",
   "com.android.systemui","com.miui.securitycenter")) {
   assertFalse(mayDismiss(source=name,packages=setOf(name)))
  }
 }

 @Test fun criticalNotificationCategoriesAreAlwaysProtected() {
  for (category in listOf("call","alarm","sys","service","transport",
   "reminder","err","missed_call","navigation","location_sharing",
   "msg","email","event")) {
   assertFalse("Category $category should be protected",mayDismiss(category=category))
  }
  assertTrue(mayDismiss(category=null))
  assertTrue(mayDismiss(category="social"))
 }

 @Test fun ongoingOrNonClearableNotificationsAreProtected() {
  assertFalse(mayDismiss(ongoing=true))
  assertFalse(mayDismiss(foreground=true))
  assertFalse(mayDismiss(clearable=false))
 }

 @Test fun fullScreenAndSecretNotificationsAreProtected() {
  assertFalse(mayDismiss(fullScreen=true))
  assertFalse(mayDismiss(secret=true))
 }

 @Test fun groupSummaryCannotBeDismissedEvenWhenAppIsAllowlisted() {
  assertFalse(mayDismiss(groupSummary=true))
  assertTrue(mayDismiss(groupSummary=false))
 }

 @Test fun personalCommunicationsAndCalendarEventsStayVisible() {
  for (category in listOf("msg", "email", "event")) {
   assertFalse("Protected category: $category", mayDismiss(category=category))
  }
 }

 @Test fun unlistedAndProtectedCategoriesCannotBeOverriddenByAllowlist() {
  assertFalse(mayDismiss(source="com.example.banking",packages=allowed))
  assertFalse(mayDismiss(category="alarm",packages=allowed))
 }
}
