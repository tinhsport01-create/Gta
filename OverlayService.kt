package com.gtamenu.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var panel: LinearLayout? = null
    private var bubble: TextView? = null
    private lateinit var status: TextView
    private val ui = Handler(Looper.getMainLooper())

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun lp(w: Int, h: Int) = WindowManager.LayoutParams(
        w, h,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        // NOT_FOCUSABLE: phím giả lập vẫn đi vào game, không bị menu giữ focus
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; x = dp(20); y = dp(80) }

    private fun makeDraggable(handle: View, params: WindowManager.LayoutParams, target: View, onTap: (() -> Unit)? = null) {
        var sx = 0f; var sy = 0f; var px = 0; var py = 0; var moved = false
        handle.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; px = params.x; py = params.y; moved = false }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - sx).toInt(); val dy = (e.rawY - sy).toInt()
                    if (Math.abs(dx) > 8 || Math.abs(dy) > 8) moved = true
                    params.x = px + dx; params.y = py + dy
                    wm.updateViewLayout(target, params)
                }
                MotionEvent.ACTION_UP -> if (!moved) onTap?.invoke()
            }
            true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ch = "menu"
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(NotificationChannel(ch, "Menu", NotificationManager.IMPORTANCE_LOW))
        startForeground(1, Notification.Builder(this, ch)
            .setContentTitle("GTA SA Menu đang chạy")
            .setSmallIcon(android.R.drawable.ic_menu_manage).build())
        if (panel == null) build()
        return START_STICKY
    }

    private fun build() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val p = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EE2B1B3A")); cornerRadius = dp(12).toFloat()
            }
        }
        val pParams = lp(dp(260), WindowManager.LayoutParams.WRAP_CONTENT)

        val bParams = lp(dp(48), dp(48))
        val b = TextView(this).apply {
            text = "M"; textSize = 20f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            background = GradientDrawable().apply { setColor(Color.parseColor("#CC8E24AA")); shape = GradientDrawable.OVAL }
            visibility = View.GONE
        }

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply {
            text = "Menu cheat (kéo để di chuyển)"; setTextColor(Color.parseColor("#FF80D0"))
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val hide = Button(this).apply { text = "Ẩn"; textSize = 12f; isAllCaps = false }
        header.addView(title); header.addView(hide)
        p.addView(header)

        status = TextView(this).apply {
            setTextColor(Color.parseColor("#80FFB0")); textSize = 12f
            text = if (Shizu.ready()) "● Shizuku sẵn sàng" else "● Shizuku CHƯA sẵn sàng"
        }
        p.addView(status)

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        for (c in CHEATS) {
            list.addView(Button(this).apply {
                text = c.label; textSize = 12f; isAllCaps = false
                setOnClickListener {
                    status.text = "Đang gửi ${c.code}..."
                    Shizu.send(c.code) { msg -> ui.post { status.text = msg } }
                }
            })
        }
        p.addView(ScrollView(this).apply { addView(list) },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(240)))

        makeDraggable(title, pParams, p)
        makeDraggable(b, bParams, b) {
            b.visibility = View.GONE; p.visibility = View.VISIBLE
        }
        hide.setOnClickListener {
            bParams.x = pParams.x; bParams.y = pParams.y
            wm.updateViewLayout(b, bParams)
            p.visibility = View.GONE; b.visibility = View.VISIBLE
        }

        wm.addView(p, pParams); wm.addView(b, bParams)
        panel = p; bubble = b
    }

    override fun onDestroy() {
        try { panel?.let { wm.removeView(it) }; bubble?.let { wm.removeView(it) } } catch (_: Throwable) {}
        panel = null; bubble = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
