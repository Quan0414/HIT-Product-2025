package com.example.hitproduct.screen.authentication.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hitproduct.R
import com.example.hitproduct.screen.authentication.send_invite_code.SendInviteCodeFragment
import com.example.hitproduct.databinding.FragmentSuccessCreateAccBinding


class SuccessCreateAccFragment : Fragment() {

    private var _binding: FragmentSuccessCreateAccBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSuccessCreateAccBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvContinue.setOnClickListener {
            val sendInviteCodeFragment = SendInviteCodeFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentStart, sendInviteCodeFragment)
                .commit()
        }

        //nut back
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}