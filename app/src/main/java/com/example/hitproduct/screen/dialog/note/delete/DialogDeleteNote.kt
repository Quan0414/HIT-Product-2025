package com.example.hitproduct.screen.dialog.note.delete

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
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.DialogDeleteNoteBinding
import com.example.hitproduct.screen.dialog.note.get.DialogNote
import com.example.hitproduct.util.Constant.ARG_NOTE_ID


class DialogDeleteNote : DialogFragment() {
    private var _binding: DialogDeleteNoteBinding? = null
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

    private val viewModel by viewModels<DialogDeleteNoteViewModel> {
        DialogDeleteNoteViewModelFactory(authRepo)
    }

    companion object {
        fun newInstance(noteId: String) = DialogDeleteNote().apply {
            arguments = Bundle().apply {
                putString(ARG_NOTE_ID, noteId)
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
        _binding = DialogDeleteNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAccept.setOnClickListener {
            val noteId = requireArguments().getString(ARG_NOTE_ID)!!
            viewModel.deleteNote(noteId)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        viewModel.deleteNote.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.error.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetDeleteNoteState()
                }

                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Xóa ghi chú thành công.",
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().supportFragmentManager.setFragmentResult("refresh_notes", Bundle())
                    requireActivity().supportFragmentManager.setFragmentResult("dismiss_dialog_note", Bundle())
                    dismiss()
                }
            }
        }
    }
}