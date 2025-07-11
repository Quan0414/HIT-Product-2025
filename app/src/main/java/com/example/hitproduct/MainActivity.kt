package com.example.hitproduct

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.hitproduct.screen.home_page.game.GameFragment
import com.example.hitproduct.screen.home_page.home.HomeFragment
import com.example.hitproduct.screen.home_page.message.MessageFragment
import com.example.hitproduct.screen.home_page.note.NoteFragment
import com.example.hitproduct.screen.home_page.setting.main.SettingFragment
import nl.joery.animatedbottombar.AnimatedBottomBar

class MainActivity : AppCompatActivity() {
    private lateinit var bottomBar: AnimatedBottomBar
    private val fragContainer = R.id.fragmentHomeContainer
    private var currentTabId: Int = R.id.home  // giữ id menu để so sánh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) Bind AnimatedBottomBar
        bottomBar = findViewById(R.id.animatedBottomBar)

        // 2) Listener để chuyển fragment
        bottomBar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                // nếu chọn lại tab đang mở, bỏ qua
                if (newTab.id == currentTabId) return
                currentTabId = newTab.id

                val frag = when (newTab.id) {
                    R.id.game    -> GameFragment()
                    R.id.note    -> NoteFragment()
                    R.id.home    -> HomeFragment()
                    R.id.message -> MessageFragment()
                    R.id.setting -> SettingFragment()
                    else         -> null
                }
                frag?.let {
                    supportFragmentManager.beginTransaction()
                        .replace(fragContainer, it)
                        .commit()
                }
            }
            override fun onTabReselected(index: Int, tab: AnimatedBottomBar.Tab) {
                // có thể xử lý khi bấm lại tab đang chọn, nếu cần
            }
        })

        // 3) Mặc định chọn HOME
        supportFragmentManager.beginTransaction()
            .replace(fragContainer, HomeFragment())
            .commit()
        // đưa con trỏ của AnimatedBottomBar về tab HOME
        bottomBar.selectTabById(R.id.home, true)

        // 4) Xử lý back giống trước
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                    } else if (currentTabId != R.id.home) {
                        bottomBar.selectTabById(R.id.home, true)
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }
}
