package com.example.chatapp.domain.model

data class ChatMessage(
    val id: String = "",
    val message: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = 0L
) 