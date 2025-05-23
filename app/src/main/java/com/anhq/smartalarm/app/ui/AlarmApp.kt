package com.anhq.smartalarm.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anhq.smartalarm.app.navigation.AlarmNavHost
import com.anhq.smartalarm.core.designsystem.theme.Neutral10

@Composable
fun AlarmApp(
    appState: AlarmAppState
) {
    val isTopLevelDestination = appState.currentTopLevelDestination != null

    Scaffold(
        bottomBar = {
            if (isTopLevelDestination) {
                NanaBottomBar(
                    destinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentTopLevelDestination
                )
            }
        }
    ) { paddingValue ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Neutral10)
            )
            Column(modifier = Modifier) {
                AlarmNavHost(
                    modifier = if (isTopLevelDestination) {
                        Modifier.padding(
                            bottom = if (paddingValue.calculateBottomPadding() > 24.dp) {
                                paddingValue.calculateBottomPadding() - 24.dp
                            } else {
                                paddingValue.calculateBottomPadding()
                            }
                        )
                    } else {
                        Modifier
                    },
                    appState = appState,
                )
            }
        }
    }
}