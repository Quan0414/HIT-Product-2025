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

class MessageAdapter(
    private val items: MutableList<ChatItem> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun submitList(list: List<ChatItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    companion object {
        private const val TYPE_TEXT_IN = 0
        private const val TYPE_TEXT_OUT = 1
        private const val TYPE_IMAGE_IN = 2
        private const val TYPE_IMAGE_OUT = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ChatItem.TextMessage ->
                if (item.fromMe) TYPE_TEXT_OUT else TYPE_TEXT_IN

            is ChatItem.ImageMessage ->
                if (item.fromMe) TYPE_IMAGE_OUT else TYPE_IMAGE_IN
        }
    }

    inner class TextInVH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tvMessReceive)
    }

    inner class TextOutVH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tvMessSend)
    }

    inner class ImageInVH(view: View) : RecyclerView.ViewHolder(view) {
        val iv: ImageView = view.findViewById(R.id.ivImgReceive)
    }

    inner class ImageOutVH(view: View) : RecyclerView.ViewHolder(view) {
        val iv: ImageView = view.findViewById(R.id.ivImgSend)
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
        when (val item = items[position]) {
            is ChatItem.TextMessage -> {
                if (holder is TextInVH && !item.fromMe) {
                    holder.tv.text = item.text
                } else if (holder is TextOutVH && item.fromMe) {
                    holder.tv.text = item.text
                }
            }

            is ChatItem.ImageMessage -> {
                if (holder is ImageInVH && !item.fromMe) {
                    Glide.with(holder.iv.context)
                        .load(item.imageUrl)
//                        .placeholder(R.drawable.ic_placeholder)
                        .into(holder.iv)
                } else if (holder is ImageOutVH && item.fromMe) {
                    Glide.with(holder.iv.context)
                        .load(item.imageUrl)
//                        .placeholder(R.drawable.ic_placeholder)
                        .into(holder.iv)
                }
            }
        }
    }
}
