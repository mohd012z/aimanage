package com.aimanage.app
import org.junit.Assert.*
import org.junit.Test
class PerformanceVpnPolicyTest {
 @Test fun memoryPercentHasSafeBounds() {
  assertEquals(50,PerformanceAdvicePolicy.ramPercent(PerformanceSnapshot(200L,100L,false,4,0L,0L)))
  assertNull(PerformanceAdvicePolicy.ramPercent(PerformanceSnapshot(0L,0L,false,1,0L,0L)))
  assertNull(PerformanceAdvicePolicy.ramPercent(PerformanceSnapshot(100L,200L,false,1,0L,0L)))
 }
 @Test fun thermalWarningOverridesRamAdvice() {
  val s=PerformanceSnapshot(200L,0L,true,2,100L,100L)
  assertTrue(PerformanceAdvicePolicy.assess(s,"Critical").contains("thermal pressure"))
  assertTrue(PerformanceAdvicePolicy.assess(s,"Normal").contains("low memory"))
 }
 @Test fun vpnModesDoNotClaimToControlSystemVpn() {
  val p=PublicVpnCatalog.providers.first()
  assertTrue(PublicVpnCatalog.explanation(VpnAdviceMode.OFF,p).contains("does not disconnect"))
  assertTrue(PublicVpnCatalog.explanation(VpnAdviceMode.HYBRID,p).contains("does not activate"))
  assertTrue(PublicVpnCatalog.explanation(VpnAdviceMode.EXTERNAL,p).contains("cannot connect"))
 }
 @Test fun everyVpnProviderUsesHttps() {
  assertTrue(PublicVpnCatalog.providers.all { it.homepage.startsWith("https://") })
 }
}
