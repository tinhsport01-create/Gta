package com.gtamenu.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import rikka.shizuku.Shizuku

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        status = TextView(this).apply { textSize = 15f }
        box.addView(TextView(this).apply { text = "GTA SA Menu"; textSize = 24f })
        box.addView(status)

        fun btn(t: String, a: () -> Unit) = box.addView(Button(this).apply {
            text = t; setOnClickListener { a(); refresh() }
        })

        btn("1. Cấp quyền hiển thị trên ứng dụng khác") {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        btn("2. Cấp quyền Shizuku") {
            try {
                if (!Shizuku.pingBinder()) {
                    Toast.makeText(this, "Hãy mở Shizuku và khởi động nó trước", Toast.LENGTH_LONG).show()
                } else {
                    Shizuku.requestPermission(1)
                }
            } catch (e: Throwable) {
                Toast.makeText(this, "Chưa cài / chưa chạy Shizuku", Toast.LENGTH_LONG).show()
            }
        }
        btn("3. Bật menu nổi") {
            if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 0)
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Chưa cấp quyền hiển thị (bước 1)", Toast.LENGTH_LONG).show()
            } else {
                startForegroundService(Intent(this, OverlayService::class.java))
            }
        }
        btn("Tắt menu") { stopService(Intent(this, OverlayService::class.java)) }
        setContentView(box)
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        status.text = "Hiển thị nổi: " + (if (Settings.canDrawOverlays(this)) "OK" else "chưa") +
            "\nShizuku: " + (if (Shizu.ready()) "OK" else "chưa sẵn sàng")
    }
}
