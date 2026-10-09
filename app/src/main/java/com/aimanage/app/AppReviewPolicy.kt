package com.aimanage.app

/** Explicitly separates observations from claims that Android cannot prove. */
internal object AppReviewPolicy {
 data class Finding(val packageName:String, val foregroundMinutes:Long, val note:String)
 fun recentUsage(apps:List<AppActivity>, ownPackage:String):List<Finding> =
  apps.filter { it.packageName != ownPackage && it.foregroundMinutes>0 }
   .take(8).map {
    Finding(it.packageName,it.foregroundMinutes,
     "Foreground time only. This does NOT prove the app is running now, generating heat, draining battery in background, or safe to uninstall. Verify in Android Battery usage before changing it.")
   }
 fun safetyNotice():String =
  "Never automatically stop/uninstall apps or silence calls, alarms, messaging, accessibility, navigation, security or work notifications. Check importance and measured battery use before adjusting any app."
}

internal object SleepBaselinePolicy {
 data class Baseline(val elapsedHours:Double, val percentPointsLost:Int, val pointsPerHour:Double)
 fun calculate(startMs:Long, startPercent:Int, endMs:Long, endPercent:Int, charging:Boolean):Baseline? {
  if(charging || startPercent !in 0..100 || endPercent !in 0..100 || endMs<=startMs) return null
  val hours=(endMs-startMs)/3_600_000.0
  if(hours < 1.0 || hours > 24.0 || endPercent>startPercent) return null
  val loss=startPercent-endPercent
  return Baseline(hours,loss,loss/hours)
 }
}
