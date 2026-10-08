package com.aimanage.app

import android.net.Uri
import java.net.IDN
import java.util.Locale

data class SecurityFinding(val level: String, val summary: String, val evidence: List<String>)
object SecurityAnalyzer {
 private val shorteners = setOf("bit.ly","tinyurl.com","t.co","is.gd","cutt.ly")
 private val suspiciousWords = listOf("verify-account","urgent-login","free-gift","claim-prize","password-reset")
 fun checkUrl(input: String): SecurityFinding {
  val parsed = try { Uri.parse(input.trim()) } catch (_: Exception) { null }
  val scheme = parsed?.scheme?.lowercase(Locale.ROOT)
  val host = parsed?.host?.lowercase(Locale.ROOT)?.trimEnd('.')
  if(scheme !in listOf("https","http") || host.isNullOrBlank())
   return SecurityFinding("UNKNOWN","Invalid or unsupported web address",listOf("Only http/https URLs can be analyzed."))
  val evidence = mutableListOf<String>()
  if(scheme == "http") evidence += "Connection does not use HTTPS."
  if(host in shorteners) evidence += "Shortened URL hides the final destination."
  if(host.startsWith("xn--") || host.split('.').any { it.startsWith("xn--") }) evidence += "Internationalized domain encoding; inspect for impersonation."
  if(host.count { it == '-' } >= 3) evidence += "Unusually many hyphens in the hostname."
  if(suspiciousWords.any { input.contains(it,ignoreCase=true) }) evidence += "Contains a phrase commonly used in deceptive links."
  val ascii = try { IDN.toASCII(host) } catch (_: Exception) { host }
  if(ascii != host) evidence += "Hostname was normalized to ASCII."
  return SecurityFinding(if(evidence.isEmpty()) "UNKNOWN" else "CAUTION",
   if(evidence.isEmpty()) "No basic warning signs detected; safety is NOT verified." else "Review this link carefully before opening.",evidence)
 }
 fun explainPhone(number: String): SecurityFinding {
  val digits = number.filter { it.isDigit() }
  if(!number.trim().startsWith("+") || digits.length !in 7..15)
   return SecurityFinding("UNKNOWN","Country cannot be reliably determined from this input.",listOf("Use international E.164 format such as +60..."))
  val prefix = when {
   digits.startsWith("60") -> "Malaysia (+60)"
   digits.startsWith("65") -> "Singapore (+65)"
   digits.startsWith("62") -> "Indonesia (+62)"
   digits.startsWith("44") -> "United Kingdom (+44)"
   digits.startsWith("91") -> "India (+91)"
   digits.startsWith("86") -> "China (+86)"
   digits.startsWith("81") -> "Japan (+81)"
   digits.startsWith("61") -> "Australia (+61)"
   digits.startsWith("1") -> "North American Numbering Plan (+1)"
   else -> "Not identified by the local sample prefix list"
  }
  return SecurityFinding("UNKNOWN","Numbering prefix: $prefix",listOf("Number allocation does not prove caller location or identity.","Caller ID may be spoofed.","Company, bank and scam status remain unverified."))
 }
}
