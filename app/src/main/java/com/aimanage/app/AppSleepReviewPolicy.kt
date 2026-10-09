package com.aimanage.app

/** Avoid false allegations about an app from foreground time alone. */
internal enum class AppReviewAction { KEEP, REVIEW_NOTIFICATIONS, REVIEW_BACKGROUND, CONSIDER_UNINSTALL }

internal data class AppReview(val action: AppReviewAction, val explanation: String)

internal object AppReviewPolicy {
 fun recommend(
  userConfirmedUnneeded: Boolean,
  unwantedNotifications: Boolean,
  androidBatteryEvidence: Boolean,
  essentialApp: Boolean
 ): AppReview {
  if (essentialApp) return AppReview(AppReviewAction.KEEP,
   "Essential or user-protected app. Preserve background operation and important notifications.")
  if (userConfirmedUnneeded) return AppReview(AppReviewAction.CONSIDER_UNINSTALL,
   "You marked this app unnecessary. Review its data and account access before uninstalling; Android must confirm.")
  if (androidBatteryEvidence) return AppReview(AppReviewAction.REVIEW_BACKGROUND,
   "You confirmed unusual battery use in Android's per-app battery screen. Review background policy manually and test notifications afterwards.")
  if (unwantedNotifications) return AppReview(AppReviewAction.REVIEW_NOTIFICATIONS,
   "Review individual notification categories in Android settings instead of turning off every alert.")
  return AppReview(AppReviewAction.KEEP,
   "No verified per-app battery drain or CPU load. Foreground time alone does not justify stopping or uninstalling.")
 }
}

internal data class SleepReading(val timeMillis: Long, val percent: Int?, val charging: Boolean)
internal data class SleepAssessment(val percentLost: Int, val hours: Double, val percentPointsPerHour: Double)

/** A manual before/after sleep comparison, never a claim of verified screen-off activity. */
internal object SleepReviewPolicy {
 fun compare(start: SleepReading, end: SleepReading): SleepAssessment? {
  if(start.charging || end.charging) return null
  val a=start.percent ?: return null
  val b=end.percent ?: return null
  if(a !in 0..100 || b !in 0..100 || b>a) return null
  val duration=end.timeMillis-start.timeMillis
  if(duration < 60L*60L*1000L || duration > 24L*60L*60L*1000L) return null
  val hours=duration/3_600_000.0
  return SleepAssessment(a-b,hours,(a-b)/hours)
 }
}
