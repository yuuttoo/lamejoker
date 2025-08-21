package com.example.storyteller.ui.joke

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storyteller.R
import com.example.storyteller.ui.state.JokeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JokeScreen(
    jokeViewModel: JokeViewModel = viewModel()
) {
    val uiState by jokeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        jokeViewModel.getNewJoke()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) }
            )
        }
    ) {
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is JokeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 24.dp))
                }
                is JokeUiState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = state.englishJoke,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.chineseTranslation,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is JokeUiState.Error -> {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
                is JokeUiState.Initial -> {
                    Text(
                        text = stringResource(R.string.welcome_message),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Use weight on Spacer to push buttons down

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { 
                    jokeViewModel.onNotFunnyClicked()
                    jokeViewModel.getNewJoke()
                }) {
                    Text(text = stringResource(R.string.not_funny_button))
                }
                Button(onClick = { jokeViewModel.getNewJoke() }) {
                    Text(text = stringResource(R.string.new_joke_button))
                }
            }
        }
    }
}