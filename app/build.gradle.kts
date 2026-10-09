plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
android { namespace = "com.aimanage.app"; compileSdk = 35
 defaultConfig { applicationId = "com.aimanage.app"; minSdk = 29; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
 flavorDimensions += "access"
 productFlavors {
  create("standard") { dimension = "access" }
  create("advanced") { dimension = "access"; applicationIdSuffix = ".advanced"; versionNameSuffix = "-advanced" }
 }
 // Production signing is injected only from protected CI secrets or a local secure environment.
 // Never commit a key, password, or release signing material to this repository.
 val releaseStorePath = System.getenv("AIMANAGE_RELEASE_KEYSTORE_PATH")
 val releaseStorePassword = System.getenv("AIMANAGE_RELEASE_STORE_PASSWORD")
 val releaseKeyAlias = System.getenv("AIMANAGE_RELEASE_KEY_ALIAS")
 val releaseKeyPassword = System.getenv("AIMANAGE_RELEASE_KEY_PASSWORD")
 if (listOf(releaseStorePath,releaseStorePassword,releaseKeyAlias,releaseKeyPassword).all { !it.isNullOrBlank() }) {
  signingConfigs {
   create("secureRelease") {
    storeFile = file(releaseStorePath!!)
    storePassword = releaseStorePassword
    keyAlias = releaseKeyAlias
    keyPassword = releaseKeyPassword
   }
  }
  buildTypes.getByName("release").signingConfig = signingConfigs.getByName("secureRelease")
 }
 buildFeatures { compose = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 testImplementation("junit:junit:4.13.2")
 implementation("androidx.work:work-runtime-ktx:2.10.1")
 implementation("androidx.documentfile:documentfile:1.0.1")
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.9.3")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-extended")
 implementation("androidx.compose.ui:ui-tooling-preview")
 debugImplementation("androidx.compose.ui:ui-tooling")
}
