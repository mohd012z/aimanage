package com.aimanage.app

import java.util.concurrent.TimeUnit

/**
 * Side-effect-free device policy decisions.
 *
 * Timestamps are wall-clock milliseconds, which can move backward when Android's
 * clock is adjusted. These policies explicitly handle clock rollback; they do not
 * assume that a positive difference can always be computed.
 */
internal object LearningSamplingPolicy {
 private val minimumIntervalMs = TimeUnit.MINUTES.toMillis(15)

 /** Keep a new capture after a clock rollback instead of blocking it indefinitely. */
 fun shouldRecord(previousTimestamp: Long?, candidateTimestamp: Long): Boolean {
  if (previousTimestamp == null) return true
  if (candidateTimestamp < previousTimestamp) return true
  return candidateTimestamp - previousTimestamp >= minimumIntervalMs
 }
}

internal object TelemetryHealthPolicy {
 private val delayedAfterMs = TimeUnit.HOURS.toMillis(3)

 fun status(
  enabled: Boolean,
  lastSuccess: Long,
  enabledSince: Long,
  lastErrorAt: Long,
  now: Long
 ): String {
  if (!enabled) return "Disabled"
  if (enabledSince > now) return "Device clock changed"
  if ((lastSuccess > now && lastSuccess >= enabledSince) ||
      (lastErrorAt > now && lastErrorAt >= enabledSince)) return "Device clock changed"
  if (lastErrorAt > 0L && lastErrorAt >= enabledSince &&
      (lastSuccess == 0L || lastErrorAt > lastSuccess)) return "Worker error: retry scheduled"
  if (lastSuccess == 0L || lastSuccess < enabledSince) return "Awaiting first background sample"
  return if (now - lastSuccess > delayedAfterMs)
   "Delayed: last sample over 3 hours ago" else "Recent sample recorded"
 }
}

internal object HealthAlertPolicy {
 private val cooldownMs = TimeUnit.HOURS.toMillis(2)

 /**
  * Treat a future last-alert timestamp as a clock change and restart the
  * cooldown from the next real alert, rather than suppressing warnings.
  */
 fun canNotify(now: Long, lastAlertAt: Long): Boolean {
  if (lastAlertAt <= 0L || now < lastAlertAt) return true
  return now - lastAlertAt >= cooldownMs
 }

 fun isElevatedTemperature(thermalStatus: String, batteryTemperatureC: Float?): Boolean =
  thermalStatus in setOf("Severe", "Critical", "Emergency", "Shutdown") ||
   (batteryTemperatureC?.let { it.isFinite() && it >= 40f } ?: false)
}
