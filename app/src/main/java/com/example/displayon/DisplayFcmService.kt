package com.example.displayon

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DisplayFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val code = Prefs.code(this) ?: return
        CoroutineScope(Dispatchers.IO).launch { runCatching { Repo.updateToken(code, token) } }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data["action"] != "SCREEN_ON") return
        wakeScreen()
    }

    @Suppress("DEPRECATION")
    private fun wakeScreen() {
        // Fallback: brief wake lock that turns the screen on.
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "displayon:wake"
        ).acquire(5_000)

        // With "display over other apps" granted, the activity may start directly from background.
        if (android.provider.Settings.canDrawOverlays(this)) {
            runCatching {
                startActivity(Intent(this, WakeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }

        // Also: full-screen intent launches WakeActivity even from background.
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel("wake", "Remote wake", NotificationManager.IMPORTANCE_HIGH)
        )
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, WakeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(this, "wake")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Display on")
            .setContentText("Screen turned on remotely")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pi, true)
            .setAutoCancel(true)
            .setTimeoutAfter(20_000)
            .build()
        nm.notify(1, n)
    }
}
