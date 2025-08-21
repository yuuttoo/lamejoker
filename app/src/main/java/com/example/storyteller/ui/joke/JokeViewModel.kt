package com.example.storyteller.ui.joke

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyteller.data.repository.JokeRepository
import com.example.storyteller.ui.state.JokeUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JokeViewModel(private val jokeRepository: JokeRepository = JokeRepository()) : ViewModel() {

    private val _uiState: MutableStateFlow<JokeUiState> = MutableStateFlow(JokeUiState.Initial)
    val uiState: StateFlow<JokeUiState> = _uiState.asStateFlow()

    private var addPunishment = false
    
    // Add joke history to avoid duplicates
    private val jokeHistory = mutableSetOf<String>()
    private val maxHistorySize = 50

    fun getNewJoke() {
        _uiState.value = JokeUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val nonce = System.currentTimeMillis() // Add a unique element to the prompt
            val randomSeed = (0..1000).random() // Add random seed for more variety
            val timestamp = System.currentTimeMillis()
            val sessionId = (0..999999).random() // Add session-specific randomness
            
            // Add different joke themes and categories for more variety
            val jokeThemes = listOf(
                "animals", "food", "work", "school", "technology", "relationships", 
                "travel", "sports", "music", "movies", "books", "science", "history",
                "politics", "weather", "shopping", "cooking", "gardening", "fitness"
            )
            val randomTheme = jokeThemes.random()
            val randomTheme2 = jokeThemes.random()
            
            Log.d("JokeViewModel", "Getting new joke with Theme1: $randomTheme, Theme2: $randomTheme2, Session: $sessionId")
            
            // Create more varied prompts with different structures and themes
            val promptVariations = listOf(
                "Tell me a short, clean joke about $randomTheme in English. Then, on a new line, provide a Traditional Chinese (繁體中文) translation separated by '---'. Make it unique and different from previous jokes. Request ID: $nonce, Seed: $randomSeed, Session: $sessionId, Theme: $randomTheme",
                "Give me a fresh joke about $randomTheme2 in English that I haven't heard before. Then translate it to Traditional Chinese (繁體中文) on a new line separated by '---'. Request ID: $nonce, Timestamp: $timestamp, Session: $sessionId, Theme: $randomTheme2",
                "Create a new, original joke in English about $randomTheme. Then provide Traditional Chinese (繁體中文) translation below separated by '---'. This should be completely different from any previous jokes. Request ID: $nonce, Random: $randomSeed, Session: $sessionId, Theme: $randomTheme",
                "Share a unique joke in English about $randomTheme2 that's creative and original. Then provide Traditional Chinese (繁體中文) below separated by '---'. Request ID: $nonce, Variation: ${randomSeed % 5}, Session: $sessionId, Theme: $randomTheme2",
                "Come up with a brand new joke in English about $randomTheme that's witty and clever. Then provide Traditional Chinese (繁體中文) translation separated by '---'. Request ID: $nonce, Style: ${randomSeed % 3}, Session: $sessionId, Theme: $randomTheme",
                "Invent a completely original joke in English about $randomTheme2 that I've never seen anywhere else. Then translate it to Traditional Chinese (繁體中文) below separated by '---'. Request ID: $nonce, Creativity: $timestamp, Session: $sessionId, Theme: $randomTheme2"
            )
            
            val randomPrompt = promptVariations.random()
            Log.d("JokeViewModel", "Selected prompt variation with theme: $randomTheme")
            
            try {
                // Force a small delay to ensure different timestamps
                kotlinx.coroutines.delay(100)
                
                val result = jokeRepository.getJoke(randomPrompt)
                
                if (result.isSuccess) {
                    val responseText = result.getOrNull() ?: throw Exception("Empty response")
                    val parts = responseText.split("---", limit = 2)
                    var englishJoke = parts.getOrNull(0)?.trim() ?: responseText
                    var chineseTranslation = parts.getOrNull(1)?.trim() ?: ""

                    Log.d("JokeViewModel", "Got joke: ${englishJoke.take(50)}... (History size: ${jokeHistory.size})")

                    // Check if this joke is already in history
                    if (jokeHistory.contains(englishJoke)) {
                        Log.d("JokeViewModel", "Duplicate joke detected, retrying...")
                        // If duplicate detected, try one more time with a completely different prompt
                        val retryPrompt = "Generate a completely different joke in English about $randomTheme2. Make it about a different topic or theme. Then provide Traditional Chinese (繁體中文) translation separated by '---'. This must be different from: $englishJoke. Request ID: ${System.currentTimeMillis()}, Retry: true, Theme: $randomTheme2"
                        val retryResult = jokeRepository.getJoke(retryPrompt)
                        
                        if (retryResult.isSuccess) {
                            val retryResponse = retryResult.getOrNull() ?: throw Exception("Empty retry response")
                            val retryParts = retryResponse.split("---", limit = 2)
                            val retryEnglishJoke = retryParts.getOrNull(0)?.trim() ?: retryResponse
                            val retryChineseTranslation = retryParts.getOrNull(1)?.trim() ?: ""
                            
                            Log.d("JokeViewModel", "Retry joke: ${retryEnglishJoke.take(50)}...")
                            
                            // Use retry result if it's different
                            if (retryEnglishJoke != englishJoke) {
                                englishJoke = retryEnglishJoke
                                chineseTranslation = retryChineseTranslation
                                Log.d("JokeViewModel", "Successfully got different joke on retry")
                            } else {
                                Log.d("JokeViewModel", "Retry still returned same joke, clearing history")
                                jokeHistory.clear() // Force clear history if retry fails
                            }
                        }
                    }

                    // Add to history and limit size
                    jokeHistory.add(englishJoke)
                    if (jokeHistory.size > maxHistorySize) {
                        jokeHistory.clear() // Reset history if it gets too large
                    }

                    if (addPunishment) {
                        val punishment = "\n\n" + "哈".repeat(20) + "😂".repeat(10)
                        chineseTranslation += punishment
                        addPunishment = false
                    }
                    
                    _uiState.value = JokeUiState.Success(englishJoke, chineseTranslation)
                } else {
                    val errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                    _uiState.value = JokeUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = JokeUiState.Error(e.localizedMessage ?: "Unknown error")
            } finally {
                // Always reset addPunishment after each getNewJoke call
                addPunishment = false
            }
        }
    }

    fun onNotFunnyClicked() {
        Log.d("JokeViewModel", "onNotFunnyClicked called")
        addPunishment = true
    }
    
    // Add method to manually clear joke history
    fun clearJokeHistory() {
        jokeHistory.clear()
        Log.d("JokeViewModel", "Joke history cleared")
    }
    
    // Add method to get current history size for debugging
    fun getJokeHistorySize(): Int = jokeHistory.size
}