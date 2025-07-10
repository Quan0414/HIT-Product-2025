package com.example.hitproduct

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.hitproduct.screen.home_page.game.GameFragment
import com.example.hitproduct.screen.home_page.home.HomeFragment
import com.example.hitproduct.screen.home_page.message.MessageFragment
import com.example.hitproduct.screen.home_page.note.NoteFragment
import com.example.hitproduct.screen.home_page.setting.account_setting.AccountSettingFragment
import com.example.hitproduct.screen.home_page.setting.main.SettingFragment

class MainActivity : AppCompatActivity() {
    private lateinit var btnGame: ImageView
    private lateinit var btnNote: ImageView
    private lateinit var btnHome: ImageView
    private lateinit var btnMess: ImageView
    private lateinit var btnSetting: ImageView
    private val fragContainer = R.id.fragmentHomeContainer

    private var currentTab: Tab = Tab.HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Bind
        btnGame = findViewById(R.id.btn_game)
        btnNote = findViewById(R.id.btn_note)
        btnHome = findViewById(R.id.btn_home)
        btnMess = findViewById(R.id.btn_mess)
        btnSetting = findViewById(R.id.btn_setting)

        // 2. Set click
        btnGame.setOnClickListener { selectTab(Tab.GAME) }
        btnNote.setOnClickListener { selectTab(Tab.NOTE) }
        btnHome.setOnClickListener { selectTab(Tab.HOME) }
        btnMess.setOnClickListener { selectTab(Tab.MESS) }
        btnSetting.setOnClickListener { selectTab(Tab.SETTING) }

        // 3. Mặc định mở Home
        selectTab(Tab.HOME)

        // thêm back‐callback
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // 1) Nếu có fragment trong back-stack → pop
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                    }
                    // 2) Không còn nested fragment, nhưng đang không ở HOME → về HOME tab
                    else if (currentTab != Tab.HOME) {
                        selectTab(Tab.HOME)
                    }
                    // 3) Đang ở HOME, back bình thường (thoát app)
                    else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

    }

    // enum để dễ maintain
    private enum class Tab { GAME, NOTE, HOME, MESS, SETTING }

    private fun selectTab(tab: Tab) {
        currentTab = tab
        // 1. Reset vị trí + trạng thái icon
        listOf(btnGame, btnNote, btnHome, btnMess, btnSetting).forEach { iv ->
            iv.translationY = 0f
            iv.alpha = 0.6f             // ví dụ dim icon không active
        }

        // 2. Bring-to-front & nâng lên cho tab HOME, hoặc chỉ dim differents
        val selectedIv = when (tab) {
            Tab.GAME -> btnGame
            Tab.NOTE -> btnNote
            Tab.HOME -> btnHome.also {
                // lên cao
                it.translationY = -20f.dpToPx(this)
                // nổi trên các icon kia
                it.elevation    = 8f.dpToPx(this)
                // hoặc: ViewCompat.setTranslationZ(it, 8f.dpToPx(this))
            }


            Tab.MESS -> btnMess
            Tab.SETTING -> btnSetting
        }
        selectedIv.alpha = 1f

        // 3. Replace fragment tương ứng
        val frag = when (tab) {
            Tab.GAME -> GameFragment()
            Tab.NOTE -> NoteFragment()
            Tab.HOME -> HomeFragment()
            Tab.MESS -> MessageFragment()
            Tab.SETTING -> SettingFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(fragContainer, frag)
            .commit()
    }

    // extension để convert dp → px
    private fun Float.dpToPx(ctx: Context): Float =
        this * ctx.resources.displayMetrics.density

    private fun Int.dpToPx(ctx: Context): Float =
        this.toFloat().dpToPx(ctx)
}
