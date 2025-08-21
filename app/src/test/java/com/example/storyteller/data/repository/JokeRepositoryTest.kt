package com.example.storyteller.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class JokeRepositoryTest {

    private lateinit var repository: JokeRepository
    private lateinit var mockGenerativeModel: GenerativeModel
    private lateinit var mockResponse: com.google.ai.client.generativeai.GenerateContentResponse

    @Before
    fun setUp() {
        // Mock the GenerativeModel constructor
        mockkStatic("com.google.ai.client.generativeai.GenerativeModel")
        
        mockGenerativeModel = mockk()
        mockResponse = mockk()
        
        // Mock the GenerativeModel constructor to return our mock
        every { 
            GenerativeModel(
                modelName = any(),
                apiKey = any()
            ) 
        } returns mockGenerativeModel
        
        repository = JokeRepository()
    }

    @Test
    fun `getJoke success returns success result`() = runTest {
        // Given
        val prompt = "Tell me a joke"
        val expectedResponse = "Why don't scientists trust atoms? Because they make up everything!"
        
        coEvery { 
            mockGenerativeModel.generateContent(any()) 
        } returns mockResponse
        
        every { mockResponse.text } returns expectedResponse
        
        // When
        val result = repository.getJoke(prompt)
        
        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Response should match expected", expectedResponse, result.getOrNull())
        
        // Verify the model was called with the correct prompt
        coVerify { mockGenerativeModel.generateContent(any()) }
    }

    @Test
    fun `getJoke failure returns failure result`() = runTest {
        // Given
        val prompt = "Tell me a joke"
        val exception = Exception("API Error")
        
        coEvery { 
            mockGenerativeModel.generateContent(any()) 
        } throws exception
        
        // When
        val result = repository.getJoke(prompt)
        
        // Then
        assertFalse("Result should be failure", result.isSuccess)
        assertEquals("Exception should match", exception, result.exceptionOrNull())
        
        // Verify the model was called
        coVerify { mockGenerativeModel.generateContent(any()) }
    }

    @Test
    fun `getJoke with null response returns failure result`() = runTest {
        // Given
        val prompt = "Tell me a joke"
        
        coEvery { 
            mockGenerativeModel.generateContent(any()) 
        } returns mockResponse
        
        every { mockResponse.text } returns null
        
        // When
        val result = repository.getJoke(prompt)
        
        // Then
        assertFalse("Result should be failure", result.isSuccess)
        assertTrue("Should contain null response error", 
            result.exceptionOrNull()?.message?.contains("Response text is null") == true)
        
        // Verify the model was called
        coVerify { mockGenerativeModel.generateContent(any()) }
    }

    @Test
    fun `getJoke adds random parameter to prompt`() = runTest {
        // Given
        val originalPrompt = "Tell me a joke"
        
        coEvery { 
            mockGenerativeModel.generateContent(any()) 
        } returns mockResponse
        
        every { mockResponse.text } returns "Test response"
        
        // When
        repository.getJoke(originalPrompt)
        
        // Then
        // Verify that the model was called with an enhanced prompt
        coVerify { 
            mockGenerativeModel.generateContent(match { 
                it.text.contains(originalPrompt) && it.text.contains("Random parameter:")
            }) 
        }
    }

    @Test
    fun `getJoke handles different prompt types`() = runTest {
        // Given
        val prompts = listOf(
            "Short joke",
            "Long detailed joke with specific requirements",
            "Joke about technology",
            "Clean family joke"
        )
        
        coEvery { 
            mockGenerativeModel.generateContent(any()) 
        } returns mockResponse
        
        every { mockResponse.text } returns "Test response"
        
        // When & Then
        prompts.forEach { prompt ->
            val result = repository.getJoke(prompt)
            assertTrue("Result should be success for prompt: $prompt", result.isSuccess)
        }
        
        // Verify the model was called for each prompt
        coVerify(exactly = prompts.size) { mockGenerativeModel.generateContent(any()) }
    }
} 