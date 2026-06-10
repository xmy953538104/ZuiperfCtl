package com.zui.perfctl

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

class PerfCtlAccessibilityService : AccessibilityService() {
    private var lastPackage = ""
    private var lastEventAtMs = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        PerfCtlQuickService.start(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString().orEmpty()
        if (!RefreshSceneController.isScenePackage(this, pkg)) {
            return
        }
        val now = SystemClock.elapsedRealtime()
        val rate = RefreshSceneController.rateForPackage(this, pkg)
        val samePackage = pkg == lastPackage
        val alreadyLocked = RefreshSceneController.isLocked(this, rate)
        if (samePackage && alreadyLocked && now - lastEventAtMs < SAME_PACKAGE_WINDOW_MS) {
            return
        }
        lastPackage = pkg
        lastEventAtMs = now

        RefreshSceneController.lockRefresh(this, rate)
        RefreshSceneController.publishScene(this, pkg)
        PerfCtlQuickService.start(this)
    }

    override fun onInterrupt() = Unit

    private companion object {
        private const val SAME_PACKAGE_WINDOW_MS = 2000L
    }
}
