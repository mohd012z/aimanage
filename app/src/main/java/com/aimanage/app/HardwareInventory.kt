package com.aimanage.app

import android.app.ActivityManager
import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager

/** Public Android SDK only. No root, hidden APIs, network requests or third-party APK code. */
data class HardwareInventory(
    val manufacturer: String,
    val model: String,
    val sdk: Int,
    val abis: List<String>,
    val processors: Int,
    val totalRamBytes: Long?,
    val availableRamBytes: Long?,
    val storageTotalBytes: Long?,
    val storageAvailableBytes: Long?,
    val screenWidthPx: Int?,
    val screenHeightPx: Int?,
    val densityDpi: Int?,
    val sensors: List<String>
)

internal object HardwareInventoryPolicy {
    fun validBytes(value: Long): Long? = value.takeIf { it >= 0L }
    fun percent(available: Long?, total: Long?): Int? =
        if (available != null && total != null && total > 0 && available in 0..total)
            ((available.toDouble() / total) * 100).toInt()
        else null
}

object HardwareInventoryReader {
    fun read(context: Context): HardwareInventory {
        val memory = runCatching {
            val manager = context.getSystemService(ActivityManager::class.java)
            ActivityManager.MemoryInfo().also { manager.getMemoryInfo(it) }
        }.getOrNull()
        val disk = runCatching { StatFs(Environment.getDataDirectory().absolutePath) }.getOrNull()
        val metrics = runCatching {
            @Suppress("DEPRECATION")
            val display = context.getSystemService(WindowManager::class.java).defaultDisplay
            DisplayMetrics().also { display.getRealMetrics(it) }
        }.getOrNull()
        val sensors = runCatching {
            context.getSystemService(SensorManager::class.java)
                .getSensorList(android.hardware.Sensor.TYPE_ALL)
                .map { it.name }
                .distinct()
                .sorted()
        }.getOrDefault(emptyList())
        return HardwareInventory(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            sdk = Build.VERSION.SDK_INT,
            abis = Build.SUPPORTED_ABIS.toList(),
            processors = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
            totalRamBytes = memory?.totalMem?.let(HardwareInventoryPolicy::validBytes),
            availableRamBytes = memory?.availMem?.let(HardwareInventoryPolicy::validBytes),
            storageTotalBytes = disk?.totalBytes?.let(HardwareInventoryPolicy::validBytes),
            storageAvailableBytes = disk?.availableBytes?.let(HardwareInventoryPolicy::validBytes),
            screenWidthPx = metrics?.widthPixels,
            screenHeightPx = metrics?.heightPixels,
            densityDpi = metrics?.densityDpi,
            sensors = sensors
        )
    }
}
