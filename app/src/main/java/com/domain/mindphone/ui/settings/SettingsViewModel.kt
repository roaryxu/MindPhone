package com.domain.mindphone.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.domain.mindphone.domain.llm.MindfulLlmManager
import com.domain.mindphone.domain.llm.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val llmManager = MindfulLlmManager(application)

    private val _activePersona = MutableStateFlow(llmManager.getActivePersona())
    val activePersona: StateFlow<Persona> = _activePersona.asStateFlow()

    private val _selectedGoals = MutableStateFlow(llmManager.getSelectedGoalIds())
    val selectedGoals: StateFlow<Set<String>> = _selectedGoals.asStateFlow()

    fun updatePersona(personaId: String) {
        llmManager.savePersona(personaId)
        _activePersona.value = llmManager.getActivePersona()
    }

    fun toggleGoal(goalId: String) {
        llmManager.toggleGoal(goalId)
        _selectedGoals.value = llmManager.getSelectedGoalIds()
    }

    override fun onCleared() {
        super.onCleared()
        llmManager.close()
    }
}
