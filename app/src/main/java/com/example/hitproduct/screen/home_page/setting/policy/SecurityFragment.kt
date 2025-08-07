package com.example.hitproduct.screen.home_page.setting.policy

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.hitproduct.MainActivity
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.databinding.FragmentSecurityBinding
import com.example.hitproduct.screen.home_page.setting.policy.change_password.ChangePasswordFragment


class SecurityFragment : BaseFragment<FragmentSecurityBinding>() {
    override fun initView() {

    }

    override fun initListener() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.txtChangePassword.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentHomeContainer, changePasswordFragment)
                .addToBackStack(null)
                .commit()
        }

    }

    override fun initData() {

    }

    override fun handleEvent() {

    }

    override fun bindData() {

    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.hideBottomNav()
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as? MainActivity)?.showBottomNav()
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSecurityBinding {
        return FragmentSecurityBinding.inflate(inflater, container, false)
    }

}