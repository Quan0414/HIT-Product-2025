package com.example.hitproduct.screen.dialog.note

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.R
import com.example.hitproduct.data.model.calendar.Note

class NoteAdapter(
    private val items: MutableList<Note> = mutableListOf(),
    private val onDelete: (Note) -> Unit,
    private val onEdit: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    fun submitList(newNotes: List<Note>) {
        items.clear()
        items.addAll(newNotes)
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val content: TextView = view.findViewById(R.id.tvNote)
        private val btnDelete: View = view.findViewById(R.id.btnDelete)

        fun bind(note: Note) {
            content.text = "${position + 1}. ${note.content}"
            btnDelete.setOnClickListener {
                onDelete(note)
            }
            itemView.setOnClickListener {
                onEdit(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun remove(note: Note) {
        val idx = items.indexOfFirst { it._id == note._id }
        if (idx != -1) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
            // cập nhật lại thứ tự
            notifyItemRangeChanged(idx, items.size - idx)
        }
    }

    fun addAtTop(note: Note) {
        items.add(0, note)
        notifyItemInserted(0)
        notifyItemRangeChanged(0, items.size)
    }
}