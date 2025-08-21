package com.example.storyteller.ui.joke

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storyteller.R
import com.example.storyteller.ui.state.JokeUiState
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(text: String, style: androidx.compose.ui.text.TextStyle) {
    var displayedText by remember(text) { mutableStateOf("") }

    LaunchedEffect(text) {
        displayedText = ""
        text.forEach { char ->
            displayedText += char
            delay(50) // Delay between each character
        }
    }

    Text(
        text = displayedText,
        style = style,
        textAlign = TextAlign.Center
    )
}

@Composable
fun FunnyLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = "Loading...",
        modifier = Modifier
            .size(48.dp)
            .rotate(rotation),
        tint = MaterialTheme.colorScheme.primary
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun JokeScreen(
    jokeViewModel: JokeViewModel = viewModel()
) {
    val uiState by jokeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (jokeViewModel.uiState.value is JokeUiState.Initial) {
            jokeViewModel.getNewJoke()
        }
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "joke content"
                ) {
                    state ->
                    when (state) {
                        is JokeUiState.Loading -> FunnyLoadingIndicator()
                        is JokeUiState.Success -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                TypewriterText(
                                    text = state.englishJoke,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(modifier = Modifier.padding(horizontal = 32.dp))
                                Spacer(modifier = Modifier.height(16.dp))

                                val (mainTranslation, punishment) = remember(state.chineseTranslation) {
                                    state.chineseTranslation.split("\n\n", limit = 2)
                                        .let { it.getOrElse(0) { "" } to it.getOrElse(1) { null } }
                                }

                                TypewriterText(
                                    text = mainTranslation,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                AnimatedVisibility(
                                    visible = punishment != null,
                                    enter = slideInVertically { it } + fadeIn(),
                                ) {
                                    if (punishment != null) {
                                        Text(
                                            text = punishment,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        is JokeUiState.Error -> Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        is JokeUiState.Initial -> Text(stringResource(R.string.welcome_message))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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