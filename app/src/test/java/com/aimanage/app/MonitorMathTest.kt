package com.aimanage.app

import org.junit.Assert.*
import org.junit.Test

class MonitorMathTest {
 private fun sample(wall:Long,cpu:Long)=MonitorSnapshot(wall,cpu,null,null,null,null,null,null,null,null,null,null)
 @Test fun appCpuIsDeltaNotInstantaneous() {
  assertEquals(25.0,MonitorMath.appCpuPercent(sample(1000,100),sample(2000,350))!!,0.001)
  assertEquals(200.0,MonitorMath.appCpuPercent(sample(1000,100),sample(2000,2100))!!,0.001)
 }
 @Test fun invalidDeltasAreUnavailable() {
  assertNull(MonitorMath.appCpuPercent(sample(1000,100),sample(1000,200)))
  assertNull(MonitorMath.appCpuPercent(sample(1000,100),sample(2000,50)))
 }
 @Test fun memoryUsageIsBounded() {
  assertEquals(75,MonitorMath.usedPercent(100,25))
  assertNull(MonitorMath.usedPercent(100,101))
  assertNull(MonitorMath.usedPercent(0,0))
 }
}
