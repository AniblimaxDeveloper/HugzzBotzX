package com.lutzzpie.parental

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class AppBlockerService : AccessibilityService() {
    companion object { var instance: AppBlockerService? = null }
    
    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onDestroy() { instance = null; super.onDestroy() }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            // BLOKIR TIKTOK, YOUTUBE, INSTAGRAM
            if (pkg == "com.zhiliaoapp.musically" || pkg == "com.google.android.youtube" || pkg == "com.instagram.android") {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }
    override fun onInterrupt() {}

    fun performTap(x: Float, y: Float) {
        dispatchGesture(GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(Path().apply { moveTo(x, y) }, 0, 40)).build(), null, null)
    }
    fun performGlobalKey(code: Int) {
        when (code) { 4 -> performGlobalAction(GLOBAL_ACTION_BACK); 3 -> performGlobalAction(GLOBAL_ACTION_HOME) }
    }
}
