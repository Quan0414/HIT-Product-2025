package com.example.hitproduct.screen.home_page.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hitproduct.data.repository.AuthRepository

class AccountSettingViewModelFactory(
    private val authRepository: AuthRepository  // nhận repo từ ngoài
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountSettingViewmodel::class.java)) {
            // khởi AccountSettingViewmodel với repo đã có
            return AccountSettingViewmodel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}