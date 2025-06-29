package com.example.hitproduct.screen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.R
import com.example.hitproduct.data.model.invite.InviteItem

class InviteAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT = 0
        private const val TYPE_RECEIVED = 1
    }

    private val inviteRequest = mutableListOf<InviteItem>()

    fun submitList(list: List<InviteItem>) {
        inviteRequest.clear()
        inviteRequest.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SENT -> {
                val view = inflater.inflate(R.layout.item_invite_sent, parent, false)
                SentViewHolder(view)
            }

            TYPE_RECEIVED -> {
                val view = inflater.inflate(R.layout.item_invite_received, parent, false)
                ReceivedViewHolder(view)
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SentViewHolder -> holder.bind(inviteRequest[position] as InviteItem.Sent)
            is ReceivedViewHolder -> holder.bind(inviteRequest[position] as InviteItem.Received)
        }
    }

    override fun getItemCount(): Int = inviteRequest.size

    override fun getItemViewType(position: Int): Int {
        return when (inviteRequest[position]) {
            is InviteItem.Sent -> TYPE_SENT
            is InviteItem.Received -> TYPE_RECEIVED
        }
    }

    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvNotification: TextView = itemView.findViewById(R.id.tvNotification)

        fun bind(item: InviteItem.Sent) {
            tvNotification.text = itemView.context.getString(
                R.string.notification_invite_sent,
                item.toUser
            )
        }
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvNotification: TextView = itemView.findViewById(R.id.tvNotification)

        fun bind(item: InviteItem.Received) {
            tvNotification.text = itemView.context.getString(
                R.string.notification_invite_received,
                item.fromUser
            )
        }
    }
}