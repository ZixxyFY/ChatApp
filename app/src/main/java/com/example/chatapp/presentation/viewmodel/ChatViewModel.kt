package com.example.chatapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.remote.GeminiApiService
import com.example.chatapp.domain.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun sendMessage(text: String) {
        val userMessage = ChatMessage(message = text, isUser = true, timestamp = System.currentTimeMillis())
        _messages.value = _messages.value + userMessage
        viewModelScope.launch {
            val reply = try {
                GeminiApiService.getGeminiReply(text)
            } catch (e: Exception) {
                "[Gemini error: ${e.message}]"
            }
            val botReply = ChatMessage(message = reply, isUser = false, timestamp = System.currentTimeMillis())
            _messages.value = _messages.value + botReply
        }
    }
} 