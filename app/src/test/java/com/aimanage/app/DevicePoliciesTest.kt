package com.aimanage.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class DevicePoliciesTest {
 private val minute = TimeUnit.MINUTES.toMillis(1)
 private val hour = TimeUnit.HOURS.toMillis(1)

 @Test fun firstCaptureIsAccepted() {
  assertTrue(LearningSamplingPolicy.shouldRecord(null, 1_000L))
 }

 @Test fun learningCapturesAreLimitedToEveryFifteenMinutes() {
  val previous = 1_000L
  assertFalse(LearningSamplingPolicy.shouldRecord(previous, previous))
  assertFalse(LearningSamplingPolicy.shouldRecord(previous, previous + 14 * minute))
  assertTrue(LearningSamplingPolicy.shouldRecord(previous, previous + 15 * minute))
  assertTrue(LearningSamplingPolicy.shouldRecord(previous, previous + 16 * minute))
 }

 @Test fun clockRollbackDoesNotBlockLearning() {
  assertTrue(LearningSamplingPolicy.shouldRecord(2 * hour, hour))
  assertTrue(LearningSamplingPolicy.shouldRecord(1_000L, 999L))
 }

 @Test fun telemetryDisabledOverridesOldErrorsAndClockChanges() {
  assertEquals("Disabled", TelemetryHealthPolicy.status(
   enabled=false, lastSuccess=300L, enabledSince=500L, lastErrorAt=600L, now=100L))
 }

 @Test fun telemetryWaitsForItsFirstSample() {
  assertEquals("Awaiting first background sample", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=0L, enabledSince=1_000L, lastErrorAt=0L, now=2_000L))
 }

 @Test fun oldSessionSuccessDoesNotCountAsCurrent() {
  assertEquals("Awaiting first background sample", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=100L, enabledSince=1_000L, lastErrorAt=0L, now=2_000L))
 }

 @Test fun currentErrorWithNoSuccessIsReported() {
  assertEquals("Worker error: retry scheduled", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=0L, enabledSince=1_000L, lastErrorAt=1_100L, now=2_000L))
 }

 @Test fun newerErrorOverridesOldSuccess() {
  assertEquals("Worker error: retry scheduled", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=1_100L, enabledSince=1_000L, lastErrorAt=1_200L, now=2_000L))
 }

 @Test fun earlierErrorDoesNotOverrideSubsequentSuccess() {
  assertEquals("Recent sample recorded", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=1_500L, enabledSince=1_000L, lastErrorAt=1_100L, now=2_000L))
 }

 @Test fun previousSessionErrorIsIgnored() {
  assertEquals("Recent sample recorded", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=1_500L, enabledSince=1_000L, lastErrorAt=900L, now=2_000L))
 }

 @Test fun sampleAgeBoundaryIsThreeHours() {
  assertEquals("Recent sample recorded", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=1_000L, enabledSince=500L, lastErrorAt=0L, now=1_000L + 3 * hour))
  assertEquals("Delayed: last sample over 3 hours ago", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=1_000L, enabledSince=500L, lastErrorAt=0L, now=1_001L + 3 * hour))
 }

 @Test fun enableTimestampInFutureDetectsClockRollbackBeforeFirstCapture() {
  assertEquals("Device clock changed", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=0L, enabledSince=2_000L, lastErrorAt=0L, now=1_000L))
 }

 @Test fun futureSampleOrErrorDetectsClockRollback() {
  assertEquals("Device clock changed", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=2_000L, enabledSince=500L, lastErrorAt=0L, now=1_000L))
  assertEquals("Device clock changed", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=0L, enabledSince=500L, lastErrorAt=2_000L, now=1_000L))
 }

 @Test fun oldSessionSampleDoesNotCountAsCurrentSample() {
  assertEquals("Awaiting first background sample", TelemetryHealthPolicy.status(
   enabled=true, lastSuccess=300L, enabledSince=500L, lastErrorAt=0L, now=600L))
 }

 @Test fun alertCooldownBlocksRepeatedNotificationUntilTwoHours() {
  assertTrue(HealthAlertPolicy.canNotify(now=100L,lastAlertAt=0L))
  assertFalse(HealthAlertPolicy.canNotify(now=1_000L + hour,lastAlertAt=1_000L))
  assertTrue(HealthAlertPolicy.canNotify(now=1_000L + 2 * hour,lastAlertAt=1_000L))
 }

 @Test fun alertCooldownRecoversAfterClockRollback() {
  assertTrue(HealthAlertPolicy.canNotify(now=1_000L,lastAlertAt=2_000L))
 }

 @Test fun alertRulesHandleSevereThermalAndBatteryHeat() {
  for (status in listOf("Severe","Critical","Emergency","Shutdown")) {
   assertTrue(HealthAlertPolicy.isElevatedTemperature(status,null))
  }
  assertTrue(HealthAlertPolicy.isElevatedTemperature("Normal",40f))
  assertFalse(HealthAlertPolicy.isElevatedTemperature("Normal",39.9f))
  assertFalse(HealthAlertPolicy.isElevatedTemperature("Normal",null))
  assertFalse(HealthAlertPolicy.isElevatedTemperature("Light",35f))
 }
}
