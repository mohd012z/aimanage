package com.aimanage.app

import org.junit.Assert.*
import org.junit.Test

class AppReviewPolicyTest {
 @Test fun neverLabelsForegroundUseAsBackgroundDrain() {
  val findings=AppReviewPolicy.recentUsage(listOf(
   AppActivity("com.aimanage.app",100,10),AppActivity("example.game",230,10),
   AppActivity("example.chat",10,10)
  ),"com.aimanage.app")
  assertEquals(listOf("example.game","example.chat"),findings.map { it.packageName })
  assertTrue(findings.all { it.note.contains("does NOT prove") })
  assertTrue(AppReviewPolicy.safetyNotice().contains("Never automatically"))
 }
 @Test fun sleepBaselineRejectsChargingShortSessionsAndClockRollback() {
  val hour=3_600_000L
  assertNull(SleepBaselinePolicy.calculate(0,80,hour,77,true))
  assertNull(SleepBaselinePolicy.calculate(0,80,hour/2,79,false))
  assertNull(SleepBaselinePolicy.calculate(hour,80,0,79,false))
  assertNull(SleepBaselinePolicy.calculate(0,80,hour,81,false))
  assertNull(SleepBaselinePolicy.calculate(0,80,25*hour,75,false))
  assertNull(SleepBaselinePolicy.calculate(0,101,hour,80,false))
 }
 @Test fun sleepBaselineRatesAreObservedNotAttributedToApps() {
  val result=SleepBaselinePolicy.calculate(0,80,8*3_600_000L,76,false)!!
  assertEquals(4,result.percentPointsLost)
  assertEquals(0.5,result.pointsPerHour,0.0001)
 }
}
