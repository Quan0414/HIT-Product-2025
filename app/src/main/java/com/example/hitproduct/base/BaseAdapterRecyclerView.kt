package com.example.hitproduct.base
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapterRecyclerView<T, VB : ViewBinding>
@JvmOverloads constructor(dataList: MutableList<T>? = null) :
    RecyclerView.Adapter<BaseViewHolder<VB>>() {
    protected var binding: VB? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        binding = inflateViewBinding(LayoutInflater.from(parent.context), parent)
        return BaseViewHolder(requireNotNull(binding)).apply {
            bindViewClick(this, viewType)
        }
    }

    fun setOnClickItem(listener: ((item: T?, position: Int) -> Unit)? = null) {
        setOnClickItem = listener
    }

    private var setOnClickItem: ((item: T?, position: Int) -> Unit)? = null


    open fun bindViewClick(baseViewHolder: BaseViewHolder<VB>, viewType: Int) {
        baseViewHolder.itemView.setOnClickListener {
            val position = baseViewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }
            setOnClickItem?.invoke(dataList.getOrNull(position), position)
        }
    }

    var dataList: MutableList<T> = dataList ?: arrayListOf()
        internal set

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        bindData(holder.binding, dataList[position], position)
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<VB>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        bindData(holder.binding, dataList[position], position)
    }

    abstract fun bindData(binding: VB, item: T, position: Int)

    open fun setData(@IntRange(from = 0) index: Int, data: T) {
        if (index >= this.dataList.size) {
            return
        }
        this.dataList[index] = data
        notifyItemChanged(index)
    }

    open fun removeAt(@IntRange(from = 0) position: Int) {
        if (position >= this.dataList.size) {
            return
        }
        this.dataList.removeAt(position)
        notifyItemChanged(position)
    }

    open fun remove(data: T) {
        val index = this.dataList.indexOf(data)
        if (index == -1) {
            return
        }
        removeAt(index)
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun setDataList(data: Collection<T>) {
        dataList.clear()
        dataList.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    abstract fun inflateViewBinding(inflater: LayoutInflater, parent: ViewGroup): VB
}
