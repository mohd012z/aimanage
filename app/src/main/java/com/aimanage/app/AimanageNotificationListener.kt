package com.aimanage.app

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Local-only notification automation. Disabled until the user grants
 * Notification Access AND explicitly enables rules inside AImanage.
 * Does not store notification contents.
 */
class AimanageNotificationListener : NotificationListenerService() {
 override fun onNotificationPosted(sbn: StatusBarNotification?) {
  if (sbn == null || sbn.packageName == packageName) return
  val prefs = getSharedPreferences("aimanage_rules", Context.MODE_PRIVATE)
  if (!prefs.getBoolean("automation_enabled", false)) return
  val allow = prefs.getStringSet("dismiss_packages", emptySet()) ?: emptySet()
  val protected = setOf("com.android.dialer","com.google.android.dialer","com.android.phone","com.android.systemui")
  if (sbn.packageName in protected || sbn.packageName !in allow) return
  if (sbn.isOngoing || sbn.notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0) return
  try { cancelNotification(sbn.key) } catch (_: SecurityException) { }
 }
}
