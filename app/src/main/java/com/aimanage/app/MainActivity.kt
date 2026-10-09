package com.aimanage.app

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Navy = Color(0xFF101A32)
private val Cyan = Color(0xFF00B8D9)
private val Panel = Color(0xFF192640)
private val Muted = Color(0xFFA9B8CE)
private val tabs = listOf("Home","Apps","Network","Protect","AI")
private val sections = listOf("Overview","Cleaner & Files","Performance","RAM","App Management","App Review","Sleep Review","Background & Autostart","CPU & Thermal","Battery","Battery Care","Charging Intelligence","Brightness & Power","AI Assistant","AI Learning","Security Intelligence","Web Scam Check","Telegram Safety","Notification Center","Automation Control","CPU & App Activity","Caller Intelligence","Voice Caller","Standby Intelligence","Display & Refresh Rate","Network & Speed","Ad Blocker","Firewall","VPN","Permissions","Device Information","Settings")

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AimanageUI() } }
}

@Composable
private fun AimanageUI() {
 val context = LocalContext.current
 var selected by remember { mutableStateOf("Overview") }
 var drawer by remember { mutableStateOf(false) }
 var deviceRefresh by remember { mutableIntStateOf(0) }
 val batteryIntent = remember(deviceRefresh) { context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) }
 val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
 val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
 val pct = BatteryPercentagePolicy.fromLevelAndScale(level, scale)
 val pm = context.getSystemService(PowerManager::class.java)
 val thermal = if (Build.VERSION.SDK_INT >= 29) when(pm?.currentThermalStatus) {
  PowerManager.THERMAL_STATUS_NONE -> "Normal"
  PowerManager.THERMAL_STATUS_LIGHT -> "Light"
  PowerManager.THERMAL_STATUS_MODERATE -> "Moderate"
  PowerManager.THERMAL_STATUS_SEVERE -> "Severe"
  PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
  PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
  PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown"
  else -> "Unavailable"
 } else "Unavailable"
 MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, onPrimary = Navy, background = Navy, onBackground = Color.White, surface = Panel, onSurface = Color.White, surfaceVariant = Panel, onSurfaceVariant = Muted, outline = Muted)) {
  Column(Modifier.fillMaxSize().background(Navy).statusBarsPadding()) {
   Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
    IconButton(onClick = { drawer = !drawer }) { Icon(Icons.Default.Menu, "Toggle categories",tint=Color.White) }
    Column(Modifier.weight(1f)) { Text("AImanage",color=Color.White,style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Android Device Intelligence", color = Muted, style = MaterialTheme.typography.labelSmall) }
    IconButton(onClick = { selected = "AI Assistant"; drawer = false }) { Icon(Icons.Default.Chat, "Open AI Advisor") }
    IconButton(onClick = { selected = "Settings" }) { Icon(Icons.Default.Settings, "Settings",tint=Cyan) }
   }
   Row(Modifier.weight(1f)) {
    if (drawer) {
     LazyColumn(Modifier.width(174.dp).fillMaxHeight().background(Panel).padding(8.dp)) {
      items(sections) { item ->
       TextButton(onClick = { selected = item; drawer = false }, modifier = Modifier.fillMaxWidth()) {
        Text(item, color = if(selected == item) Cyan else Color.White, style = MaterialTheme.typography.labelMedium)
       }
      }
     }
    }
    LazyColumn(Modifier.weight(1f).fillMaxHeight(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
     item { Text(selected,color=Color.White,style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
     if(selected == "Overview" || selected == "Home") {
      item { PanelCard("Device Health", "Live battery and thermal snapshot", Icons.Default.Favorite) {
       Text("Battery  ${pct?.let { "$it%" } ?: "Unavailable"}", style = MaterialTheme.typography.headlineMedium)
       Text("Thermal status  $thermal", color = Muted)
       Text("Device  ${Build.MANUFACTURER} ${Build.MODEL}", color = Muted)
       Action("Refresh device health") { deviceRefresh++ }
      } }
      item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
       Box(Modifier.weight(1f)) { SmallCard("Apps","Manage", Icons.Default.Apps) { selected = "App Management" } }
       Box(Modifier.weight(1f)) { SmallCard("Cooling","Monitor", Icons.Default.AcUnit) { selected = "CPU & Thermal" } }
      } }
      item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
       Box(Modifier.weight(1f)) { SmallCard("Firewall","Planned", Icons.Default.Security) { selected = "Firewall" } }
       Box(Modifier.weight(1f)) { SmallCard("VPN","Planned", Icons.Default.VpnLock) { selected = "VPN" } }
      } }
      item { PanelCard("AI Phone Advisor","Recommendations • App health • Overnight standby",Icons.Default.Chat) {
       Text("Review apps without disrupting important notifications, compare overnight battery loss, or ask the offline assistant.",color=Muted)
       Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { SmallCard("AI Advisor","Ask",Icons.Default.Chat) { selected="AI Assistant" } }
        Box(Modifier.weight(1f)) { SmallCard("App Review","Assess",Icons.Default.Apps) { selected="App Review" } }
       }
       Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { SmallCard("Sleep Review","Compare",Icons.Default.Bedtime) { selected="Sleep Review" } }
        Box(Modifier.weight(1f)) { SmallCard("AI Learning","Profiles",Icons.Default.Insights) { selected="AI Learning" } }
       }
       Text("No automatic force-stop, uninstall, hardware cooling, or network speed boosting.",color=Muted,style=MaterialTheme.typography.bodySmall)
      } }
      item { PanelCard("Storage & Device Care","Manage only what Android allows",Icons.Default.CleaningServices) {
       Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { SmallCard("Cleaner","Own cache",Icons.Default.DeleteSweep) { selected="Cleaner & Files" } }
        Box(Modifier.weight(1f)) { SmallCard("Files","Organize",Icons.Default.FolderCopy) { selected="Cleaner & Files" } }
       }
       Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { SmallCard("Battery","Review",Icons.Default.BatteryStd) { selected="Battery Care" } }
        Box(Modifier.weight(1f)) { SmallCard("App Review","Evidence",Icons.Default.Apps) { selected="App Review" } }
       }
       Text("Cache cleanup affects AImanage only; selected folder copies require your approval. Android's own Xiaomi Security tools have deeper system privileges.",color=Muted)
      } }
      item { PanelCard("Quick Actions","Android-owned controls",Icons.Default.Tune) {
       Action("Battery settings") { launch(context, Settings.ACTION_BATTERY_SAVER_SETTINGS) }
       Action("Application settings") { launch(context, Settings.ACTION_APPLICATION_SETTINGS) }
       Action("Device settings") { launch(context, Settings.ACTION_SETTINGS) }
      } }
     } else {
      item { DetailSection(when(selected) { "Apps" -> "App Review"; "AI" -> "AI Assistant"; else -> selected }, pct, thermal) { action -> launch(context, action) } }
     }
    }
   }
   NavigationBar(containerColor = Panel) {
    tabs.forEach { tab ->
     val icon = when(tab) { "Home" -> Icons.Default.Home; "Apps" -> Icons.Default.Apps; "Network" -> Icons.Default.Wifi; "AI" -> Icons.Default.SmartToy; else -> Icons.Default.Shield }
     NavigationBarItem(selected = selected == tab || (tab == "Home" && selected == "Overview") || (tab == "Apps" && selected == "App Review") || (tab == "AI" && selected == "AI Assistant"), onClick = { selected = if(tab == "Apps") "App Review" else tab; drawer = false }, icon = { Icon(icon, tab) }, label = { Text(tab) })
    }
   }
  }
 }
}
@Composable private fun PanelCard(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,body:@Composable ColumnScope.()->Unit) {
 Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()) {
  Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
   Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) { Icon(icon,null,tint=Cyan); Text(title,color=Color.White,fontWeight=FontWeight.Bold) }
   Text(subtitle,color=Muted,style=MaterialTheme.typography.bodySmall)
   body()
  }
 }
}
@Composable private fun SmallCard(title:String,status:String,icon:androidx.compose.ui.graphics.vector.ImageVector,click:()->Unit) {
 Card(onClick=click,colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()) {
  Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) { Icon(icon,null,tint=Cyan); Text(title,fontWeight=FontWeight.Bold); Text(status,color=Muted,style=MaterialTheme.typography.labelSmall) }
 }
}
@Composable private fun Action(label:String,click:()->Unit) { OutlinedButton(onClick=click,modifier=Modifier.fillMaxWidth()) { Text(label,color=Cyan) } }
@Composable private fun DetailSection(section:String,pct:Int?,thermal:String,open:(String)->Unit) {
 val description = when(section) {
  "Apps","App Management","Background & Autostart" -> "Android restricts silent third-party app control. Open system settings to manage autostart and background restrictions."
  "Thermal","CPU & Thermal" -> "Read-only thermal status. No CPU governor or cooling hardware control."
  "Battery" -> "Battery charge reading and Android battery settings."
  "Network","Network & Speed" -> "Read-only active network and Android connectivity validation; real throughput testing and per-app rate limits are not implemented."
  "Protect","Ad Blocker","Firewall","VPN" -> "No in-app VPN or firewall is active. Private DNS can be set by you in Android Settings; AImanage cannot change it silently."
  "Permissions" -> "Only request permissions when a working feature needs them."
  else -> "System information and Android settings."
 }
 PanelCard(section,description,Icons.Default.SettingsSuggest) {
  if(section == "Cleaner & Files") {
   val ctx=LocalContext.current
   var cacheSize by remember { mutableStateOf(SafeStorageTools.cacheBytes(ctx)) }
   var cacheConfirm by remember { mutableStateOf(false) }
   var folderUri by remember { mutableStateOf<Uri?>(null) }
   var overview by remember { mutableStateOf<StorageOverview?>(null) }
   var organizeConfirm by remember { mutableStateOf(false) }
   var result by remember { mutableStateOf("") }
   val selectFolder=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
    folderUri=uri
    if(uri!=null) {
     try { ctx.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
     catch (_:SecurityException) {}
     overview=try { SafeStorageTools.preview(ctx,uri) } catch (_:Exception) { null }
    }
    result=if(uri==null) "Folder selection cancelled." else "Selected-folder preview ready."
   }
   Text("Private cache",color=Cyan,fontWeight=FontWeight.Bold)
   Text("AImanage cache: ${cacheSize / 1024L} KiB. Android normally manages other apps' caches; they cannot be cleared silently.",color=Muted)
   Row(verticalAlignment=Alignment.CenterVertically) {
    Checkbox(checked=cacheConfirm,onCheckedChange={cacheConfirm=it})
    Text("Confirm clearing AImanage temporary files")
   }
   Action("Clear AImanage cache") {
    if(cacheConfirm) {
     val removed=SafeStorageTools.clearOwnCache(ctx)
     cacheSize=SafeStorageTools.cacheBytes(ctx)
     result="Deleted $removed items from AImanage cache only."
     cacheConfirm=false
    } else result="Confirm the cache option first."
   }
   Text("Folder organizer",color=Cyan,fontWeight=FontWeight.Bold)
   Text("Select a folder using Android's Storage Access Framework; no all-files permission is required.",color=Muted)
   Action("Select folder to review") { selectFolder.launch(null) }
   overview?.let {
    Text("Selected folder: ${it.files} top-level files, ${it.folders} folders (up to 200 entries previewed).",color=Muted)
    Text("Copies at most 100 files of up to 100 MB each into Images / Videos / Audio / Documents / Archives / APK_Installers / Other. Original files are kept, so storage usage can increase.",color=Muted)
    Row(verticalAlignment=Alignment.CenterVertically) {
     Checkbox(checked=organizeConfirm,onCheckedChange={organizeConfirm=it})
     Text("Confirm copies into category folders")
    }
    Action("Copy selected files into categories") {
     if(organizeConfirm && folderUri!=null) {
      result=SafeStorageTools.copyToCategoryFolders(ctx,folderUri!!)
      .message
      organizeConfirm=false
      overview=SafeStorageTools.preview(ctx,folderUri!!)
     } else result="Select a folder and confirm before copying."
    }
   }
   if(result.isNotEmpty()) Text(result,color=Cyan)
   Action("Open Android storage settings") { open(Settings.ACTION_INTERNAL_STORAGE_SETTINGS) }
  }
  if(section == "Protect") {
   Text("Security and privacy advisor",color=Cyan,fontWeight=FontWeight.Bold)
   Text("Keep important notifications active. Review unwanted categories instead of muting all alerts.",color=Muted)
   Action("Open app permissions") { open(Settings.ACTION_APPLICATION_SETTINGS) }
   Action("Review notification categories") { open(Settings.ACTION_APP_NOTIFICATION_SETTINGS) }
   Action("Configure Private DNS manually") { open(Settings.ACTION_WIRELESS_SETTINGS) }
   Action("Review VPN configuration") { open(Settings.ACTION_VPN_SETTINGS) }
  }
  if(section == "Thermal" || section == "CPU & Thermal") {
   val ctx=LocalContext.current
   var reading by remember { mutableStateOf(DeviceReadings.battery(ctx)) }
   Text("Battery temperature: ${reading.temperatureC?.let { "$it °C" } ?: "Unavailable"}",color=Color.White)
   Text("For cooling, review demanding foreground tasks and heavy charging. Android does not provide a reliable source of per-app heat attribution to this app.",color=Muted)
   Action("Refresh temperature") { reading=DeviceReadings.battery(ctx) }
  }
  if(section == "Battery" || section == "Battery Care" || section == "Standby Intelligence") {
   val context = LocalContext.current
   var b by remember(section) { mutableStateOf(DeviceReadings.battery(context)) }
   Text("Charge: ${b.percent?.let { "$it%" } ?: "Unavailable"}")
   Text("Charging: ${if(b.charging) "Yes" else "No"}")
   Text("Battery temperature: ${b.temperatureC?.let { "$it °C" } ?: "Unavailable"}")
   Text("Voltage: ${b.voltageMv?.let { "$it mV" } ?: "Unavailable"}")
   Text("Current: ${b.currentMa?.let { "$it mA" } ?: "Unavailable"}")
   Text("Charge counter: ${b.chargeCounterMah?.let { "$it mAh" } ?: "Unavailable"}")
   Text("Battery health code: ${b.healthCode ?: "Unavailable"} (system status, not capacity estimate)")
   Action("Refresh battery readings") { b = DeviceReadings.battery(context) }
   Text("Charging limits cannot be controlled by ordinary Android apps.",color=Muted)
   Text("Standby drain history and charging alerts: planned.",color=Muted)
  }
  if(section == "Charging Intelligence" || section == "Brightness & Power") {
   val context = LocalContext.current
   var d by remember(section) { mutableStateOf(ChargingDiagnostics.assess(context)) }
   Text("Battery-side charging power: ${d.batteryPowerW?.let { "%.1f W".format(it) } ?: "Unavailable"}")
   Text("Brightness setting: ${d.brightnessPercent?.let { "$it%" } ?: "Unavailable"}")
   Text("Adaptive brightness: ${d.adaptiveBrightness?.toString() ?: "Unavailable"}")
   Text("Power saver: ${if(d.powerSaveEnabled) "On" else "Off"}")
   d.observations.forEach { Text("• $it", color=Muted) }
   Action("Refresh charging and display readings") { d = ChargingDiagnostics.assess(context) }
   Action("Open display settings") { open(Settings.ACTION_DISPLAY_SETTINGS) }
   Action("Open battery saver") { open(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
   Text("Charging observations are locally computed; charge-current readings depend on hardware support.",color=Muted)
  }
  if(section == "Security Intelligence" || section == "Web Scam Check" || section == "Telegram Safety" || section == "Caller Intelligence" || section == "Voice Caller"  ) {
   var query by remember { mutableStateOf("") }
   var result by remember { mutableStateOf<SecurityFinding?>(null) }
   Text("Offline security check: enter a website URL or an international phone number.", color=Muted)
   OutlinedTextField(value=query,onValueChange={query=it},label={Text("URL or +country-code number")},modifier=Modifier.fillMaxWidth(),singleLine=true)
   Action("Analyze input") { result = if(query.trim().startsWith("+")) SecurityAnalyzer.explainPhone(query) else SecurityAnalyzer.checkUrl(query) }
   result?.let { finding ->
    Text("Assessment: ${finding.level}",fontWeight=FontWeight.Bold)
    Text(finding.summary)
    finding.evidence.forEach { Text("• $it",color=Muted) }
   }
   Text("Telegram account checks, live caller detection, notification reading, and automatic speech announcements are not yet active.",color=Muted)
  }
  if(section == "Automation Control" || section == "Notification Center") {
   val context = LocalContext.current
   val prefs = remember { context.getSharedPreferences("aimanage_rules",android.content.Context.MODE_PRIVATE) }
   var enabled by remember { mutableStateOf(prefs.getBoolean("automation_enabled",false)) }
   var packageInput by remember { mutableStateOf("") }
   var packages by remember { mutableStateOf(prefs.getStringSet("dismiss_packages",emptySet())?.toSet() ?: emptySet()) }
   if (!context.packageName.endsWith(".advanced")) {
    Text("Standard edition: Notification Access and Usage Access are not requested. Notification automation is unavailable.",color=Muted)
   } else {
   Text("Notification automation is opt-in and only applies to explicitly selected apps.",color=Muted)
   Row(verticalAlignment=Alignment.CenterVertically) {
    Text("Enable notification rules",modifier=Modifier.weight(1f))
    Switch(checked=enabled,onCheckedChange={enabled=it;prefs.edit().putBoolean("automation_enabled",it).apply()})
   }
   Action("Grant notification access") { open(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }
   Action("Grant usage access") { open(Settings.ACTION_USAGE_ACCESS_SETTINGS) }
   OutlinedTextField(value=packageInput,onValueChange={packageInput=it},label={Text("Exact app package name")},modifier=Modifier.fillMaxWidth())
   Action("Add app to auto-dismiss list") {
    val p=packageInput.trim()
    if(p.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))) {
     packages=packages+p
     prefs.edit().putStringSet("dismiss_packages",packages).apply()
     packageInput=""
    }
   }
   packages.sorted().forEach { p ->
    Row(verticalAlignment=Alignment.CenterVertically) {
     Text(p,modifier=Modifier.weight(1f),style=MaterialTheme.typography.bodySmall)
     TextButton(onClick={packages=packages-p;prefs.edit().putStringSet("dismiss_packages",packages).apply()}) { Text("Remove") }
    }
   }
   Text("Calls, alarms, messages, calendar events, group summaries, system/security alerts and ongoing notifications are protected from auto-dismissal.",color=Muted)
   Text("Per-app CPU measurement and service termination are not available to ordinary Android apps.",color=Muted)
   }
  }
  if(section == "CPU & App Activity" || section == "Standby Intelligence") {
   val context = LocalContext.current
   var report by remember { mutableStateOf(ActivityMonitor.report(context)) }
   Text("Thermal pressure: ${report.thermalStatus}")
   Text("Battery Saver: ${if(report.powerSaver) "Enabled" else "Disabled"}")
   Text("Usage access: ${if(report.usageAccessGranted) "Granted" else "Required"}")
   if(!report.usageAccessGranted && context.packageName.endsWith(".advanced")) Action("Grant usage access") { open(Settings.ACTION_USAGE_ACCESS_SETTINGS) }
   if(!context.packageName.endsWith(".advanced")) Text("Per-app usage history requires the separately installed Advanced edition and explicit user permission.",color=Muted)
   Action("Refresh app activity") { report=ActivityMonitor.report(context) }
   report.recentApps.forEach { app ->
    Text("${app.packageName} — ${app.foregroundMinutes} min foreground",color=Muted,style=MaterialTheme.typography.bodySmall)
   }
   report.notes.forEach { Text("• $it",color=Muted,style=MaterialTheme.typography.bodySmall) }
  }
  if(section == "App Review" || section == "App Management") {
   val context=LocalContext.current
   var packageName by remember { mutableStateOf("") }
   var unneeded by remember { mutableStateOf(false) }
   var unwantedAlerts by remember { mutableStateOf(false) }
   var batteryEvidence by remember { mutableStateOf(false) }
   var essential by remember { mutableStateOf(false) }
   val valid=packageName.trim().matches(Regex("[A-Za-z0-9_]+(\\\\.[A-Za-z0-9_]+)+"))
   Text("App review assistant",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
   Text("Enter an exact package ID from Android App Info. AImanage cannot identify live background services or measure another app's CPU load; it does not close apps automatically.",color=Muted)
   OutlinedTextField(value=packageName,onValueChange={packageName=it},label={Text("Package ID (example: com.example.app)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
   Row(verticalAlignment=Alignment.CenterVertically) {
    Checkbox(checked=essential,onCheckedChange={essential=it})
    Text("Essential: calls, messages, navigation, alarms or work",modifier=Modifier.weight(1f))
   }
   Row(verticalAlignment=Alignment.CenterVertically) {
    Checkbox(checked=unwantedAlerts,onCheckedChange={unwantedAlerts=it})
    Text("Its notifications are unwanted",modifier=Modifier.weight(1f))
   }
   Row(verticalAlignment=Alignment.CenterVertically) {
    Checkbox(checked=batteryEvidence,onCheckedChange={batteryEvidence=it})
    Text("Android battery screen confirms unusual background use",modifier=Modifier.weight(1f))
   }
   Row(verticalAlignment=Alignment.CenterVertically) {
    Checkbox(checked=unneeded,onCheckedChange={unneeded=it})
    Text("I no longer need this app",modifier=Modifier.weight(1f))
   }
   if(valid) {
    val recommendation=AppReviewPolicy.recommend(unneeded,unwantedAlerts,batteryEvidence,essential)
    Text("Recommendation: ${recommendation.action.name.replace('_',' ')}",fontWeight=FontWeight.Bold,color=Cyan)
    Text(recommendation.explanation,color=Muted)
    Action("Open this app's Android information") {
     try {
      context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+packageName.trim())))
     } catch (_:Exception) { launch(context,Settings.ACTION_APPLICATION_SETTINGS) }
    }
    if(recommendation.action==AppReviewAction.CONSIDER_UNINSTALL) {
     Action("Request uninstall (Android confirmation required)") {
      try { context.startActivity(Intent(Intent.ACTION_DELETE,Uri.parse("package:"+packageName.trim()))) }
      catch (_:Exception) { launch(context,Settings.ACTION_APPLICATION_SETTINGS) }
     }
    }
   } else Text("Enter a valid exact package ID to enable an app-specific review.",color=Muted)
   if(context.packageName.endsWith(".advanced")) {
    var activity by remember { mutableStateOf(ActivityMonitor.report(context)) }
    Text("Advanced foreground usage (not running background processes or battery drain)",fontWeight=FontWeight.Bold)
    if(!activity.usageAccessGranted) Text("Usage Access is optional and not granted.",color=Muted)
    activity.recentApps.take(10).forEach { app ->
     Text("${app.packageName}: ${app.foregroundMinutes} min foreground",color=Muted)
     TextButton(onClick={packageName=app.packageName}) { Text("Review this app") }
    }
    Action("Refresh foreground activity") { activity=ActivityMonitor.report(context) }
   } else Text("Standard edition intentionally has no Usage Access. You can still review an app by entering its package ID.",color=Muted)
  }
  if(section == "Sleep Review" || section == "Standby Intelligence") {
   val context=LocalContext.current
   val prefs=remember { context.getSharedPreferences("aimanage_sleep_review",Context.MODE_PRIVATE) }
   var begin by remember { mutableStateOf(prefs.getLong("start_time",0L)) }
   var result by remember { mutableStateOf("No overnight comparison yet.") }
   Text("Overnight standby comparison",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
   Text("Before sleeping, tap Start. When you wake, tap Finish. The phone may still perform Android maintenance, sync, alarms or network tasks. This does not prove the screen was off throughout.",color=Muted)
   Text("Baseline: ${if(begin>0L) java.text.DateFormat.getDateTimeInstance().format(java.util.Date(begin)) else "Not started"}",color=Muted)
   Action("Start bedtime baseline") {
    val b=DeviceReadings.battery(context)
    begin=System.currentTimeMillis()
    prefs.edit().putLong("start_time",begin).putInt("start_pct",b.percent ?: -1).putBoolean("start_charging",b.charging).apply()
    result="Bedtime baseline saved locally. Leave normal notifications and alarms active."
   }
   if(begin>0L) {
    Action("Finish and compare battery") {
     val b=DeviceReadings.battery(context)
     val s=SleepReading(begin,prefs.getInt("start_pct",-1).takeIf { it in 0..100 },prefs.getBoolean("start_charging",false))
     val end=SleepReading(System.currentTimeMillis(),b.percent,b.charging)
     val measured=SleepReviewPolicy.compare(s,end)
     result=if(measured==null) "No valid comparison. At least one hour is required; charging, battery increase, invalid readings or clock changes invalidate the estimate."
      else "Battery changed by ${measured.percentLost} percentage points over ${"%.1f".format(measured.hours)} h (${"%.2f".format(measured.percentPointsPerHour)} points/h). This is a before/after comparison, not verified screen-off drain or per-app attribution."
     prefs.edit().clear().apply()
     begin=0L
    }
   }
   Text(result,color=Muted)
   Action("Open Android battery usage details") { launch(context,Settings.ACTION_BATTERY_SAVER_SETTINGS) }
   Text("For overnight alerts and calls, keep essential apps unrestricted. Android Doze may defer background work; Xiaomi battery policies can add restrictions. Never disable safety or notification protections to save power.",color=Muted)
  }
  if(section == "AI Assistant") {
   val context=LocalContext.current
   var question by remember { mutableStateOf("") }
   val conversation=remember { mutableStateListOf<Pair<Boolean,String>>() }
   var proposed by remember { mutableStateOf<Boolean?>(null) }
   var settingsAction by remember { mutableStateOf<String?>(null) }
   val ask:(String)->Unit={ input ->
    if(input.isNotBlank()) {
     conversation.add(true to input.trim())
     val answer=DeviceAssistant.reply(context,input)
     conversation.add(false to answer.message)
     proposed=answer.proposedRule
     settingsAction=answer.settingsAction
     question=""
    }
   }
   Text("AImanage Advisor",color=Cyan,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
   Text("On-device diagnostic rules • Not connected to LOLA or a cloud LLM",color=Muted)
   Text("Answers use Android readings when available. I will say when permissions or evidence are insufficient.",color=Muted,style=MaterialTheme.typography.bodySmall)
   conversation.takeLast(12).forEach { (user,message) ->
    Card(colors=CardDefaults.cardColors(containerColor=if(user) Color(0xFF123D52) else Navy),
     shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()) {
     Column(Modifier.padding(12.dp)) {
      Text(if(user) "You" else "AImanage",color=if(user) Cyan else Color.White,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelMedium)
      val compact = !user && !context.getSharedPreferences("aimanage_ui_options",Context.MODE_PRIVATE).getBoolean("show_explanations",true)
      val displayed = if(compact && message.length>190) message.take(190)+"…" else message
      Text(displayed,color=Color.White)
     }
    }
   }
   OutlinedTextField(value=question,onValueChange={question=it},label={Text("Ask about your phone")},modifier=Modifier.fillMaxWidth(),minLines=2,maxLines=4)
   Button(onClick={ask(question)},enabled=question.isNotBlank(),modifier=Modifier.fillMaxWidth()) {
    Icon(Icons.Default.Send,null); Spacer(Modifier.width(8.dp));Text("Send question")
   }
   Text("Try a question",color=Muted,style=MaterialTheme.typography.labelMedium)
   val uiPrefs=remember { context.getSharedPreferences("aimanage_ui_options",Context.MODE_PRIVATE) }
   if(uiPrefs.getBoolean("quick_prompts",true)) {
    listOf("apps background","Cooling and thermal","How to save battery without slowing apps?","What drains battery overnight?","CPU and RAM","Free VPN options").forEach { prompt ->
     OutlinedButton(onClick={ask(prompt)},modifier=Modifier.fillMaxWidth()) { Text(prompt) }
    }
   }
   settingsAction?.let { action -> Action("Review suggested Android settings") { open(action) } }
   proposed?.let { enabled ->
    Action(if(enabled) "Confirm notification rule ON" else "Confirm notification rule OFF") {
     DeviceAssistant.setNotificationAutomation(context,enabled)
     conversation.add(false to "Preference saved. Any required Android permission must still be explicitly granted.")
     proposed=null
    }
    Action("Cancel suggested change") { proposed=null }
   }
   Action("Clear this conversation") { conversation.clear();settingsAction=null;proposed=null }
  }
  if(section == "AI Learning") {
   val alertContext = LocalContext.current
   var healthAlerts by remember { mutableStateOf(DeviceAlertEngine.enabled(alertContext)) }
   var alertPermissionMessage by remember { mutableStateOf("") }
   val requestAlertPermission = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
   ) { granted ->
    DeviceAlertEngine.setEnabled(alertContext, granted)
    healthAlerts = granted
    alertPermissionMessage = if(granted)
     "Notification permission granted. Device health alerts enabled."
    else "Notification permission denied. Device health alerts remain disabled."
   }
   Row(verticalAlignment=Alignment.CenterVertically) {
    Text("Device health alerts",modifier=Modifier.weight(1f))
    Switch(checked=healthAlerts,onCheckedChange={requested ->
     if(requested && Build.VERSION.SDK_INT >= 33 &&
       alertContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
      requestAlertPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
     } else {
      DeviceAlertEngine.setEnabled(alertContext,requested)
      healthAlerts=requested
      alertPermissionMessage = if(requested) "Device health alerts enabled." else "Device health alerts disabled."
     }
    })
   }
   if(alertPermissionMessage.isNotBlank()) Text(alertPermissionMessage,color=Muted)
   Text("Alerts require notification permission and periodic sampling to be enabled. Android may delay checks.",color=Muted)

   val appContext = LocalContext.current
   var sampling by remember { mutableStateOf(TelemetryScheduler.enabled(appContext)) }
   Row(verticalAlignment=Alignment.CenterVertically) {
    Text("Automatic local sampling",modifier=Modifier.weight(1f))
    Switch(checked=sampling,onCheckedChange={
     sampling=it
     TelemetryScheduler.setEnabled(appContext,it)
    })
   }
   Text("Best-effort sampling approximately every 30 minutes. Android may delay background work.",color=Muted)
   var monitoringRefresh by remember { mutableIntStateOf(0) }
   var samplesRefresh by remember { mutableIntStateOf(0) }
   val monitoringStatus=remember(monitoringRefresh,sampling) { TelemetryScheduler.health(appContext) }
   val lastRun=remember(monitoringRefresh,sampling) { TelemetryScheduler.lastSuccess(appContext) }
   val enabledSince=remember(monitoringRefresh,sampling) { appContext.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE).getLong("telemetry_enabled_since",0L) }
   val workerError=remember(monitoringRefresh,sampling) { TelemetryScheduler.lastError(appContext) }
   val workerErrorAt=remember(monitoringRefresh,sampling) { TelemetryScheduler.lastErrorAt(appContext) }
   Text("Background monitoring: $monitoringStatus",color=Muted)
   Action("Refresh monitoring status") { monitoringRefresh++; samplesRefresh++ }
   val currentRun=lastRun>0L && lastRun>=enabledSince
   Text("Last successful background sample: ${if(lastRun==0L) "Not recorded" else java.text.DateFormat.getDateTimeInstance().format(java.util.Date(lastRun))}${if(sampling && !currentRun && lastRun>0L) " (previous monitoring session)" else ""}",color=Muted)
   if(sampling && workerError!=null) {
    val previousError=workerErrorAt>0L && workerErrorAt<enabledSince
    Text("Last worker error category: $workerError${if(previousError) " (previous monitoring session)" else ""}",color=Muted)
    if(workerErrorAt>0L) Text("Last worker error time: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(workerErrorAt))}",color=Muted)
   }

   val context = LocalContext.current
   var samples by remember { mutableStateOf(DeviceLearningEngine.load(context)) }
   LaunchedEffect(samplesRefresh) { samples=DeviceLearningEngine.load(context) }
   Text("Local learning samples: ${samples.size}")
   var adviceProfile by remember { mutableStateOf(AdviceProfile.BALANCED) }
   Text("Device optimization profile (advice only)",fontWeight=FontWeight.Bold)
   AdviceProfile.values().forEach { profile ->
    Row(verticalAlignment=Alignment.CenterVertically) {
     RadioButton(selected=adviceProfile==profile,onClick={adviceProfile=profile})
     Text(profile.name.replace('_',' ').lowercase().replaceFirstChar { it.titlecase() })
    }
   }
   val measured=DeviceMeasurementPolicy.recentDischarge(samples.map {
    DischargeSegmentPolicy.Point(it.timestamp,it.batteryPercent,it.charging)
   },System.currentTimeMillis())
   Text(DeviceMeasurementPolicy.recommendation(adviceProfile,measured,samples.lastOrNull()?.thermalStatus ?: thermal),color=Muted)
   Text("Advisory only: no app was closed, throttled, restricted or reconfigured.",color=Muted)
   var captureMessage by remember { mutableStateOf("") }
   Action("Capture device snapshot") {
    val before=samples.size
    DeviceLearningEngine.record(context,DeviceLearningEngine.capture(context))
    samples=DeviceLearningEngine.load(context)
    captureMessage=if(samples.size>before) "Snapshot saved locally." else "Snapshot not added: samples are limited to one every 15 minutes."
   }
   if(captureMessage.isNotBlank()) Text(captureMessage,color=Muted)
   DeviceLearningEngine.analyze(samples).forEach { insight ->
    Text("${insight.severity}: ${insight.title}",fontWeight=FontWeight.Bold)
    Text(insight.evidence,color=Muted)
    Text(insight.suggestion,color=Muted)
   }
   Action("Delete local learning history") {
    DeviceLearningEngine.clear(context)
    samples=DeviceLearningEngine.load(context)
    captureMessage=if(samples.isEmpty()) "Local learning history cleared." else "Some learning samples remain; please retry."
   }
   Text("Learning is rule-based and on-device. Automatic sampling runs only when enabled; no model training or cloud upload.",color=Muted)
  }
  if(section == "Network" || section == "Network & Speed") {
   val context=LocalContext.current
   var network by remember { mutableStateOf(NetworkDiagnostics.snapshot(context)) }
   Text("Transport: ${network.transport}")
   Text("Internet capability: ${if(network.internetCapability) "Reported" else "Not reported"}")
   Text("Android validated: ${if(network.validated) "Yes" else "No"}")
   Text("Captive portal: ${if(network.captivePortal) "Detected" else "Not detected"}")
   Text("Metered: ${network.metered?.let { if(it) "Yes" else "No" } ?: "Unknown"}")
   Text("VPN transport: ${if(network.vpnActive) "Reported" else "Not reported"}")
   Text("Reported link capacity down/up: ${network.downstreamKbps?.toString() ?: "Unknown"} / ${network.upstreamKbps?.toString() ?: "Unknown"} kbps")
   Text("These capacity estimates are not measured download or upload speeds.",color=Muted)
   Text(NetworkAdvicePolicy.explain(network),color=Muted)
   Action("Refresh network snapshot") { network=NetworkDiagnostics.snapshot(context) }
  }
  if(section == "CPU & Thermal" || section == "CPU & App Activity" || section == "Performance" || section == "RAM") {
   val context=LocalContext.current
   var performance by remember { mutableStateOf(PerformanceDiagnostics.snapshot(context)) }
   Text("Performance & RAM diagnostics",color=Cyan,fontWeight=FontWeight.Bold)
   Text("CPU cores available to AImanage: ${performance.cores}")
   Text("This app CPU runtime since process start: ${performance.appCpuTimeMillis} ms",color=Muted)
   Text("This is cumulative CPU time for AImanage only, not whole-device CPU % or another app's process usage.",color=Muted)
   Text("RAM used (system estimate): ${PerformanceAdvicePolicy.ramPercent(performance)?.let { "$it%" } ?: "Unavailable"}")
   Text("RAM available: ${performance.availableRamBytes / 1048576L} MiB / ${performance.totalRamBytes / 1048576L} MiB",color=Muted)
   Text("Android low-memory signal: ${if(performance.lowMemory) "Yes" else "No"}")
   Text(PerformanceAdvicePolicy.assess(performance,thermal),color=Muted)
   Action("Refresh CPU & RAM") { performance=PerformanceDiagnostics.snapshot(context) }
  }
  if(section == "VPN" || section == "Protect") {
   val context=LocalContext.current
   val prefs=remember { context.getSharedPreferences("aimanage_vpn_advice",Context.MODE_PRIVATE) }
   var mode by remember { mutableStateOf(VpnAdviceMode.values().firstOrNull { it.name==prefs.getString("mode","OFF") } ?: VpnAdviceMode.OFF) }
   var providerIndex by remember { mutableIntStateOf(prefs.getInt("provider",0).coerceIn(0,PublicVpnCatalog.providers.lastIndex)) }
   var modeExpanded by remember { mutableStateOf(false) }
   var providerExpanded by remember { mutableStateOf(false) }
   val provider=PublicVpnCatalog.providers[providerIndex]
   Text("External VPN advisor",color=Cyan,fontWeight=FontWeight.Bold)
   Text("Advice preference — does NOT control an external VPN connection.",color=Muted)
   Box {
    OutlinedButton(onClick={modeExpanded=true},modifier=Modifier.fillMaxWidth()) { Text("Mode: ${mode.name.replace('_',' ')}") }
    DropdownMenu(expanded=modeExpanded,onDismissRequest={modeExpanded=false}) {
     VpnAdviceMode.values().forEach { item ->
      DropdownMenuItem(text={Text(item.name.replace('_',' '))},onClick={
       mode=item;modeExpanded=false;prefs.edit().putString("mode",item.name).apply()
      })
     }
    }
   }
   Box {
    OutlinedButton(onClick={providerExpanded=true},modifier=Modifier.fillMaxWidth()) { Text("Provider: ${provider.name}") }
    DropdownMenu(expanded=providerExpanded,onDismissRequest={providerExpanded=false}) {
     PublicVpnCatalog.providers.forEachIndexed { index,item ->
      DropdownMenuItem(text={Text(item.name)},onClick={
       providerIndex=index;providerExpanded=false;prefs.edit().putInt("provider",index).apply()
      })
     }
    }
   }
   Text(provider.note,color=Muted)
   Text(PublicVpnCatalog.explanation(mode,provider),color=Muted)
   if(mode!=VpnAdviceMode.OFF) {
    Text("Setup requires your confirmation in the provider app and Android. AImanage cannot create a VPN server profile automatically.",color=Muted)
    Action("Set up ${provider.name} (user-confirmed)") {
     val packageId=PublicVpnCatalog.packageName(provider)
     val installed=packageId?.let { context.packageManager.getLaunchIntentForPackage(it) }
     try {
      if(installed!=null) context.startActivity(installed)
      else context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(provider.homepage)))
     } catch (_:Exception) { open(Settings.ACTION_VPN_SETTINGS) }
    }
   }
   Action("Review system VPN settings") { open(Settings.ACTION_VPN_SETTINGS) }
   Text("For DNS-only filtering, configure Android Private DNS independently. VPN/DNS changes may affect battery, latency and app connectivity.",color=Muted)
  }
  if(section == "Display & Refresh Rate") {
   val context = LocalContext.current
   Text("Current display rate: ${DeviceReadings.refreshRate(context)?.let { "$it Hz" } ?: "Unavailable"}")
   Text("Supported modes: ${DeviceReadings.supportedRates(context).joinToString { "$it Hz" }}")
   Text("Refresh-rate changes are controlled by Android / HyperOS.",color=Muted)
  }
  if(section == "Ad Blocker" || section == "VPN" || section == "Network & Speed" || section == "Network") {
   Text("Private DNS is configured in Android Settings. DNS filters may block some ads but cannot block all app/video advertisements.",color=Muted)
   Action("Open network / Private DNS settings") { open(Settings.ACTION_WIRELESS_SETTINGS) }
   Action("Open VPN settings") { open(Settings.ACTION_VPN_SETTINGS) }
  }
  if(section == "Thermal" || section == "CPU & Thermal") Text("Current thermal status: $thermal")
  if(section == "Settings") {
   val ctx=LocalContext.current
   val prefs=remember { ctx.getSharedPreferences("aimanage_ui_options",Context.MODE_PRIVATE) }
   var quickPrompts by remember { mutableStateOf(prefs.getBoolean("quick_prompts",true)) }
   var descriptions by remember { mutableStateOf(prefs.getBoolean("show_explanations",true)) }
   Text("AImanage preferences",color=Cyan,fontWeight=FontWeight.Bold)
   Row(verticalAlignment=Alignment.CenterVertically) {
    Text("Show AI quick questions",modifier=Modifier.weight(1f))
    Switch(checked=quickPrompts,onCheckedChange={quickPrompts=it;prefs.edit().putBoolean("quick_prompts",it).apply()})
   }
   Row(verticalAlignment=Alignment.CenterVertically) {
    Text("Show detailed explanations",modifier=Modifier.weight(1f))
    Switch(checked=descriptions,onCheckedChange={descriptions=it;prefs.edit().putBoolean("show_explanations",it).apply()})
   }
   Text("Preferences are stored only on this device; they do not change Android permissions or stop other apps.",color=Muted)
   Action("Review AImanage app permissions") { open(Settings.ACTION_APPLICATION_SETTINGS) }
   Action("Review battery settings") { open(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
   Action("Manage Android notifications") { open(Settings.ACTION_APP_NOTIFICATION_SETTINGS) }
  }
  if(section == "Device Information") { Text("Model: ${Build.MANUFACTURER} ${Build.MODEL}"); Text("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})") }
  Action("Open Android app settings") { open(Settings.ACTION_APPLICATION_SETTINGS) }
  Action("Open battery saver settings") { open(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
  Action("Open system settings") { open(Settings.ACTION_SETTINGS) }
 }
}
private fun launch(context:android.content.Context,action:String) {
 try { context.startActivity(Intent(action)) } catch (_:Exception) { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
}
