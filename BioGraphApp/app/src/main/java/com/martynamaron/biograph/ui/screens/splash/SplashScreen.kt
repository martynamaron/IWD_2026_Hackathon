package com.martynamaron.biograph.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.martynamaron.biograph.R
import com.martynamaron.biograph.ui.theme.GreenDarkest
import com.martynamaron.biograph.ui.theme.GreenLightest
import com.martynamaron.biograph.ui.theme.GreenMid
import com.martynamaron.biograph.ui.theme.SyneFontFamily
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.3f) }
    val logoRotation = remember { Animatable(-15f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(30f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val subtitleOffsetY = remember { Animatable(20f) }
    val glowAlpha = remember { Animatable(0f) }

    // Gentle floating animation for the logo
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    // Slow rotating glow
    val glowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowRotation"
    )

    LaunchedEffect(Unit) {
        coroutineScope {
            // Glow appears first
            launch {
                glowAlpha.animateTo(
                    targetValue = 0.6f,
                    animationSpec = tween(600, easing = EaseOutCubic)
                )
            }
            // Logo springs in with overshoot
            launch {
                delay(200)
                logoAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(700, easing = FastOutSlowInEasing)
                )
            }
            launch {
                delay(200)
                logoScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(900, easing = EaseOutBack)
                )
            }
            launch {
                delay(200)
                logoRotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(900, easing = EaseOutBack)
                )
            }
            // Title slides up and fades in
            launch {
                delay(600)
                titleAlpha.animateTo(1f, tween(500, easing = EaseOutCubic))
            }
            launch {
                delay(600)
                titleOffsetY.animateTo(0f, tween(500, easing = EaseOutCubic))
            }
            // Subtitle follows
            launch {
                delay(850)
                subtitleAlpha.animateTo(1f, tween(500, easing = EaseOutCubic))
            }
            launch {
                delay(850)
                subtitleOffsetY.animateTo(0f, tween(500, easing = EaseOutCubic))
            }
        }
        delay(900)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Animated radial glow behind the logo
        Canvas(
            modifier = Modifier
                .size(300.dp)
                .alpha(glowAlpha.value)
                .rotate(glowRotation)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            // Soft green radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GreenLightest.copy(alpha = 0.4f),
                        GreenMid.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            // Small orbiting dots
            for (i in 0..2) {
                val angle = Math.toRadians((i * 120).toDouble())
                val dotRadius = radius * 0.65f
                val dotX = center.x + dotRadius * cos(angle).toFloat()
                val dotY = center.y + dotRadius * sin(angle).toFloat()
                drawCircle(
                    color = GreenLightest.copy(alpha = 0.5f),
                    radius = 6.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.black_logo),
                contentDescription = "BioGraph logo",
                modifier = Modifier
                    .size(160.dp)
                    .offset(y = floatOffset.dp)
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value)
                    .rotate(logoRotation.value)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "BioGraph",
                fontFamily = SyneFontFamily,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDarkest,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffsetY.value.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Track your life, discover patterns",
                fontFamily = SyneFontFamily,
                fontSize = 16.sp,
                color = GreenMid,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(subtitleAlpha.value)
                    .offset(y = subtitleOffsetY.value.dp)
            )
        }
    }
}
