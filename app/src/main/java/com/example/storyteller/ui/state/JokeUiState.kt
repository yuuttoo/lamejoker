package com.example.storyteller.ui.state

sealed interface JokeUiState {
    object Initial : JokeUiState
    object Loading : JokeUiState
    data class Success(val englishJoke: String, val chineseTranslation: String) : JokeUiState
    data class Error(val errorMessage: String) : JokeUiState
}