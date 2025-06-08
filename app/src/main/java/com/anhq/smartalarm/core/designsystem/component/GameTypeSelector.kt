package com.anhq.smartalarm.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.theme.body4
import com.anhq.smartalarm.core.designsystem.theme.body5
import com.anhq.smartalarm.core.designsystem.theme.label2
import com.anhq.smartalarm.core.model.AlarmGameType

@Composable
fun GameTypeSelector(
    selectedGameType: AlarmGameType,
    onGameTypeSelected: (AlarmGameType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "dropdown_arrow"
    )

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
                style = MaterialTheme.typography.label2,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Header showing selected game type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(getGameTypeIcon(selectedGameType)),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = getGameTypeText(selectedGameType),
                        style = MaterialTheme.typography.body4
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(rotationState),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expandable content
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    AlarmGameType.entries.forEach { type ->
                        if (type != selectedGameType) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onGameTypeSelected(type)
                                        expanded = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(getGameTypeIcon(type)),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = getGameTypeText(type),
                                    style = MaterialTheme.typography.body4,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getGameTypeText(type: AlarmGameType): String {
    return when (type) {
        AlarmGameType.NONE -> "Không có thử thách"
        AlarmGameType.MATH_PROBLEM -> "Giải toán"
        AlarmGameType.WORD_PUZZLE -> "Tìm từ"
        AlarmGameType.MEMORY_CARD -> "Lật thẻ ghi nhớ"
        AlarmGameType.MEMORY_TILES -> "Ghi nhớ ô"
        AlarmGameType.SHAKE_PHONE -> "Lắc điện thoại"
    }
}

private fun getGameTypeIcon(type: AlarmGameType): Int {
    return when (type) {
        AlarmGameType.NONE -> R.drawable.ic_noti_off
        AlarmGameType.MATH_PROBLEM -> R.drawable.ic_noti_off
        AlarmGameType.WORD_PUZZLE -> R.drawable.ic_noti_off
        AlarmGameType.MEMORY_CARD -> R.drawable.ic_noti_off
        AlarmGameType.MEMORY_TILES -> R.drawable.ic_noti_off
        AlarmGameType.SHAKE_PHONE -> R.drawable.ic_noti_off
    }
} 