package com.example.hitproduct.screen.dialog.profile_detail

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.hitproduct.R
import com.example.hitproduct.databinding.DialogProfileDetailBinding
import io.getstream.avatarview.glide.loadImage
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DialogProfileDetail : DialogFragment() {
    private var _binding: DialogProfileDetailBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog?.setCanceledOnTouchOutside(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogProfileDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lấy dữ liệu user được truyền qua arguments
        val args = requireArguments()
        val avatarUrl = args.getString("avatarUrl")
        val firstName = args.getString("firstName")
        val lastName = args.getString("lastName")
        val username = args.getString("username")
        val nickname = args.getString("nickname")
        val dateOfBirth = args.getString("dateOfBirth")
        val gender = args.getString("gender")

        if (avatarUrl != "/example.png") {
            binding.avatarView.loadImage(avatarUrl)
        } else {
            binding.avatarView.loadImage(R.drawable.avatar_default2)
        }

        // Full name hoặc username
        val fullName = listOfNotNull(
            firstName?.takeIf { it.isNotBlank() },
            lastName?.takeIf { it.isNotBlank() }
        ).takeIf { it.isNotEmpty() }
            ?.joinToString(" ")
            ?: username.orEmpty().takeIf { it.isNotBlank() }
//            ?: "Chưa có name"
        binding.tvName.text = fullName

        // Biệt danh
        binding.tvNickname.text = nickname.takeIf { !it.isNullOrBlank() }
//            ?: "Chưa có nickname"

        // Ngày sinh
        binding.tvBirthday.text = dateOfBirth?.let { toDisplayDate(it) }
            .takeIf { !it.isNullOrBlank() }
            ?: "--/--/----"

        // Giới tính
        binding.tvGender.text = gender.takeIf { !it.isNullOrBlank() }
//            ?: "Chưa có giới tính"

        // Cung hoàng đạo (tùy chọn)
        binding.tvZodiac.text = calculateZodiac(dateOfBirth)
            .takeIf { it.isNotBlank() }
//            ?: "Chưa có cung"

        // Đóng dialog
        binding.btnClose.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun toDisplayDate(iso: String): String {
        return try {
            val parser =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            val date = parser.parse(iso)!!
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        } catch (_: Exception) {
            ""
        }
    }

    private fun calculateZodiac(dob: String?): String {
        if (dob.isNullOrBlank()) return ""
        val parts = dob.split("-")
        if (parts.size < 3) return ""
        val m = parts[1].toIntOrNull() ?: return ""
        val d = parts[2].take(2).toIntOrNull() ?: return ""
        return when {
            (m == 1 && d >= 20) || (m == 2 && d <= 18) -> "Bảo Bình"
            (m == 2 && d >= 19) || (m == 3 && d <= 20) -> "Song Ngư"
            (m == 3 && d >= 21) || (m == 4 && d <= 19) -> "Bạch Dương"
            (m == 4 && d >= 20) || (m == 5 && d <= 20) -> "Kim Ngưu"
            (m == 5 && d >= 21) || (m == 6 && d <= 20) -> "Song Tử"
            (m == 6 && d >= 21) || (m == 7 && d <= 22) -> "Cự Giải"
            (m == 7 && d >= 23) || (m == 8 && d <= 22) -> "Sư Tử"
            (m == 8 && d >= 23) || (m == 9 && d <= 22) -> "Xử Nữ"
            (m == 9 && d >= 23) || (m == 10 && d <= 22) -> "Thiên Bình"
            (m == 10 && d >= 23) || (m == 11 && d <= 21) -> "Bọ Cạp"
            (m == 11 && d >= 22) || (m == 12 && d <= 21) -> "Nhân Mã"
            (m == 12 && d >= 22) || (m == 1 && d <= 19) -> "Ma Kết"
            else -> ""
        }
    }
}
