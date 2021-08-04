package com.easycodingg.socializeapp.ui.home.addpost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycodingg.socializeapp.api.SocializeApi
import com.easycodingg.socializeapp.api.responses.MessageResponse
import com.easycodingg.socializeapp.utils.AuthUtils
import com.easycodingg.socializeapp.utils.Resource
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val socializeApi: SocializeApi
): ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _addPostResponse: MutableLiveData<Resource<MessageResponse>> = MutableLiveData()
    val addPostResponse: LiveData<Resource<MessageResponse>> = _addPostResponse

    fun onPostButtonClicked(accessToken: String, postImageFile: File?, caption: String) {
        if(postImageFile == null) {
            sendShowMessageEvent("No Image selected")
            return
        }

        val fileRequestBody = postImageFile.asRequestBody("image/jpg".toMediaType())
        val filePart = MultipartBody.Part.createFormData("postImage", postImageFile.name, fileRequestBody)

        val captionRequestBody = caption.toRequestBody("text/plain".toMediaType())

        sendDisableButtonsEvent()
        addPost(AuthUtils.getBearerToken(accessToken), filePart, captionRequestBody)
    }

    fun onSuccessfullyPosted() = sendResetAllViewsEvent()

    private fun addPost(
            bearerToken: String,
            filePart: MultipartBody.Part,
            captionBody: RequestBody
    ) = viewModelScope.launch {
        try {
            _addPostResponse.postValue(Resource.Loading())
            val response = socializeApi.addPost(bearerToken, filePart, captionBody)

            if(response.isSuccessful) {
                response.body()?.let {
                    _addPostResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _addPostResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch (e: Exception) {
            _addPostResponse.postValue(e.localizedMessage?.let{ Resource.Error(it) })
        }
    }

    private fun sendShowMessageEvent(message: String) = viewModelScope.launch {
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
        object DisableButtons: Event()
        object ResetAllViews: Event()
    }
}