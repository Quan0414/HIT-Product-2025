package com.example.hitproduct

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.hitproduct.databinding.ActivityMainBinding
import com.example.hitproduct.screen.home_page.game.GameFragment
import com.example.hitproduct.screen.home_page.home.HomeFragment
import com.example.hitproduct.screen.home_page.message.MessageFragment
import com.example.hitproduct.screen.home_page.note.NoteFragment
import com.example.hitproduct.screen.home_page.setting.main.SettingFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // index 0..4 tương đương Message, Note, Home, Couple(Game), Setting
    private val fragments = listOf(
        MessageFragment(),
        NoteFragment(),
        HomeFragment(),
        GameFragment(),
        SettingFragment()
    )
    private var currentIndex = 2   // mặc định show Home


    var coin: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

}
