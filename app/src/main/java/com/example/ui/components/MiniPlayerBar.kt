package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SongEntity
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjBorderOutline
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjDeepSurface
import com.example.ui.theme.DjElectricCyan
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjNeonEmerald
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary

@Composable
fun MiniPlayerBar(
    currentSong: SongEntity,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    visualizerBands: List<Float>,
    isMasterMuted: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onMuteToggle: () -> Unit,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val coverColor = try {
        Color(android.graphics.Color.parseColor(currentSong.coverColorHex))
    } catch (_: Exception) {
        DjAmberGold
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DjElevatedCard)
            .border(1.dp, DjBorderOutline, RoundedCornerShape(16.dp))
            .clickable { onExpandClick() }
    ) {
        // Track progress bar on top edge of mini player
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = DjNeonEmerald,
            trackColor = DjDeepSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mini Deck Disc / Visualizer icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(coverColor.copy(alpha = 0.25f))
                    .border(1.dp, coverColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    DJVisualizer(
                        bands = visualizerBands.take(5),
                        height = 24.dp,
                        barWidth = 3.dp,
                        spacing = 2.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(coverColor)
                    )
                }
            }

            // Song Title & Artist
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = currentSong.title,
                        color = DjTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // BPM Tag
                    Text(
                        text = "${currentSong.bpm} BPM",
                        color = DjAmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = currentSong.artist,
                        color = DjTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "• ${formatDuration(currentPositionMs)} / ${formatDuration(durationMs)}",
                        color = DjElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Master Kill / Mute switch (essential DJ tool)
            IconButton(
                onClick = onMuteToggle,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isMasterMuted) DjCrimsonCue else DjDeepSurface)
            ) {
                Icon(
                    imageVector = if (isMasterMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Mute",
                    tint = if (isMasterMuted) Color.White else DjTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Play / Pause button
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) DjCrimsonCue else DjAmberGold)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Next button
            IconButton(
                onClick = onNextClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DjDeepSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = DjTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
