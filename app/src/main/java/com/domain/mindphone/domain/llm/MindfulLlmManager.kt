package com.domain.mindphone.domain.llm

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Persona(val id: String, val name: String, val prompt: String)

object PersonaOptions {
    val options = listOf(
        Persona("coach", "Mindful Coach", "You are a strict but compassionate mindfulness coach."),
        Persona("drill_sergeant", "Drill Sergeant", "You are an aggressive, no-nonsense military drill sergeant. If the user is wasting time, you order them to drop and give you 20."),
        Persona("monk", "Zen Monk", "You are a peaceful Zen monk. You suggest deep breathing and meditation instead of mindless scrolling. Speak calmly and spiritually."),
        Persona("philosopher", "Socrates", "You are Socrates. You respond to their reason with deep, probing philosophical questions about the nature of their desires and time.")
    )
    
    fun getById(id: String): Persona = options.find { it.id == id } ?: options.first()
}

data class Goal(val id: String, val name: String, val instruction: String)

object GoalOptions {
    val options = listOf(
        Goal("sleep_better", "Sleep better with less late-night usage", "The user wants to improve their sleep. Deny addictive apps much more strictly if it is late at night. Remind them of their sleep goal."),
        Goal("socialise_more", "Socialise more instead of scrolling", "The user wants to connect with others. Be more lenient in allowing apps if the reason involves talking to, messaging, or relating to other people, but deny scrolling."),
        Goal("lock_in", "Lock in and focus with 0 distractions", "The user wants to focus intensely. Allow productivity and work-related tasks, but be extremely strict and deny everything else."),
        Goal(id="easy_mode", name="I'm being denied too often", instruction = "The user has activated easy mode, be more lenient and allow more often, only deny blatant cases.")
    )
}

class MindfulLlmManager(private val context: Context) {
    private var llmInference: LlmInference? = null
    
    private val prefs: SharedPreferences = context.getSharedPreferences("mindful_prefs", Context.MODE_PRIVATE)
    
    // Persistent, uneditable, invisible basic instructions
    private val coreSystemInstructions = """
        Your primary task is to evaluate if a user should open an addictive app.
        If the user's reason is productive, intentional, or necessary, you MUST output EXACTLY and ONLY: "DECISION: ALLOW". Do not add any other words.
        If the reason is mindless, bored, or negative, you MUST output EXACTLY: "DECISION: DENY" on the first line, followed by a new line, and then a 1-sentence mindful alternative.
    """.trimIndent()

    fun getActivePersona(): Persona {
        val savedId = prefs.getString("persona_id", "coach") ?: "coach"
        return PersonaOptions.getById(savedId)
    }

    fun savePersona(id: String) {
        prefs.edit { putString("persona_id", id) }
    }

    fun getSelectedGoalIds(): Set<String> {
        return prefs.getStringSet("selected_goals", emptySet()) ?: emptySet()
    }

    fun toggleGoal(id: String) {
        val current = getSelectedGoalIds().toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        prefs.edit { putStringSet("selected_goals", current) }
    }

    private fun buildSystemContext(includeCoreInstructions: Boolean = true): String {
        val base = if (includeCoreInstructions) "$coreSystemInstructions\n\n" else ""
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        
        val goals = getSelectedGoalIds().mapNotNull { id -> GoalOptions.options.find { it.id == id } }
        val goalsText = if (goals.isNotEmpty()) {
            "User Goals (Factor these into your decision and reference them in your personality):\n" +
            goals.joinToString("\n") { "- ${it.name}: ${it.instruction}" } + "\n\n"
        } else ""
        
        return "${base}Current Time: $currentTime\n\n$goalsText"
    }

    // Switched back to the stable 4-bit Gemma 3 model
    private val modelFileName = "gemma3-1b-it-int4.litertlm"

    suspend fun loadModel() = withContext(Dispatchers.IO) {
        if (llmInference == null) {
            val modelFile = File(context.filesDir, modelFileName)

            // Cleanup old models to save space
            context.filesDir.listFiles()?.forEach { file ->
                if ((file.name.endsWith(".litertlm") || file.name.endsWith(".task") || file.name.endsWith(".bin")) && file.name != modelFileName) {
                    file.delete()
                }
            }

            if (!modelFile.exists()) {
                copyModelFromAssetsToFilesDir(modelFile)
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(512)
                .build()
                
            llmInference = LlmInference.createFromOptions(context, options)
        }
    }

    private fun copyModelFromAssetsToFilesDir(outputFile: File) {
        try {
            context.assets.open(modelFileName).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024 * 1024) // 4MB buffer
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to copy model from assets. Please put $modelFileName in app/src/main/assets/", e)
        }
    }

    suspend fun evaluateReason(appName: String, reason: String): String = withContext(Dispatchers.IO) {
        val llm = llmInference ?: return@withContext "DECISION: DENY\nModel not loaded. Please try again."
        
        val systemContext = buildSystemContext(includeCoreInstructions = true)
        val personalityPrompt = getActivePersona().prompt
        val fullPrompt = "$systemContext\nPersona Instructions: $personalityPrompt\n\nApp: $appName\nReason: $reason"
        
        try {
            llm.generateResponse(fullPrompt).trim()
        } catch (e: Exception) {
            "DECISION: DENY\nAn error occurred during evaluation."
        }
    }

    suspend fun chat(message: String): String = withContext(Dispatchers.IO) {
        val llm = llmInference ?: return@withContext "Model not loaded."
        
        val systemContext = buildSystemContext(includeCoreInstructions = false)
        val activePersona = getActivePersona()
        val personalityPrompt = activePersona.prompt
        
        // Contextually aware chat
        val fullPrompt = "$systemContext\nPersona Instructions: $personalityPrompt\n\nUser: $message\n${activePersona.name}:"
        
        try {
            llm.generateResponse(fullPrompt).trim()
        } catch (e: Exception) {
            "Error generating response."
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null
    }
}
