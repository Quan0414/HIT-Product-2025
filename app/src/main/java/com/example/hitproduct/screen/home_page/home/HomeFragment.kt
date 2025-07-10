package com.example.hitproduct.screen.home_page.home

import android.view.LayoutInflater
import android.view.ViewGroup
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
            setRenderMode(RenderMode.HARDWARE)
            // start
            playAnimation()
        }

    }

    override fun initListener() {

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

}