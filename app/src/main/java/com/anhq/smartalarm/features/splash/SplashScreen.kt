package com.anhq.smartalarm.features.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.anhq.smartalarm.R
import com.anhq.smartalarm.features.alarm.navigation.navigateToAlarm
import com.anhq.smartalarm.features.onboarding.navigation.navigateToOnboarding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashRoute(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    SplashScreen(
        onSplashFinished = {
            if (viewModel.isFirstRun()) {
                navController.navigateToOnboarding()
            } else {
                navController.navigateToAlarm()
            }
        }
    )
}

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.clock_splash)
    )

    LaunchedEffect(key1 = true) {
        launch {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(800)
        )
        }
        launch {
            delay(400) // Delay text appearance slightly
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(800)
            )
        }
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value),
            composition = composition,
            iterations = LottieConstants.IterateForever,
        )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Báo thức thông minh",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                modifier = Modifier.alpha(alpha.value),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quản lý thời gian hiệu quả",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.alpha(alpha.value),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}