package com.example.hitproduct.screen.home_page.setting.main

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.databinding.FragmentSettingBinding
import com.example.hitproduct.screen.home_page.setting.account_setting.AccountSettingFragment


class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    override fun initView() {

    }

    override fun initListener() {

        binding.tvAccountManagement.setOnClickListener {
            val accountSettingFragment = AccountSettingFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentHomeContainer, accountSettingFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    override fun initData() {

    }

    override fun handleEvent() {

    }

    override fun bindData() {

    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
    }

}