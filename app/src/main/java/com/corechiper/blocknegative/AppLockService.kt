package com.corechiper.blocknegative

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppLockService : AccessibilityService() {

    private val blockedApps = hashSetOf(
        "com.zhiliaoapp.musically",      // TikTok Global
        "com.ss.android.ugc.trill",      // TikTok SEA
        "com.facebook.katana",           // Facebook App
        "com.facebook.lite"              // Facebook Lite
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (blockedApps.contains(packageName)) {
                val intent = Intent(this, LockOverlayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("TARGET_PACKAGE", packageName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}
