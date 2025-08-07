package com.example.hitproduct.screen.authentication.register.success

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hitproduct.R
import com.example.hitproduct.screen.authentication.send_invite_code.SendInviteCodeFragment
import com.example.hitproduct.databinding.FragmentSuccessCreateAccBinding
import com.example.hitproduct.screen.authentication.create_pin.CreatePinFragment
import com.example.hitproduct.screen.authentication.login.LoginActivity


class SuccessCreateAccFragment : Fragment() {

    private var _binding: FragmentSuccessCreateAccBinding? = null
    private val binding get() = _binding!!

    private val flow by lazy {
        arguments?.getString("flow") ?: throw IllegalArgumentException("Flow argument is required")
    }

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
            if (flow == "forgot-password") {
                Toast.makeText(
                    requireContext(),
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT
                ).show()
                // chuyen ve login activity
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
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
        }

        //nut back
        binding.backIcon.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}