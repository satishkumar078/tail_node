package com.tailnode.app.login.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.tailnode.app.location.activity.LocationActivity
import com.tailnode.app.login.viewModel.LoginViewModel
import com.tailnode.app.store.AppState
import com.tailnode.app.store.PrefsStore
import com.tailnode.databinding.ActivityLoginBinding
import com.tailnode.utils.ToastDialog
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding.viewModel = loginViewModel

        binding.fabNext.setOnClickListener {
            if (!loginViewModel.isNameValid()) {
                ToastDialog(context = this, msg = "Please enter name").show()
                return@setOnClickListener
            }
            if (!loginViewModel.isMobileValid()) {
                ToastDialog(context = this, msg = "Please enter valid mobile number").show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                PrefsStore.setUserName(loginViewModel.name.get()?:"")
                PrefsStore.setUserMobile(loginViewModel.mobile.get()?:"")
                PrefsStore.setAppState(AppState.LOCATION)
            }

            launchActivity(LocationActivity::class)
        }
    }

    private fun launchActivity(cls: KClass<*>) {
        val intent = Intent(this, cls.java)
        startActivity(intent)
        supportFinishAfterTransition()
    }
}