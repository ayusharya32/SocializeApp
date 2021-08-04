package com.easycodingg.socializeapp.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(): ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    fun onSavedAccessTokenFound() = sendRedirectToHomeActivityEvent()

    fun onSavedAccessTokenNotFound() = sendRedirectToLoginActivityEvent()

    private fun sendRedirectToHomeActivityEvent() = viewModelScope.launch {
        delay(1500)
        eventChannel.send(Event.RedirectToHomeActivity)
    }

    private fun sendRedirectToLoginActivityEvent() = viewModelScope.launch {
        delay(1500)
        eventChannel.send(Event.RedirectToLoginActivity)
    }

    sealed class Event {
        object RedirectToHomeActivity: Event()
        object RedirectToLoginActivity: Event()
    }
}
