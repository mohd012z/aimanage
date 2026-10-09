package com.aimanage.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
private val tabs = listOf("Home","Apps","Thermal","Network","Protect")
private val sections = listOf("Overview","App Management","Background & Autostart","CPU & Thermal","Battery","Battery Care","Charging Intelligence","Brightness & Power","AI Assistant","AI Learning","Security Intelligence","Web Scam Check","Telegram Safety","Notification Center","Automation Control","CPU & App Activity","Caller Intelligence","Voice Caller","Standby Intelligence","Display & Refresh Rate","Network & Speed","Ad Blocker","Firewall","VPN","Permissions","Device Information","Settings")

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AimanageUI() } }
}

@Composable
private fun AimanageUI() {
 val context = LocalContext.current
 var selected by remember { mutableStateOf("Overview") }
 var drawer by remember { mutableStateOf(false) }
 val batteryIntent = remember { context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) }
 val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
 val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
 val pct = if (level >= 0 && scale > 0) level * 100 / scale else null
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
 MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, background = Navy, surface = Panel, onSurface = Color.White)) {
  Column(Modifier.fillMaxSize().background(Navy)) {
   Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
    IconButton(onClick = { drawer = !drawer }) { Icon(Icons.Default.Menu, "Toggle categories") }
    Column(Modifier.weight(1f)) { Text("AImanage", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Android Device Intelligence", color = Muted, style = MaterialTheme.typography.labelSmall) }
    IconButton(onClick = { selected = "Settings" }) { Icon(Icons.Default.Settings, "Settings") }
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
     item { Text(selected, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
     if(selected == "Overview" || selected == "Home") {
      item { PanelCard("Device Health", "Live battery and thermal snapshot", Icons.Default.Favorite) {
       Text("Battery  ${pct?.let { "$it%" } ?: "Unavailable"}", style = MaterialTheme.typography.headlineMedium)
       Text("Thermal status  $thermal", color = Muted)
       Text("Device  ${Build.MANUFACTURER} ${Build.MODEL}", color = Muted)
      } }
      item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
       Box(Modifier.weight(1f)) { SmallCard("Apps","Manage", Icons.Default.Apps) { selected = "App Management" } }
       Box(Modifier.weight(1f)) { SmallCard("Cooling","Monitor", Icons.Default.AcUnit) { selected = "CPU & Thermal" } }
      } }
      item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
       Box(Modifier.weight(1f)) { SmallCard("Firewall","Planned", Icons.Default.Security) { selected = "Firewall" } }
       Box(Modifier.weight(1f)) { SmallCard("VPN","Planned", Icons.Default.VpnLock) { selected = "VPN" } }
      } }
      item { PanelCard("Quick Actions","Android-owned controls",Icons.Default.Tune) {
       Action("Battery settings") { launch(context, Settings.ACTION_BATTERY_SAVER_SETTINGS) }
       Action("Application settings") { launch(context, Settings.ACTION_APPLICATION_SETTINGS) }
       Action("Device settings") { launch(context, Settings.ACTION_SETTINGS) }
      } }
     } else {
      item { DetailSection(selected, pct, thermal) { action -> launch(context, action) } }
     }
    }
   }
   NavigationBar(containerColor = Panel) {
    tabs.forEach { tab ->
     val icon = when(tab) { "Home" -> Icons.Default.Home; "Apps" -> Icons.Default.Apps; "Thermal" -> Icons.Default.DeviceThermostat; "Network" -> Icons.Default.Wifi; else -> Icons.Default.Shield }
     NavigationBarItem(selected = selected == tab || (tab == "Home" && selected == "Overview"), onClick = { selected = tab; drawer = false }, icon = { Icon(icon, tab) }, label = { Text(tab) })
    }
   }
  }
 }
}
@Composable private fun PanelCard(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,body:@Composable ColumnScope.()->Unit) {
 Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()) {
  Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
   Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) { Icon(icon,null,tint=Cyan); Text(title,fontWeight=FontWeight.Bold) }
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
@Composable private fun Action(label:String,click:()->Unit) { OutlinedButton(onClick=click,modifier=Modifier.fillMaxWidth()) { Text(label) } }
@Composable private fun DetailSection(section:String,pct:Int?,thermal:String,open:(String)->Unit) {
 val description = when(section) {
  "Apps","App Management","Background & Autostart" -> "Android restricts silent third-party app control. Open system settings to manage autostart and background restrictions."
  "Thermal","CPU & Thermal" -> "Read-only thermal status. No CPU governor or cooling hardware control."
  "Battery" -> "Battery charge reading and Android battery settings."
  "Network","Network & Speed" -> "Network monitoring and per-app rate limits are future work."
  "Protect","Ad Blocker","Firewall","VPN" -> "Not active. DNS blocking, firewall and remote VPN require a separately implemented VpnService and explicit user consent."
  "Permissions" -> "Only request permissions when a working feature needs them."
  else -> "System information and Android settings."
 }
 PanelCard(section,description,Icons.Default.SettingsSuggest) {
  if(section == "Battery" || section == "Battery Care" || section == "Standby Intelligence") {
   val context = LocalContext.current
   val b = remember { DeviceReadings.battery(context) }
   Text("Charge: ${b.percent?.let { "$it%" } ?: "Unavailable"}")
   Text("Charging: ${if(b.charging) "Yes" else "No"}")
   Text("Battery temperature: ${b.temperatureC?.let { "$it °C" } ?: "Unavailable"}")
   Text("Voltage: ${b.voltageMv?.let { "$it mV" } ?: "Unavailable"}")
   Text("Current: ${b.currentMa?.let { "$it mA" } ?: "Unavailable"}")
   Text("Charge counter: ${b.chargeCounterMah?.let { "$it mAh" } ?: "Unavailable"}")
   Text("Battery health code: ${b.healthCode ?: "Unavailable"} (system status, not capacity estimate)")
   Text("Charging limits cannot be controlled by ordinary Android apps.",color=Muted)
   Text("Standby drain history and charging alerts: planned.",color=Muted)
  }
  if(section == "Charging Intelligence" || section == "Brightness & Power" || section == "AI Assistant") {
   val context = LocalContext.current
   val d = remember { ChargingDiagnostics.assess(context) }
   Text("Battery-side charging power: ${d.batteryPowerW?.let { "%.1f W".format(it) } ?: "Unavailable"}")
   Text("Brightness setting: ${d.brightnessPercent?.let { "$it%" } ?: "Unavailable"}")
   Text("Adaptive brightness: ${d.adaptiveBrightness?.toString() ?: "Unavailable"}")
   Text("Power saver: ${if(d.powerSaveEnabled) "On" else "Off"}")
   d.observations.forEach { Text("• $it", color=Muted) }
   Action("Open display settings") { open(Settings.ACTION_DISPLAY_SETTINGS) }
   Action("Open battery saver") { open(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
   Text("Charging alerts and interactive AI chat are planned; this screen currently provides rule-based observations.",color=Muted)
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
   Text("Protected call/system notifications and ongoing foreground-service notifications are not auto-dismissed.",color=Muted)
   Text("Per-app CPU measurement and service termination are not available to ordinary Android apps.",color=Muted)
  }
  if(section == "CPU & App Activity" || section == "Standby Intelligence") {
   val context = LocalContext.current
   var report by remember { mutableStateOf(ActivityMonitor.report(context)) }
   Text("Thermal pressure: ${report.thermalStatus}")
   Text("Battery Saver: ${if(report.powerSaver) "Enabled" else "Disabled"}")
   Text("Usage access: ${if(report.usageAccessGranted) "Granted" else "Required"}")
   if(!report.usageAccessGranted) Action("Grant usage access") { open(Settings.ACTION_USAGE_ACCESS_SETTINGS) }
   Action("Refresh app activity") { report=ActivityMonitor.report(context) }
   report.recentApps.forEach { app ->
    Text("${app.packageName} — ${app.foregroundMinutes} min foreground",color=Muted,style=MaterialTheme.typography.bodySmall)
   }
   report.notes.forEach { Text("• $it",color=Muted,style=MaterialTheme.typography.bodySmall) }
  }
  if(section == "AI Assistant") {
   val context = LocalContext.current
   var question by remember { mutableStateOf("") }
   var reply by remember { mutableStateOf<AssistantReply?>(null) }
   Text("Offline device assistant — evidence-based answers and permission-aware actions.",color=Muted)
   OutlinedTextField(value=question,onValueChange={question=it},label={Text("Ask AImanage")},modifier=Modifier.fillMaxWidth(),minLines=2)
   Action("Ask assistant") { reply=DeviceAssistant.reply(context,question) }
   reply?.let { answer ->
    Text(answer.message)
    answer.settingsAction?.let { action -> Action("Open Android settings") { open(action) } }
    answer.proposedRule?.let { enabled ->
     Action(if(enabled) "Confirm enable automation" else "Confirm disable automation") {
      DeviceAssistant.setNotificationAutomation(context,enabled)
      reply=AssistantReply("Notification automation ${if(enabled) "enabled" else "disabled"}. Android Notification Access must still be granted.")
     }
     Action("Cancel proposed change") { reply=null }
    }
   }
  }
  if(section == "AI Learning") {
   val alertContext = LocalContext.current
   var healthAlerts by remember { mutableStateOf(DeviceAlertEngine.enabled(alertContext)) }
   Row(verticalAlignment=Alignment.CenterVertically) {
    Text("Device health alerts",modifier=Modifier.weight(1f))
    Switch(checked=healthAlerts,onCheckedChange={requested ->
     if(requested && android.os.Build.VERSION.SDK_INT >= 33 &&
       alertContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
      (alertContext as? Activity)?.requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),9001)
     } else {
      DeviceAlertEngine.setEnabled(alertContext,requested)
      healthAlerts=requested
     }
    })
   }
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
   val monitoringStatus=remember(monitoringRefresh,sampling) { TelemetryScheduler.health(appContext) }
   val lastRun=remember(monitoringRefresh,sampling) { TelemetryScheduler.lastSuccess(appContext) }
   val enabledSince=remember(monitoringRefresh,sampling) { appContext.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE).getLong("telemetry_enabled_since",0L) }
   val workerError=remember(monitoringRefresh,sampling) { TelemetryScheduler.lastError(appContext) }
   Text("Background monitoring: $monitoringStatus",color=Muted)
   Action("Refresh monitoring status") { monitoringRefresh++ }
   val currentRun=lastRun>0L && lastRun>=enabledSince
   Text("Last successful background sample: ${if(lastRun==0L) "Not recorded" else java.text.DateFormat.getDateTimeInstance().format(java.util.Date(lastRun))}${if(sampling && !currentRun && lastRun>0L) " (previous monitoring session)" else ""}",color=Muted)
   workerError?.let { Text("Last worker error category: $it",color=Muted) }

   val context = LocalContext.current
   var samples by remember { mutableStateOf(DeviceLearningEngine.load(context)) }
   Text("Local learning samples: ${samples.size}")
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
    samples=emptyList()
   }
   Text("Learning is rule-based and on-device. Automatic sampling runs only when enabled; no model training or cloud upload.",color=Muted)
  }
  if(section == "Display & Refresh Rate") {
   val context = LocalContext.current
   Text("Current display rate: ${DeviceReadings.refreshRate(context)?.let { "$it Hz" } ?: "Unavailable"}")
   Text("Supported modes: ${DeviceReadings.supportedRates(context).joinToString { "$it Hz" }}")
   Text("Refresh-rate changes are controlled by Android / HyperOS.",color=Muted)
  }
  if(section == "Thermal" || section == "CPU & Thermal") Text("Current thermal status: $thermal")
  if(section == "Device Information") { Text("Model: ${Build.MANUFACTURER} ${Build.MODEL}"); Text("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})") }
  Action("Open Android app settings") { open(Settings.ACTION_APPLICATION_SETTINGS) }
  Action("Open battery saver settings") { open(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
  Action("Open system settings") { open(Settings.ACTION_SETTINGS) }
 }
}
private fun launch(context:android.content.Context,action:String) {
 try { context.startActivity(Intent(action)) } catch (_:Exception) { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
}
