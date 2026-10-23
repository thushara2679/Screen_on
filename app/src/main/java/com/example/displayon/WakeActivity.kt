package com.example.displayon

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView

/** Blank screen that lights the display, then closes itself. */
class WakeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager)
            .requestDismissKeyguard(this, null) // only dismisses if no secure lock

        setContentView(TextView(this).apply {
            text = "Display on"
            textSize = 28f
            gravity = android.view.Gravity.CENTER
        })
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 20_000)
    }
}
