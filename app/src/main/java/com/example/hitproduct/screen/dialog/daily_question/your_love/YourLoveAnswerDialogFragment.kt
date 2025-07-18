package com.example.hitproduct.screen.dialog.daily_question.your_love

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
import androidx.fragment.app.activityViewModels
import com.example.hitproduct.MainActivity
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.DialogYourLoveAnswerBinding


class YourLoveAnswerDialogFragment : DialogFragment() {

    private var _binding: DialogYourLoveAnswerBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext()
            .getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }

    private val viewModel by activityViewModels<YourLoveAnswerViewModel> {
        YourLoveAnswerViewModelFactory(authRepo)
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogYourLoveAnswerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvQuestion.text = (activity as MainActivity).question

        viewModel.fetchYourLoveAnswer()

        viewModel.yourLoveAnswer.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        state.error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    val answer = state.data.partnerAnswer
                    binding.outlinedTextView.text = answer
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