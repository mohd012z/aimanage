package com.aimanage.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceMeasurementPolicyTest {
 private fun p(hours:Long, pct:Int?, charging:Boolean=false)=
  DischargeSegmentPolicy.Point(hours*3_600_000L,pct,charging)

 @Test fun computesObservedDischargeOnlyAcrossValidUninterruptedSamples() {
  val result=DeviceMeasurementPolicy.discharge(listOf(p(1,90),p(2,85),p(3,80)))
  assertEquals(5.0,result!!.percentPointsPerHour,0.0001)
  assertEquals(2.0,result.durationHours,0.0001)
 }

 @Test fun rejectsInsufficientOrInterruptedHistory() {
  assertNull(DeviceMeasurementPolicy.discharge(emptyList()))
  assertNull(DeviceMeasurementPolicy.discharge(listOf(p(1,80))))
  assertNull(DeviceMeasurementPolicy.discharge(listOf(p(1,90),p(2,85,true),p(3,80))))
  assertNull(DeviceMeasurementPolicy.discharge(listOf(p(1,90),p(1,85))))
  assertNull(DeviceMeasurementPolicy.discharge(listOf(p(1,90),p(2,110))))
 }

 @Test fun separatesPreviousAndLatestDischargeSessions() {
  val result=DeviceMeasurementPolicy.discharge(listOf(p(0,95),p(1,90,true),p(2,86),p(3,82)))
  assertEquals(4.0,result!!.percentPointsPerHour,0.0001)
 }

 @Test fun currentTrendRequiresFreshAndNonFutureMeasurements() {
  val points=listOf(p(1,90),p(2,85),p(3,80))
  val newest=3L*3_600_000L
  assertEquals(5.0,DeviceMeasurementPolicy.recentDischarge(points,newest)!!.percentPointsPerHour,0.0001)
  assertEquals(5.0,DeviceMeasurementPolicy.recentDischarge(points,newest+3L*3_600_000L)!!.percentPointsPerHour,0.0001)
  assertNull(DeviceMeasurementPolicy.recentDischarge(points,newest+3L*3_600_000L+1L))
  assertNull(DeviceMeasurementPolicy.recentDischarge(points,newest-1L))
  assertNull(DeviceMeasurementPolicy.recentDischarge(emptyList(),newest))
 }

 @Test fun profilesDoNotMisrepresentScreenOffOrThermalSafety() {
  for(profile in AdviceProfile.values()) {
   val advice=DeviceMeasurementPolicy.recommendation(profile,DischargeMeasurement(5.0,2.0),"Normal")
   assertTrue(advice.contains("not specifically screen-off"))
   val hot=DeviceMeasurementPolicy.recommendation(profile,null,"Critical")
   assertTrue(hot.contains("do not override throttling"))
  }
 }
}
