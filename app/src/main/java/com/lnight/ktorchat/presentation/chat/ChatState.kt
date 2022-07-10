package com.lnight.ktorchat.presentation.chat

import com.lnight.ktorchat.domain.model.Message

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false
)