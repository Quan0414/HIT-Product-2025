package com.example.hitproduct.screen.authentication.login

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hitproduct.R
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val prefs by lazy {
        getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Log.d("LoginActivity", "Notification permission granted")
            } else {
                Log.d("LoginActivity", "Notification permission denied")
                // Có thể show dialog dẫn user vào Settings nếu cần
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Kiểm tra nếu chưa có Fragment, thay thế Fragment bằng LoginFragment
        if (supportFragmentManager.findFragmentByTag("LOGIN_FRAGMENT") == null) {
            val loginFragment = LoginFragment() // Tạo một instance của LoginFragment
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentStart,
                    loginFragment,
                    "LOGIN_FRAGMENT"
                ) // Thay thế vào container
                .commit()
        }

    }
}
