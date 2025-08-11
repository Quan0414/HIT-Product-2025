package com.example.hitproduct

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.util.CryptoHelper
import com.example.hitproduct.common.util.DialogNetworkDisconnect
import com.example.hitproduct.databinding.ActivityMainBinding
import com.example.hitproduct.screen.home_page.calendar.NoteFragment
import com.example.hitproduct.screen.home_page.couple.CoupleFragment
import com.example.hitproduct.screen.home_page.home.HomeFragment
import com.example.hitproduct.screen.home_page.message.MessageFragment
import com.example.hitproduct.screen.home_page.setting.main.SettingFragment
import com.example.hitproduct.socket.SocketManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val net by lazy { (application as MyApp).networkMonitor }
    private var netDialog: DialogNetworkDisconnect? = null

    // index 0..4 tương đương Message, Note, Home, Couple(Game), Setting
    private val fragments = listOf(
        MessageFragment(),
        NoteFragment(),
        HomeFragment(),
        CoupleFragment(),
        SettingFragment()
    )
    private var currentIndex = 2   // mặc định show Home

    private val prefs by lazy {
        getSharedPreferences(AuthPrefersConstants.PREFS_NAME, MODE_PRIVATE)
    }

    var coin: Int = 0
    var question: String = ""
    var yourAnswer: String = ""
    var yourLoveAnswer: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                net.isOnline.collect { online ->
                    if (online) hideNoNetDialog() else showNoNetDialog()
                }
            }
        }

        val token = prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, "")
        SocketManager.connect(token ?: "")
        Log.d("MainActivity", "Socket connected: ${SocketManager.isConnected()}")
        SocketManager.onNotificationReceived {}
        SocketManager.onNewPubKeyReceived { data ->
            val newPubKey = data.optString("public_key", "")
            Log.d("MainActivity", "New pubkey receive: $newPubKey")
            CryptoHelper.storePeerPublicKey(this, newPubKey)
            CryptoHelper.deriveAndStoreSharedAesKey(this)
        }

        // 1) Pre-add all fragments, hide trừ Home (index 2)
        supportFragmentManager.beginTransaction().apply {
            fragments.forEachIndexed { idx, frag ->
                add(
                    binding.fragmentHomeContainer.id,
                    frag,
                    "tab$idx"
                )
                if (idx != currentIndex) hide(frag)
            }
        }.commitNow()

        setupBottomNav()
        selectTab(currentIndex)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars(window)
    }

//    override fun onBackPressed() {
//        if (currentIndex != 2) {
//            selectTab(2)
//        } else {
//            super.onBackPressed()
//        }
//    }

    private fun hideSystemBars(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
    }

    private fun setupBottomNav() {
        val defaults = listOf(
            binding.ivMessageDefault,
            binding.ivNoteDefault,
            binding.ivHomeDefault,
            binding.ivCoupleDefault,
            binding.ivSettingDefault
        )
        defaults.forEachIndexed { idx, iv ->
            iv.setOnClickListener { selectTab(idx) }
        }
    }

    private fun selectTab(idx: Int) {
        // 1) Reset: hiện tất cả icon mặc định, ẩn tất cả overlay
        val defaults = listOf(
            binding.ivMessageDefault,
            binding.ivNoteDefault,
            binding.ivHomeDefault,
            binding.ivCoupleDefault,
            binding.ivSettingDefault
        )
        val overlays = listOf(
            binding.flMessageOverlay,
            binding.flNoteOverlay,
            binding.flHomeOverlay,
            binding.flCoupleOverlay,
            binding.flSettingOverlay
        )
        defaults.forEach { it.visibility = View.VISIBLE }
        overlays.forEach { it.visibility = View.GONE }

        // 2) Luôn show overlay cho tab được chọn + ẩn icon default
        defaults[idx].visibility = View.GONE
        overlays[idx].visibility = View.VISIBLE

        // 3) Chỉ chạy fragment transaction khi thực sự đổi tab
        if (idx != currentIndex) {
            supportFragmentManager.beginTransaction().apply {
                hide(fragments[currentIndex])
                show(fragments[idx])
            }.commit()
            currentIndex = idx
        }
    }

    fun switchToTab(idx: Int) {
        selectTab(idx)
    }

    fun goToHomeTab() = switchToTab(2)

    fun hideBottomNav() {
        binding.bottomNavigationContainer.visibility = View.GONE
    }

    fun showBottomNav() {
        binding.bottomNavigationContainer.visibility = View.VISIBLE
    }

    private fun showNoNetDialog() {
        if (isFinishing || isDestroyed) return
        if (netDialog?.isAdded == true) return
        netDialog = DialogNetworkDisconnect().apply { isCancelable = false }
        netDialog!!.show(supportFragmentManager, "net_disconnect")
    }

    private fun hideNoNetDialog() {
        netDialog?.dismissAllowingStateLoss()
        netDialog = null
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

}
