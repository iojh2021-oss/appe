package iojh2021oss.appeglass

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout

class GlassOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var glassView: FrameLayout? = null
    private var opacity = 24

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(41, notification())
        when (intent?.action) {
            ACTION_UPDATE -> {
                opacity = intent.getIntExtra(EXTRA_OPACITY, opacity).coerceIn(0, 65)
                removeOverlay()
                showOverlay()
            }
            ACTION_START, null -> {
                opacity = intent?.getIntExtra(EXTRA_OPACITY, opacity)?.coerceIn(0, 65) ?: opacity
                if (Settings.canDrawOverlays(this)) showOverlay() else stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (glassView != null || !Settings.canDrawOverlays(this)) return
        val alpha = (opacity * 255 / 100).coerceIn(0, 165)
        val glass = FrameLayout(this).apply {
            // A subtle cool tint and soft edge highlight; no screen capture is performed.
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.argb(alpha, 205, 232, 255),
                    Color.argb((alpha * 0.55f).toInt(), 225, 241, 255),
                    Color.argb((alpha * 0.75f).toInt(), 190, 220, 245)
                )
            )
            contentDescription = "Appe Glass overlay. Open the app to turn it off."
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }
        try {
            windowManager.addView(glass, params)
            glassView = glass
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun removeOverlay() {
        glassView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        glassView = null
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL_ID, "Glass overlay", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun notification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, CHANNEL_ID)
            else @Suppress("DEPRECATION") Notification.Builder(this)
        return builder.setContentTitle("Appe Glass فعال است")
            .setContentText("برای خاموش‌کردن، اپ را باز کنید.")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        removeOverlay()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "iojh2021oss.appeglass.START"
        const val ACTION_UPDATE = "iojh2021oss.appeglass.UPDATE"
        const val EXTRA_OPACITY = "opacity"
        private const val CHANNEL_ID = "appe_glass_overlay"
    }
}
