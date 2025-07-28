package com.example.hitproduct.screen.splash

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.data.model.onboarding.OnboardingItem
import com.example.hitproduct.databinding.FragmentOnBoardingBinding
import com.example.hitproduct.screen.adapter.OnboardingAdapter
import com.example.hitproduct.screen.authentication.login.LoginActivity

class OnboardingFragment : BaseFragment<FragmentOnBoardingBinding>() {

    private val pages = listOf(
        OnboardingItem(
            title = "Kết nối với người bạn yêu quý",
            content = "Kết nối với nửa ấy của bạn một cách dễ dàng, cùng nhau trải nhiệm những điều tuyệt vời nhất của Đếm Ngày “Xa” Nhau nhé."
        ),
        OnboardingItem(
            title = "Ghi lại từng ngày bạn bên cạnh cậu ấy",
            content = "Đếm từng ngày bạn được hạnh phúc bên cạnh cậu ấy và ghi nhớ những cột mốc quan trọng trong câu chuyện của hai người."
        ),
        OnboardingItem(
            title = "Cùng nhau chăm sóc thú cưng với nửa kia của bạn",
            content = "Làm nhiệm vụ, kiếm tiền, chăm sóc thú cưng, và tất cả điều đó cùng với nửa bên kia của mình, không còn gì tuyệt vời hơn phải không?"
        ),
        OnboardingItem(
            title = "Thêm xóa, chỉnh sửa ghi chú cùng nhau",
            content = "Lên kế hoạch đi chơi, xem phim, làm việc,.. và nhiều thứ khác cùng nhau để cả hai có thêm thời gian cùng chuẩn bị cho những kỉ niệm đáng trân trọng nhé."
        ),
        OnboardingItem(
            title = "Và muôn vàn hoạt động để tăng sự gắn kết",
            content = "Trả lời câu hỏi, nhắn tin, nuôi thú cưng, cá nhân hóa giao diện,... rất nhiều hoạt động để bạn có thể làm cùng nửa kia của mình."
        )
    )


    override fun initView() {
        // 1. Gán adapter cho ViewPager2
        val adapter = OnboardingAdapter(pages)
        binding.viewPager.adapter = adapter

        // 2. Kết nối DotsIndicator
        binding.dotsIndicator.setViewPager2(binding.viewPager)

    }

    override fun initListener() {

        binding.tvContinue.setOnClickListener {
            val currentPage = binding.viewPager.currentItem
            if (currentPage < pages.lastIndex) {
                binding.viewPager.currentItem = currentPage + 1
            } else {
                requireContext()
                    .getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("onboarding_done", true)
                    .apply()
                // Nếu đã ở trang cuối, chuyển đến LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            }
        }

        binding.btnSkip.setOnClickListener {
            requireContext()
                .getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_done", true)
                .apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.tvContinue.text =
                        if (position == pages.lastIndex) "Hoàn thành" else "Tiếp"
                }
            }
        )
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
    ): FragmentOnBoardingBinding {
        return FragmentOnBoardingBinding.inflate(inflater, container, false)
    }


}