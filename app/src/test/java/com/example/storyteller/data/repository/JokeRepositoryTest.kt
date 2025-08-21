package com.example.storyteller.data.repository

import org.junit.Test
import org.junit.Assert.assertTrue

class JokeRepositoryTest {

    @Test
    fun `repository can be instantiated`() {
        // Given & When
        val repository = JokeRepository()
        
        // Then
        assertTrue("Repository should be instantiated", repository is JokeRepository)
    }

    @Test
    fun `repository has getJoke method`() {
        // Given
        val repository = JokeRepository()
        
        // When & Then
        // This test just verifies the method exists and can be called
        // We can't easily test the actual API call without complex mocking
        assertTrue("Repository should have getJoke method", 
            repository::class.members.any { it.name == "getJoke" })
    }
} 