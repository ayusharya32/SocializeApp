package com.easycodingg.socializeapp.ui.home.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycodingg.socializeapp.api.SocializeApi
import com.easycodingg.socializeapp.api.responses.MessageResponse
import com.easycodingg.socializeapp.api.responses.Post
import com.easycodingg.socializeapp.api.responses.User
import com.easycodingg.socializeapp.ui.home.settings.SettingsViewModel
import com.easycodingg.socializeapp.utils.AuthUtils
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val socializeApi: SocializeApi
): ViewModel() {

    private val _userResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val userResponse: LiveData<Resource<User>> = _userResponse

    private val _postsResponse: MutableLiveData<Resource<List<Post>>> = MutableLiveData()
    val postsResponse: LiveData<Resource<List<Post>>> = _postsResponse

    private val _deletePostResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val deletePostResponse: LiveData<Resource<String>> = _deletePostResponse

    private val _addLikeResponse: MutableLiveData<Resource<Post>> = MutableLiveData()
    val addLikeResponse: LiveData<Resource<Post>> = _addLikeResponse

    private val _removeLikeResponse: MutableLiveData<Resource<Post>> = MutableLiveData()
    val removeLikeResponse: LiveData<Resource<Post>> = _removeLikeResponse

    fun onTokenRetrieved(accessToken: String) {
        val bearerToken = AuthUtils.getBearerToken(accessToken)
        getCurrentUser(bearerToken)
        getCurrentUserPosts(bearerToken)
    }

    fun onPostDeleteButtonClicked(accessToken: String, postId: String) {
        val bearerToken = AuthUtils.getBearerToken(accessToken)
        removeUserPost(bearerToken, postId)
    }

    fun refreshPostList(accessToken: String) {
        val bearerToken = AuthUtils.getBearerToken(accessToken)
        getCurrentUserPosts(bearerToken)
    }

    fun onPostLikeButtonClicked(accessToken: String, post: Post) {
        val bearerToken = AuthUtils.getBearerToken(accessToken)

        if(post.isLiked) {
            removeLikeFromPost(bearerToken, post)
        } else {
            addLikeToPost(bearerToken, post)
        }
    }

    private fun getCurrentUser(bearerToken: String) = viewModelScope.launch {
        try {
            _userResponse.postValue(Resource.Loading())
            val response = socializeApi.getCurrentUser(bearerToken)

            if(response.isSuccessful) {
                response.body()?.let {
                    _userResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _userResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _userResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    private fun getCurrentUserPosts(bearerToken: String) = viewModelScope.launch {
        try {
            _postsResponse.postValue(Resource.Loading())
            val response = socializeApi.getCurrentUserPosts(bearerToken)

            if(response.isSuccessful) {
                response.body()?.let {
                    _postsResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _postsResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _postsResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    private fun removeUserPost(bearerToken: String, postId: String) = viewModelScope.launch {
        try {
            _deletePostResponse.postValue(Resource.Loading())
            val response = socializeApi.removePost(bearerToken, postId)

            if(response.isSuccessful) {
                response.body()?.let {
                    _deletePostResponse.postValue(Resource.Success(postId))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _deletePostResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _deletePostResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    private fun addLikeToPost(bearerToken: String, post: Post) = viewModelScope.launch {
        try {
            _addLikeResponse.postValue(Resource.Loading())
            val response = socializeApi.addLikeToPost(bearerToken, post.postId)

            if(response.isSuccessful) {
                response.body()?.let {
                    _addLikeResponse.postValue(Resource.Success(post.apply { isLiked = !isLiked }))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _addLikeResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _addLikeResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    private fun removeLikeFromPost(bearerToken: String, post: Post) = viewModelScope.launch {
        try {
            _removeLikeResponse.postValue(Resource.Loading())
            val response = socializeApi.removeLikeFromPost(bearerToken, post.postId)

            if(response.isSuccessful) {
                response.body()?.let {
                    _removeLikeResponse.postValue(Resource.Success(post.apply { isLiked = !isLiked }))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _removeLikeResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _removeLikeResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    sealed class Event {
        data class ShowErrorMessage(val message: String): Event()
    }
}