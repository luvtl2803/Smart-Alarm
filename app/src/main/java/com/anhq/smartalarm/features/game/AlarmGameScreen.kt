package com.anhq.smartalarm.features.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhq.smartalarm.core.designsystem.theme.title1
import com.anhq.smartalarm.core.designsystem.theme.title2
import com.anhq.smartalarm.core.game.MathProblemGame
import com.anhq.smartalarm.core.game.MemoryCardGame
import com.anhq.smartalarm.core.game.MemoryTilesGame
import com.anhq.smartalarm.core.game.ShakePhoneGame
import com.anhq.smartalarm.core.game.WordPuzzleGame
import com.anhq.smartalarm.core.model.GameDifficulty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AlarmGameScreen(
    viewModel: AlarmGameViewModel = hiltViewModel(),
    onGameComplete: () -> Unit,
    onSnoozeClick: () -> Unit
) {
    val game = viewModel.currentGame
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    LaunchedEffect(game) {
        game?.onGameComplete = {
            onGameComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = game?.title ?: "Loading...",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = game?.description ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
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
                    is MemoryCardGame -> MemoryCardGameContent(game)
                    is WordPuzzleGame -> WordPuzzleGameContent(game)
                    else -> CircularProgressIndicator()
                }
            }

            // Snooze button at bottom
            if (game != null && !game.isGameCompleted()) {

                if (viewModel.snoozeCount < uiState.maxSnoozeCount) {
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
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = answer,
            onValueChange = {
                answer = it
                showError = false
            },
            label = { Text("Nhập đáp án", color = MaterialTheme.colorScheme.onSurface) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError,
            supportingText = if (showError) {
                { Text("Sai rồi, thử lại đi!", color = MaterialTheme.colorScheme.error) }
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
            Text("Kiểm tra", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun MemoryTilesGameContent(game: MemoryTilesGame) {
    var showingSequence by remember { mutableStateOf(false) }
    var highlightedTiles by remember { mutableStateOf(emptyList<Int>()) }
    var playerTiles by remember { mutableStateOf(emptyList<Int>()) }
    var errorTile by remember { mutableStateOf<Int?>(null) }
    var isInteractionEnabled by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(500)
        showingSequence = true
        game.showSequence()
    }

    LaunchedEffect(game) {
        game.onSequenceShow = { sequence ->
            highlightedTiles = sequence
        }

        game.onSequenceComplete = {
            showingSequence = false
            highlightedTiles = emptyList()
        }

        game.onWrongTile = { wrongTileIndex ->
            isInteractionEnabled = false
            errorTile = wrongTileIndex
            scope.launch {
                delay(1000)
                errorTile = null
                isInteractionEnabled = true
            }
        }

        game.onResetProgress = {
            playerTiles = emptyList()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (showingSequence) "Xem mẫu hình" else "Lặp lại mẫu hình",
            style = MaterialTheme.typography.title1,
            color = MaterialTheme.colorScheme.onBackground
        )

//        Text(
//            text = "${game.getCorrectTiles()}/${game.getTotalTiles()}",
//            style = MaterialTheme.typography.title1
//        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(game.getGridSize()),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(game.getGridSize() * game.getGridSize()) { index ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            when {
                                errorTile == index -> MaterialTheme.colorScheme.error
                                highlightedTiles.contains(index) -> MaterialTheme.colorScheme.primary
                                playerTiles.contains(index) -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable(
                            enabled = !showingSequence && isInteractionEnabled
                        ) {
                            playerTiles = playerTiles + index
                            game.selectTile(index)
                        }
                )
            }
        }

        if (!showingSequence) {
            Button(
                onClick = {
                    showingSequence = true
                    game.reset()
                    game.showSequence()
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Phát lại", color = MaterialTheme.colorScheme.onPrimary)
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
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
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

@Composable
fun MemoryCardGameContent(game: MemoryCardGame) {
    var flippedPositions by remember { mutableStateOf(emptyList<Int>()) }
    var isInteractionEnabled by remember { mutableStateOf(true) }
    var cards by remember { mutableStateOf(game.getCards()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(game) {
        game.onCardFlip = { position ->
            flippedPositions = flippedPositions + position
            cards = game.getCards()
            
            // Chỉ vô hiệu hóa tương tác sau khi đã lật 2 thẻ
            if (flippedPositions.size >= 2) {
                isInteractionEnabled = false
            }
        }

        game.onPairMatched = {
            scope.launch {
                delay(500) // Cho phép người chơi thấy cặp thẻ khớp
                cards = game.getCards()
                flippedPositions = emptyList()
                isInteractionEnabled = true
            }
        }

        game.onPairMismatched = {
            scope.launch {
                delay(1000) // Cho người chơi thấy cặp thẻ không khớp
                cards = game.getCards()
                flippedPositions = emptyList()
                isInteractionEnabled = true
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tìm các cặp thẻ giống nhau",
            style = MaterialTheme.typography.title1,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "${game.getMatchedPairs()}/${game.getTotalPairs()} cặp",
            style = MaterialTheme.typography.title2,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(game.getGridSize()),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards.size) { position ->
                val card = cards[position]
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            when {
                                card.isMatched -> MaterialTheme.colorScheme.primary
                                card.isFlipped || flippedPositions.contains(position) ->
                                    MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable(
                            enabled = isInteractionEnabled &&
                                    !card.isMatched &&
                                    !flippedPositions.contains(position) &&
                                    flippedPositions.size < 2 // Chỉ cho phép lật tối đa 2 thẻ
                        ) {
                            game.flipCard(position)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (card.isMatched || card.isFlipped || flippedPositions.contains(position)) {
                        Text(
                            text = card.pairId.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                game.reset()
                cards = game.getCards()
                flippedPositions = emptyList()
                isInteractionEnabled = true
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Bắt đầu lại", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun WordPuzzleGameContent(game: WordPuzzleGame) {
    var answer by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Từ cần giải mã:",
            style = MaterialTheme.typography.title2,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = game.getCurrentScrambledWord(),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Số lần thử còn lại: ${game.getAttemptsLeft()}/${game.getMaxAttempts()}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (showHint) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Gợi ý: ${game.getHint()}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = answer,
            onValueChange = {
                answer = it
                showError = false
            },
            label = { Text("Nhập đáp án", color = MaterialTheme.colorScheme.onSurface) },
            isError = showError,
            supportingText = if (showError) {
                { Text("Sai rồi, thử lại đi!", color = MaterialTheme.colorScheme.error) }
            } else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (game.checkAnswer(answer)) {
                    // Game will call completeGame() internally if answer is correct
                } else {
                    showError = true
                    answer = ""
                    if (game.getAttemptsLeft() == 0) {
                        // Game over, reset
                        game.reset()
                        showHint = false
                    }
                }
            }
        ) {
            Text("Kiểm tra", color = MaterialTheme.colorScheme.onPrimary)
        }

        if (!showHint && game.getAttemptsLeft() < game.getMaxAttempts()) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showHint = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Xem gợi ý", color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTilesGame() {
    val game = MemoryTilesGame(
        GameDifficulty.MEDIUM
    )
    MemoryTilesGameContent(
        game = game
    )
}