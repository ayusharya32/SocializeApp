package com.easycodingg.socializeapp.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.ui.home.HomeActivity
import com.easycodingg.socializeapp.ui.login.LoginActivity
import com.easycodingg.socializeapp.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity: AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        collectEvents()

        userPreferences.authToken.asLiveData().observe(this) { authToken ->
            if(!authToken.isNullOrEmpty()) {
                viewModel.onSavedAccessTokenFound()
            } else {
                viewModel.onSavedAccessTokenNotFound()
            }
        }
    }

    private fun collectEvents() = lifecycleScope.launchWhenStarted {
        viewModel.events.collect { event ->
            when(event) {
                is SplashViewModel.Event.RedirectToLoginActivity -> {
                    Intent(this@SplashActivity, LoginActivity::class.java).also {
                        startActivity(it)
                        finish()
                    }
                }
                is SplashViewModel.Event.RedirectToHomeActivity -> {
                    val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        }
    }
}