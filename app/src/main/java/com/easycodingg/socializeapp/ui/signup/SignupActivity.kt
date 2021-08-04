package com.easycodingg.socializeapp.ui.signup

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
import com.easycodingg.socializeapp.databinding.ActivitySignupBinding
import com.easycodingg.socializeapp.ui.home.HomeActivity
import com.easycodingg.socializeapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SignupActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: SignupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        collectEvents()
        observeRegisterResponse()

        binding.btnSignUp.setOnClickListener {
            closeKeyboard()
            registerUser()
        }
    }

    private fun registerUser() {
        binding.apply {
            btnSignUp.isEnabled = false

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val userCredentialsValid = viewModel.validateUserCredentials(name, email, password)

            if(userCredentialsValid) {
                viewModel.registerUser(name, email, password)
            }
        }
    }

    private fun observeRegisterResponse() {
        viewModel.registerResponse.observe(this@SignupActivity) { response ->
            binding.apply {
                btnSignUp.isEnabled = true
                pbSignup.isVisible = response is Resource.Loading
            }

            if(response is Resource.Success) {
                response.data?.let {
                    Log.d("RegisterResponse", it.accessToken)
                    viewModel.saveAccessToken(it.accessToken)
                }
            }
            if(response is Resource.Error) {
                Log.d("RegisterResponseError", response.errorMessage)
                Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun collectEvents() = lifecycleScope.launchWhenStarted {
        viewModel.events.collect { event ->
            when(event) {
                is SignupViewModel.Event.ShowErrorMessage -> {
                    Toast.makeText(this@SignupActivity, event.message, Toast.LENGTH_SHORT).show()
                    binding.btnSignUp.isEnabled = true
                }
                is SignupViewModel.Event.RedirectToHomeActivity -> {
                    Intent(this@SignupActivity, HomeActivity::class.java).also {
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