package com.example.storyteller.ui.joke

import com.example.storyteller.data.repository.JokeRepository
import com.example.storyteller.ui.state.JokeUiState
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun `initial state is Initial`() {
        assertEquals(JokeUiState.Initial, viewModel.uiState.value)
    }
}
