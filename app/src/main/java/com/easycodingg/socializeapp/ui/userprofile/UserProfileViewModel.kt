package com.easycodingg.socializeapp.ui.userprofile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycodingg.socializeapp.api.SocializeApi
import com.easycodingg.socializeapp.api.responses.MessageResponse
import com.easycodingg.socializeapp.api.responses.Post
import com.easycodingg.socializeapp.api.responses.User
import com.easycodingg.socializeapp.utils.AuthUtils
import com.easycodingg.socializeapp.utils.Resource
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val socializeApi: SocializeApi
): ViewModel() {

    private val _currentUserResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val currentUserResponse: LiveData<Resource<User>> = _currentUserResponse

    private val _postsResponse: MutableLiveData<Resource<List<Post>>> = MutableLiveData()
    val postsResponse: LiveData<Resource<List<Post>>> = _postsResponse

    private val _followUserResponse: MutableLiveData<Resource<MessageResponse>> = MutableLiveData()
    val followUserResponse: LiveData<Resource<MessageResponse>> = _followUserResponse

    private val _unfollowUserResponse: MutableLiveData<Resource<MessageResponse>> = MutableLiveData()
    val unfollowUserResponse: LiveData<Resource<MessageResponse>> = _unfollowUserResponse

    private val _addLikeResponse: MutableLiveData<Resource<Post>> = MutableLiveData()
    val addLikeResponse: LiveData<Resource<Post>> = _addLikeResponse

    private val _removeLikeResponse: MutableLiveData<Resource<Post>> = MutableLiveData()
    val removeLikeResponse: LiveData<Resource<Post>> = _removeLikeResponse

    fun onTokenRetrieved(accessToken: String, userId: String) {
        val bearerToken = AuthUtils.getBearerToken(accessToken)
        getUserPosts(bearerToken, userId)
        getCurrentUser(bearerToken)
    }

    fun onFollowToggleClicked(accessToken: String, currentState: String, followingId: String) {
        val bearerToken = AuthUtils.getBearerToken(accessToken)

        if(currentState == "Follow") {
            followUser(bearerToken, followingId)
        } else if(currentState == "Unfollow") {
            unfollowUser(bearerToken, followingId)
        }
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
        Log.d("Usery", "ViewModel : Inside Get Current User")
        try {
            _currentUserResponse.postValue(Resource.Loading())
            val response = socializeApi.getCurrentUser(bearerToken)

            if(response.isSuccessful) {
                response.body()?.let {
                    Log.d("Usery", "ViewModel : Current User : $it")
                    _currentUserResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _currentUserResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _currentUserResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    private fun getUserPosts(bearerToken: String, userId: String) = viewModelScope.launch {
        Log.d("Usery", "ViewModel : Inside Get User Posts")
        try {
            _postsResponse.postValue(Resource.Loading())
            val response = socializeApi.getUserPosts(bearerToken, userId)

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

    private fun followUser(bearerToken: String, followingId: String) = viewModelScope.launch {
        try {
            _followUserResponse.postValue(Resource.Loading())
            val response = socializeApi.followUser(bearerToken, followingId)

            if(response.isSuccessful) {
                response.body()?.let {
                    _followUserResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _followUserResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _followUserResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    private fun unfollowUser(bearerToken: String, followingId: String) = viewModelScope.launch {
        try {
            _unfollowUserResponse.postValue(Resource.Loading())
            val response = socializeApi.unFollowUser(bearerToken, followingId)

            if(response.isSuccessful) {
                response.body()?.let {
                    _unfollowUserResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _unfollowUserResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _unfollowUserResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
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

}