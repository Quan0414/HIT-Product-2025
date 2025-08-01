package com.example.hitproduct

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.hitproduct.databinding.DialogEmojiBinding
import com.example.hitproduct.screen.adapter.EmojiAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class DialogEmoji(
    private val onEmojiSelected: (String) -> Unit
) : BottomSheetDialogFragment() {
    private var _binding: DialogEmojiBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EmojiAdapter

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // full-width, wrap-content height, đẩy xuống đáy
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEmojiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emojiList = listOf(
            "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊",
            "😋", "😎", "😍", "😘", "😗", "😙", "😚", "☺️", "🙂", "🤗",
            "🤩", "🤔", "😐", "😑", "😶", "🙄", "😏", "😣", "😥", "😮",
            "🤐", "😯", "😪", "😫", "🥱", "😴", "😌", "😛", "😜", "😝",
            "🤤", "😒", "😓", "😔", "😕", "🙃", "🤑", "😲", "☹️", "🙁",
            "😖", "😞", "😟", "😤", "😢", "😭", "😦", "😧", "😨", "😩",
            "🤯", "😬", "😰", "😱", "🥵", "🥶", "😳", "🤪", "😵", "😡",
            "😠", "🤬", "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "🥳", "🥴",
            "😇", "🤠", "🧐", "🤓", "😈", "👿", "👹", "👺", "💀", "👻",
            "👽", "🤖"
        )

        adapter = EmojiAdapter(emojiList) { emoji ->
            onEmojiSelected(emoji)
            dismiss()
        }

        binding.rvEmoji.adapter = adapter
        binding.rvEmoji.layoutManager = GridLayoutManager(requireContext(), 8)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}