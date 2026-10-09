package com.aimanage.app

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
private val sections = listOf("Overview","App Management","Background & Autostart","CPU & Thermal","Battery","Battery Care","Charging Intelligence","Brightness & Power","AI Assistant","AI Learning","Sleep Check","Security Intelligence","Web Scam Check","Telegram Safety","Notification Center","Automation Control","CPU & App Activity","Caller Intelligence","Voice Caller","Standby Intelligence","Display & Refresh Rate","Network & Speed","Ad Blocker","Firewall","VPN","Permissions","Device Information","Settings")

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
  Column(Modifier.fillMaxSize().background(Navy)) {
   Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
    IconButton(onClick = { drawer = !drawer }) { Icon(Icons.Default.Menu, "Toggle categories", tint=Color.White) }
    Column(Modifier.weight(1f)) { Text("AImanage",color=Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Android Device Intelligence", color = Muted, style = MaterialTheme.typography.labelSmall) }
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
     item { Text(selected, color=Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
     if(selected == "Overview" || selected == "Home") {
      item { PanelCard("Device Health", "Live battery and thermal snapshot", Icons.Default.Favorite) {
       Text("Battery  ${pct?.let { "$it%" } ?: "Unavailable"}", color=Color.White,style = MaterialTheme.typography.headlineMedium)
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
      item { PanelCard("AI Device Advisor","App review, bedtime battery test and network guidance",Icons.Default.SmartToy) {
       Action("Open AI Assistant") { selected="AI" }
       Action("Overnight sleep check") { selected="Sleep Check" }
      } }
      item { PanelCard("Quick Actions","Android-owned controls",Icons.Default.Tune) {
       Action("Battery settings") { launch(context, Settings.ACTION_BATTERY_SAVER_SETTINGS) }
       Action("Application settings") { launch(context, Settings.ACTION_APPLICATION_SETTINGS) }
       Action("Device settings") { launch(context, Settings.ACTION_SETTINGS) }
      } }
     } else {
      item { DetailSection(if(selected=="AI") "AI Assistant" else selected, pct, thermal) { action -> launch(context, action) } }
     }
    }
   }
   NavigationBar(containerColor = Panel) {
    tabs.forEach { tab ->
     val icon = when(tab) { "Home" -> Icons.Default.Home; "Apps" -> Icons.Default.Apps; "Network" -> Icons.Default.Wifi; "AI" -> Icons.Default.SmartToy; else -> Icons.Default.Shield }
     NavigationBarItem(selected = selected == tab || (tab == "Home" && selected == "Overview"), onClick = { selected = tab; drawer = false }, icon = { Icon(icon, tab) }, label = { Text(tab) })
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
  "Apps","App Management","Background & Autostart" -> "Review recent activity with evidence. Only Android Settings can restrict or uninstall other apps; never force-stop important services automatically."
  "Thermal","CPU & Thermal" -> "Live Android thermal status and safer cooling advice. Background CPU usage by other apps is not available to normal apps."
  "Battery" -> "Battery charge reading and Android battery settings."
  "Network","Network & Speed" -> "Read-only active network and Android connectivity validation; real throughput testing and per-app rate limits are not implemented."
  "Protect","Ad Blocker","Firewall","VPN" -> "Protection guidance: security review, Private DNS, notification settings and VPN status. No active firewall or VPN tunnel."
  "Permissions" -> "Only request permissions when a working feature needs them."
  else -> "System information and Android settings."
 }
 PanelCard(section,description,Icons.Default.SettingsSuggest) {
  if(section == "Apps" || section == "App Management" || section == "Background & Autostart") {
   val ctx=LocalContext.current
   var activity by remember { mutableStateOf(ActivityMonitor.report(ctx)) }
   Text("App review • evidence-based",fontWeight=FontWeight.Bold,color=Cyan)
   Text(if(ctx.packageName.endsWith(".advanced"))
    "Optional Usage Access shows past foreground time, NOT running background processes or app-specific battery drain."
    else "Standard edition deliberately has no sensitive Usage Access. See Android Battery > App battery usage for verified per-app energy use.",color=Muted)
   if(!activity.usageAccessGranted && ctx.packageName.endsWith(".advanced"))
    Action("Grant optional usage access") { open(Settings.ACTION_USAGE_ACCESS_SETTINGS) }
   AppReviewPolicy.recentUsage(activity.recentApps,ctx.packageName).forEach { finding ->
    HorizontalDivider(color=Muted.copy(alpha=0.35f))
    Text(finding.packageName,color=Color.White,fontWeight=FontWeight.SemiBold)
    Text("Foreground (past 24h): ${finding.foregroundMinutes} min",color=Muted)
    Text(finding.note,color=Muted,style=MaterialTheme.typography.bodySmall)
    Action("Review app in Android Settings") { openAppDetails(ctx,finding.packageName) }
   }
   Text(AppReviewPolicy.safetyNotice(),color=Muted)
   Action("Refresh observed app activity") { activity=ActivityMonitor.report(ctx) }
   Action("Open Android Battery settings") { open(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
   Action("Review installed apps / uninstall manually") { open(Settings.ACTION_APPLICATION_SETTINGS) }
  }
  if(section == "Protect") {
   Text("Safety-first phone protection",fontWeight=FontWeight.Bold,color=Cyan)
   Text("Review installed apps in Android Settings; only uninstall software you recognize as unneeded. Do not remove system, authentication, work, accessibility or security services.",color=Muted)
   Action("Review apps and permissions") { open(Settings.ACTION_APPLICATION_SETTINGS) }
   Text("Reduce notification noise without missing calls, alarms, chats or security alerts. Adjust unimportant app categories in Android Settings.",color=Muted)
   Action("Review notification settings") { open(Settings.ACTION_APP_NOTIFICATION_SETTINGS) }
   Text("Private DNS can filter some ad domains, but AImanage does not alter DNS or intercept traffic.",color=Muted)
   Action("Open network / Private DNS settings") { open(Settings.ACTION_WIRELESS_SETTINGS) }
   Action("Review existing VPN") { open(Settings.ACTION_VPN_SETTINGS) }
  }
  if(section == "Thermal" || section == "CPU & Thermal") {
   val ctx=LocalContext.current
   var reading by remember { mutableStateOf(DeviceReadings.battery(ctx)) }
   Text("Battery temperature: ${reading.temperatureC?.let { "$it °C" } ?: "Unavailable"}",color=Color.White)
   Text("Cooling advice",fontWeight=FontWeight.Bold,color=Cyan)
   Text("If the phone is hot, pause demanding foreground tasks, avoid direct sunlight and heavy charging, and allow Android thermal safeguards to work. There is no reliable per-app heat attribution or forced cooling available to this app.",color=Muted)
   Action("Refresh battery temperature") { reading=DeviceReadings.battery(ctx) }
  }
  if(section == "Standby Intelligence" || section == "Sleep Check") {
   val ctx=LocalContext.current
   val prefs=remember { ctx.getSharedPreferences("sleep_baseline",Context.MODE_PRIVATE) }
   var startedAt by remember { mutableLongStateOf(prefs.getLong("started_at",0L)) }
   var startPercent by remember { mutableIntStateOf(prefs.getInt("start_percent",-1)) }
   var outcome by remember { mutableStateOf("No sleep comparison captured yet.") }
   Text("Overnight idle baseline • manual comparison",fontWeight=FontWeight.Bold,color=Cyan)
   Text("Before sleep, tap Start; after waking, tap Finish. AImanage compares battery percentage at both moments. It cannot prove that the screen remained off or identify which app caused any drain.",color=Muted)
   Text(if(startedAt>0) "Baseline started: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(startedAt))} at $startPercent%" else "Baseline not started.",color=Muted)
   Action("Start bedtime battery baseline") {
    val b=DeviceReadings.battery(ctx)
    if(b.percent!=null && !b.charging) {
     startedAt=System.currentTimeMillis();startPercent=b.percent
     prefs.edit().putLong("started_at",startedAt).putInt("start_percent",startPercent).apply()
     outcome="Baseline started; compare after at least one hour without charging."
    } else outcome="Unplug charging and check battery reading before starting."
   }
   Action("Finish and compare after sleep") {
    val b=DeviceReadings.battery(ctx)
    val result=SleepBaselinePolicy.calculate(startedAt,startPercent,System.currentTimeMillis(),b.percent ?: -1,b.charging)
    outcome=if(result==null) "No reliable baseline: requires 1–24 hours, discharging and valid battery levels. A charging or clock-changed session cannot be compared."
      else "Observed ${result.percentPointsLost} percentage points over ${"%.1f".format(result.elapsedHours)} hours (${"%.2f".format(result.pointsPerHour)} points/hour). This is a whole-device observation, not per-app attribution."
    if(result!=null){startedAt=0L;startPercent=-1;prefs.edit().clear().apply()}
   }
   Text(outcome,color=Muted)
   Text("While sleeping, Android may use Doze/App Standby to defer background jobs and network access, while permitted calls, alarms and priority alerts can still wake the device. Charging, Wi-Fi/cellular signal and notifications can affect observed use.",color=Muted)
  }
  if(section == "Battery" || section == "Battery Care") {
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
  if(section == "Charging Intelligence" || section == "Brightness & Power" || section == "AI Assistant") {
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
  if(section == "CPU & App Activity") {
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
  if(section == "AI Assistant") {
   val context = LocalContext.current
   var question by remember { mutableStateOf("") }
   var reply by remember { mutableStateOf<AssistantReply?>(null) }
   Text("Offline device assistant — evidence-based answers and permission-aware actions.",color=Muted)
   OutlinedTextField(value=question,onValueChange={question=it},label={Text("Ask AImanage")},modifier=Modifier.fillMaxWidth(),minLines=2)
   Action("Ask assistant") { reply=DeviceAssistant.reply(context,question) }
   Text("Quick questions",color=Muted,style=MaterialTheme.typography.labelMedium)
   val quickQuestions=listOf("Which apps should I close?","Which apps can I uninstall?","Mute unneeded notifications","Phone hot: which apps?","Sleeping standby drain","Save battery without slowing apps","Private DNS block ads","Network speed")
   quickQuestions.forEach { prompt ->
    TextButton(onClick={ question=prompt; reply=DeviceAssistant.reply(context,prompt) }) { Text(prompt) }
   }
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
  if(section == "Thermal" || section == "CPU & Thermal") Text("Current thermal status: $thermal",color=Color.White)
  if(section == "Device Information") { Text("Model: ${Build.MANUFACTURER} ${Build.MODEL}"); Text("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})") }
  Action("Open Android app settings") { open(Settings.ACTION_APPLICATION_SETTINGS) }
  Action("Open battery saver settings") { open(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
  Action("Open system settings") { open(Settings.ACTION_SETTINGS) }
 }
}
private fun openAppDetails(context:Context,packageId:String) {
 try {
  val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
   android.net.Uri.parse("package:$packageId"))
  context.startActivity(intent)
 } catch (_:Exception) { launch(context,Settings.ACTION_APPLICATION_SETTINGS) }
}
private fun launch(context:android.content.Context,action:String) {
 try { context.startActivity(Intent(action)) } catch (_:Exception) { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
}
