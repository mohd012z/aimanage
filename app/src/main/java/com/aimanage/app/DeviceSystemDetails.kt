package com.aimanage.app

import android.os.Build
import android.os.SystemClock
import java.io.File
import java.util.TimeZone
import java.util.Locale

/** Part 2 device and system diagnostics. No identifiers are transmitted or persisted. */
data class DeviceSystemDetails(
 val brand:String,val manufacturer:String,val model:String,val device:String,
 val board:String,val hardware:String,val product:String,val fingerprint:String,
 val androidRelease:String,val sdk:Int,val securityPatch:String?,
 val buildId:String,val baseband:String?,val kernel:String,
 val javaVersion:String,val language:String,val timezone:String,
 val supportedAbis:List<String>,val cores:Int,
 val coreFrequencies:List<CoreFrequency>
)
data class CoreFrequency(val core:Int,val currentKhz:Long?,val minKhz:Long?,val maxKhz:Long?,val governor:String?)

internal object CoreFrequencyReader {
 fun readNumber(path:String):Long?=runCatching {
  File(path).takeIf { it.isFile && it.canRead() }?.readText()?.trim()?.toLongOrNull()?.takeIf { it>0L }
 }.getOrNull()
 fun readText(path:String):String?=runCatching {
  File(path).takeIf { it.isFile && it.canRead() }?.readText()?.trim()?.takeIf { it.isNotEmpty() }
 }.getOrNull()
 fun read():List<CoreFrequency> {
  val count=Runtime.getRuntime().availableProcessors().coerceIn(1,256)
  return (0 until count).map { i ->
   val root="/sys/devices/system/cpu/cpu$i/cpufreq/"
   CoreFrequency(i,readNumber(root+"scaling_cur_freq"),readNumber(root+"cpuinfo_min_freq"),
    readNumber(root+"cpuinfo_max_freq"),readText(root+"scaling_governor"))
  }
 }
}

object DeviceSystemReader {
 fun read():DeviceSystemDetails = DeviceSystemDetails(
  Build.BRAND,Build.MANUFACTURER,Build.MODEL,Build.DEVICE,Build.BOARD,
  Build.HARDWARE,Build.PRODUCT,Build.FINGERPRINT,Build.VERSION.RELEASE,
  Build.VERSION.SDK_INT,Build.VERSION.SECURITY_PATCH,Build.ID,
  runCatching { Build.getRadioVersion() }.getOrNull(),
  System.getProperty("os.version") ?: "Unavailable",
  System.getProperty("java.vm.version") ?: "Unavailable",
  Locale.getDefault().toLanguageTag(),TimeZone.getDefault().id,
  Build.SUPPORTED_ABIS.toList(),Runtime.getRuntime().availableProcessors(),
  CoreFrequencyReader.read()
 )
}
