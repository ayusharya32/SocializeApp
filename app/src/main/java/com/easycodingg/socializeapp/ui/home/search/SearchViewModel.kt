package com.easycodingg.socializeapp.ui.home.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycodingg.socializeapp.api.SocializeApi
import com.easycodingg.socializeapp.api.responses.MessageResponse
import com.easycodingg.socializeapp.api.responses.User
import com.easycodingg.socializeapp.utils.AuthUtils
import com.easycodingg.socializeapp.utils.Resource
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor (
    private val socializeApi: SocializeApi
): ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _searchResponse: MutableLiveData<Resource<List<User>>> = MutableLiveData()
    val searchResponse: LiveData<Resource<List<User>>> = _searchResponse

    fun onSearchButtonClicked(accessToken: String, searchQuery: String) {
        if(searchQuery.length < 2) {
            sendShowErrorMessageEvent("Search Query should be of at least 2 characters")
            return
        }
        searchUsers(AuthUtils.getBearerToken(accessToken), searchQuery)
    }

    private fun searchUsers(bearerToken: String, searchQuery: String) = viewModelScope.launch {
        try {
            _searchResponse.postValue(Resource.Loading())
            val response = socializeApi.searchUsers(bearerToken, searchQuery)

            if(response.isSuccessful) {
                response.body()?.let {
                    _searchResponse.postValue(Resource.Success(it))
                }
            } else {
                response.errorBody()?.let {
                    val errorResponse = Gson().fromJson(it.string(), MessageResponse::class.java)
                    _searchResponse.postValue(Resource.Error(errorResponse.message))
                }
            }
        } catch(e: Exception) {
            _searchResponse.postValue(e.localizedMessage?.let { Resource.Error(it) })
        }
    }

    private fun sendShowErrorMessageEvent(message: String) = viewModelScope.launch {
        eventChannel.send(Event.ShowErrorMessage(message))
    }

    sealed class Event {
        data class ShowErrorMessage(val message: String): Event()
    }
}