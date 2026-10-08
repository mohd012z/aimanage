package com.aimanage.app

import android.content.Context
import android.content.Intent
import android.provider.Settings
import java.util.Locale

data class AssistantReply(val message:String, val settingsAction:String?=null, val proposedRule:Boolean?=null)

/** Transparent, offline command interpreter; does not pretend to be a language model. */
object DeviceAssistant {
 fun reply(context:Context, input:String):AssistantReply {
  val q=input.lowercase(Locale.ROOT).trim()
  if(q.isBlank()) return AssistantReply("Ask about battery, temperature, app activity, notification rules, or Android settings.")
  if(listOf("notification","notifikasi","alert").any { it in q }) {
   if(listOf("enable automation","turn on automation","aktif automasi").any { it in q })
    return AssistantReply("I can enable the existing notification auto-dismiss rules. This only affects apps you explicitly allowlisted. Confirm below.",proposedRule=true)
   if(listOf("disable automation","turn off automation","matikan automasi").any { it in q })
    return AssistantReply("I can disable notification auto-dismiss automation. Confirm below.",proposedRule=false)
   return AssistantReply("To read notifications, grant Notification Access. Automatic dismissal only works for user-selected packages and does not cover protected system notifications.",Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
  }
  if(listOf("temperature","thermal","hot","panas","cpu").any { it in q }) {
   val r=ActivityMonitor.report(context)
   return AssistantReply("Android thermal status: ${r.thermalStatus}. Foreground app usage does not reveal per-app CPU load. I cannot force-stop other apps or bypass thermal protection.")
  }
  if(listOf("battery","charging","bateri","cas").any { it in q }) {
   val b=DeviceReadings.battery(context)
   return AssistantReply("Battery: ${b.percent?.let { "$it%" } ?: "unavailable"}; temperature: ${b.temperatureC?.let { "$it °C" } ?: "unavailable"}. Hardware charging limits are not controllable by this app.",Settings.ACTION_BATTERY_SAVER_SETTINGS)
  }
  if(listOf("app","background","standby","aplikasi").any { it in q }) {
   val r=ActivityMonitor.report(context)
   return AssistantReply("Usage Access: ${if(r.usageAccessGranted) "granted" else "not granted"}. Recorded recent foreground apps: ${r.recentApps.size}. I can guide you to Android app controls but cannot silently stop background services.",Settings.ACTION_USAGE_ACCESS_SETTINGS)
  }
  if(listOf("brightness","display","refresh","screen","skrin").any { it in q })
   return AssistantReply("Android controls display brightness and refresh modes. Open Display settings to make changes.",Settings.ACTION_DISPLAY_SETTINGS)
  if(listOf("security","scam","telegram","phishing").any { it in q })
   return AssistantReply("Use Security Intelligence to check a URL or international number. No online scam verification or Telegram account access is active.")
  return AssistantReply("I can explain device diagnostics and open supported Android settings. Try: 'battery status', 'phone hot', 'background apps', or 'enable automation'.")
 }
 fun setNotificationAutomation(context:Context,enabled:Boolean) {
  context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE).edit().putBoolean("automation_enabled",enabled).apply()
 }
 fun settingsIntent(action:String)=Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
