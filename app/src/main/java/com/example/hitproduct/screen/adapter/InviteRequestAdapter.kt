package com.example.hitproduct.screen.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.R
import com.example.hitproduct.data.model.invite.InviteItem

class InviteAdapter(
    private val onAccept: (InviteItem) -> Unit,
    private val onReject: (InviteItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        Log.d("InviteDebug", "onCreateViewHolder viewType=$viewType")
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
        Log.d("InviteDebug", "Bind pos=$position, item=${inviteRequest[position]}")

    }

    override fun getItemCount(): Int {
        Log.d("InviteDebug", "itemCount = ${inviteRequest.size}")
        return inviteRequest.size
    }

    override fun getItemViewType(position: Int): Int {
        val type = when (inviteRequest[position]) {
            is InviteItem.Sent -> TYPE_SENT
            is InviteItem.Received -> TYPE_RECEIVED
        }
        Log.d("InviteDebug", "getItemViewType pos=$position → type=$type")
        return type
    }

    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvNotification: TextView = itemView.findViewById(R.id.tvNotification)

        fun bind(item: InviteItem.Sent) {
            tvNotification.text = itemView.context.getString(
                R.string.notification_invite_sent,
                item.toUser
            )
            itemView.findViewById<ImageView>(R.id.imgDelete)
                .setOnClickListener { onAccept(item) }
            itemView.findViewById<ImageView>(R.id.imgSuccess)
                .setOnClickListener { onReject(item) }
        }
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvNotification: TextView = itemView.findViewById(R.id.tvNotification)

        fun bind(item: InviteItem.Received) {
            tvNotification.text = itemView.context.getString(
                R.string.notification_invite_received,
                item.fromUser
            )
            itemView.findViewById<ImageView>(R.id.imgDelete)
                .setOnClickListener { onAccept(item) }
            itemView.findViewById<ImageView>(R.id.imgSuccess)
                .setOnClickListener { onReject(item) }
        }
    }
}