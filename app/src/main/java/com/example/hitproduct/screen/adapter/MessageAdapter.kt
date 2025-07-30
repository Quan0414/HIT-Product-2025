package com.example.hitproduct.screen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hitproduct.R
import com.example.hitproduct.data.model.message.ChatItem
import io.getstream.avatarview.AvatarView
import io.getstream.avatarview.glide.loadImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MessageAdapter(
    private val items: MutableList<ChatItem> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val expandedIds = mutableSetOf<String>()

    fun submitList(list: List<ChatItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun prependMessages(newMessages: List<ChatItem>) {
        if (newMessages.isEmpty()) return
        items.addAll(0, newMessages)
        notifyItemRangeInserted(0, newMessages.size)
    }

    companion object {
        private const val TYPE_TEXT_IN = 0
        private const val TYPE_TEXT_OUT = 1
        private const val TYPE_IMAGE_IN = 2
        private const val TYPE_IMAGE_OUT = 3
        private const val TYPE_TYPING = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ChatItem.TextMessage ->
                if (item.fromMe) TYPE_TEXT_OUT else TYPE_TEXT_IN

            is ChatItem.ImageMessage ->
                if (item.fromMe) TYPE_IMAGE_OUT else TYPE_IMAGE_IN

            is ChatItem.TypingIndicator -> TYPE_TYPING
        }
    }

    inner class TextInVH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tvMessReceive)
        val avatar: AvatarView = view.findViewById(R.id.imgAvatar)
        val tvTime: TextView = view.findViewById(R.id.tvTimeReceive)
    }

    inner class TextOutVH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tvMessSend)
        val tvTime: TextView = view.findViewById(R.id.tvTimeSend)
    }

    inner class ImageInVH(view: View) : RecyclerView.ViewHolder(view) {
        val iv: ImageView = view.findViewById(R.id.ivImgReceive)
        val avatar: AvatarView = view.findViewById(R.id.imgAvatar)
    }

    inner class ImageOutVH(view: View) : RecyclerView.ViewHolder(view) {
        val iv: ImageView = view.findViewById(R.id.ivImgSend)
    }

    inner class TypingVH(view: View) : RecyclerView.ViewHolder(view) {
        val imgTyping: ImageView = view.findViewById(R.id.imgTyping)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT_IN -> TextInVH(inf.inflate(R.layout.item_mess_receive, parent, false))
            TYPE_TEXT_OUT -> TextOutVH(inf.inflate(R.layout.item_mess_send, parent, false))
            TYPE_IMAGE_IN -> ImageInVH(inf.inflate(R.layout.item_img_receive, parent, false))
            TYPE_IMAGE_OUT -> ImageOutVH(inf.inflate(R.layout.item_img_send, parent, false))
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        // Chỉ show avatar cho incoming (fromMe == false)
        // và khi message kế tiếp là của mình hoặc không còn message nào
        val showAvatar = !item.fromMe && (
                items.getOrNull(position + 1)?.fromMe == true
                        || position == items.lastIndex
                )
        val isLast = position == items.lastIndex
        val showTime = isLast || expandedIds.contains(item.id)


        when (item) {
            is ChatItem.TextMessage -> {
                if (holder is TextInVH) {
                    holder.tv.text = item.text
                    holder.avatar.visibility = if (showAvatar) View.VISIBLE else View.INVISIBLE
                    if (showAvatar) {
                        holder.avatar.loadAvatar(item.avatarUrl)
                    }
                    // show/hide time
                    holder.tvTime.visibility = if (showTime) View.VISIBLE else View.GONE
                    holder.tvTime.text = item.sentAt.formatTime()

                    holder.itemView.setOnClickListener {
                        toggleExpanded(item.id, position)
                    }
                }
                if (holder is TextOutVH) {
                    holder.tv.text = item.text

                    holder.tvTime.visibility = if (showTime) View.VISIBLE else View.GONE
                    holder.tvTime.text = item.sentAt.formatTime()

                    holder.itemView.setOnClickListener {
                        toggleExpanded(item.id, position)
                    }
                }
            }

            is ChatItem.ImageMessage -> {
                if (holder is ImageInVH) {
                    // bind ảnh
                    Glide.with(holder.iv).load(item.imageUrl).into(holder.iv)

                    // bind avatar
                    holder.avatar.visibility = if (showAvatar) View.VISIBLE else View.INVISIBLE
                    if (showAvatar) {
                        holder.avatar.loadAvatar(item.avatarUrl)
                    }
                }
                if (holder is ImageOutVH) {
                    Glide.with(holder.iv).load(item.imageUrl).into(holder.iv)
                }
            }

            ChatItem.TypingIndicator -> TODO()
        }
    }

    private fun toggleExpanded(id: String, pos: Int) {
        if (expandedIds.contains(id)) expandedIds.remove(id)
        else expandedIds.add(id)
        notifyItemChanged(pos)
    }


    private fun AvatarView.loadAvatar(rawUrl: String?) {
        val avatar = rawUrl
            ?.takeIf { it.isNotBlank() && it != "/example.png" }
            ?.replaceFirst("http://", "https://")
        if (avatar != null) {
            this.loadImage(avatar)
        } else {
            this.loadImage(R.drawable.avatar_default)
        }
    }

    private fun String.formatTime(): String {
        return try {
            val instant = Instant.parse(this)
            val zoned = instant.atZone(ZoneId.systemDefault())
            DateTimeFormatter
                .ofPattern("HH:mm d 'TH'M", Locale("vi"))
                .format(zoned)
        } catch (e: Exception) {
            this
        }
    }

}
