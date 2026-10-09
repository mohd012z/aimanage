package com.aimanage.app

import java.util.Locale

/** User-selectable advice only: does not silently modify Android settings. */
internal enum class AdviceProfile { BALANCED, BATTERY_PRIORITY, PERFORMANCE_PRIORITY }

internal data class DischargeMeasurement(val percentPointsPerHour: Double, val durationHours: Double)

/** Uses only an uninterrupted discharging segment; never labels it screen-off drain. */
internal object DeviceMeasurementPolicy {
 private const val MAX_MEASUREMENT_AGE_MS = 3L * 60L * 60L * 1000L

 /**
  * A valid historical trend is not automatically a current trend.
  * Reject future timestamps (clock changes) and samples older than 3 hours.
  */
 fun recentDischarge(points: List<DischargeSegmentPolicy.Point>, nowMillis: Long): DischargeMeasurement? {
  val newest = points.lastOrNull() ?: return null
  if (newest.timestamp > nowMillis || newest.timestamp < 0L) return null
  if (nowMillis - newest.timestamp > MAX_MEASUREMENT_AGE_MS) return null
  return discharge(points)
 }

 fun discharge(points: List<DischargeSegmentPolicy.Point>): DischargeMeasurement? {
  val segment = DischargeSegmentPolicy.latest(points)
  if(segment.size < 2) return null
  val first=segment.first()
  val last=segment.last()
  val duration=(last.timestamp-first.timestamp)/3_600_000.0
  if(duration < 1.0 || !duration.isFinite()) return null
  val lost=(first.percent ?: return null) - (last.percent ?: return null)
  if(lost < 0) return null
  return DischargeMeasurement(lost/duration,duration)
 }

 fun recommendation(profile: AdviceProfile, discharge: DischargeMeasurement?, thermal: String): String {
  if(thermal in setOf("Severe","Critical","Emergency","Shutdown"))
   return "Android reports severe thermal pressure. Pause demanding work and let system thermal controls operate; do not override throttling."
  val observed=discharge?.let {
   String.format(Locale.US,"Observed %.1f percentage points/hour over %.1f hours (not specifically screen-off). ",it.percentPointsPerHour,it.durationHours)
  } ?: "No reliable discharge trend yet; record at least one hour of uninterrupted discharge. "
  val advice=when(profile) {
   AdviceProfile.BALANCED -> "Keep notifications and background activity for essential apps. Review screen brightness and only apps with verified background battery use."
   AdviceProfile.BATTERY_PRIORITY -> "Try lower brightness and screen timeout first; selectively restrict nonessential apps after reviewing Android battery-use evidence. Global Battery Saver may affect sync and performance."
   AdviceProfile.PERFORMANCE_PRIORITY -> "Favor foreground responsiveness and needed notifications; avoid blanket battery restrictions. High refresh and demanding tasks can increase consumption and heat."
  }
  return observed+advice
 }
}
