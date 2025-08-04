package com.example.hitproduct.screen.authentication.register.success

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hitproduct.R
import com.example.hitproduct.screen.authentication.send_invite_code.SendInviteCodeFragment
import com.example.hitproduct.databinding.FragmentSuccessCreateAccBinding
import com.example.hitproduct.screen.authentication.create_pin.CreatePinFragment


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
            val createPinFragment = CreatePinFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentStart, createPinFragment)
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