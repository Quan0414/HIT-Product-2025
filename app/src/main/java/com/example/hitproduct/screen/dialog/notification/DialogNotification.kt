package com.example.hitproduct.screen.dialog.notification

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hitproduct.data.model.notification.Notification
import com.example.hitproduct.databinding.DialogNotificationBinding
import com.example.hitproduct.socket.SocketManager


class DialogNotification : DialogFragment() {
    private var _binding: DialogNotificationBinding? = null
    private val binding get() = _binding!!

    private val notifications = mutableListOf<Notification>()
    private val adapter by lazy { NotificationAdapter() }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvNotification.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DialogNotification.adapter
        }

        SocketManager.notifications.observe(viewLifecycleOwner) { data ->
            val noti = Notification(
                coupleId = data.optString("coupleId"),
                fromUserId = data.optString("fromUserId"),
                toUserId = data.optString("toUserId"),
                type = data.optString("type"),
                content = data.optString("content"),
                isRead = data.optBoolean("isRead"),
                id = data.optString("_id"),
                createdAt = data.optString("createdAt"),
                updatedAt = data.optString("updatedAt"),
                version = data.optInt("__v")
            )

            notifications.add(0, noti)
            adapter.submitList(notifications.toList())
            binding.rvNotification.scrollToPosition(0)
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}