package com.easycodingg.socializeapp.ui.home.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycodingg.socializeapp.api.SocializeApi
import com.easycodingg.socializeapp.api.responses.MessageResponse
import com.easycodingg.socializeapp.utils.AuthUtils
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val socializeApi: SocializeApi
): ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _updateUserResponse: MutableLiveData<Resource<MessageResponse>> = MutableLiveData()
    val updateUserResponse: LiveData<Resource<MessageResponse>> = _updateUserResponse

    fun onUpdateButtonClicked(accessToken: String, updateName: String, updateImageFile: File?) {

        if(updateImageFile == null && updateName.isEmpty()) {
            sendMessageEvent("Nothing To Update")
            return
        }

        val (isNameValid, errorName) = AuthUtils.validateName(updateName)
        if(!isNameValid && updateName.isNotEmpty()) {
            sendMessageEvent(errorName)
            return
        }

        val filePart = updateImageFile?.let { imageFile ->
            val requestBody = imageFile.asRequestBody("image/jpg".toMediaType())
            MultipartBody.Part.createFormData("profileImage", imageFile.name, requestBody)
        }

        val nameRequestBody = updateName.toRequestBody("text/plain".toMediaTypeOrNull())

        sendDisableButtonsEvent()
        updateUser(accessToken, nameRequestBody, filePart)
    }

    fun onLogOutButtonClicked() = viewModelScope.launch {
        userPreferences.saveAuthToken("")
        sendMessageEvent("Logged Out Successfully")
        eventChannel.send(Event.RedirectToLoginActivity)
    }

    fun onProfileUpdatedSuccessfully() = sendResetAllViewsEvent()

    private fun updateUser(
            accessToken: String,
            nameRequestBody: RequestBody,
            filePart: MultipartBody.Part?
    ) = viewModelScope.launch {
        try {
            _updateUserResponse.postValue(Resource.Loading())
            val response = if(filePart != null) {
                socializeApi.updateUser(AuthUtils.getBearerToken(accessToken), filePart, nameRequestBody)
            } else {
                socializeApi.updateName(AuthUtils.getBearerToken(accessToken), nameRequestBody)
            }

            if(response.isSuccessful) {
                response.body()?.let {
                    _updateUserResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _updateUserResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch (e: Exception) {
            _updateUserResponse.postValue(e.localizedMessage?.let{ Resource.Error(it) })
        }
    }

    private fun sendMessageEvent(message: String) = viewModelScope.launch {
        eventChannel.send(Event.ShowMessage(message))
    }

    private fun sendDisableButtonsEvent() = viewModelScope.launch {
        eventChannel.send(Event.DisableButtons)
    }

    private fun sendResetAllViewsEvent() = viewModelScope.launch {
        eventChannel.send(Event.ResetAllViews)
    }

    sealed class Event {
        data class ShowMessage(val message: String): Event()
        object RedirectToLoginActivity: Event()
        object DisableButtons: Event()
        object ResetAllViews: Event()
    }
}