package com.example.hitproduct.screen.home_page.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
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
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import com.example.hitproduct.MainActivity
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.toThousandComma
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentHomeBinding
import com.example.hitproduct.screen.dialog.daily_question.your.YourDailyQuestionDialogFragment
import com.example.hitproduct.screen.dialog.mission.DialogMission
import com.example.hitproduct.screen.dialog.shop.ShopDialogFragment
import com.example.hitproduct.screen.dialog.start_date.DialogStartDate
import com.example.hitproduct.socket.SocketManager
import com.example.hitproduct.common.util.Constant
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit


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
        R.raw.meo_doi,
        R.raw.meo_khoc,
        R.raw.meo_the_luoi
    )
    private val normalCat = listOf(
        R.raw.meo_cay,
        R.raw.meo_gio_tay,
        R.raw.meo_ngu,
    )
    private val happyCat = listOf(
        R.raw.meo_nhay1,
        R.raw.meo_nhay2,
        R.raw.meo_nhay3,
        R.raw.meo_nhay4,
    )
    private var eattingCat = R.raw.meo_an

    private var currentCatList: List<Int> = normalCat
    private var currentCat: Int? = null

    private var hasShownStartDateDialog = false
    private val shopDialog by lazy { ShopDialogFragment() }
    private val questionDialog by lazy { YourDailyQuestionDialogFragment() }

    override fun initView() {

        viewModel.listenToSocket()

        childFragmentManager.setFragmentResultListener(
            "update_start_date",
            viewLifecycleOwner
        ) { _, _ ->
            hasShownStartDateDialog = true
            viewModel.getCoupleProfile()
            Log.d("HomeFragment", "Start date updated, fetching couple profile again")
        }

        binding.gifCat.visibility = View.GONE
        binding.gifCat.apply {
            speed = 1.0f
            repeatCount = 0
            renderMode = RenderMode.HARDWARE

            addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // dùng view.postDelayed để đảm bảo chạy trên UI thread
                    postDelayed({ playAnimation() }, 0)
                }
            })

            setOnClickListener {
                val next = currentCatList
                    .filter { it != currentCat }
                    .randomOrNull() ?: currentCatList.random()
                currentCat = next
                val keyState = catStateGifMap.filterValues { it == currentCat }
                    .keys
                    .firstOrNull()
                Log.d("HomeFragment", "Cat state key: $keyState")
                if (keyState != null) {
                    SocketManager.sendCatStateToSocket(keyState, checkMyLoveId() ?: "")
                    Log.d("HomeFragment", "Cat state sent to server")
                }
                setAnimation(next)
                playAnimation()
            }
        }


    }

    override fun initListener() {

        binding.imageView.setOnClickListener {
            val dialog = DialogStartDate()
            dialog.show(
                childFragmentManager,
                DialogStartDate::class.java.simpleName
            )
        }

        binding.icon1.setOnClickListener {
            val value = "${binding.state1.progress / 10}/${binding.state1.max / 10}"
            showTooltip(
                it, value,
                R.layout.tooltip,
                R.id.popup_state1
            )
        }
        binding.icon2.setOnClickListener {
            val value = "${binding.state2.progress}/${binding.state2.max}"
            showTooltip(
                it, value,
                R.layout.tooltip2,
                R.id.popup_state2
            )
        }

        binding.btnFeed.setOnClickListener {
            if (!shopDialog.isAdded) {
                shopDialog.show(childFragmentManager, "shop")
            } else {
                shopDialog.dialog?.show()
            }
        }

        binding.btnQuestion.setOnClickListener {
            if (!questionDialog.isAdded) {
                questionDialog.show(childFragmentManager, "question")
            } else {
                questionDialog.dialog?.show()
            }
        }

        binding.btnMission.setOnClickListener {
            DialogMission().show(childFragmentManager, "mission")
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
                    val userAID = state.data.userA.id
                    prefs.edit()
                        .putString(AuthPrefersConstants.USER_A_ID, userAID)
                        .apply()
                    val userBID = state.data.userB.id
                    prefs.edit()
                        .putString(AuthPrefersConstants.USER_B_ID, userBID)
                        .apply()

                    if (!state.data.loveStartedAtEdited && !hasShownStartDateDialog) {
                        hasShownStartDateDialog = true
                        val dialog = DialogStartDate()
                        dialog.show(
                            childFragmentManager,
                            DialogStartDate::class.java.simpleName
                        )
                    }

                    binding.tvMoney.text = state.data.coin.toThousandComma()
                    (activity as MainActivity).coin = state.data.coin

                    // 1. Parse startDate (ISO string) thành OffsetDateTime
                    val startDateStr =
                        state.data.loveStartedAt            // ví dụ "2025-07-11T10:24:04.501Z"
                    val startInstant = Instant.parse(startDateStr)
                    val startDate = startInstant
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    val nowDate = LocalDate.now(ZoneId.systemDefault())

                    val totalDays = ChronoUnit.DAYS.between(startDate, nowDate).toInt()

                    val period = Period.between(startDate, nowDate)
                    val years = period.years
                    val months = period.months
                    val daysTotal = period.days

                    // 4. Chuyển days thành tuần + ngày
                    val weeks = daysTotal / 7
                    val days = daysTotal % 7

                    requireActivity().supportFragmentManager.setFragmentResult(
                        "total_date_updated",
                        bundleOf("totalLoveDate" to totalDays)
                    )

                    requireActivity().supportFragmentManager.setFragmentResult(
                        "date_components",
                        bundleOf(
                            "year" to years,
                            "month" to months,
                            "week" to weeks,
                            "day" to days
                        )
                    )

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

                    updateStateBar(binding.state1, binding.icon1, hunger * 10)
                    updateStateBar(binding.state2, binding.icon2, happiness)

                    currentCatList = when {
                        hunger < Constant.HUNGER_LOW -> hungryCat
                        hunger < Constant.HUNGER_MEDIUM -> normalCat
                        else -> happyCat
                    }

                    val first = currentCatList.random()
                    val keyState = catStateGifMap.filterValues { it == first }
                        .keys
                        .firstOrNull()
                    Log.d("HomeFragment", "Cat state key: $keyState")
                    if (keyState != null) {
                        SocketManager.sendCatStateToSocket(keyState, checkMyLoveId() ?: "")
                        Log.d("HomeFragment", "Cat state sent to server")
                    }
                    currentCat = first
                    binding.gifCat.apply {
                        visibility = View.VISIBLE
                        setAnimation(first)
                        playAnimation()
                    }
                }
            }
        }

        // ===== Quan sát LiveData của socket =====
        SocketManager.notifications.observe(viewLifecycleOwner) { notifications ->
            Log.d("HomeFragment", "Received notifications: $notifications")
        }


        viewModel.activeKey.observe(viewLifecycleOwner) { key ->
            val gifRes = catStateGifMap[key] ?: R.raw.meo_cay
            binding.gifCat.setAnimation(gifRes)
            binding.gifCat.playAnimation()
        }

        viewModel.hunger.observe(viewLifecycleOwner) { hunger ->
            animateStateChange(binding.state1, binding.icon1, hunger * 10)
            currentCatList = when {
                hunger < Constant.HUNGER_LOW -> hungryCat
                hunger < Constant.HUNGER_MEDIUM -> normalCat
                else -> happyCat
            }
        }

        viewModel.happiness.observe(viewLifecycleOwner) { hp ->
            animateStateChange(binding.state2, binding.icon2, hp)
        }

        viewModel.coin.observe(viewLifecycleOwner) { coin ->
            binding.tvMoney.text = coin.toThousandComma()
            (activity as MainActivity).coin = coin
        }

        viewModel.eatEvent.observe(viewLifecycleOwner) {
            binding.gifCat.apply {
                isClickable = false
                removeAllAnimatorListeners()

                setAnimation(eattingCat)
                repeatCount = 1
                playAnimation()

                addAnimatorListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        removeAnimatorListener(this)
                        isClickable = true

                        val next = currentCatList.random()
                        currentCat = next
                        val keyState = catStateGifMap.filterValues { it == currentCat }
                            .keys
                            .firstOrNull()
                        Log.d("HomeFragment", "Cat state key: $keyState")
                        if (keyState != null) {
                            SocketManager.sendCatStateToSocket(keyState, checkMyLoveId() ?: "")
                            Log.d("HomeFragment", "Cat state sent to server")
                        }

                        setAnimation(next)
                        repeatCount = LottieDrawable.INFINITE
                        playAnimation()
                    }
                })
            }
        }
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    private fun moveIcon(bar: ProgressBar, icon: View) {
        // nếu width chưa đo xong thì defer lại
        if (bar.width == 0 || icon.width == 0) {
            bar.post { moveIcon(bar, icon) }
            return
        }

        // phần track fill (bỏ padding)
        val trackWidth = bar.width - bar.paddingLeft - bar.paddingRight
        // tỉ lệ (0f..1f)
        val ratio = bar.progress.toFloat() / bar.max
        // vị trí center tính từ bar.paddingLeft
        val centerX = bar.paddingLeft + ratio * trackWidth
        // muốn icon center trùng centerX
        val halfIcon = icon.width / 2f
        val desired = centerX - halfIcon

        // clamp trong [0 .. trackWidth - icon.width]
        val maxTrans = (trackWidth - icon.width).toFloat().coerceAtLeast(0f)
        val tx = desired.coerceIn(0f, maxTrans)

        // dịch chuyển tương đối
        icon.translationX = tx
    }


    private fun tintOnlyFill(bar: ProgressBar, @ColorRes fillColorRes: Int) {
        val color = ContextCompat.getColor(requireContext(), fillColorRes)
        val layer = bar.progressDrawable.mutate() as LayerDrawable

        val prog = layer.findDrawableByLayerId(R.id.progress)
        if (prog is ClipDrawable) {
            val shape = prog.drawable
            if (shape is GradientDrawable) {
                // Chỉ đổi màu solid, giữ nguyên stroke
                shape.setColor(color)
            }
        }

        bar.progressDrawable = layer
    }


    private fun updateStateBar(bar: ProgressBar, icon: View, value: Int) {
        bar.progress = value
        Log.d("HomeFragment", "Updating state bar ${bar.id} with value: $value")
        // chỉ đổi màu fill của state1
        if (bar.id == R.id.state1) {
            val colorRes = when {
                value > Constant.HUNGER_MEDIUM * 10 -> R.color.status4
                value <= Constant.HUNGER_MEDIUM * 10 && value > Constant.HUNGER_LOW * 10 -> R.color.status3
                else -> R.color.status1
            }
            tintOnlyFill(bar, colorRes)
        }

        moveIcon(bar, icon)
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

    private fun registerSocketListeners() {

        SocketManager.onListenForPetActive { data ->
            Log.d("HomeFragment", "onListenForPetActive fired with $data")
            val key = data.optString("active")
            val gifRes = catStateGifMap[key] ?: R.raw.meo_cay
            binding.gifCat.setAnimation(gifRes)
            binding.gifCat.playAnimation()
        }


        SocketManager.onFeedPetSuccess { data ->
            Log.d("HomeFragment", "onFeedPetSuccess fired with $data")
            val newHunger = data.optInt("hunger")
            val newHappiness = data.optInt("happiness")
            val newCoin = data.optInt("coin")

            animateStateChange(binding.state1, binding.icon1, newHunger * 10)
            animateStateChange(binding.state2, binding.icon2, newHappiness)
            binding.tvMoney.text = newCoin.toThousandComma()
            (activity as MainActivity).coin = newCoin

            currentCatList = when {
                newHunger < Constant.HUNGER_LOW -> hungryCat
                newHunger < Constant.HUNGER_MEDIUM -> normalCat
                else -> happyCat
            }
            binding.gifCat.apply {
                isClickable = false
                removeAllAnimatorListeners()
                setAnimation(eattingCat)
                repeatCount = 1
                playAnimation()

                // khi kết thúc ăn, tự chuyển sang random cat
                addAnimatorListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        // gỡ listener này để không lặp lại
                        removeAnimatorListener(this)
                        isClickable = true

                        // chọn 1 GIF random từ currentCatList
                        val next = currentCatList.random()
                        currentCat = next

                        // play bình thường
                        setAnimation(next)
                        repeatCount = LottieDrawable.INFINITE
                        playAnimation()
                    }
                })
            }
        }

        SocketManager.onDecreaseHunger { data ->
            Log.d("HomeFragment", "onDecreaseHunger fired with $data")
            val newHunger = data.optInt("hunger")
            animateStateChange(binding.state1, binding.icon1, newHunger * 10)
            currentCatList = when {
                newHunger < Constant.HUNGER_LOW -> hungryCat
                newHunger < Constant.HUNGER_MEDIUM -> normalCat
                else -> happyCat
            }
        }
    }

    private fun animateStateChange(
        bar: ProgressBar,
        icon: View,
        newValue: Int,
        duration: Long = 800L    // thời gian animation (ms)
    ) {
        val oldValue = bar.progress
        ValueAnimator.ofInt(oldValue, newValue).apply {
            this.duration = duration
            addUpdateListener { anim ->
                val v = anim.animatedValue as Int
                bar.progress = v
                // di chuyển icon theo progress
                moveIcon(bar, icon)
            }
            start()
        }
    }

    private val catStateGifMap = mapOf(
        "hungry_key" to R.raw.meo_doi,
        "normal_key" to R.raw.meo_ngu,
        "happy_key" to R.raw.meo_nhay1,
        "angry_key" to R.raw.meo_cay,
        "sleepy_key" to R.raw.meo_ngu,
        "excited_key" to R.raw.meo_nhay2,
        "playful_key" to R.raw.meo_the_luoi,
        "sad_key" to R.raw.meo_khoc,
        "surprised_key" to R.raw.meo_nhay3,
        "annoyed_key" to R.raw.meo_gio_tay,
        "curious_key" to R.raw.meo_nhay4,
        "eating_key" to R.raw.meo_an
    )

    private fun checkMyLoveId(): String? {
        //so sanh my user id va user id
        val myId = prefs.getString(AuthPrefersConstants.MY_USER_ID, null)
        if (myId == prefs.getString(AuthPrefersConstants.USER_A_ID, null)) {
            return prefs.getString(AuthPrefersConstants.USER_B_ID, null)
        }
        return prefs.getString(AuthPrefersConstants.USER_A_ID, null)
    }
}