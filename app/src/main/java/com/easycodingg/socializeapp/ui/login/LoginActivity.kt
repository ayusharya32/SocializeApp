package com.easycodingg.socializeapp.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.easycodingg.socializeapp.databinding.ActivityLoginBinding
import com.easycodingg.socializeapp.ui.home.HomeActivity
import com.easycodingg.socializeapp.ui.signup.SignupActivity
import com.easycodingg.socializeapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class LoginActivity: AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeLoginResponse()
        collectEvents()

        binding.btnLogin.setOnClickListener {
            closeKeyboard()
            loginUser()
        }

        binding.tvNewUser.setOnClickListener {
            Intent(this, SignupActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    private fun loginUser() {
        binding.apply {
            btnLogin.isEnabled = false

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val userCredentialsValid = viewModel.validateUserCredentials(email, password)

            if(userCredentialsValid) {
                viewModel.loginUser(email, password)
            }
        }
    }

    private fun observeLoginResponse() {
        viewModel.loginResponse.observe(this) { response ->
            binding.apply {
                btnLogin.isEnabled = true
                pbLogin.isVisible = response is Resource.Loading
            }

            if(response is Resource.Success) {
                response.data?.let {
                    Log.d("LoginResponse", it.accessToken)
                    viewModel.saveAccessToken(it.accessToken)
                }
            }

            if(response is Resource.Error) {
                Log.d("LoginResponseError", response.errorMessage)
                Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun collectEvents() = lifecycleScope.launchWhenStarted {
        viewModel.events.collect { event ->
            when(event) {
                is LoginViewModel.Event.ShowErrorMessage -> {
                    Toast.makeText(this@LoginActivity, event.message, Toast.LENGTH_SHORT).show()
                    binding.btnLogin.isEnabled = true
                }
                is LoginViewModel.Event.RedirectToHomeActivity -> {
                    Intent(this@LoginActivity, HomeActivity::class.java).also {
                        startActivity(it)
                        finish()
                    }
                }
            }
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus

        view?.let {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}