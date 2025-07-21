package com.example.hitproduct.screen.dialog.daily_question.your

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
import com.example.hitproduct.databinding.DialogYourDailyQuestionBinding
import com.example.hitproduct.screen.dialog.daily_question.your_love.YourLoveAnswerDialogFragment

class YourDailyQuestionDialogFragment : DialogFragment() {

    private var _binding: DialogYourDailyQuestionBinding? = null
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

    private val viewModel by activityViewModels<YourDailyQuestionViewModel> {
        YourDailyQuestionViewModelFactory(authRepo)
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
        _binding = DialogYourDailyQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchDailyQuestion()

        viewModel.dailyQuestion.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    Toast.makeText(
                        requireContext(), state.error.message, Toast.LENGTH_SHORT
                    ).show()
                }

                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    val question = state.data.question.question
                    binding.apply {
                        tvQuestion.text = question
                    }
                    (activity as MainActivity).question = question
                }
            }
        }

        viewModel.saveDailyQuestion.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    Toast.makeText(
                        requireContext(), state.error.message, Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetSaveQuestionState()

                }

                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Đã lưu câu trả lời",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

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
                    if (state.data.partnerAnswer == null) {
                        Toast.makeText(
                            requireContext(),
                            "Cậu ấy chưa trả lời câu hỏi này!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@observe
                    }
                    val answer = state.data.partnerAnswer ?: "(Trống)"
                    (activity as MainActivity).yourLoveAnswer = answer
                    val dialog = YourLoveAnswerDialogFragment()
                    dialog.show(parentFragmentManager, "YourLoveAnswerDialogFragment")
//                    viewModel.resetYourLoveAnswerState()
                }
            }
        }


        binding.btnSaveAnswer.setOnClickListener {
            val answer = binding.outlinedEditText.text.toString().trim()
            viewModel.saveDailyQuestion(answer)
            binding.outlinedEditText.text?.clear()
        }

        binding.btnCheckQuestion.setOnClickListener {
            viewModel.fetchYourLoveAnswer()
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