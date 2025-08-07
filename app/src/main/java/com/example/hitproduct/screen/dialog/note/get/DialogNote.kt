package com.example.hitproduct.screen.dialog.note.get

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hitproduct.common.util.Constant.ARG_NOTES
import com.example.hitproduct.data.model.calendar.Note
import com.example.hitproduct.databinding.DialogNoteBinding
import com.example.hitproduct.screen.adapter.NoteAdapter
import com.example.hitproduct.screen.dialog.note.create.DialogCreateNote
import com.example.hitproduct.screen.dialog.note.delete.DialogDeleteNote
import com.example.hitproduct.screen.dialog.note.edit.DialogEditNote
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DialogNote : DialogFragment() {
    private var _binding: DialogNoteBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(notes: List<Note>): DialogNote {
            val args = Bundle().apply {
                putSerializable(ARG_NOTES, ArrayList(notes))
            }
            return DialogNote().apply { arguments = args }
        }
    }

    private lateinit var notes: List<Note>
    private lateinit var selectedDate: LocalDate

    private val adapter by lazy {
        NoteAdapter(
            items = mutableListOf(),
            onDelete = { note ->
                showConfirmDelete(note)
            },
            onEdit = { note ->
                showEditNote(note)
            }
        )
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lấy list note từ arguments
        notes = (requireArguments()
            .getSerializable(ARG_NOTES) as? ArrayList<Note>)
            .orEmpty()
        // Derive ngày từ Note đầu tiên (giả sử cùng ngày)
        selectedDate = Instant.parse(notes.first().date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvNote.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DialogNote.adapter
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            "dismiss_dialog_note", viewLifecycleOwner
        ) { _, _ ->
            dismiss()
        }

        // Lấy notes an toàn từ args
        val notes = (arguments?.getSerializable(ARG_NOTES) as? ArrayList<Note>)
            .orEmpty()
        adapter.submitList(notes)

        binding.btnAddNote.setOnClickListener {
            DialogCreateNote
                .newInstance(selectedDate.toString())
                .show(
                    requireActivity().supportFragmentManager,
                    DialogCreateNote::class.java.simpleName
                )
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showConfirmDelete(note: Note) {
        DialogDeleteNote
            .newInstance(note._id)
            .show(requireActivity().supportFragmentManager, ":dialog_delete_note")
    }

    private fun showEditNote(note: Note) {
        DialogEditNote
            .newInstance(note)
            .show(requireActivity().supportFragmentManager, "dialog_create_note")
    }

}