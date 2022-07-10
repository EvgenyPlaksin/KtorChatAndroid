package com.lnight.ktorchat.data.remote

import com.lnight.ktorchat.domain.model.Message
import com.lnight.ktorchat.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatSocketService {

    suspend fun initSession(
        username: String
    ): Resource<Unit>

    suspend fun sendMessage(message: String)

    fun observeMessages(): Flow<Message>

    suspend fun closeSession()

    companion object {
        const val BASE_URL = "ws://192.168.0.105:8080"
    }

    sealed class Endpoints(val url: String) {
        object ChatSocketRoute: Endpoints("$BASE_URL/chat-socket")
    }

}