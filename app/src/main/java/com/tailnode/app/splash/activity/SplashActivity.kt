package com.tailnode.app.splash.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tailnode.app.location.activity.LocationActivity
import com.tailnode.app.login.activity.LoginActivity
import com.tailnode.app.store.AppState
import com.tailnode.app.store.PrefsStore
import com.tailnode.databinding.ActivitySplashBinding
import com.tailnode.utils.Constants
import kotlin.reflect.KClass

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PrefsStore.appState().observe(this, {
            when(it) {
                AppState.LOCATION -> launchActivity(LocationActivity::class)
                else -> launchActivity(LoginActivity::class)
            }
        })
    }

    private fun launchActivity(cls: KClass<*>) {
        val intent = Intent(this, cls.java)
        startActivity(intent)
        supportFinishAfterTransition()
    }
}