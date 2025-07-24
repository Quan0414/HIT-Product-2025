package com.example.hitproduct

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.databinding.ActivityMainBinding
import com.example.hitproduct.screen.home_page.couple.CoupleFragment
import com.example.hitproduct.screen.home_page.home.HomeFragment
import com.example.hitproduct.screen.home_page.message.MessageFragment
import com.example.hitproduct.screen.home_page.calendar.NoteFragment
import com.example.hitproduct.screen.home_page.setting.main.SettingFragment
import com.example.hitproduct.socket.SocketManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val prefs by lazy {
        getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }
    private lateinit var token: String

    // index 0..4 tương đương Message, Note, Home, Couple(Game), Setting
    private val fragments = listOf(
        MessageFragment(),
        NoteFragment(),
        HomeFragment(),
        CoupleFragment(),
        SettingFragment()
    )
    private var currentIndex = 2   // mặc định show Home


    var coin: Int = 0
    var question: String = ""
    var yourAnswer: String = ""
    var yourLoveAnswer: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, "").orEmpty()
        SocketManager.connect(token)

        // Hide nav-bar + status-bar, bật immersive sticky
//        window.decorView.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//                    View.SYSTEM_UI_FLAG_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY


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

        // 2) Khởi tạo UI bottom nav
        setupBottomNav()

        // 3) Sau cùng show tab Home overlay
        selectTab(currentIndex)
    }

    private fun setupBottomNav() {
        // lấy các default icon và overlay container
        val defaults = listOf<ImageView>(
            binding.ivMessageDefault,
            binding.ivNoteDefault,
            binding.ivHomeDefault,
            binding.ivCoupleDefault,
            binding.ivSettingDefault
        )
        val overlays = listOf<FrameLayout>(
            binding.flMessageOverlay,
            binding.flNoteOverlay,
            binding.flHomeOverlay,
            binding.flCoupleOverlay,
            binding.flSettingOverlay
        )

        // gắn click cho từng default icon
        defaults.forEachIndexed { idx, iv ->
            iv.setOnClickListener {
                selectTab(idx)
            }
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

}
