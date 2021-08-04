package com.easycodingg.socializeapp.ui.comments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycodingg.socializeapp.api.SocializeApi
import com.easycodingg.socializeapp.api.requests.AddCommentRequest
import com.easycodingg.socializeapp.api.responses.Comment
import com.easycodingg.socializeapp.api.responses.MessageResponse
import com.easycodingg.socializeapp.utils.AuthUtils
import com.easycodingg.socializeapp.utils.Resource
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val socializeApi: SocializeApi
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _addCommentResponse: MutableLiveData<Resource<MessageResponse>> = MutableLiveData()
    val addCommentResponse: LiveData<Resource<MessageResponse>> = _addCommentResponse

    private val _removeCommentResponse: MutableLiveData<Resource<MessageResponse>> = MutableLiveData()
    val removeCommentResponse: LiveData<Resource<MessageResponse>> = _removeCommentResponse

    private val _commentListResponse: MutableLiveData<Resource<List<Comment>>> = MutableLiveData()
    val commentListResponse: LiveData<Resource<List<Comment>>> = _commentListResponse

    fun onTokenRetrieved(accessToken: String, postId: String) {
        val bearerToken = AuthUtils.getBearerToken(accessToken)
        getPostComments(bearerToken, postId)
    }

    fun onSendButtonClicked(accessToken: String, postId: String, commentText: String) {
        if(commentText.isEmpty()) {
            sendShowMessageEvent("Empty Comment")
            return
        }

        val bearerToken = AuthUtils.getBearerToken(accessToken)
        addCommentToPost(bearerToken, postId, commentText)
    }

    fun onCommentDeleteButtonClicked(accessToken: String, commentId: String) {
        val bearerToken = AuthUtils.getBearerToken(accessToken)
        removeCommentFromPost(bearerToken, commentId)
    }

    fun refreshCommentList(accessToken: String, postId: String) {
        val bearerToken = AuthUtils.getBearerToken(accessToken)
        getPostComments(bearerToken, postId)
    }

    private fun getPostComments(bearerToken: String, postId: String) = viewModelScope.launch {
        try {
            _commentListResponse.postValue(Resource.Loading())
            val response = socializeApi.getPostComments(bearerToken, postId)

            if(response.isSuccessful) {
                response.body()?.let {
                    _commentListResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _commentListResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _commentListResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    private fun addCommentToPost(
            bearerToken: String,
            postId: String,
            commentText: String
    ) = viewModelScope.launch {
        try {
            _addCommentResponse.postValue(Resource.Loading())
            val addCommentRequest = AddCommentRequest(postId, commentText)
            val response = socializeApi.addCommentToPost(bearerToken, addCommentRequest)

            if(response.isSuccessful) {
                response.body()?.let {
                    _addCommentResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _addCommentResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _addCommentResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    private fun removeCommentFromPost(bearerToken: String, commentId: String) = viewModelScope.launch {
        try {
            _removeCommentResponse.postValue(Resource.Loading())
            val response = socializeApi.removeCommentFromPost(bearerToken, commentId)

            if(response.isSuccessful) {
                response.body()?.let {
                    _removeCommentResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _removeCommentResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _removeCommentResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    private fun sendShowMessageEvent(message: String) = viewModelScope.launch {
        eventChannel.send(Event.ShowMessage(message))
    }

    sealed class Event {
        data class ShowMessage(val message: String): Event()
    }
}