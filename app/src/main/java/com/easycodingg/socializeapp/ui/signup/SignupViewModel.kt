package com.easycodingg.socializeapp.ui.signup

import androidx.lifecycle.*
import com.easycodingg.socializeapp.api.SocializeApi
import com.easycodingg.socializeapp.api.requests.RegisterRequest
import com.easycodingg.socializeapp.api.responses.AuthErrorResponse
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
class SignupViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val socializeApi: SocializeApi
): ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _registerResponse: MutableLiveData<Resource<Token>> = MutableLiveData()
    val registerResponse: LiveData<Resource<Token>> = _registerResponse

    fun registerUser(
            name: String,
            email: String,
            password: String
    ) = viewModelScope.launch {
        val registerBody = RegisterRequest(name, email, password)

        _registerResponse.postValue(Resource.Loading())
        try {
            val response = socializeApi.registerUser(registerBody)

            if(response.isSuccessful) {
                response.body()?.let {
                    _registerResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), AuthErrorResponse::class.java)

                    val errorMessage = "${errorResponse.name}\n${errorResponse.email}\n${errorResponse.password}"
                    _registerResponse.postValue(Resource.Error(errorMessage))
                }
            }
        } catch(e: Exception) {
            _registerResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    fun saveAccessToken(accessToken: String) = viewModelScope.launch {
        userPreferences.saveAuthToken(accessToken)
        sendRedirectToHomeActivityEvent()
    }

    fun validateUserCredentials(
            name: String,
            email: String,
            password: String
    ): Boolean {

        val (isNameValid, errorName) = AuthUtils.validateName(name)
        if(!isNameValid) {
            sendErrorMessageEvent(errorName)
            return false
        }

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