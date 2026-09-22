package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjElectricCyan
import com.example.ui.theme.DjNeonEmerald

@Composable
fun DJVisualizer(
    bands: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    barWidth: Dp = 6.dp,
    spacing: Dp = 4.dp
) {
    Row(
        modifier = modifier
            .height(height)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        bands.forEachIndexed { index, amplitude ->
            val animatedHeight by animateFloatAsState(
                targetValue = amplitude.coerceIn(0.08f, 1.0f),
                animationSpec = tween(durationMillis = 90),
                label = "vis_band_$index"
            )

            val barBrush = Brush.verticalGradient(
                colors = listOf(
                    DjCrimsonCue,
                    DjAmberGold,
                    DjElectricCyan,
                    DjNeonEmerald
                )
            )

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(animatedHeight)
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    .background(barBrush)
            )
        }
    }
}
