package com.domain.mindphone.domain.llm

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MindfulLlmManager(private val context: Context) {
    private var llmInference: LlmInference? = null
    
    // Using SharedPreferences to store the custom system prompt
    private val prefs: SharedPreferences = context.getSharedPreferences("mindful_prefs", Context.MODE_PRIVATE)
    
    // Default strict persona
    private val defaultSystemPrompt = """
        You are a strict but compassionate mindfulness coach evaluating if a user should open an addictive app.
        If the user's reason is productive, intentional, or necessary (e.g., 'tutorial for work', 'replying to mom'), you MUST output EXACTLY and ONLY: "DECISION: ALLOW". Do not add any other words.
        If the reason is mindless, bored, or negative (e.g., 'I'm bored', 'scrolling'), you MUST output EXACTLY: "DECISION: DENY" on the first line, followed by a new line, and then a 1-sentence mindful alternative.
    """.trimIndent()

    fun getSystemPrompt(): String {
        return prefs.getString("system_prompt", defaultSystemPrompt) ?: defaultSystemPrompt
    }

    fun saveSystemPrompt(newPrompt: String) {
        prefs.edit { putString("system_prompt", newPrompt) }
    }

    // Switched to the ~500MB Gemma 3 1B model
    private val modelFileName = "gemma3-1b-it-int4.litertlm"

    suspend fun loadModel() = withContext(Dispatchers.IO) {
        if (llmInference == null) {
            val modelFile = File(context.filesDir, modelFileName)

            if (!modelFile.exists()) {
                copyModelFromAssetsToFilesDir(modelFile)
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(256)
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
        
        val systemPrompt = getSystemPrompt()
        val fullPrompt = "$systemPrompt\n\nApp: $appName\nReason: $reason"
        
        try {
            llm.generateResponse(fullPrompt).trim()
        } catch (e: Exception) {
            "DECISION: DENY\nAn error occurred during evaluation."
        }
    }

    suspend fun chat(message: String): String = withContext(Dispatchers.IO) {
        val llm = llmInference ?: return@withContext "Model not loaded."
        
        val systemPrompt = getSystemPrompt()
        // Simple prompt format for chatting to test the persona
        val fullPrompt = "$systemPrompt\n\nUser: $message\nCoach:"
        
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
