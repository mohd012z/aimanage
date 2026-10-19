package com.aimanage.app

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.os.StatFs
import android.os.SystemClock

/** A snapshot is not a CPU utilization percentage. CPU time requires two samples. */
data class MonitorSnapshot(
 val elapsedMs: Long,
 val appCpuMs: Long,
 val ramTotal: Long?,
 val ramAvailable: Long?,
 val ramLow: Boolean?,
 val storageTotal: Long?,
 val storageAvailable: Long?,
 val batteryPercent: Int?,
 val batteryTemperatureC: Float?,
 val batteryCurrentMa: Int?,
 val thermalStatus: Int?,
 val powerSave: Boolean?
)

internal object MonitorMath {
 /** App process CPU time across all threads; can exceed 100% on multicore devices. */
 fun appCpuPercent(older: MonitorSnapshot, newer: MonitorSnapshot): Double? {
  val wall = newer.elapsedMs - older.elapsedMs
  val cpu = newer.appCpuMs - older.appCpuMs
  return if (wall > 0 && cpu >= 0) cpu.toDouble() * 100.0 / wall else null
 }
 fun usedPercent(total: Long?, available: Long?): Int? =
  if(total != null && available != null && total > 0 && available in 0..total)
   ((total - available).toDouble() * 100 / total).toInt() else null
}

object DeviceMonitor {
 fun capture(context: Context): MonitorSnapshot {
  val memory = runCatching {
   ActivityManager.MemoryInfo().also {
    context.getSystemService(ActivityManager::class.java).getMemoryInfo(it)
   }
  }.getOrNull()
  val storage = runCatching { StatFs(Environment.getDataDirectory().absolutePath) }.getOrNull()
  val battery = DeviceReadings.battery(context)
  val power = context.getSystemService(PowerManager::class.java)
  return MonitorSnapshot(
   SystemClock.elapsedRealtime(),
   Process.getElapsedCpuTime(),
   memory?.totalMem?.takeIf { it > 0 },
   memory?.availMem?.takeIf { it >= 0 },
   memory?.lowMemory,
   storage?.totalBytes?.takeIf { it > 0 },
   storage?.availableBytes?.takeIf { it >= 0 },
   battery.percent,
   battery.temperatureC,
   battery.currentMa,
   if(Build.VERSION.SDK_INT >= 29) power.currentThermalStatus else null,
   power.isPowerSaveMode
  )
 }
}
