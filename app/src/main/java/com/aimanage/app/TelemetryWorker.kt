package com.aimanage.app

import android.content.Context
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
   Result.success()
  } catch (_:Exception) { Result.retry() }
 }
}
object TelemetryScheduler {
 private const val NAME="aimanage_telemetry"
 fun setEnabled(context:Context,enabled:Boolean) {
  context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE)
   .edit().putBoolean("telemetry_enabled",enabled).apply()
  val manager=WorkManager.getInstance(context)
  if(enabled) {
   val request=PeriodicWorkRequestBuilder<TelemetryWorker>(30,TimeUnit.MINUTES).build()
   manager.enqueueUniquePeriodicWork(NAME,ExistingPeriodicWorkPolicy.KEEP,request)
  } else manager.cancelUniqueWork(NAME)
 }
 fun enabled(context:Context)=context.getSharedPreferences("aimanage_rules",Context.MODE_PRIVATE)
  .getBoolean("telemetry_enabled",false)
}
