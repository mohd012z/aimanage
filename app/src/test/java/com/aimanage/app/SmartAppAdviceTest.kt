package com.aimanage.app
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartAppAdviceTest {
 @Test fun noPermissionIsNotReportedAsBackgroundActivity() {
  val result=SmartAppAdvice.recommendations(false,200,false).joinToString()
  assertTrue(result.contains("No Usage Access"))
  assertTrue(!result.contains("High foreground time"))
 }
 @Test fun systemAppsAreProtected() {
  val result=SmartAppAdvice.recommendations(true,300,true).joinToString()
  assertTrue(result.contains("do not uninstall"))
 }
 @Test fun highForegroundUseIsNotMisrepresented() {
  val result=SmartAppAdvice.recommendations(true,180,false).joinToString()
  assertTrue(result.contains("not evidence of background drain"))
 }
 @Test fun sleepAttributionIsNotInvented() {
  assertTrue(SmartAppAdvice.standbyExplanation(10).contains("cannot be attributed"))
 }
}
