package com.example.hitproduct.screen.home_page.couple

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.activityViewModels
import com.example.hitproduct.screen.dialog.inlove_status.DialogInloveStatus
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentCoupleBinding
import com.example.hitproduct.screen.dialog.profile_detail.DialogProfileDetail
import com.example.hitproduct.screen.home_page.home.HomeViewModel
import com.example.hitproduct.screen.home_page.home.HomeViewModelFactory
import io.getstream.avatarview.glide.loadImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs


class CoupleFragment : BaseFragment<FragmentCoupleBinding>() {

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

    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var animInRight: Animation
    private lateinit var animOutLeft: Animation
    private lateinit var animInLeft: Animation
    private lateinit var animOutRight: Animation

    override fun initView() {
        animInRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        animOutLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left)
        animInLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_left)
        animOutRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_right)

        requireActivity().supportFragmentManager.setFragmentResultListener(
            "total_date_updated",
            viewLifecycleOwner
        ) { _, bundle ->
            val totalDate = bundle.getInt("totalLoveDate", 0)
            binding.l1.tvDay.text = totalDate.toString()
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            "date_components",
            viewLifecycleOwner
        ) { _, bundle ->
            val y = bundle.getInt("year", 0)
            val m = bundle.getInt("month", 0)
            val w = bundle.getInt("week", 0)
            val d = bundle.getInt("day", 0)

            // bind vào include layout_one (id là l1)
            binding.l2.tvYearNumber.text = y.toString()
            binding.l2.tvMonthNumber.text = m.toString()
            binding.l2.tvWeekNumber.text = w.toString()
            binding.l2.tvDayNumber.text = d.toString()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initListener() {
        binding.imgAvatar1.setOnClickListener {
            val userA = (viewModel.coupleProfile.value as? UiState.Success)?.data?.userA
                ?: return@setOnClickListener
            DialogProfileDetail().apply {
                arguments = Bundle().apply {
                    putString("avatarUrl", userA.avatar
                        ?.takeIf { it.isNotBlank() }
                        ?.replaceFirst("http://", "https://")
                        ?: "")
                    putString("firstName", userA.firstName)
                    putString("lastName", userA.lastName)
                    putString("username", userA.username)
                    putString("nickname", userA.nickname)
                    putString("dateOfBirth", userA.dateOfBirth)
                    putString("gender", userA.gender)
                }
            }.show(childFragmentManager, "DialogProfileDetail")
        }

        binding.imgAvatar2.setOnClickListener {
            val userB = (viewModel.coupleProfile.value as? UiState.Success)?.data?.userB
                ?: return@setOnClickListener
            DialogProfileDetail().apply {
                arguments = Bundle().apply {
                    putString("avatarUrl", userB.avatar
                        ?.takeIf { it.isNotBlank() }
                        ?.replaceFirst("http://", "https://")
                        ?: "")
                    putString("firstName", userB.firstName)
                    putString("lastName", userB.lastName)
                    putString("username", userB.username)
                    putString("nickname", userB.nickname)
                    putString("dateOfBirth", userB.dateOfBirth)
                    putString("gender", userB.gender)
                }
            }.show(childFragmentManager, "DialogProfileDetail")
        }

        // 1. Khởi tạo GestureDetector với đúng signature
        gestureDetector = GestureDetectorCompat(requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    val startX = e1?.x ?: 0f
                    val endX = e2.x ?: 0f
                    val diffX = endX - startX

                    // Lấy index hiện tại và tổng số child
                    val current = binding.viewSwitcher.displayedChild
                    val last = binding.viewSwitcher.childCount - 1

                    if (abs(diffX) > abs((e2.y ?: 0f) - (e1?.y ?: 0f))
                        && abs(diffX) > SWIPE_THRESHOLD
                        && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffX < 0) {
                            // swipe phải→trái: muốn showNext
                            if (current < last) {
                                binding.viewSwitcher.inAnimation = animInRight
                                binding.viewSwitcher.outAnimation = animOutLeft
                                binding.viewSwitcher.showNext()
                                updateButtonIcon()
                            }
                        } else {
                            // swipe trái→phải: muốn showPrevious
                            if (current > 0) {
                                binding.viewSwitcher.inAnimation = animInLeft
                                binding.viewSwitcher.outAnimation = animOutRight
                                binding.viewSwitcher.showPrevious()
                                updateButtonIcon()
                            }
                        }
                        return true
                    }
                    return false
                }

            }
        )

        // 2. Bắt touch trên viewSwitcher
        binding.viewSwitcher.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }


    override fun initData() {
        viewModel.getCoupleProfile()

        val statuses = listOf(
            "Tình yêu giống như một bản nhạc vĩ đại: khi hai trái tim cùng hòa nhịp, giai điệu của hạnh phúc sẽ vang vọng mãi trong tâm hồn.",
            "Yêu không phải là tìm thấy người hoàn hảo, mà là học cách nhìn thấy sự hoàn hảo trong những khuyết điểm mà cả hai cùng chia sẻ.",
            "Khi em nắm tay anh, dẫu cả thế giới có đổi thay, anh vẫn cảm nhận được một điểm tựa vững chắc để bước qua mọi bão giông.",
            "Tình yêu là hành trình khám phá bản thân và người kia: mỗi ngày bên nhau, ta học cách yêu thương sâu hơn, kiên nhẫn hơn và bao dung hơn.",
            "Chỉ cần hai trái tim đồng điệu, những khoảnh khắc giản đơn như ngắm hoàng hôn hay cùng pha một tách trà cũng trở thành kỷ niệm bất tận.",
            "Yêu thương không bắt đầu từ những lời hoa mỹ, mà đến từ những cử chỉ nhỏ nhặt hàng ngày, khi ta luôn nhớ nâng niu và trân trọng nhau.",
            "Tình yêu thật sự là khi ta nhìn vào mắt đối phương và thấy mình trong đó, như để biết rằng mình không còn cô đơn giữa cuộc đời rộng lớn.",
            "Giữa bộn bề cuộc sống, tình yêu là nơi ta tìm về bình yên, nơi những tiếng cười và nụ hôn xua tan bao lo lắng bộn bề.",
            "Khi những ngôn từ trở nên bất lực, chỉ cần sự hiện diện bên cạnh, một cái siết tay ấm áp, hai tâm hồn đã khẽ thì thầm lời yêu ngọt ngào.",
            "Tình yêu chân thành không đo bằng thời gian bên nhau, mà đo bằng những khoảnh khắc ta cùng cố gắng, tha thứ và trưởng thành vì nhau."
        )


        val todayKey = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) // ví dụ "20250808"
        val prefs = requireContext().getSharedPreferences(
            AuthPrefersConstants.DAILY_STATUS,
            Context.MODE_PRIVATE
        )
        var index = prefs.getInt("index", -1)
        val savedDate = prefs.getString("date", "")

        if (index !in statuses.indices || savedDate != todayKey) {
            index = statuses.indices.random()
            prefs.edit()
                .putInt("index", index)
                .putString("date", todayKey)
                .apply()
        }

        val statusText = statuses[index]
        binding.l2.tvStatus.apply {
            text = statusText
//            tag = statusText // Lưu text gốc vào tag
            tag = "Thời gian trôi nhanh như chó chạy ngoài đồng."
            onClickExpand {
                val originalText = tag as String
                DialogInloveStatus().apply {
                    arguments = bundleOf("statusData" to originalText)
                }.show(childFragmentManager, "DialogInloveStatus")
            }
        }


    }

    override fun handleEvent() {

    }

    override fun bindData() {
        viewModel.coupleProfile.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                }

                UiState.Idle -> {
                }

                UiState.Loading -> {
                }

                is UiState.Success -> {
                    // User A
                    val userA = state.data.userA
                    val displayNameA = userA.nickname
                        ?.takeIf { it.isNotBlank() }
                        ?: userA.firstName
                            ?.takeIf { it.isNotBlank() }
                        ?: userA.username
                    binding.tvNickname1.text = displayNameA

                    val avatarDataA: Any = userA.avatar
                        ?.takeIf { it.isNotBlank() }
                        ?.replaceFirst("http://", "https://")
                        ?: R.drawable.avatar_default
                    if (avatarDataA != "/example.png") {
                        binding.imgAvatar1.loadImage(avatarDataA)
                    } else {
                        binding.imgAvatar1.loadImage(R.drawable.avatar_default2)
                    }

                    // User B
                    val userB = state.data.userB
                    val displayNameB = userB.nickname
                        ?.takeIf { it.isNotBlank() }
                        ?: userB.firstName
                            ?.takeIf { it.isNotBlank() }
                        ?: userB.username
                    binding.tvNickname2.text = displayNameB

                    val avatarDataB: Any = userB.avatar
                        ?.takeIf { it.isNotBlank() }
                        ?.replaceFirst("http://", "https://")
                        ?: R.drawable.avatar_default2
                    if (avatarDataB != "/example.png") {
                        binding.imgAvatar2.loadImage(avatarDataB)
                    } else {
                        binding.imgAvatar2.loadImage(R.drawable.avatar_default2)
                    }
                }

            }
        }
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCoupleBinding {
        return FragmentCoupleBinding.inflate(inflater, container, false)
    }

    private fun updateButtonIcon() {
        val idx = binding.viewSwitcher.displayedChild
        val iconRes = if (idx == 0) R.drawable.btn_switch else R.drawable.btn_switch2
        binding.btnSwitch.setImageResource(iconRes)
    }
}