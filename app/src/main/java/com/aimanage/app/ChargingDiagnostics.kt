package com.aimanage.app

import android.content.Context
import android.provider.Settings
import android.os.PowerManager

data class ChargingAssessment(
 val batteryPowerW: Double?,
 val brightnessPercent: Int?,
 val adaptiveBrightness: Boolean?,
 val powerSaveEnabled: Boolean,
 val observations: List<String>
)

object ChargingDiagnostics {
 fun assess(context: Context): ChargingAssessment {
  val b = DeviceReadings.battery(context)
  val pm = context.getSystemService(PowerManager::class.java)
  val brightness = try { Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) } catch (_:Exception) { -1 }
  val mode = try { Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) } catch (_:Exception) { -1 }
  val watts = if(b.charging && b.currentMa != null && b.voltageMv != null) {
   kotlin.math.abs(b.currentMa.toDouble() * b.voltageMv.toDouble()) / 1_000_000.0
  } else null
  val observations = buildList {
   if(!b.charging) add("Device is not reporting an active charging session.")
   if(b.temperatureC == null) add("Battery temperature is unavailable.")
   else if(b.temperatureC >= 40f) add("Battery temperature is elevated; charging performance may be thermally limited.")
   if(b.currentMa == null) add("Charging current is unavailable; charging power cannot be estimated.")
   if(b.charging && b.currentMa != null && b.currentMa <= 0) add("Current direction is device-dependent; reported current does not confirm incoming charge power.")
   if(watts != null) add("Estimated battery-side power: %.1f W. This is not charger input power or Xiaomi fast-charge certification.".format(java.util.Locale.US,watts))
   if(pm.isPowerSaveMode) add("Android Battery Saver is enabled.")
   add("For accurate slow-charge diagnosis, compare repeated samples over a charging session.")
  }
  return ChargingAssessment(watts,brightness.takeIf { it >= 0 }?.times(100)?.div(255),mode.takeIf { it >= 0 }?.let { it == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC },pm.isPowerSaveMode,observations)
 }
}
