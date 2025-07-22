package com.example.hitproduct.screen.dialog.note.create

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
import com.example.hitproduct.databinding.DialogCreateNoteBinding
import com.example.hitproduct.screen.dialog.note.get.DialogNote
import com.example.hitproduct.util.Constant
import java.time.LocalDate

class DialogCreateNote : DialogFragment() {
    private var _binding: DialogCreateNoteBinding? = null
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

    private val viewModel by viewModels<DialogCreateNoteViewModel> {
        DialogCreateNoteViewModelFactory(authRepo)
    }

    companion object {
        fun newInstance(date: String) = DialogCreateNote().apply {
            arguments = Bundle().apply {
                putString(Constant.ARG_DATE, date)
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
        _binding = DialogCreateNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveAnswer.setOnClickListener {
            val date = requireArguments().getString(Constant.ARG_DATE)
                ?: LocalDate.now().toString()
            val content = binding.outlinedEditText.text.toString().trim()
            viewModel.createNote(content, date)
        }

        viewModel.createNote.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        state.error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetCreateNoteState()
                }

                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Tạo ghi chú thành công.",
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().supportFragmentManager.setFragmentResult("refresh_notes", Bundle())
                    requireActivity().supportFragmentManager.setFragmentResult("dismiss_dialog_note", Bundle())

                    dismiss()
                }
            }
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }
}