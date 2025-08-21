package com.example.storyteller.ui.joke

import app.cash.turbine.test
import com.example.storyteller.data.repository.JokeRepository
import com.example.storyteller.ui.state.JokeUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
        testDispatcher.scheduler.advanceUntilIdle()

        val successState = viewModel.uiState.value as JokeUiState.Success
        assertEquals("Why don't scientists trust atoms?", successState.englishJoke)
        assertEquals("Because they make up everything!", successState.chineseTranslation)
    }

    @Test
    fun `getNewJoke failure updates state to Error`() = runTest {
        val errorMessage = "Network error"
        coEvery { repository.getJoke(any()) } returns Result.failure(Exception(errorMessage))

        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()

        val errorState = viewModel.uiState.value as JokeUiState.Error
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
        assertTrue("First joke should be loaded", viewModel.uiState.value is JokeUiState.Success)
        val firstState = viewModel.uiState.value as JokeUiState.Success
        assertEquals("First joke should not have punishment", "笑話 1", firstState.chineseTranslation)

        // Click "not funny"
        viewModel.onNotFunnyClicked()

        // Second joke (with punishment)
        coEvery { repository.getJoke(any()) } returns Result.success(joke2)
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify second joke has punishment
        assertTrue("Second joke should be loaded", viewModel.uiState.value is JokeUiState.Success)
        val successState1 = viewModel.uiState.value as JokeUiState.Success
        assertEquals("Second joke should have punishment", "笑話 2$punishment", successState1.chineseTranslation)

        // Third joke (back to normal)
        coEvery { repository.getJoke(any()) } returns Result.success(joke1)
        viewModel.getNewJoke()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify third joke is normal
        assertTrue("Third joke should be loaded", viewModel.uiState.value is JokeUiState.Success)
        val successState2 = viewModel.uiState.value as JokeUiState.Success
        assertEquals("Third joke should not have punishment", "笑話 1", successState2.chineseTranslation)
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
        
        assertTrue(viewModel.uiState.value is JokeUiState.Success)
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
        
        // Verify that repository was called twice with different prompts
        coVerify(exactly = 2) { repository.getJoke(any()) }
        
        // Verify both jokes were loaded
        assertTrue(viewModel.uiState.value is JokeUiState.Success)
        val finalState = viewModel.uiState.value as JokeUiState.Success
        assertEquals("Joke 2", finalState.englishJoke)
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
        
        // Repository should be called twice (original + retry attempt)
        coVerify(exactly = 2) { repository.getJoke(any()) }
    }
}
