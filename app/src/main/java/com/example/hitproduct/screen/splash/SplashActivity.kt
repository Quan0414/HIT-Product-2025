package com.example.hitproduct.screen.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.screen.authentication.login.LoginActivity
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val prefs by lazy {
        getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(this),
            prefs
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, /*bottom=*/0)
            insets
        }

        val img = findViewById<ImageView>(R.id.imgOnboarding)
        img.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0f
            scaleY = 0f

            post {
                // 1) Phình to + fade-in
                animate()
                    .alpha(1f)
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(600)
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction {
                        // 2) Delay giữa hai bước
                        Handler(Looper.getMainLooper()).postDelayed({
                            // 3) Thu nhỏ về size gốc
                            img.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(400)
                                .setInterpolator(DecelerateInterpolator())
                                .withEndAction {
                                    // 4) tiếp tục flow: start ProgressBar…
                                    startProgressBarAndRoute()
                                }
                                .start()
                        }, 50) // delay 200ms giữa 2 animation
                    }
                    .start()
            }
        }

    }

    private fun routeNext() {
        // Kiểm tra kết nối internet
        if (!isNetworkAvailable()) {
            // Nếu không có kết nối, hiển thị thông báo lỗi
            Toast.makeText(
                this,
                "Không có kết nối Internet. Vui lòng kiểm tra lại!",
                Toast.LENGTH_LONG
            ).show()
            Handler(mainLooper).postDelayed({
                finishAffinity()
            }, 1000)
            return
        }

        val token = prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, null)
        if (token.isNullOrEmpty()) {
            // Chưa login → sang Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // Đã có token → check couple
            lifecycleScope.launch {
                when (val res = authRepo.fetchProfile()) {
                    is DataResult.Success -> {
                        val myUserId = res.data.id
                        prefs.edit().putString(AuthPrefersConstants.MY_USER_ID, myUserId).apply()

                        val coupleOjb = res.data.couple
                        if (coupleOjb != null) {
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
                        if (res.error.message == "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại!") {
                            prefs.edit().remove(AuthPrefersConstants.ACCESS_TOKEN).apply()
                            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                            finish()
                            Toast.makeText(
                                this@SplashActivity,
                                "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Lỗi khác, có thể do mạng, thông báo lỗi
                            Toast.makeText(
                                this@SplashActivity,
                                "Lỗi: ${res.error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    // Kiểm tra kết nối internet
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities)
        return activeNetwork != null && activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun startProgressBarAndRoute() {
        val pb = findViewById<ProgressBar>(R.id.state)
        pb.visibility = View.VISIBLE
        pb.max = 1000
        pb.progress = 0

        // Tạo animator progress 0→max
        val animator = ObjectAnimator.ofInt(pb, "progress", 0, pb.max).apply {
            duration = 1000L
            interpolator = LinearInterpolator()
        }

        // Lắng nghe khi animation kết thúc → chuyển màn luôn
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                val done = prefs.getBoolean("onboarding_done", false)
                if (!done) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentSplash, OnboardingFragment(), "ON_BOARDING_FRAGMENT")
                        .commit()
                } else {
                    routeNext()
                }
            }
        })

        animator.start()
    }


}
