package com.example.hitproduct.screen.dialog.note.edit

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.model.calendar.Note
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.DialogEditNoteBinding
import com.example.hitproduct.common.util.Constant.ARG_NOTES
import com.example.hitproduct.common.util.FcmClient
import com.example.hitproduct.common.util.NotificationConfig


class DialogEditNote : DialogFragment() {
    private var _binding: DialogEditNoteBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }

    private val viewModel by viewModels<DialogEditNoteViewModel> {
        DialogEditNoteViewModelFactory(authRepo)
    }

    companion object {
        fun newInstance(note: Note) = DialogEditNote().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_NOTES, note)
            }
        }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val note = arguments?.getSerializable(ARG_NOTES) as? Note
        val noteId = note?._id ?: ""
        binding.etContent.setText(note?.content ?: "")

        binding.btnSave.setOnClickListener {
            val content = binding.etContent.text.toString().trim()
            viewModel.editNote(noteId, content)
        }

        viewModel.editNote.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        state.error.message ?: "Error occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetEditNoteState()
                }

                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật ghi chú thành công.",
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().supportFragmentManager.setFragmentResult("refresh_notes", Bundle())
                    requireActivity().supportFragmentManager.setFragmentResult("dismiss_dialog_note", Bundle())
                    dismiss()

                    val myLoveId = authRepo.getMyLoveId()
                    val payload = mapOf(
                        "type" to "note_updated",
                    )
                    val tpl = NotificationConfig.getTemplate("note_updated", payload)
                    FcmClient.sendToTopic(
                        receiverUserId = myLoveId,
                        title = tpl.title,
                        body = tpl.body,
                        data = payload
                    )
                }
            }
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