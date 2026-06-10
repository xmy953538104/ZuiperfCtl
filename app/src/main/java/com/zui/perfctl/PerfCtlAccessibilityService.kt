package com.zui.perfctl

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class PerfCtlAccessibilityService : AccessibilityService() {
    private var lastPackage = ""
    private var lastEventAtMs = 0L
    private var lastRate = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        PerfCtlQuickService.start(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString().orEmpty()
        if (!isScenePackage(pkg)) {
            return
        }
        val now = SystemClock.elapsedRealtime()
        if (pkg == lastPackage && now - lastEventAtMs < DUPLICATE_WINDOW_MS) {
            return
        }
        lastPackage = pkg
        lastEventAtMs = now

        val rate = rateForPackage(pkg)
        lockRefresh(rate)
        publishScene(pkg)
        PerfCtlQuickService.start(this)
    }

    override fun onInterrupt() = Unit

    private fun isScenePackage(pkg: String): Boolean {
        return PackageNames.isValid(pkg) &&
            pkg != packageName &&
            pkg != "com.android.systemui"
    }

    private fun rateForPackage(pkg: String): Int {
        Settings.System.getString(contentResolver, PerfCtlContract.KEY_RULES_TEXT)
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

    private fun lockRefresh(rate: Int) {
        val active = Settings.System.getString(contentResolver, PerfCtlContract.KEY_ACTIVE_REFRESH)
        val peak = Settings.System.getString(contentResolver, "peak_refresh_rate")
        val min = Settings.System.getString(contentResolver, "min_refresh_rate")
        if (rate == lastRate &&
            active == rate.toString() &&
            peak == "$rate.0" &&
            min == "$rate.0") {
            return
        }
        lastRate = rate
        Settings.System.putString(contentResolver, PerfCtlContract.KEY_ACTIVE_REFRESH, rate.toString())
        Settings.System.putString(contentResolver, "peak_refresh_rate", "$rate.0")
        Settings.System.putString(contentResolver, "min_refresh_rate", "$rate.0")
    }

    private fun publishScene(pkg: String) {
        Settings.System.putString(
            contentResolver,
            PerfCtlContract.KEY_SCENE_EVENT_TEXT,
            "${SystemClock.elapsedRealtimeNanos()}|$pkg",
        )
        Settings.System.putString(contentResolver, PerfCtlContract.KEY_TOP_PACKAGE, pkg)
    }

    private companion object {
        private const val BASE_REFRESH_RATE = 120
        private const val DUPLICATE_WINDOW_MS = 120L
    }
}
