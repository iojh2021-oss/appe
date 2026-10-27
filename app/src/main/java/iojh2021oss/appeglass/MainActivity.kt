package iojh2021oss.appeglass

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {
    private var opacity = 24

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(0xFF101923.toInt())
        }
        val title = TextView(this).apply {
            text = "APPE  •  GLASS MEMORY"
            textSize = 24f
            setTextColor(0xFFEAF6FF.toInt())
            gravity = Gravity.CENTER
        }
        val description = TextView(this).apply {
            text = "لایه شیشه‌ای شناور با حافظه تصویری؛ تصویر صفحه با اجازه شما ثبت و روی گلس نگه داشته می‌شود."
            textSize = 16f
            setTextColor(0xFFB8CAD8.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 18, 0, 24)
        }
        val amountLabel = TextView(this).apply {
            text = "شدت شیشه: $opacity%"
            textSize = 16f
            setTextColor(0xFFEAF6FF.toInt())
            gravity = Gravity.CENTER
        }
        val slider = SeekBar(this).apply {
            max = 65
            progress = opacity
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    opacity = progress
                    amountLabel.text = "شدت شیشه: $opacity%"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    sendUpdate()
                }
            })
        }
        val start = Button(this).apply {
            text = "روشن‌کردن لایه شیشه"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                } else {
                    val i = Intent(this@MainActivity, GlassOverlayService::class.java)
                        .setAction(GlassOverlayService.ACTION_START)
                        .putExtra(GlassOverlayService.EXTRA_OPACITY, opacity)
                    if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
                }
            }
        }
        val stop = Button(this).apply {
            text = "خاموش‌کردن شیشه"
            setOnClickListener { stopService(Intent(this@MainActivity, GlassOverlayService::class.java)) }
        }
        listOf(title, description, amountLabel, slider, start, stop).forEach {
            root.addView(it, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                if (it is SeekBar) 54 else ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 12
            })
        }
        setContentView(root)
    }

    private fun sendUpdate() {
        if (Settings.canDrawOverlays(this)) {
            val i = Intent(this, GlassOverlayService::class.java)
                .setAction(GlassOverlayService.ACTION_UPDATE)
                .putExtra(GlassOverlayService.EXTRA_OPACITY, opacity)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
        }
    }
}
