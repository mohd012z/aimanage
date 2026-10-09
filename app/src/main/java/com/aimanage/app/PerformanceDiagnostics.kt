package com.aimanage.app

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.os.SystemClock

/** Read-only metrics. No root, task termination, CPU governor, or clock manipulation. */
data class PerformanceSnapshot(
 val totalRamBytes: Long, val availableRamBytes: Long,
 val lowMemory: Boolean, val cores: Int,
 val appCpuTimeMillis: Long, val elapsedRealtimeMillis: Long
)

internal object PerformanceAdvicePolicy {
 fun ramPercent(snapshot:PerformanceSnapshot):Int? =
  if(snapshot.totalRamBytes>0L && snapshot.availableRamBytes in 0L..snapshot.totalRamBytes)
   (((snapshot.totalRamBytes-snapshot.availableRamBytes).toDouble()/snapshot.totalRamBytes)*100).toInt()
  else null

 fun assess(snapshot:PerformanceSnapshot, thermal:String):String {
  val thermalPressure=thermal in setOf("Severe","Critical","Emergency","Shutdown")
  if(thermalPressure) return "Android reports high thermal pressure: allow system throttling and pause demanding foreground work. Do not force-stop critical services."
  if(snapshot.lowMemory) return "Android reports low memory. Close an unused demanding foreground app normally, and allow Android to manage cached background processes. Keep essential notifications and services active."
  return "No low-memory warning from Android. Free RAM is not a performance target: cached apps can improve reopening speed. Avoid memory cleaners and indiscriminate force-stop actions."
 }
}

object PerformanceDiagnostics {
 fun snapshot(context:Context):PerformanceSnapshot {
  val manager=context.getSystemService(ActivityManager::class.java)
  val info=ActivityManager.MemoryInfo()
  try { manager?.getMemoryInfo(info) } catch (_:Exception) {}
  return PerformanceSnapshot(
   info.totalMem.coerceAtLeast(0L),
   info.availMem.coerceAtLeast(0L),
   info.lowMemory,
   Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
   Process.getElapsedCpuTime().coerceAtLeast(0L),
   SystemClock.elapsedRealtime()
  )
 }
}
