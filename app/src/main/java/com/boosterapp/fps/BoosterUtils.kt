package com.boosterapp.fps

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings

/**
 * Ye class actual "boosting" ka kaam karti hai.
 * Sach yeh hai: bina root ke koi app GPU/FPS ko directly control nahi kar sakta.
 * Isliye ye class wahi karti hai jo real-world booster apps karte hain:
 *  1) Background apps ko kill karke RAM free karna
 *  2) Do Not Disturb on karke notifications/calls se distraction hatana
 *  3) RAM stats dikhana taaki user ko fark dikhe
 */
object BoosterUtils {

    // Ye system/important packages kabhi kill nahi honge
    private val PROTECTED_PREFIXES = listOf(
        "com.android",
        "com.google.android.gms",
        "com.google.android.gsf",
        "android",
        "com.boosterapp.fps" // apna hi app
    )

    fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info
    }

    fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb > 1024) String.format("%.2f GB", mb / 1024.0)
        else String.format("%.0f MB", mb)
    }

    /**
     * Installed apps ki list laata hai (system apps ko chhod kar), sorted by name.
     */
    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        return apps
            .filter { appInfo ->
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                !isSystemApp && appInfo.packageName != context.packageName
            }
            .map { appInfo ->
                val isGame = isLikelyGame(appInfo)
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo),
                    isGame = isGame
                )
            }
            .sortedWith(compareByDescending<AppInfo> { it.isGame }.thenBy { it.appName.lowercase() })
    }

    private fun isLikelyGame(appInfo: ApplicationInfo) =
        appInfo.category == ApplicationInfo.CATEGORY_GAME

    private fun isProtectedPackage(packageName: String) =
        PROTECTED_PREFIXES.any { packageName.startsWith(it) }

    /**
     * Background apps ko kill karta hai (protected aur user-selected apps chhod kar).
     * Returns: kitne apps kill hue.
     */
    fun boostNow(context: Context, appsToKill: List<AppInfo>): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var killedCount = 0

        for (app in appsToKill) {
            if (app.isProtected || isProtectedPackage(app.packageName)) continue
            try {
                am.killBackgroundProcesses(app.packageName)
                killedCount++
            } catch (e: SecurityException) {
                // Kuch OEM apps kill nahi hone dete, safe ignore
            }
        }
        return killedCount
    }

    fun isDndPermissionGranted(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    fun requestDndPermission(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * Game Mode on -> Do Not Disturb (priority only) on karta hai.
     * Game Mode off -> normal notifications wapas.
     */
    fun setGameMode(context: Context, enabled: Boolean) {
        if (!isDndPermissionGranted(context)) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.setInterruptionFilter(
            if (enabled) NotificationManager.INTERRUPTION_FILTER_PRIORITY
            else NotificationManager.INTERRUPTION_FILTER_ALL
        )
    }

    // --- Performance Shortcuts ---
    // Ye koi "boost" nahi karte — sirf real Android settings screens seedha khol dete hain,
    // taaki user khud refresh rate, battery mode, ya dev options manually change kar sake.

    private fun openSettingsScreen(context: Context, action: String): Boolean {
        return try {
            val intent = Intent(action)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Display settings (refresh rate, brightness, screen timeout waghera OEM par depend karta hai). */
    fun openDisplaySettings(context: Context): Boolean =
        openSettingsScreen(context, Settings.ACTION_DISPLAY_SETTINGS)

    /** Battery settings — kuch OEM par ye seedha battery saver kholta hai, kuch par battery usage screen. */
    fun openBatterySettings(context: Context): Boolean {
        val opened = openSettingsScreen(context, "android.settings.BATTERY_SAVER_SETTINGS")
        return if (opened) true else openSettingsScreen(context, Settings.ACTION_BATTERY_SAVER_SETTINGS)
    }

    /**
     * Developer Options — agar phone par pehli baar enable nahi hui, to Android ye screen
     * khud hi nahi dikhata (ActivityNotFoundException), is case me caller ko batana padta hai
     * ki About Phone me Build Number 7 baar tap karo.
     */
    fun openDeveloperOptions(context: Context): Boolean =
        openSettingsScreen(context, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
}
