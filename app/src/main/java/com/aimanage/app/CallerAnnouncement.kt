package com.aimanage.app

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Opt-in speech helper. No call interception or background listener is enabled. */
class CallerAnnouncement(context: Context, private val onReady: (Boolean)->Unit = {}) : TextToSpeech.OnInitListener {
 private val tts = TextToSpeech(context.applicationContext, this)
 private var ready = false
 override fun onInit(status: Int) {
  ready = status == TextToSpeech.SUCCESS
  if(ready) tts.language = Locale.getDefault()
  onReady(ready)
 }
 fun announce(displayName: String) {
  if(!ready) return
  val safe = displayName.take(80).ifBlank { "Unknown caller" }
  tts.speak("Incoming call from $safe",TextToSpeech.QUEUE_FLUSH,null,"aimanage-caller")
 }
 fun close() { tts.stop(); tts.shutdown() }
}
