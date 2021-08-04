package com.easycodingg.socializeapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycodingg.socializeapp.api.SocializeApi
import com.easycodingg.socializeapp.api.requests.LoginRequest
import com.easycodingg.socializeapp.api.responses.AuthErrorResponse
import com.easycodingg.socializeapp.api.responses.MessageResponse
import com.easycodingg.socializeapp.api.responses.Token
import com.easycodingg.socializeapp.utils.AuthUtils
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val socializeApi: SocializeApi
): ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _loginResponse: MutableLiveData<Resource<Token>> = MutableLiveData()
    val loginResponse: LiveData<Resource<Token>> = _loginResponse

    fun loginUser(
            email: String,
            password: String
    ) = viewModelScope.launch {
        val loginBody = LoginRequest(email, password)

        _loginResponse.postValue(Resource.Loading())
        try {
            val response = socializeApi.loginUser(loginBody)

            if(response.isSuccessful) {
                response.body()?.let {
                    _loginResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _loginResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _loginResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    fun saveAccessToken(accessToken: String) = viewModelScope.launch {
        userPreferences.saveAuthToken(accessToken)
        sendRedirectToHomeActivityEvent()
    }

    fun validateUserCredentials(
            email: String,
            password: String
    ): Boolean {

        val (isEmailNameValid, errorEmail) = AuthUtils.validateEmail(email)
        if(!isEmailNameValid) {
            sendErrorMessageEvent(errorEmail)
            return false
        }

        val (isPasswordNameValid, errorPassword) = AuthUtils.validatePassword(password)
        if(!isPasswordNameValid) {
            sendErrorMessageEvent(errorPassword)
            return false
        }
        return true
    }

    private fun sendErrorMessageEvent(message: String) = viewModelScope.launch {
        eventChannel.send(Event.ShowErrorMessage(message))
    }

    private fun sendRedirectToHomeActivityEvent() = viewModelScope.launch {
        eventChannel.send(Event.RedirectToHomeActivity)
    }

    sealed class Event {
        data class ShowErrorMessage(val message: String): Event()
        object RedirectToHomeActivity : Event()
    }
}