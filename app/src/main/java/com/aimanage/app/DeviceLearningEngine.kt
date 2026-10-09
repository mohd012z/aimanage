package com.aimanage.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class DeviceLearningSample(
 val timestamp: Long, val batteryPercent: Int?, val batteryTempC: Float?,
 val charging: Boolean, val thermalStatus: String, val powerSaver: Boolean
)
data class DeviceInsight(val severity: String, val title: String, val evidence: String, val suggestion: String)

/** Local, bounded history with transparent rule-based learning. No external uploads. */
object DeviceLearningEngine {
 private const val KEY = "samples"
 private const val MAX_SAMPLES = 168
 fun capture(context: Context): DeviceLearningSample {
  val battery = DeviceReadings.battery(context)
  val power = context.getSystemService(android.os.PowerManager::class.java)
  return DeviceLearningSample(System.currentTimeMillis(),battery.percent,battery.temperatureC,
   battery.charging,when(power.currentThermalStatus) {
    android.os.PowerManager.THERMAL_STATUS_SEVERE -> "Severe"
    android.os.PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
    android.os.PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
    android.os.PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown"
    android.os.PowerManager.THERMAL_STATUS_MODERATE -> "Moderate"
    android.os.PowerManager.THERMAL_STATUS_LIGHT -> "Light"
    else -> "Normal"
   },power.isPowerSaveMode)
 }
 fun record(context: Context, sample: DeviceLearningSample): Int {
  val prefs=context.getSharedPreferences("device_learning",Context.MODE_PRIVATE)
  val history=load(context).toMutableList()
  // Keep only one sample every 15 minutes; no permanent background service.
  if(!LearningSamplingPolicy.shouldRecord(history.lastOrNull()?.timestamp,sample.timestamp)) return history.size
  history.add(sample)
  val arr=JSONArray()
  history.takeLast(MAX_SAMPLES).forEach {
   arr.put(JSONObject().put("time",it.timestamp).put("battery",it.batteryPercent)
    .put("temp",it.batteryTempC?.toDouble()).put("charging",it.charging)
    .put("thermal",it.thermalStatus).put("saver",it.powerSaver))
  }
  prefs.edit().putString(KEY,arr.toString()).apply()
  return arr.length()
 }
 fun load(context: Context): List<DeviceLearningSample> {
  val raw=context.getSharedPreferences("device_learning",Context.MODE_PRIVATE).getString(KEY,"[]") ?: "[]"
  return try {
   val arr=JSONArray(raw)
   (0 until arr.length()).map { i ->
    val o=arr.getJSONObject(i)
    DeviceLearningSample(o.optLong("time"),if(o.isNull("battery")) null else o.optInt("battery"),
     if(o.isNull("temp")) null else o.optDouble("temp").toFloat(),
     o.optBoolean("charging"),o.optString("thermal"),o.optBoolean("saver"))
   }
  } catch (_:Exception) { emptyList() }
 }
 fun analyze(samples: List<DeviceLearningSample>): List<DeviceInsight> {
  if(samples.isEmpty()) return listOf(DeviceInsight("INFO","No history","No measurements recorded.","Capture a sample to begin learning."))
  val last=samples.last()
  val insights=mutableListOf<DeviceInsight>()
  if(last.batteryTempC != null && last.batteryTempC >= 40f)
   insights += DeviceInsight("WARNING","Elevated battery temperature",
    "Latest battery reading: ${last.batteryTempC} °C.",
    "Reduce demanding activity and check the device's charging environment.")
  if(last.thermalStatus in setOf("Severe","Critical","Emergency","Shutdown"))
   insights += DeviceInsight("WARNING","Android reports thermal pressure",
    "System thermal status: ${last.thermalStatus}.","Allow Android thermal safeguards to operate.")
  val recent=samples.takeLast(24).filter { !it.charging && it.batteryPercent != null }
  if(recent.size>=2 && recent.zipWithNext().all { (a,b) -> b.timestamp > a.timestamp && b.batteryPercent!! <= a.batteryPercent!! }) {
   val first=recent.first(); val end=recent.last()
   val elapsed=(end.timestamp-first.timestamp)/3600000.0
   if(elapsed>=1 && end.batteryPercent!! <= first.batteryPercent!!) {
    val rate=(first.batteryPercent-end.batteryPercent).toDouble()/elapsed
    if(rate>=5) insights += DeviceInsight("CAUTION","High observed discharge rate",
     String.format(Locale.US,"%.1f percentage points/hour across %.1f hours; not necessarily screen-off drain.",rate,elapsed),
     "Compare a dedicated screen-off session and review recent app usage.")
   }
  }
  if(samples.size<4) insights += DeviceInsight("INFO","Learning baseline incomplete",
   "${samples.size} samples recorded.","Collect more measurements across normal charging and idle periods.")
  if(insights.isEmpty()) insights += DeviceInsight("INFO","No rule threshold crossed",
   "${samples.size} local samples analyzed.","Continue sampling; absence of alerts is not proof of optimal performance.")
  return insights
 }
 fun clear(context:Context) { context.getSharedPreferences("device_learning",Context.MODE_PRIVATE).edit().clear().apply() }
}
