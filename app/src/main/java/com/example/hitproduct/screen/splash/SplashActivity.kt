package com.example.hitproduct.screen.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hitproduct.R
import com.example.hitproduct.screen.authentication.login.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Dùng Handler để delay 2 giây trước khi chuyển đến MainActivity
        Handler().postDelayed({
            // Chuyển đến MainActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // Kết thúc SplashActivity để không quay lại màn hình Splash khi nhấn nút back
            finish()
        }, 1000) // 20
    }
}