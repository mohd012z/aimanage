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
  if(listOf("clean cache","clear cache","cache file","storage cleaner","rearrange files","organize files","sort files","clean junk").any { it in q })
   return AssistantReply("Open Cleaner & Files from the sidebar. AImanage can delete its own temporary cache after confirmation. Android does not allow it to silently delete other apps' caches. To organize documents, select a folder with Android's picker; the app copies supported files into category folders and keeps originals so you can review them. No all-files access is requested.",Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
  if("lola" in q || "gguf" in q)
   return AssistantReply("LOLA is not connected to this Android app, and no GGUF model is installed. This assistant currently performs local rule-based diagnostics. A cloud or on-device inference backend would need a separately verified integration and your consent; it cannot be described as active.")
  // Handle conversational combinations first; a generic "apps" match must not hide a request for cooling.
  if(listOf("apps background","background apps","apps running","running background","app not open","background service").any { it in q })
   return AssistantReply("Background activity review: Android does not expose a trustworthy real-time list of every other app's background services. In App Review, check Android's per-app Battery usage, then limit only nonessential apps with verified excess use. For calls, alarms, navigation and messaging keep unrestricted operation. AImanage cannot automatically force-stop another app.",Settings.ACTION_APPLICATION_SETTINGS)
  if(listOf("cooling","cool phone","reduce heat","hot phone","overheat","phone hot").any { it in q }) {
   val reading=DeviceReadings.battery(context)
   val status=ActivityMonitor.report(context).thermalStatus
   return AssistantReply("Live thermal check: Android reports $status; battery temperature ${reading.temperatureC?.let { "$it °C" } ?: "unavailable"}. Pause demanding foreground apps and heavy charging if warm, move out of sunlight, allow ventilation and Android's thermal control to operate. Do not force-stop vital services or use rapid cooling tricks.")
  }
  if(listOf("overnight","sleep","sleeping","bedtime","idle drain").any { it in q })
   return AssistantReply("Open Sleep Review from the navigation menu. Capture a baseline before bed and compare in the morning. Android may use Doze, defer sync and wake for calls or alarms. A before/after reading does not prove the phone stayed idle or identify an app. Review Android battery usage for per-app evidence.",Settings.ACTION_BATTERY_SAVER_SETTINGS)
  if(listOf("uninstall","delete app","remove app").any { it in q })
   return AssistantReply("Open App Review to select an exact package and confirm that you no longer need it. AImanage will never uninstall automatically. Use Android's confirmation screen, and preserve accounts and app data before removal.",Settings.ACTION_APPLICATION_SETTINGS)
  if(listOf("disable notification","mute notification","notification spam").any { it in q })
   return AssistantReply("Use App Review to flag unwanted notification categories. Keep calls, messaging, alarms, navigation and work alerts enabled. Android App Info allows category-level changes; AImanage cannot silently switch other apps' notifications.",Settings.ACTION_APPLICATION_SETTINGS)
  if(listOf("dns","block ads","adblock","iklan").any { it in q })
   return AssistantReply("Private DNS can block some advertising domains. In Android Settings, locate Private DNS, select provider hostname and enter a provider you trust (example: dns.adguard-dns.com). Verify normal browsing and app connectivity afterwards. DNS filters cannot block all in-app or video ads; some apps may break, and the provider receives DNS queries. AImanage cannot silently configure Private DNS.",Settings.ACTION_WIRELESS_SETTINGS)
  if(listOf("cpu","ram","memory","speed up phone","speed up","processor","slow phone","hang phone").any { it in q }) {
   val metric=PerformanceDiagnostics.snapshot(context)
   val status=ActivityMonitor.report(context).thermalStatus
   val ram=PerformanceAdvicePolicy.ramPercent(metric)?.let { "$it%" } ?: "unknown"
   return AssistantReply("Read-only CPU/RAM: ${metric.cores} CPU cores reported, RAM in use approx. $ram, Android low-memory=${metric.lowMemory}; AImanage CPU runtime=${metric.appCpuTimeMillis} ms (cumulative). ${PerformanceAdvicePolicy.assess(metric,status)} No other app's CPU or background process can be stopped silently.",Settings.ACTION_APPLICATION_SETTINGS)
  }
  if("vpn" in q)
   return AssistantReply("Open VPN from the AImanage menu. Choose Off, Hybrid advisory, or External VPN advisory, then a listed provider. This only changes guidance: the VPN app handles connection and Android consent. Cloudflare WARP also offers DNS-only mode; VPN is not guaranteed to increase speed.",Settings.ACTION_VPN_SETTINGS)
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
