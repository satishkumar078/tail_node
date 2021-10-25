package com.tailnode.app.login.viewModel

import android.util.Patterns
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    var countryCode = ObservableField("+91")
    var mobile = ObservableField<String>()
    var name = ObservableField<String>()

    fun isMobileValid(): Boolean {
        val mobile = mobile.get()?:""
        return mobile.isNotEmpty() && Patterns.PHONE.matcher(mobile).matches()
    }

    fun isNameValid() : Boolean {
        return !name.get().isNullOrEmpty()
    }
}