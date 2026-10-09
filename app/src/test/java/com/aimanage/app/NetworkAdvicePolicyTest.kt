package com.aimanage.app

import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkAdvicePolicyTest {
 private fun snapshot(
  transport:String="Wi-Fi", internet:Boolean=true,
  validated:Boolean=true, portal:Boolean=false
 )=NetworkSnapshot(transport,internet,validated,portal,false,false,null,null)

 @Test fun disconnectedIsNotClaimedOnline() {
  assertTrue(NetworkAdvicePolicy.explain(snapshot(transport="None")).contains("No active network"))
 }
 @Test fun captivePortalTakesPrecedenceOverValidation() {
  assertTrue(NetworkAdvicePolicy.explain(snapshot(validated=false,portal=true)).contains("captive portal"))
 }
 @Test fun missingCapabilityDoesNotClaimInternet() {
  assertTrue(NetworkAdvicePolicy.explain(snapshot(internet=false)).contains("does not advertise internet"))
 }
 @Test fun validationIsNotMisrepresentedAsSpeedTest() {
  assertTrue(NetworkAdvicePolicy.explain(snapshot()).contains("not been measured"))
 }
 @Test fun unvalidatedDoesNotMeanDefinitelyOffline() {
  assertTrue(NetworkAdvicePolicy.explain(snapshot(validated=false)).contains("not proof"))
 }
}
