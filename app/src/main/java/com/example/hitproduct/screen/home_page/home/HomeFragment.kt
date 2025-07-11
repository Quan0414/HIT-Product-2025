package com.example.hitproduct.screen.home_page.home

import android.content.res.ColorStateList
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
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.databinding.FragmentHomeBinding


class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private lateinit var lottieView: LottieAnimationView

    override fun initView() {

        binding.gifCat.apply {
            setAnimation(R.raw.sleep_cat)
            // tăng tốc playback
            speed = 3.0f
            // lặp vô hạn
            repeatCount = LottieDrawable.INFINITE
            // ưu tiên render GPU
            renderMode = RenderMode.HARDWARE
            // start
            playAnimation()
        }

        // Khởi tạo 2 thanh ngay khi view được inflate
        updateStateBar(binding.state1, binding.icon1, 88)   // ví dụ giá trị ban đầu = 0
        updateStateBar(binding.state2, binding.icon2, 0)
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

    }

    override fun handleEvent() {

    }

    override fun bindData() {

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
                else        -> R.color.status4
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
        val yOffset = - (ph + 80)

        popup.showAsDropDown(anchor, xOffset, yOffset)
    }





}