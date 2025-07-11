package com.example.hitproduct.screen.dialog.disconnect

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.hitproduct.databinding.DialogDisconnectBinding


class DialogDisconnectFragment(
    private val onConfirm: () -> Unit
) : DialogFragment() {

    private var _binding: DialogDisconnectBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.83).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogDisconnectBinding.inflate(
            LayoutInflater.from(requireContext())
        )
        val dialog = Dialog(requireContext()).apply {
            // Xoá vùng title mặc định
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            setCanceledOnTouchOutside(false)
            // Cho nền trong suốt (giữ bo tròn của bg_dialog_rounded)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialog.setCanceledOnTouchOutside(false)

        // Nút huỷ
        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        // Nút đồng ý
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