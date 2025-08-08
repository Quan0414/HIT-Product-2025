package com.example.hitproduct.common.util

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.hitproduct.MyApp
import com.example.hitproduct.R
import com.example.hitproduct.databinding.DialogNetworkDisconnectBinding
import kotlinx.coroutines.launch

class DialogNetworkDisconnect : DialogFragment() {

    private var _binding: DialogNetworkDisconnectBinding? = null
    private val binding get() = _binding!!

    private val net: NetworkMonitor by lazy { (requireActivity().application as MyApp).networkMonitor }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNetworkDisconnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // 83% chiều rộng màn hình
            val width = (resources.displayMetrics.widthPixels * 0.88f).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        // Nếu mở dialog mà đã có mạng → đóng luôn
        if (net.isOnline.value) dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this).asGif().load(R.drawable.cat_disconnect).into(binding.gifCat)

//        binding.btnClose.setOnClickListener { dismiss() }

        // Lắng nghe mạng realtime: có mạng là đóng
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                net.isOnline.collect { online ->
                    if (online) dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
