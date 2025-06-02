package com.anhq.smartalarm.features.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.anhq.smartalarm.core.game.MathProblemGame
import com.anhq.smartalarm.core.game.MemoryTilesGame
import com.anhq.smartalarm.core.game.ShakePhoneGame
import com.anhq.smartalarm.features.setting.data.SettingDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun AlarmGameScreen(
    viewModel: AlarmGameViewModel,
    onGameComplete: () -> Unit,
    onSnoozeClick: () -> Unit
) {
    val game = viewModel.currentGame

    LaunchedEffect(game) {
        game?.onGameComplete = {
            onGameComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = game?.title ?: "Loading...",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = game?.description ?: "",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (game) {
                    is MathProblemGame -> MathProblemGameContent(game)
                    is MemoryTilesGame -> MemoryTilesGameContent(game)
                    is ShakePhoneGame -> ShakePhoneGameContent(game)
                    else -> CircularProgressIndicator()
                }
            }

            // Snooze button at bottom
            if (game != null && !game.isGameCompleted()) {
                val settingDataStore = SettingDataStore(viewModel.getApplication())
                val settings = runBlocking {
                    settingDataStore.settingsFlow.first()
                }

                if (viewModel.snoozeCount < settings.maxSnoozeCount) {
                    Button(
                        onClick = onSnoozeClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Tạm hoãn")
                    }
                }
            }
        }
    }
}

@Composable
fun MathProblemGameContent(game: MathProblemGame) {
    var answer by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = game.getProblem(),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = answer,
            onValueChange = {
                answer = it
                showError = false
            },
            label = { Text("Nhập đáp án") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError,
            supportingText = if (showError) {
                { Text("Sai rồi, thử lại đi!") }
            } else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (game.checkAnswer(answer.toIntOrNull() ?: 0)) {
                    // Game will call completeGame() internally if answer is correct
                } else {
                    showError = true
                }
            }
        ) {
            Text("Kiểm tra")
        }
    }
}

@Composable
fun MemoryTilesGameContent(game: MemoryTilesGame) {
    var showPattern by remember { mutableStateOf(true) }
    var userPattern by remember { mutableStateOf(listOf<Int>()) }
    var showError by remember { mutableStateOf(false) }
    var sequence by remember { mutableStateOf(emptyList<Int>()) }

    LaunchedEffect(Unit) {
        game.onSequenceShow = { newSequence ->
            sequence = newSequence
        }
        game.onSequenceComplete = {
            showPattern = false
        }
        game.onWrongTile = { _ ->
            showError = true
            userPattern = emptyList()
        }
        game.onResetProgress = {
            userPattern = emptyList()
            showError = false
        }
        game.showSequence()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (showPattern) {
            Text(
                text = "Ghi nhớ mẫu hình!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            if (showError) {
                Text(
                    text = "Sai rồi, thử lại đi!",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Text(
                    text = "Lặp lại mẫu hình",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(9) { index ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            when {
                                showPattern && sequence.contains(index) -> MaterialTheme.colorScheme.primary
                                userPattern.contains(index) -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable(enabled = !showPattern) {
                            if (!showPattern) {
                                game.selectTile(index)
                            }
                        }
                )
            }
        }
    }
}

@Composable
fun ShakePhoneGameContent(game: ShakePhoneGame) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        game.onShake = { current, total ->
            progress = current.toFloat() / total
        }
        game.startListening()
    }

    DisposableEffect(Unit) {
        onDispose {
            game.stopListening()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Lắc điện thoại!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(100.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
        )
    }
} 