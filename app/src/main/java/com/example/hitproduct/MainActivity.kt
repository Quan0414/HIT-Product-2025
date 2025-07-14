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

    // Giữ tab hiện tại
    private var currentTabId: Int = R.id.home

    // Tạo sẵn các Fragment và map với tab-id
    private val fragments: Map<Int, androidx.fragment.app.Fragment> = mapOf(
        R.id.game    to GameFragment(),
        R.id.home    to HomeFragment(),
        R.id.note    to NoteFragment(),
        R.id.message to MessageFragment(),
        R.id.setting to SettingFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomBar = findViewById(R.id.animatedBottomBar)

        // Add tất cả fragments, hide hết ngoại trừ HOME
        supportFragmentManager
            .beginTransaction()
            .apply {
                fragments.forEach { (tabId, fragment) ->
                    add(fragContainer, fragment, tabId.toString())
                    if (tabId != currentTabId) hide(fragment)
                }
            }
            .commitNow()

        // Chọn tab HOME làm mặc định
        bottomBar.selectTabById(currentTabId, true)

        // Lắng nghe sự kiện đổi tab
        bottomBar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                val newId = newTab.id
                // nếu chọn lại tab đang mở thì bỏ qua
                if (newId == currentTabId) return

                supportFragmentManager
                    .beginTransaction()
                    .apply {
                        fragments[currentTabId]?.let { hide(it) }
                        fragments[newId]?.let { show(it) }
                    }
                    .commit()

                currentTabId = newId
            }

            override fun onTabReselected(index: Int, tab: AnimatedBottomBar.Tab) {
                // Bạn có thể scroll lên đầu hoặc refresh nếu cần
            }
        })

        // Xử lý back: nếu không phải HOME thì về HOME, ngược lại thoát app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentTabId != R.id.home) {
                    // Quay lại tab HOME
                    bottomBar.selectTabById(R.id.home, true)
                } else {
                    // Cho phép hệ thống xử lý (thoát)
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}
