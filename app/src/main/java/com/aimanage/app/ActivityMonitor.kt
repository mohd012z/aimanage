package com.aimanage.app

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import android.os.PowerManager
import java.util.concurrent.TimeUnit

data class AppActivity(val packageName:String,val foregroundMinutes:Long,val lastUsedMillis:Long)
data class DeviceActivityReport(
 val usageAccessGranted:Boolean,
 val recentApps:List<AppActivity>,
 val thermalStatus:String,
 val powerSaver:Boolean,
 val notes:List<String>
)
object ActivityMonitor {
 fun report(context:Context):DeviceActivityReport {
  val ops=context.getSystemService(AppOpsManager::class.java)
  val mode=ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,Process.myUid(),context.packageName)
  val granted=mode==AppOpsManager.MODE_ALLOWED
  val now=System.currentTimeMillis()
  val stats=if(granted) try {
   val manager=context.getSystemService(UsageStatsManager::class.java)
   manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,now-TimeUnit.HOURS.toMillis(24),now)
    .orEmpty().filter { it.totalTimeInForeground>0 }.sortedByDescending { it.totalTimeInForeground }
    .take(20).map { AppActivity(it.packageName,TimeUnit.MILLISECONDS.toMinutes(it.totalTimeInForeground),it.lastTimeUsed) }
  }catch (_:Exception){emptyList()} else emptyList()
  val pm=context.getSystemService(PowerManager::class.java)
  val thermal=when(pm.currentThermalStatus) {
   PowerManager.THERMAL_STATUS_NONE -> "Normal"
   PowerManager.THERMAL_STATUS_LIGHT -> "Light"
   PowerManager.THERMAL_STATUS_MODERATE -> "Moderate"
   PowerManager.THERMAL_STATUS_SEVERE -> "Severe"
   PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
   PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
   PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown"
   else -> "Unknown"
  }
  return DeviceActivityReport(granted,stats,thermal,pm.isPowerSaveMode,listOf(
   "Foreground usage is not CPU usage, background CPU usage, or battery drain.",
   "Android does not expose other apps' per-process CPU utilization to ordinary apps.",
   "Usage stats may be delayed or incomplete depending on device policy."
  ))
 }
}
