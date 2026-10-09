package com.aimanage.app

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Local, opt-in notification rules. Does not read, store or upload message text.
 * System and safety-critical notifications are excluded even when an app is listed.
 */
class AimanageNotificationListener : NotificationListenerService() {
 override fun onNotificationPosted(sbn: StatusBarNotification?) {
  if (sbn == null) return
  val prefs=getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE)
  val enabled=prefs.getBoolean("automation_enabled",false)
  if(!enabled) return
  val allow=prefs.getStringSet("dismiss_packages",emptySet())?.toSet() ?: emptySet()
  val notification=sbn.notification
  val permitted=NotificationDismissPolicy.canDismiss(
   automationEnabled=enabled,
   sourcePackage=sbn.packageName,
   ownPackage=packageName,
   allowlistedPackages=allow,
   ongoing=sbn.isOngoing,
   foregroundService=notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0,
   clearable=sbn.isClearable,
   category=notification.category,
   hasFullScreenIntent=notification.fullScreenIntent != null,
   secretVisibility=notification.visibility == Notification.VISIBILITY_SECRET
  )
  if(!permitted) return
  try { cancelNotification(sbn.key) } catch (_: SecurityException) { }
 }
}
