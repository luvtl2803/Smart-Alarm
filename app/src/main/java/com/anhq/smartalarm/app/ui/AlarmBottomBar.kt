package com.anhq.smartalarm.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anhq.smartalarm.app.navigation.TopLevelDestination
import com.anhq.smartalarm.core.designsystem.theme.Pure01
import com.anhq.smartalarm.core.designsystem.theme.Pure02
import com.anhq.smartalarm.core.designsystem.theme.label3

@Composable
fun NanaBottomBar(
    currentDestination: TopLevelDestination?,
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit
) {
    BottomAppBar(
        Modifier.clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
    ) {
        destinations.forEach { destination ->
            val selected = destination == currentDestination
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    if (selected) {
                        Icon(
                            painter = painterResource(id = destination.iconId),
                            contentDescription = stringResource(id = destination.labelId),
                            Modifier
                                .graphicsLayer(alpha = 0.99f)
                                .drawWithCache {
                                    val gradient = Brush.horizontalGradient(
                                        listOf(Pure01, Pure02)
                                    )
                                    onDrawWithContent {
                                        drawContent()
                                        drawRect(gradient, blendMode = BlendMode.SrcAtop)
                                    }
                                }
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = destination.iconId),
                            contentDescription = stringResource(id = destination.labelId),

                        )
                    }
                },
                label = {
                    if (selected)
                        Text(
                            text = stringResource(id = destination.labelId),
                            style = MaterialTheme.typography.label3
                        )
                    else
                        Text(
                            text = stringResource(id = destination.labelId),
                            style = MaterialTheme.typography.label3
                        )
                }
            )
        }
    }
}


@Preview
@Composable
private fun AlarmBottomBarPreview() {
    NanaBottomBar(
        currentDestination = TopLevelDestination.ALARM,
        destinations = TopLevelDestination.entries,
        onNavigateToDestination = {}
    )
}