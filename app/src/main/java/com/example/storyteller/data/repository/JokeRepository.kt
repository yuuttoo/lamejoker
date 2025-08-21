package com.example.storyteller.data.repository

import android.util.Log
import com.example.storyteller.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class JokeRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    suspend fun getJoke(prompt: String): Result<String> {
        return try {
            // Add a random parameter to avoid API caching
            val randomParam = (0..999999).random()
            val enhancedPrompt = "$prompt\n\nRandom parameter: $randomParam"
            
            Log.d("JokeRepository", "Making API call with random param: $randomParam")
            
            val response = generativeModel.generateContent(
                content {
                    text(enhancedPrompt)
                }
            )
            val text = response.text
            if (text != null) {
                Log.d("JokeRepository", "API response received, length: ${text.length}")
                Result.success(text)
            } else {
                Log.e("JokeRepository", "API response text is null")
                Result.failure(Exception("Response text is null"))
            }
        } catch (e: Exception) {
            Log.e("JokeRepository", "API call failed: ${e.message}")
            Result.failure(e)
        }
    }
}