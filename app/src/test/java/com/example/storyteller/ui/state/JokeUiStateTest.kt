package com.example.storyteller.ui.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JokeUiStateTest {

    @Test
    fun `Initial state properties`() {
        // Given
        val initialState = JokeUiState.Initial
        
        // Then
        assertTrue("Initial state should be Initial", initialState is JokeUiState.Initial)
    }

    @Test
    fun `Loading state properties`() {
        // Given
        val loadingState = JokeUiState.Loading
        
        // Then
        assertTrue("Loading state should be Loading", loadingState is JokeUiState.Loading)
    }

    @Test
    fun `Success state properties`() {
        // Given
        val englishJoke = "Why don't scientists trust atoms?"
        val chineseTranslation = "為什麼科學家不相信原子？"
        val successState = JokeUiState.Success(englishJoke, chineseTranslation)
        
        // Then
        assertTrue("Success state should be Success", successState is JokeUiState.Success)
        assertEquals("English joke should match", englishJoke, successState.englishJoke)
        assertEquals("Chinese translation should match", chineseTranslation, successState.chineseTranslation)
    }

    @Test
    fun `Error state properties`() {
        // Given
        val errorMessage = "Network error occurred"
        val errorState = JokeUiState.Error(errorMessage)
        
        // Then
        assertTrue("Error state should be Error", errorState is JokeUiState.Error)
        assertEquals("Error message should match", errorMessage, errorState.errorMessage)
    }

    @Test
    fun `Success state data class behavior`() {
        // Given
        val joke1 = JokeUiState.Success("Joke 1", "笑話 1")
        val joke2 = JokeUiState.Success("Joke 1", "笑話 1")
        val joke3 = JokeUiState.Success("Joke 2", "笑話 2")
        
        // Then
        assertEquals("Equal jokes should be equal", joke1, joke2)
        assertFalse("Different jokes should not be equal", joke1 == joke3)
        assertEquals("Hash codes should be equal for equal objects", joke1.hashCode(), joke2.hashCode())
    }

    @Test
    fun `Error state data class behavior`() {
        // Given
        val error1 = JokeUiState.Error("Error message")
        val error2 = JokeUiState.Error("Error message")
        val error3 = JokeUiState.Error("Different error")
        
        // Then
        assertEquals("Equal errors should be equal", error1, error2)
        assertFalse("Different errors should not be equal", error1 == error3)
        assertEquals("Hash codes should be equal for equal objects", error1.hashCode(), error2.hashCode())
    }

    @Test
    fun `State type checking`() {
        // Given
        val states = listOf(
            JokeUiState.Initial,
            JokeUiState.Loading,
            JokeUiState.Success("Test", "測試"),
            JokeUiState.Error("Test error")
        )
        
        // Then
        assertTrue("Should have 4 different state types", states.distinct().size == 4)
        assertTrue("Should contain Initial state", states.any { it is JokeUiState.Initial })
        assertTrue("Should contain Loading state", states.any { it is JokeUiState.Loading })
        assertTrue("Should contain Success state", states.any { it is JokeUiState.Success })
        assertTrue("Should contain Error state", states.any { it is JokeUiState.Error })
    }

    @Test
    fun `Success state with empty strings`() {
        // Given
        val successState = JokeUiState.Success("", "")
        
        // Then
        assertTrue("Should be Success state", successState is JokeUiState.Success)
        assertEquals("Empty English joke", "", successState.englishJoke)
        assertEquals("Empty Chinese translation", "", successState.chineseTranslation)
    }

    @Test
    fun `Error state with empty message`() {
        // Given
        val errorState = JokeUiState.Error("")
        
        // Then
        assertTrue("Should be Error state", errorState is JokeUiState.Error)
        assertEquals("Empty error message", "", errorState.errorMessage)
    }
} 