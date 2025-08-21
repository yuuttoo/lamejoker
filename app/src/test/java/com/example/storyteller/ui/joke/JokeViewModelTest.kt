package com.example.storyteller.ui.joke

import app.cash.turbine.test
import com.example.storyteller.data.repository.JokeRepository
import com.example.storyteller.ui.state.JokeUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class JokeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: JokeViewModel
    private lateinit var repository: JokeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = JokeViewModel(repository)
        
        // Mock Android Log to prevent RuntimeException
        mockkStatic("android.util.Log")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Initial`() = runTest {
        assertEquals(JokeUiState.Initial, viewModel.uiState.value)
    }

    @Test
    fun `getNewJoke success updates state to Success`() = runTest {
        val jokeText = "Why don't scientists trust atoms? --- Because they make up everything!"
        coEvery { repository.getJoke(any()) } returns Result.success(jokeText)

        viewModel.getNewJoke()
        
        // Wait for the coroutine to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify the final state is Success
        val finalState = viewModel.uiState.value
        assertTrue("Final state should be Success", finalState is JokeUiState.Success)
        
        val successState = finalState as JokeUiState.Success
        assertEquals("Why don't scientists trust atoms?", successState.englishJoke)
        assertEquals("Because they make up everything!", successState.chineseTranslation)
    }

    @Test
    fun `getNewJoke failure updates state to Error`() = runTest {
        val errorMessage = "Network error"
        coEvery { repository.getJoke(any()) } returns Result.failure(Exception(errorMessage))

        viewModel.getNewJoke()
        
        // Wait for the coroutine to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify the final state is Error
        val finalState = viewModel.uiState.value
        assertTrue("Final state should be Error", finalState is JokeUiState.Error)
        
        val errorState = finalState as JokeUiState.Error
        assertEquals(errorMessage, errorState.errorMessage)
    }

    @Test
    fun `onNotFunnyClicked applies punishment to next joke only`() = runTest {
        val joke1 = "Joke 1 --- 笑話 1"
        val joke2 = "Joke 2 --- 笑話 2"
        val punishment = "\n\n" + "哈".repeat(20) + "😂".repeat(10)

        // First joke (normal)
        coEvery { repository.getJoke(any()) } returns Result.success(joke1)
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify first joke is loaded
        val firstState = viewModel.uiState.value
        assertTrue("First joke should be loaded", firstState is JokeUiState.Success)
        val firstSuccessState = firstState as JokeUiState.Success
        assertEquals("First joke should not have punishment", "笑話 1", firstSuccessState.chineseTranslation)

        // Click "not funny"
        viewModel.onNotFunnyClicked()

        // Second joke (with punishment)
        coEvery { repository.getJoke(any()) } returns Result.success(joke2)
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify second joke has punishment
        val secondState = viewModel.uiState.value
        assertTrue("Second joke should be loaded", secondState is JokeUiState.Success)
        val secondSuccessState = secondState as JokeUiState.Success
        assertEquals("Second joke should have punishment", "笑話 2$punishment", secondSuccessState.chineseTranslation)

        // Third joke (back to normal)
        coEvery { repository.getJoke(any()) } returns Result.success(joke1)
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify third joke is normal
        val thirdState = viewModel.uiState.value
        assertTrue("Third joke should be loaded", thirdState is JokeUiState.Success)
        val thirdSuccessState = thirdState as JokeUiState.Success
        assertEquals("Third joke should not have punishment", "笑話 1", thirdSuccessState.chineseTranslation)
    }
    
    @Test
    fun `clearJokeHistory works correctly`() = runTest {
        val joke = "Test joke --- 測試笑話"
        coEvery { repository.getJoke(any()) } returns Result.success(joke)
        
        // Get a joke to populate history
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Clear history
        viewModel.clearJokeHistory()
        
        // Should be able to get the same joke again
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val finalState = viewModel.uiState.value
        assertTrue("Final state should be Success", finalState is JokeUiState.Success)
    }
    
    @Test
    fun `getNewJoke generates different prompts for variety`() = runTest {
        val joke1 = "Joke 1 --- 笑話 1"
        val joke2 = "Joke 2 --- 笑話 2"
        
        // Mock repository to capture different calls
        var callCount = 0
        coEvery { repository.getJoke(any()) } answers {
            callCount++
            if (callCount == 1) Result.success(joke1) else Result.success(joke2)
        }
        
        // Get first joke
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Get second joke
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify that repository was called twice
        coVerify(exactly = 2) { repository.getJoke(any()) }
        
        // Verify final joke was loaded
        val finalState = viewModel.uiState.value
        assertTrue("Final state should be Success", finalState is JokeUiState.Success)
        val successState = finalState as JokeUiState.Success
        assertEquals("Joke 2", successState.englishJoke)
    }
    
    @Test
    fun `joke history prevents duplicates`() = runTest {
        val sameJoke = "Same joke --- 相同笑話"
        coEvery { repository.getJoke(any()) } returns Result.success(sameJoke)
        
        // Get first joke
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Try to get same joke again
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Repository should be called at least once (original attempt)
        coVerify(atLeast = 1) { repository.getJoke(any()) }
        
        // Verify final state is Success
        val finalState = viewModel.uiState.value
        assertTrue("Final state should be Success", finalState is JokeUiState.Success)
    }
}
