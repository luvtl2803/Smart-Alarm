package com.anhq.smartalarm.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.theme.body2
import com.anhq.smartalarm.core.designsystem.theme.body4
import com.anhq.smartalarm.core.designsystem.theme.title3
import com.anhq.smartalarm.core.model.AlarmGameType

@Composable
fun GameTypeSelector(
    selectedGameType: AlarmGameType,
    onGameTypeSelected: (AlarmGameType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.game_type),
                style = title3,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    GameTypeItem(
                        gameType = AlarmGameType.NONE,
                        isSelected = AlarmGameType.NONE == selectedGameType,
                        onClick = { onGameTypeSelected(AlarmGameType.NONE) }
                    )
                }
                
                items(AlarmGameType.entries.size - 1) { index ->
                    val gameType = AlarmGameType.entries.filter { it != AlarmGameType.NONE }[index]
                    GameTypeItem(
                        gameType = gameType,
                        isSelected = gameType == selectedGameType,
                        onClick = { onGameTypeSelected(gameType) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameTypeItem(
    gameType: AlarmGameType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Image(
                painter = painterResource(getGameTypeImage(gameType)),
                contentDescription = getGameTypeText(gameType),
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        Text(
            text = getGameTypeText(gameType),
            style = body4,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

private fun getGameTypeText(type: AlarmGameType): String {
    return when (type) {
        AlarmGameType.NONE -> "Không có"
        AlarmGameType.MATH_PROBLEM -> "Giải toán"
        AlarmGameType.WORD_PUZZLE -> "Tìm từ"
        AlarmGameType.MEMORY_CARD -> "Lật thẻ"
        AlarmGameType.MEMORY_TILES -> "Ghi nhớ ô"
        AlarmGameType.SHAKE_PHONE -> "Lắc điện thoại"
    }
}

private fun getGameTypeImage(type: AlarmGameType): Int {
    return when (type) {
        AlarmGameType.NONE -> R.drawable.img_game_none
        AlarmGameType.MATH_PROBLEM -> R.drawable.img_game_math
        AlarmGameType.WORD_PUZZLE -> R.drawable.img_game_word
        AlarmGameType.MEMORY_CARD -> R.drawable.img_game_memory_card
        AlarmGameType.MEMORY_TILES -> R.drawable.img_game_memory_tiles
        AlarmGameType.SHAKE_PHONE -> R.drawable.img_game_shake
    }
} 