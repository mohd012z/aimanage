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
  if(listOf("sleep","overnight","bedtime","while sleeping","screen off","standby mode").any { it in q })
   return AssistantReply("During sleep, Android Doze and App Standby may defer background tasks, but calls, alarms and high-priority notifications can still operate. Use Sleep Check to record battery percentage before bed and after waking. This is a whole-device baseline, not proof of screen-off time or an individual app's battery drain.")
  if(listOf("uninstall","remove app","delete app","unneeded apps").any { it in q })
   return AssistantReply("No app is automatically marked safe to uninstall. Check whether you recognize it, still use it, and whether it is critical to security, messaging, work, accessibility or device operation. Open Android's installed-app settings and decide manually.",Settings.ACTION_APPLICATION_SETTINGS)
  if(listOf("disable notification","mute notification","silence notification").any { it in q })
   return AssistantReply("Disable only low-value notification categories after reviewing each app. Keep calls, alarms, important chats, sign-in and security warnings enabled. AImanage never silently mutes other apps.",Settings.ACTION_APP_NOTIFICATION_SETTINGS)
  if(listOf("background running","running in background","background drain","cooling app").any { it in q })
   return AssistantReply("Android limits ordinary apps' visibility of other processes. Advanced edition can show recent foreground usage, but that cannot identify a currently running background process or its heat. Check Android Battery > App battery usage and selectively review nonessential high-background-use apps.",Settings.ACTION_APPLICATION_SETTINGS)
  if(listOf("dns","block ads","adblock","iklan").any { it in q })
   return AssistantReply("Private DNS can block some advertising domains. In Android Settings, locate Private DNS, select provider hostname and enter a provider you trust (example: dns.adguard-dns.com). Verify normal browsing and app connectivity afterwards. DNS filters cannot block all in-app or video ads; some apps may break, and the provider receives DNS queries. AImanage cannot silently configure Private DNS.",Settings.ACTION_WIRELESS_SETTINGS)
  if("vpn" in q)
   return AssistantReply("AImanage has no active VPN service or firewall. A real VPN needs a separately implemented VpnService and Android consent. Review VPN connections through Android Settings; one active VPN is normally permitted per user profile.",Settings.ACTION_VPN_SETTINGS)
  if(listOf("network","wifi","wi-fi","internet","latency","bandwidth","data speed").any { it in q })
   return AssistantReply("Compare Wi-Fi and mobile data signal, router congestion and network conditions. DNS may affect resolution but cannot increase radio bandwidth. Ordinary apps cannot speed up the modem or throttle other apps at will.",Settings.ACTION_WIRELESS_SETTINGS)
  if(listOf("save battery","saving battery","battery saver without","performance","optimize").any { it in q })
   return AssistantReply("Balanced battery plan: retain normal performance for important foreground apps; lower unnecessary screen brightness and timeout first. In Android per-app Battery settings review only rarely used apps with confirmed background drain. Avoid restricting calls, alarms, navigation and messaging without checking delivery. Global Battery Saver may reduce performance and delay sync.",Settings.ACTION_BATTERY_SAVER_SETTINGS)
  if(listOf("close app","which app","kill app","stop apps").any { it in q }) {
   if(!context.packageName.endsWith(".advanced")) return AssistantReply("Standard edition has no Usage Access. Open Android battery usage settings and review apps with verified background consumption. Do not force-close apps solely for having long foreground use; never indiscriminately restrict important apps.",Settings.ACTION_APPLICATION_SETTINGS)
   val r=ActivityMonitor.report(context)
   val top=if(r.usageAccessGranted) r.recentApps.take(3).joinToString("; ") { it.packageName + ": " + it.foregroundMinutes + " min foreground" } else "Usage Access not granted"
   return AssistantReply("Recent foreground time (NOT battery drain or CPU usage): $top. Check the Android per-app battery report before deciding whether to restrict anything. I cannot silently force-stop apps.",Settings.ACTION_APPLICATION_SETTINGS)
  }
  if(listOf("cooling","cool phone","reduce heat").any { it in q })
   return AssistantReply("Check Android thermal status and reduce demanding workloads or charging if severe. Keep airflow unobstructed and avoid direct sun. AImanage cannot change the CPU governor or bypass thermal safeguards.")
  if(listOf("refresh rate","brightness","display").any { it in q })
   return AssistantReply("Lowering brightness and screen timeout can save battery without slowing app computation. A lower refresh rate can help but may reduce visual smoothness; use Android display controls.",Settings.ACTION_DISPLAY_SETTINGS)
  if(listOf("notification","notifikasi","alert").any { it in q }) {
   if(context.packageName.endsWith(".advanced") && listOf("enable automation","turn on automation","aktif automasi").any { it in q })
    return AssistantReply("I can enable the existing notification auto-dismiss rules. This only affects apps you explicitly allowlisted. Confirm below.",proposedRule=true)
   if(listOf("disable automation","turn off automation","matikan automasi").any { it in q })
    return AssistantReply("I can disable notification auto-dismiss automation. Confirm below.",proposedRule=false)
   return if(context.packageName.endsWith(".advanced")) AssistantReply("The Advanced edition can use notification access only with explicit consent, for user-allowlisted auto-dismiss rules.",Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) else AssistantReply("Standard edition does not include notification reading or dismissal. Manage individual app notification categories in Android Settings.",Settings.ACTION_APPLICATION_SETTINGS)
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
  if(enabled && !context.packageName.endsWith(".advanced")) return
  context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE).edit().putBoolean("automation_enabled",enabled).apply()
 }
 fun settingsIntent(action:String)=Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
