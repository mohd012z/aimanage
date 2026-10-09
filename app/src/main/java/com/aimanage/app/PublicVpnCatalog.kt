package com.aimanage.app

/** These are external services; AImanage does not establish or terminate a VPN. */
enum class VpnAdviceMode { OFF, HYBRID, EXTERNAL }
data class VpnProvider(val name:String,val homepage:String,val note:String)
internal object PublicVpnCatalog {
 val providers=listOf(
  VpnProvider("Proton VPN","https://protonvpn.com/free-vpn/android","External provider with a free Android plan; its own app handles consent and connection."),
  VpnProvider("Cloudflare WARP","https://developers.cloudflare.com/1.1.1.1/setup/android/","Encrypted traffic option or DNS-only mode; not an anonymity or country-selection service.")
 )
 fun explanation(mode:VpnAdviceMode, provider:VpnProvider):String=when(mode) {
  VpnAdviceMode.OFF -> "VPN advice is off in AImanage. This does not disconnect an already running VPN; use Android VPN settings or the provider app."
  VpnAdviceMode.HYBRID -> "Hybrid means advisory choice: consider trusted Private DNS or an external VPN depending on your needs. Android may permit only one active VPN and combined VPN/DNS setups may affect connectivity. AImanage does not activate either."
  VpnAdviceMode.EXTERNAL -> "Selected external provider: ${provider.name}. Open the official website, review its privacy terms, install through trusted distribution and grant consent in its own app. AImanage cannot connect or disconnect it."
 }
}
