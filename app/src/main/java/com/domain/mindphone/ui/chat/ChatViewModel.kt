package com.domain.mindphone.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.domain.mindphone.domain.llm.MindfulLlmManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val llmManager = MindfulLlmManager(application)

    private val _systemPrompt = MutableStateFlow(llmManager.getSystemPrompt())
    val systemPrompt: StateFlow<String> = _systemPrompt.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isModelLoading = MutableStateFlow(true)
    val isModelLoading: StateFlow<Boolean> = _isModelLoading.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                llmManager.loadModel()
                _isModelLoading.value = false
                _messages.value = listOf(ChatMessage("Model loaded. Try chatting with me to test my persona!", false))
            } catch (e: Exception) {
                _messages.value = listOf(ChatMessage("Failed to load model: ${e.message}", false))
                _isModelLoading.value = false
            }
        }
    }

    fun updateSystemPrompt(newPrompt: String) {
        _systemPrompt.value = newPrompt
        llmManager.saveSystemPrompt(newPrompt)
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isGenerating.value) return

        val userMsg = ChatMessage(text, true)
        _messages.value = _messages.value + userMsg
        _isGenerating.value = true

        viewModelScope.launch {
            val responseText = llmManager.chat(text)
            val aiMsg = ChatMessage(responseText, false)
            _messages.value = _messages.value + aiMsg
            _isGenerating.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        llmManager.close()
    }
}
