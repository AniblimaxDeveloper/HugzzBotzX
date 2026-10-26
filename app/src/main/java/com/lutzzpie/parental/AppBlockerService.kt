package com.lutzzpie.parental

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AppBlockerService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            // Daftar aplikasi yang diblokir (Bisa ditambah)
            if (packageName == "com.zhiliaoapp.musically" || // TikTok
                packageName == "com.google.android.youtube" || // YouTube
                packageName == "com.instagram.android") {     // Instagram
                performGlobalAction(GLOBAL_ACTION_HOME) // Tendang ke Home Screen
            }
        }
    }
    override fun onInterrupt() {}
}
