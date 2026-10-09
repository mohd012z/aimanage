package com.aimanage.app

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.view.Display
import android.hardware.display.DisplayManager

data class BatterySnapshot(
 val percent: Int?, val temperatureC: Float?, val voltageMv: Int?,
 val currentMa: Int?, val chargeCounterMah: Int?, val charging: Boolean,
 val healthCode: Int?
)
object DeviceReadings {
 fun battery(context: Context): BatterySnapshot {
  val i = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
  val manager = context.getSystemService(BatteryManager::class.java)
  val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
  val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
  val temp = i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
  val voltage = i?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
  val status = i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
  val health = i?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
  val current = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
  val counter = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
  return BatterySnapshot(
   BatteryPercentagePolicy.fromLevelAndScale(level, scale),
   if(temp >= 0) temp / 10f else null,
   voltage.takeIf { it >= 0 },
   current.takeIf { it != Int.MIN_VALUE }?.div(1000),
   counter.takeIf { it != Int.MIN_VALUE }?.div(1000),
   status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL,
   health.takeIf { it >= 0 }
  )
 }
 fun refreshRate(context: Context): Float? {
  val manager = context.getSystemService(DisplayManager::class.java)
  return manager.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate
 }
 fun supportedRates(context: Context): List<Float> {
  val manager = context.getSystemService(DisplayManager::class.java)
  return manager.getDisplay(Display.DEFAULT_DISPLAY)?.supportedModes?.map { it.refreshRate }?.distinct()?.sorted() ?: emptyList()
 }
}
