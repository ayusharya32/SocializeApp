package com.easycodingg.socializeapp.ui.home.feed

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
class FeedViewModel @Inject constructor(
    private val socializeApi: SocializeApi
): ViewModel() {

    private val _feedPostsResponse: MutableLiveData<Resource<List<Post>>> = MutableLiveData()
    val feedPostsResponse: LiveData<Resource<List<Post>>> = _feedPostsResponse

    private val _addLikeResponse: MutableLiveData<Resource<Post>> = MutableLiveData()
    val addLikeResponse: LiveData<Resource<Post>> = _addLikeResponse

    private val _removeLikeResponse: MutableLiveData<Resource<Post>> = MutableLiveData()
    val removeLikeResponse: LiveData<Resource<Post>> = _removeLikeResponse

    private val _userResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val userResponse: LiveData<Resource<User>> = _userResponse

    fun onTokenRetrieved(accessToken: String) {
        val bearerToken = AuthUtils.getBearerToken(accessToken)
        getCurrentUser(bearerToken)
        getFeedPosts(bearerToken)
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

    private fun getFeedPosts(bearerToken: String) = viewModelScope.launch {
        try {
            _feedPostsResponse.postValue(Resource.Loading())
            val response = socializeApi.getFeedPosts(bearerToken)

            if(response.isSuccessful) {
                response.body()?.let {
                    _feedPostsResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _feedPostsResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _feedPostsResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
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