package com.example.hitproduct.screen.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.hitproduct.MainActivity
import com.example.hitproduct.R
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.api.RetrofitClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.screen.authentication.login.LoginActivity
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val prefs by lazy {
        getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            RetrofitClient.getInstance().create(ApiService::class.java),
            prefs
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        Handler(mainLooper).postDelayed({
            routeNext()
        }, 1000)
    }

    private fun routeNext() {
        val token = prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, null)
        if (token.isNullOrEmpty()) {
            // Chưa login → sang Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // Đã có token → check couple
            lifecycleScope.launch {
                when (val res = authRepo.checkCouple(token)) {
                    is DataResult.Success -> {
                        val userData = res.data
                        val coupleId = userData?.coupleId
                        if (coupleId != null) {
                            // Đã có đôi → vào Main
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        } else {
                            // Chưa có đôi → xoá token, notify, về Login
                            prefs.edit().remove(AuthPrefersConstants.ACCESS_TOKEN).apply()
                            Toast.makeText(
                                this@SplashActivity,
                                "Bạn chưa có đôi, vui lòng đăng nhập lại",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        }
                        finish()
                    }

                    is DataResult.Error -> {
                        // Lỗi (hết session, mạng…) → xoá token, về Login
                        prefs.edit().remove(AuthPrefersConstants.ACCESS_TOKEN).apply()
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}
