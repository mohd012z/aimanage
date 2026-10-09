package com.aimanage.app

import android.content.Context
import kotlinx.coroutines.CancellationException
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Opt-in best-effort background samples. WorkManager is not an exact timer. */
class TelemetryWorker(context:Context, params:WorkerParameters):CoroutineWorker(context,params) {
 override suspend fun doWork():Result {
  val prefs=applicationContext.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE)
  if(!prefs.getBoolean("telemetry_enabled",false)) return Result.success()
  return try {
   val sample=DeviceLearningEngine.capture(applicationContext)
   DeviceLearningEngine.record(applicationContext,sample)
   DeviceAlertEngine.check(applicationContext,sample)
   prefs.edit().putLong("telemetry_last_success",System.currentTimeMillis()).remove("telemetry_last_error").remove("telemetry_last_error_at").apply()
   Result.success()
  } catch (cancelled:CancellationException) {
   throw cancelled
  } catch (error:Exception) {
   prefs.edit().putLong("telemetry_last_error_at",System.currentTimeMillis()).putString("telemetry_last_error",when(error) {
    is SecurityException -> "Permission denied"
    is java.io.IOException -> "Device I/O unavailable"
    is IllegalStateException -> "Device state unavailable"
    else -> "Telemetry sampling error"
   }).apply()
   Result.retry()
  }
 }
}
object TelemetryScheduler {
 private const val NAME="aimanage_telemetry"
 fun setEnabled(context:Context,enabled:Boolean) {
  val prefs=context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE)
  val wasEnabled=prefs.getBoolean("telemetry_enabled",false)
  val edit=prefs.edit().putBoolean("telemetry_enabled",enabled)
  if(enabled && !wasEnabled) edit.putLong("telemetry_enabled_since",System.currentTimeMillis())
  edit.apply()
  val manager=WorkManager.getInstance(context)
  if(enabled) {
   val request=PeriodicWorkRequestBuilder<TelemetryWorker>(30,TimeUnit.MINUTES)
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS)
    .build()
   manager.enqueueUniquePeriodicWork(NAME,ExistingPeriodicWorkPolicy.KEEP,request)
  } else {
   manager.cancelUniqueWork(NAME)
   context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE).edit().remove("telemetry_last_error").remove("telemetry_last_error_at").apply()
  }
 }
 /** A delayed sample is not necessarily a failure: Doze and OEM policies defer work. */
 fun health(context:Context,now:Long=System.currentTimeMillis()):String {
  if(!enabled(context)) return "Disabled"
  val last=lastSuccess(context)
  val enabledSince=context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE).getLong("telemetry_enabled_since",0L)
  if(last==0L || last<enabledSince) return "Awaiting first background sample"
  val age=now-last
  return if(age<0L) "Device clock changed" else if(age>3*60*60*1000L) "Delayed: last sample over 3 hours ago" else "Recent sample recorded"
 }
 fun lastSuccess(context:Context):Long=context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE).getLong("telemetry_last_success",0L)
 fun lastError(context:Context):String?=context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE).getString("telemetry_last_error",null)
 fun lastErrorAt(context:Context):Long=context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE).getLong("telemetry_last_error_at",0L)
 fun enabled(context:Context)=context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE)
  .getBoolean("telemetry_enabled",false)
}
