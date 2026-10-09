package com.aimanage.app

import org.junit.Assert.*
import org.junit.Test

class AppSleepReviewPolicyTest {
 @Test fun noEvidenceNeverRecommendsClose() {
  assertEquals(AppReviewAction.KEEP,AppReviewPolicy.recommend(false,false,false,false).action)
 }
 @Test fun essentialAppsAreProtectedEvenWhenMarkedUnneeded() {
  assertEquals(AppReviewAction.KEEP,AppReviewPolicy.recommend(true,true,true,true).action)
 }
 @Test fun backgroundReviewNeedsConfirmedBatteryEvidence() {
  assertEquals(AppReviewAction.REVIEW_NOTIFICATIONS,AppReviewPolicy.recommend(false,true,false,false).action)
  assertEquals(AppReviewAction.REVIEW_BACKGROUND,AppReviewPolicy.recommend(false,false,true,false).action)
 }
 @Test fun uninstallRequiresExplicitUserAssessment() {
  assertEquals(AppReviewAction.CONSIDER_UNINSTALL,AppReviewPolicy.recommend(true,false,false,false).action)
 }
 private fun reading(hour:Long,percent:Int?,charge:Boolean=false)=SleepReading(hour*3_600_000L,percent,charge)
 @Test fun overnightBatteryRateIsMeasuredAsBeforeAfterOnly() {
  val sample=SleepReviewPolicy.compare(reading(1,83),reading(9,75))!!
  assertEquals(8,sample.percentLost)
  assertEquals(8.0,sample.hours,0.00001)
  assertEquals(1.0,sample.percentPointsPerHour,0.00001)
 }
 @Test fun overnightEstimateRejectsInvalidConditions() {
  assertNull(SleepReviewPolicy.compare(reading(1,80,true),reading(9,75)))
  assertNull(SleepReviewPolicy.compare(reading(1,80),reading(9,75,true)))
  assertNull(SleepReviewPolicy.compare(reading(1,80),reading(9,85)))
  assertNull(SleepReviewPolicy.compare(reading(1,80),reading(2,105)))
  assertNull(SleepReviewPolicy.compare(reading(9,80),reading(1,75)))
  assertNull(SleepReviewPolicy.compare(reading(1,80),reading(26,70)))
  assertNull(SleepReviewPolicy.compare(reading(1,null),reading(9,70)))
 }
}
