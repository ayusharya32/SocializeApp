package com.easycodingg.socializeapp.ui.userlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycodingg.socializeapp.api.SocializeApi
import com.easycodingg.socializeapp.api.responses.MessageResponse
import com.easycodingg.socializeapp.api.responses.User
import com.easycodingg.socializeapp.utils.AuthUtils
import com.easycodingg.socializeapp.utils.Constants.LIST_TYPE_CURRENT_USER_FOLLOWERS
import com.easycodingg.socializeapp.utils.Constants.LIST_TYPE_CURRENT_USER_FOLLOWING
import com.easycodingg.socializeapp.utils.Constants.LIST_TYPE_USER_FOLLOWING
import com.easycodingg.socializeapp.utils.Resource
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val socializeApi: SocializeApi
): ViewModel() {

    private val _userListResponse: MutableLiveData<Resource<List<User>>> = MutableLiveData()
    val userListResponse: LiveData<Resource<List<User>>> = _userListResponse

    fun onTokenRetrieved(
            accessToken: String,
            userListType: String,
            queryUserId: String = ""
    ) = viewModelScope.launch {
        try {
            _userListResponse.postValue(Resource.Loading())
            val bearerToken = AuthUtils.getBearerToken(accessToken)

            val response = when(userListType) {
                LIST_TYPE_CURRENT_USER_FOLLOWING -> {
                    socializeApi.getCurrentUserFollowing(bearerToken)
                }
                LIST_TYPE_CURRENT_USER_FOLLOWERS -> {
                    socializeApi.getCurrentUserFollowers(bearerToken)
                }
                LIST_TYPE_USER_FOLLOWING -> {
                    socializeApi.getFollowingByUserId(bearerToken, queryUserId)
                }
                else -> {
                    socializeApi.getFollowersByUserId(bearerToken, queryUserId)
                }
            }

            if(response.isSuccessful) {
                response.body()?.let {
                    _userListResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _userListResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _userListResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }
}