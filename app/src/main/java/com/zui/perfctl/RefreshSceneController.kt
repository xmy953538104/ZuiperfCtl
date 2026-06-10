package com.zui.perfctl

import android.content.Context
import android.os.SystemClock
import android.provider.Settings

object RefreshSceneController {
    const val BASE_REFRESH_RATE = 120
    private const val SCENE_EVENT_TTL_NANOS = 60_000_000_000L
    private val defaultScenePackages = setOf(
        "com.zui.launcher",
        "com.android.launcher3",
        "com.lenovo.launcher",
        "com.motorola.launcher",
    )

    fun isScenePackage(context: Context, pkg: String): Boolean {
        return PackageNames.isValid(pkg) &&
            pkg != context.packageName &&
            pkg != "com.android.systemui"
    }

    fun currentScenePackage(context: Context): String {
        recentScenePackage(context)?.let { return it }
        val pkg = Settings.System.getString(
            context.contentResolver,
            PerfCtlContract.KEY_TOP_PACKAGE,
        ).orEmpty()
        return pkg.takeIf { isScenePackage(context, it) }.orEmpty()
    }

    fun currentLearnPackage(context: Context): String {
        return currentScenePackage(context)
            .takeIf { isQuickLearnPackage(context, it) }
            .orEmpty()
    }

    fun rateForPackage(context: Context, pkg: String): Int {
        Settings.System.getString(context.contentResolver, PerfCtlContract.KEY_RULES_TEXT)
            ?.lineSequence()
            ?.forEach { line ->
                val parts = line.trim().split("=", limit = 2)
                val rate = parts.getOrNull(1)?.toIntOrNull()
                if (parts.getOrNull(0) == pkg && rate in PerfCtlContract.rates) {
                    return rate ?: BASE_REFRESH_RATE
                }
            }
        return BASE_REFRESH_RATE
    }

    fun currentRate(context: Context): Int {
        Settings.System.getString(context.contentResolver, PerfCtlContract.KEY_ACTIVE_REFRESH)
            ?.toIntOrNull()
            ?.takeIf { it in PerfCtlContract.rates }
            ?.let { return it }
        val min = Settings.System.getString(context.contentResolver, "min_refresh_rate")
            ?.removeSuffix(".0")
            ?.toIntOrNull()
        val peak = Settings.System.getString(context.contentResolver, "peak_refresh_rate")
            ?.removeSuffix(".0")
            ?.toIntOrNull()
        return when {
            min != null && min == peak && min in PerfCtlContract.rates -> min
            peak in PerfCtlContract.rates -> peak!!
            else -> BASE_REFRESH_RATE
        }
    }

    fun isLocked(context: Context, rate: Int): Boolean {
        val resolver = context.contentResolver
        return Settings.System.getString(resolver, PerfCtlContract.KEY_ACTIVE_REFRESH) == rate.toString() &&
            refreshValueMatches(Settings.System.getString(resolver, "peak_refresh_rate"), rate) &&
            refreshValueMatches(Settings.System.getString(resolver, "min_refresh_rate"), rate)
    }

    fun lockRefresh(context: Context, rate: Int) {
        if (isLocked(context, rate)) {
            return
        }
        val resolver = context.contentResolver
        Settings.System.putString(resolver, PerfCtlContract.KEY_ACTIVE_REFRESH, rate.toString())
        Settings.System.putString(resolver, "peak_refresh_rate", "$rate.0")
        Settings.System.putString(resolver, "min_refresh_rate", "$rate.0")
    }

    fun publishScene(context: Context, pkg: String) {
        if (!isScenePackage(context, pkg)) {
            return
        }
        val resolver = context.contentResolver
        Settings.System.putString(
            resolver,
            PerfCtlContract.KEY_SCENE_EVENT_TEXT,
            "${SystemClock.elapsedRealtimeNanos()}|$pkg",
        )
        Settings.System.putString(resolver, PerfCtlContract.KEY_TOP_PACKAGE, pkg)
    }

    private fun refreshValueMatches(value: String?, rate: Int): Boolean {
        return value == rate.toString() || value?.startsWith("$rate.") == true
    }

    private fun recentScenePackage(context: Context): String? {
        val event = Settings.System.getString(
            context.contentResolver,
            PerfCtlContract.KEY_SCENE_EVENT_TEXT,
        ).orEmpty()
        val separator = event.indexOf('|')
        if (separator <= 0 || separator >= event.lastIndex) {
            return null
        }
        val timestamp = event.substring(0, separator).toLongOrNull() ?: return null
        val age = SystemClock.elapsedRealtimeNanos() - timestamp
        if (age !in 0..SCENE_EVENT_TTL_NANOS) {
            return null
        }
        return event.substring(separator + 1).takeIf { isScenePackage(context, it) }
    }

    private fun isQuickLearnPackage(context: Context, pkg: String): Boolean {
        return isScenePackage(context, pkg) && pkg !in defaultScenePackages
    }
}
