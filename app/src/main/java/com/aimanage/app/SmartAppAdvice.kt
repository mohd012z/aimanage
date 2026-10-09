package com.aimanage.app

/** Conservative evidence policy. Usage duration is never treated as CPU or battery drain. */
internal object SmartAppAdvice {
 fun recommendations(usageAvailable:Boolean, foregroundMinutes:Long, isSystem:Boolean):List<String> {
  val advice=mutableListOf<String>()
  if(isSystem) return listOf("System or essential app: do not uninstall or restrict automatically.")
  if(!usageAvailable) return listOf("No Usage Access. Check Android per-app battery usage manually before deciding.")
  if(foregroundMinutes>120L) advice+="High foreground time is not evidence of background drain. Check Android battery-use statistics."
  advice+="If rarely used, consider disabling nonessential notification categories in Android Settings."
  advice+="For cooling, close demanding foreground tasks yourself only when Android reports heat; do not force-stop essential apps."
  advice+="Uninstall only if you recognize the app, no longer need it, and have backed up its data."
  return advice
 }
 fun standbyExplanation(sampleCount:Int):String =
  if(sampleCount<2) "Insufficient battery samples for a sleep-period trend. Enable local sampling and compare before bedtime and after waking."
  else "Historical battery samples are available, but screen-off and sleep intervals are not recorded. Overnight loss cannot be attributed to any app. Compare morning and evening battery and Android battery usage."
}
