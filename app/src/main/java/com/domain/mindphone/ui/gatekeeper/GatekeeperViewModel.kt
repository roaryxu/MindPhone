package com.domain.mindphone.ui.gatekeeper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.domain.mindphone.domain.llm.MindfulLlmManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GatekeeperState {
    object LoadingModel : GatekeeperState()
    object AwaitingInput : GatekeeperState()
    object Evaluating : GatekeeperState()
    data class Result(val isAllowed: Boolean, val message: String) : GatekeeperState()
}

class GatekeeperViewModel(application: Application) : AndroidViewModel(application) {
    private val llmManager = MindfulLlmManager(application)

    private val _uiState = MutableStateFlow<GatekeeperState>(GatekeeperState.LoadingModel)
    val uiState: StateFlow<GatekeeperState> = _uiState.asStateFlow()

    fun loadModel() {
        viewModelScope.launch {
            _uiState.value = GatekeeperState.LoadingModel
            try {
                llmManager.loadModel()
                _uiState.value = GatekeeperState.AwaitingInput
            } catch (e: Exception) {
                _uiState.value = GatekeeperState.Result(
                    isAllowed = false, 
                    message = "Could not load mindful model. Consider taking a deep breath instead."
                )
            }
        }
    }

    fun evaluate(appName: String, reason: String) {
        viewModelScope.launch {
            _uiState.value = GatekeeperState.Evaluating
            try {
                val response = llmManager.evaluateReason(appName, reason)
                
                // More forgiving parsing: check if the string contains the allow keyword anywhere,
                // just in case the model adds leading spaces, newlines, or extra text.
                val isAllowed = response.contains("DECISION: ALLOW", ignoreCase = true)
                
                val message = if (!isAllowed) {
                    val lines = response.split("\n").filter { it.isNotBlank() }
                    if (lines.size > 1) {
                        lines.drop(1).joinToString(" ").trim()
                    } else {
                        response.replace("DECISION: DENY", "", ignoreCase = true).trim().takeIf { it.isNotBlank() } 
                            ?: "Notice your impulse. Try stepping away for a moment."
                    }
                } else {
                    "" 
                }
                
                _uiState.value = GatekeeperState.Result(isAllowed, message)
            } catch (e: Exception) {
                _uiState.value = GatekeeperState.Result(
                    isAllowed = false, 
                    message = "Evaluation failed. Let's practice mindfulness anyway."
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        llmManager.close()
    }
}
