package com.example.hitproduct.screen.dialog.note

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hitproduct.data.model.note.Note
import com.example.hitproduct.databinding.DialogNoteBinding


class DialogNote : DialogFragment() {
    private var _binding: DialogNoteBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_NOTES = "arg_notes"

        fun newInstance(notes: List<Note>): DialogNote {
            val args = Bundle().apply {
                putSerializable(ARG_NOTES, ArrayList(notes))
            }
            return DialogNote().apply { arguments = args }
        }
    }

    private val adapter by lazy { NoteAdapter() }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // full-width, wrap-content height, đẩy xuống đáy
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = DialogNoteBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvNote.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DialogNote.adapter
        }

        // Lấy notes an toàn từ args
        val notes = (arguments?.getSerializable(ARG_NOTES) as? ArrayList<Note>)
            .orEmpty()
        adapter.submitList(notes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}