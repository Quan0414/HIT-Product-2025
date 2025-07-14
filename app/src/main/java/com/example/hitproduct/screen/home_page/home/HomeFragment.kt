package com.example.hitproduct.screen.home_page.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentHomeBinding
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId


class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }

    private val viewModel by activityViewModels<HomeViewModel> {
        HomeViewModelFactory(authRepo)
    }

    private val hungryCat = listOf(
        R.raw.angry_cat,
        R.raw.cat_cry,
        R.raw.cat_the_luoi
    )
    private val normalCat = listOf(
        R.raw.spicy_cat,
        R.raw.cat_hands_up,
        R.raw.sleep_cat,
    )
    private val happyCat = listOf(
        R.raw.dance_cat,
        R.raw.dance_cat2,
        R.raw.dance_cat3,
        R.raw.dance_cat4,
    )

    private var eattingCat = R.raw.cat_eat

    private var currentCatList: List<Int> = normalCat
    private var currentCat: Int? = null

    override fun initView() {

        binding.gifCat.visibility = View.GONE
        binding.gifCat.apply {
            speed = 3.0f
            repeatCount = LottieDrawable.INFINITE
            renderMode = RenderMode.HARDWARE

            setOnClickListener {
                val next = currentCatList
                    .filter { it != currentCat }
                    .randomOrNull() ?: currentCatList.random()
                currentCat = next
                setAnimation(next)
                playAnimation()
            }
        }

    }

    override fun initListener() {
        binding.icon1.setOnClickListener {
            val value = "${binding.state1.progress}/${binding.state1.max}"
            showTooltip(
                it, value,
                R.layout.tooltip,         // layout cũ của state1
                R.id.popup_state1         // id TextView trong tooltip.xml
            )
        }
        binding.icon2.setOnClickListener {
            val value = "${binding.state2.progress}/${binding.state2.max}"
            showTooltip(
                it, value,
                R.layout.tooltip2,   // layout mới cho state2
                R.id.popup_state2         // id TextView trong tooltip_happy.xml
            )
        }
    }


    override fun initData() {
        viewModel.getCoupleProfile()
        viewModel.getPet()
    }

    override fun handleEvent() {

    }

    @SuppressLint("SetTextI18n")
    override fun bindData() {
        viewModel.coupleProfile.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {}
                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    binding.tvMoney.text = state.data.coin.toString()

                    // 1. Parse startDate (ISO string) thành OffsetDateTime
                    val startDateStr =
                        state.data.loveStartedAt            // ví dụ "2025-07-11T10:24:04.501Z"
                    val startInstant = Instant.parse(startDateStr)
                    val startDate = startInstant
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    val nowDate = LocalDate.now(ZoneId.systemDefault())

                    val period = Period.between(startDate, nowDate)
                    val years = period.years
                    val months = period.months
                    val daysTotal = period.days

                    // 4. Chuyển days thành tuần + ngày
                    val weeks = daysTotal / 7
                    val days = daysTotal % 7

                    // 6. Cập nhật UI
                    binding.tvYearNumber.text = years.toString()
                    binding.tvMonthNumber.text = months.toString()
                    binding.tvWeekNumber.text = weeks.toString()
                    binding.tvDayNumber.text = days.toString()
                }
            }
        }

        viewModel.pet.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    binding.gifCat.visibility = View.GONE
                }

                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    val hunger = state.data.hunger
                    val happiness = state.data.happiness

                    updateStateBar(binding.state1, binding.icon1, 20)
                    updateStateBar(binding.state2, binding.icon2, 100)

                    currentCatList = when {
                        hunger < 30 -> hungryCat
                        hunger < 70 -> normalCat
                        else -> happyCat
                    }

                    val first = currentCatList.random()
                    currentCat = first
                    binding.gifCat.apply {
                        visibility = View.VISIBLE
                        setAnimation(first)
                        playAnimation()
                    }
                }
            }
        }
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    // Hàm helper tính vị trí X cho icon
    private fun moveIcon(bar: ProgressBar, icon: View) {
        // tỉ lệ fill (0..1)
        val ratio = bar.progress.toFloat() / bar.max

        // chiều rộng thực của track
        val trackWidth = bar.width - bar.paddingLeft - bar.paddingRight

        // vị trí tâm của fill (tính từ trái FrameLayout)
        val centerX = bar.paddingLeft + ratio * trackWidth

        // vị trí left của icon (để center icon = centerX)
        val desiredLeft = centerX - icon.width / 2f

        // clamp left sao cho icon luôn nằm trong khung:
        // min = 0 (góc trái), max = bar.width - icon.width (góc phải)
        val clampedLeft = desiredLeft.coerceIn(
            0f,
            (bar.width - icon.width).toFloat()
        )

        // gán toạ độ
        icon.x = clampedLeft
    }

    private fun tintOnlyFill(bar: ProgressBar, @ColorRes fillColorRes: Int) {
        val color = ContextCompat.getColor(requireContext(), fillColorRes)
        // Lấy progressDrawable là LayerDrawable
        val layer = bar.progressDrawable.mutate() as LayerDrawable

        // Tìm layer progress (ClipDrawable hoặc ShapeDrawable)
        val prog = layer.findDrawableByLayerId(android.R.id.progress)
        prog?.let {
            // wrap để tint an toàn
            val wrapped = DrawableCompat.wrap(it).mutate()
            DrawableCompat.setTint(wrapped, color)
            // gán lại vào layer
            layer.setDrawableByLayerId(android.R.id.progress, wrapped)
        }

        // apply lại drawable đã chỉnh
        bar.progressDrawable = layer
    }


    private fun updateStateBar(bar: ProgressBar, icon: View, value: Int) {
        bar.progress = value

        // chỉ đổi màu fill của state1
        if (bar.id == R.id.state1) {
            val colorRes = when {
                value <= 24 -> R.color.status1
                value <= 49 -> R.color.status2
                value <= 74 -> R.color.status3
                else -> R.color.status4
            }
            tintOnlyFill(bar, colorRes)
        }

        bar.post { moveIcon(bar, icon) }
    }


    //popup tooltip
    private fun showTooltip(
        anchor: View,
        text: String,
        @LayoutRes layoutRes: Int,
        @IdRes textViewId: Int
    ) {
        val popupView = layoutInflater.inflate(layoutRes, null)
        val tv = popupView.findViewById<TextView>(textViewId)
        tv.text = text

        val popup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 8f
        }

        popupView.measure(0, 0)
        val pw = popupView.measuredWidth
        val ph = popupView.measuredHeight

        val xOffset = anchor.width / 2 - pw / 2
        val yOffset = -(ph + 80)

        popup.showAsDropDown(anchor, xOffset, yOffset)
    }


}