package com.aimanage.app

import android.app.Activity
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
private val sections = listOf("Overview","App Management","Background & Autostart","CPU & Thermal","Battery","Network & Speed","Ad Blocker","Firewall","VPN","Permissions","Device Information","Settings")

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
  if(section == "Battery") Text("Charge: ${pct?.let { "$it%" } ?: "Unavailable"}")
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
