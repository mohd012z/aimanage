package com.aimanage.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/** Notification permission and user opt-in are both required. */
object DeviceAlertEngine {
 private const val CHANNEL="device_health_alerts"
 private const val PREFS="aimanage_rules"
 private const val COOLDOWN=2*60*60*1000L
 fun enabled(context:Context)=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getBoolean("health_alerts_enabled",false)
 fun setEnabled(context:Context,value:Boolean) {
  context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putBoolean("health_alerts_enabled",value).apply()
 }
 fun check(context:Context,sample:DeviceLearningSample) {
  if(!enabled(context)) return
  if(!TelemetryScheduler.enabled(context)) return
  if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return
  val critical=sample.thermalStatus in setOf("Severe","Critical","Emergency","Shutdown")
  val warm=sample.batteryTempC?.let { it>=40f } ?: false
  if(!critical && !warm) return
  val prefs=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
  val now=System.currentTimeMillis()
  val last=prefs.getLong("last_health_alert",0L)
  if(now-last<COOLDOWN) return
  val manager=context.getSystemService(NotificationManager::class.java)
  manager.createNotificationChannel(NotificationChannel(CHANNEL,"Device health alerts",NotificationManager.IMPORTANCE_DEFAULT))
  val intent=Intent(context,MainActivity::class.java)
  val pending=PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val body=if(critical) "Android reports ${sample.thermalStatus} thermal pressure." else "Battery sensor reports ${sample.batteryTempC} °C."
  val notification=android.app.Notification.Builder(context,CHANNEL)
   .setSmallIcon(android.R.drawable.ic_dialog_alert)
   .setContentTitle("AImanage: device health warning")
   .setContentText(body)
   .setContentIntent(pending)
   .setAutoCancel(true)
   .build()
  manager.notify(101,notification)
  prefs.edit().putLong("last_health_alert",now).apply()
 }
}
