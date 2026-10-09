package com.aimanage.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Read-only transport snapshot. No requests, traffic interception or VPN. */
data class NetworkSnapshot(
 val transport: String,
 val internetCapability: Boolean,
 val validated: Boolean,
 val captivePortal: Boolean,
 val metered: Boolean?,
 val vpnActive: Boolean,
 val downstreamKbps: Int?,
 val upstreamKbps: Int?
)

/** These are link estimates reported by Android, not measured speed-test throughput. */
internal object NetworkAdvicePolicy {
 fun explain(snapshot: NetworkSnapshot): String {
  if(snapshot.transport=="None") return "No active network was reported. Check Wi-Fi or mobile data."
  if(snapshot.captivePortal) return "Android reports a captive portal. Sign in to the network using the normal Android prompt before testing connectivity."
  if(!snapshot.internetCapability) return "The active link does not advertise internet capability. Check network settings."
  if(!snapshot.validated) return "Android has not validated internet connectivity. This is not proof the internet is unavailable; VPN, Private DNS or captive network policies can affect validation."
  return "Android validated the active network. Actual latency and throughput have not been measured; signal quality and congestion can still affect performance."
 }
}

object NetworkDiagnostics {
 fun snapshot(context: Context): NetworkSnapshot {
  val manager=context.getSystemService(ConnectivityManager::class.java)
  if(manager==null) return NetworkSnapshot("None",false,false,false,null,false,null,null)
  return try {
   val network=manager.activeNetwork
   val caps=if(network!=null) manager.getNetworkCapabilities(network) else null
   if(caps==null) NetworkSnapshot("None",false,false,false,null,false,null,null)
   else {
    val transport=when {
     caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
     caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
     caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
     caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
     else -> "Other"
    }
    NetworkSnapshot(
     transport,
     caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
     caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
     caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL),
     !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
     caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN),
     caps.linkDownstreamBandwidthKbps.takeIf { it>0 },
     caps.linkUpstreamBandwidthKbps.takeIf { it>0 }
    )
   }
  } catch (_: SecurityException) {
   NetworkSnapshot("Unavailable",false,false,false,null,false,null,null)
  }
 }
}
