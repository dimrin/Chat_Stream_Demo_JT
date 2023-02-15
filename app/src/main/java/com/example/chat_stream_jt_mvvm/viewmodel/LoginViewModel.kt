package com.example.chat_stream_jt_mvvm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chat_stream_jt_mvvm.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val client: ChatClient) : ViewModel() {

    private val _loginEvent = MutableSharedFlow<LogInEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val _loadingState = MutableLiveData<UILoadingState>()
    val loadingState: LiveData<UILoadingState> get() = _loadingState

    private fun isValidUsername(username: String): Boolean {
        return username.length > Constants.MIN_USERNAME_LENGTH
    }

    fun loginUser(username: String, token: String? = null) {
        val trimmedUsername = username.trim()

        viewModelScope.launch {
            if (isValidUsername(username = trimmedUsername) && token != null) {
                loginRegisterUser(trimmedUsername, token)
            } else if (isValidUsername(username = trimmedUsername) && token == null) {
                loginGuestUser(trimmedUsername)
            } else {
                _loginEvent.emit(LogInEvent.ErrorInputTooShort)
            }
        }
    }

    private fun loginGuestUser(username: String) {

        _loadingState.value = UILoadingState.Loading

        client.connectGuestUser(userId = username, username = username).enqueue{ result ->

            _loadingState.value = UILoadingState.NotLoading

            if (result.isSuccess) {
                viewModelScope.launch {
                    _loginEvent.emit(LogInEvent.Success)
                }
            } else {
                viewModelScope.launch {
                    _loginEvent.emit(LogInEvent.ErrorLogIn(result.error().message ?: "Unknown Error"))
                }
            }
        }

    }

    private fun loginRegisterUser(username: String, token: String){
        val user = User(id = username, name = username)

        _loadingState.value = UILoadingState.Loading

        client.connectUser(user = user, token = token).enqueue { result ->

            _loadingState.value = UILoadingState.NotLoading

            if (result.isSuccess) {
                viewModelScope.launch {
                    _loginEvent.emit(LogInEvent.Success)
                }
            } else {
                viewModelScope.launch {
                    _loginEvent.emit(LogInEvent.ErrorLogIn(result.error().message ?: "Unknown Error"))
                }
            }
        }
    }


    sealed class LogInEvent {
        object ErrorInputTooShort : LogInEvent()
        data class ErrorLogIn(val error: String) : LogInEvent()
        object Success: LogInEvent()
    }

    sealed class UILoadingState {
        object Loading: UILoadingState()
        object NotLoading: UILoadingState()
    }

}
