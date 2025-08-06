package com.example.hitproduct.screen.dialog.confirm_pin

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.hitproduct.databinding.DialogConfirmPinBinding


class DialogConfirmPin(
    private val onConfirm: () -> Unit
) : DialogFragment() {
    private var _binding: DialogConfirmPinBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.83).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogConfirmPinBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext()).apply {
            // Xoá vùng title mặc định
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            setCanceledOnTouchOutside(false)
            // Cho nền trong suốt (giữ bo tròn của bg_dialog_rounded)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialog.setCanceledOnTouchOutside(false)

        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnLogout.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}