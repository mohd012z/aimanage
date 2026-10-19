package com.aimanage.app

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/** Read-only battery diagnostics. Unavailable data is represented as null. */
data class BatteryDiagnostics(
 val levelPercent:Int?, val health:String, val status:String, val source:String,
 val technology:String?, val temperatureC:Double?, val voltageMv:Int?,
 val currentMa:Double?, val powerW:Double?, val cycleCount:Int?
)

internal object BatteryDiagnosticMath {
 fun powerWatts(currentMicroamps:Int?, voltageMillivolts:Int?):Double? =
  if(currentMicroamps != null && voltageMillivolts != null && voltageMillivolts > 0)
   currentMicroamps.toDouble() * voltageMillivolts / 1_000_000_000.0 else null
 fun currentMilliamps(currentMicroamps:Int?):Double? = currentMicroamps?.div(1000.0)
}

object BatteryDiagnosticsReader {
 fun read(context:Context):BatteryDiagnostics {
  val intent = context.registerReceiver(null,IntentFilter(Intent.ACTION_BATTERY_CHANGED))
  fun extra(key:String):Int? = intent?.getIntExtra(key,Int.MIN_VALUE)?.takeIf { it != Int.MIN_VALUE }
  val level=extra(BatteryManager.EXTRA_LEVEL)
  val scale=extra(BatteryManager.EXTRA_SCALE)
  val percent=if(level!=null && scale!=null && scale>0) (level*100/scale).coerceIn(0,100) else null
  val health=when(extra(BatteryManager.EXTRA_HEALTH)) {
   BatteryManager.BATTERY_HEALTH_GOOD->"Good"
   BatteryManager.BATTERY_HEALTH_OVERHEAT->"Overheat"
   BatteryManager.BATTERY_HEALTH_DEAD->"Dead"
   BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE->"Over-voltage"
   BatteryManager.BATTERY_HEALTH_COLD->"Cold"
   else->"Unknown"
  }
  val status=when(extra(BatteryManager.EXTRA_STATUS)) {
   BatteryManager.BATTERY_STATUS_CHARGING->"Charging"
   BatteryManager.BATTERY_STATUS_DISCHARGING->"Discharging"
   BatteryManager.BATTERY_STATUS_FULL->"Full"
   BatteryManager.BATTERY_STATUS_NOT_CHARGING->"Not charging"
   else->"Unknown"
  }
  val source=when(extra(BatteryManager.EXTRA_PLUGGED)) {
   BatteryManager.BATTERY_PLUGGED_AC->"AC"
   BatteryManager.BATTERY_PLUGGED_USB->"USB"
   BatteryManager.BATTERY_PLUGGED_WIRELESS->"Wireless"
   else->"Battery / unknown"
  }
  val manager=context.getSystemService(BatteryManager::class.java)
  val rawCurrent=runCatching { manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) }
   .getOrNull()?.takeIf { it != Int.MIN_VALUE }
  val voltage=extra(BatteryManager.EXTRA_VOLTAGE)?.takeIf { it > 0 }
  return BatteryDiagnostics(
   percent,health,status,source,
   intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY),
   extra(BatteryManager.EXTRA_TEMPERATURE)?.div(10.0),
   voltage,BatteryDiagnosticMath.currentMilliamps(rawCurrent),
   BatteryDiagnosticMath.powerWatts(rawCurrent,voltage),
   null // OEM charge-cycle readings are not uniformly exposed across supported Android versions
  )
 }
}
