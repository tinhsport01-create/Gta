package com.gtamenu.app

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

data class Cheat(val label: String, val code: String)

// Muốn thêm / sửa cheat: chỉ cần sửa danh sách này (nhãn + mã cheat).
val CHEATS = listOf(
    Cheat("Máu + giáp + 250k [HESOYAM]", "HESOYAM"),
    Cheat("Bộ vũ khí 1 [LXGIWYL]", "LXGIWYL"),
    Cheat("Bộ vũ khí 2 [KJKSZPJ]", "KJKSZPJ"),
    Cheat("Bộ vũ khí 3 [UZUMYMW]", "UZUMYMW"),
    Cheat("Đạn vô hạn [WANRLTW]", "WANRLTW"),
    Cheat("Không bị truy nã [AEZAKMI]", "AEZAKMI"),
    Cheat("Giảm sao truy nã [ASNAEB]", "ASNAEB"),
    Cheat("Tăng sao truy nã [OSRBLHH]", "OSRBLHH"),
    Cheat("Jetpack [ROCKETMAN]", "ROCKETMAN"),
    Cheat("Xe tăng Rhino [AIWPRTON]", "AIWPRTON"),
    Cheat("Trực thăng Hunter [OHDUDE]", "OHDUDE"),
    Cheat("Cơ bắp [BUFFMEUP]", "BUFFMEUP"),
)

object Shizu {
    fun ready(): Boolean = try {
        Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (e: Throwable) {
        false
    }

    // Gửi từng phím chữ vào cửa sổ đang focus (game) bằng lệnh "input keyevent" qua Shizuku.
    fun send(code: String, onDone: (String) -> Unit) {
        Thread {
            try {
                val cmd = code.lowercase().filter { it in 'a'..'z' }
                    .map { "input keyboard keyevent ${29 + (it - 'a')}" }
                    .joinToString(" ; ")
                val m = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java, Array<String>::class.java, String::class.java
                )
                m.isAccessible = true
                val p = m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
                p.waitFor()
                onDone("Đã gửi: $code")
            } catch (e: Throwable) {
                onDone("Lỗi: ${e.message}")
            }
        }.start()
    }
}
