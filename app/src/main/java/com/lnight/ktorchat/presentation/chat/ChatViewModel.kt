package com.lnight.ktorchat.presentation.chat

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lnight.ktorchat.data.remote.ChatSocketService
import com.lnight.ktorchat.data.remote.MessageService
import com.lnight.ktorchat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
   private val messageService: MessageService,
   private val chatSocketService: ChatSocketService,
   private val savedInstanceHandle: SavedStateHandle
): ViewModel() {

    private val _messageText = mutableStateOf("")
    val messageText: State<String> = _messageText

    private val _state = mutableStateOf<ChatState>(ChatState())
    val state: State<ChatState> = _state

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun onMessageChange(message: String) {
        _messageText.value = message
    }

    fun connectToChat() {
        getAllMessages()
        savedInstanceHandle.get<String>("username")?.let { username ->
            viewModelScope.launch {
                val result = chatSocketService.initSession(username)

                when(result) {
                    is Resource.Success -> {
                        chatSocketService.observeMessages()
                            .onEach { message ->
                                 val newList = state.value.messages.toMutableList().apply {
                                     add(0, message)
                                 }
                                _state.value = state.value.copy(
                                    messages = newList
                                )
                            }.launchIn(viewModelScope)
                    }
                    is Resource.Error -> {
                        _toastEvent.emit(result.message ?: "Unknown error")
                    }
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            chatSocketService.closeSession()
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            if(messageText.value.isNotBlank()) {
                chatSocketService.sendMessage(messageText.value)
                _messageText.value = ""
            }
        }
    }

    private fun getAllMessages() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = messageService.getAllMessages()
            _state.value = _state.value.copy(
                messages = result,
                isLoading = false
            )
        }
    }

}