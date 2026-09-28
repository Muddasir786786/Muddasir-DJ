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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SmartDisplay
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.SongEntity
import com.example.data.youtube.YouTubeVideoItem
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjBorderOutline
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjDeepSurface
import com.example.ui.theme.DjElectricCyan
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjNeonEmerald
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary

/**
 * Mini Player Bar for active YouTube video streams.
 * Displays thumbnail, video title, channel, Play/Pause button, Next button, and Close/Stop button.
 * Tapping the bar expands the full official YouTube player.
 */
@Composable
fun YouTubeMiniPlayerBar(
    video: YouTubeVideoItem,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onCloseClick: () -> Unit,
    onExpandClick: () -> Unit,
    onNextClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DjElevatedCard)
            .border(1.dp, DjCrimsonCue.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable { onExpandClick() }
    ) {
        // Active streaming progress / glow indicator
        LinearProgressIndicator(
            progress = { 1f },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = if (isPlaying) DjCrimsonCue else DjAmberGold,
            trackColor = DjDeepSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Thumbnail image with YouTube badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DjDeepSurface)
                    .border(1.dp, DjBorderOutline, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (video.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = null,
                        tint = DjCrimsonCue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Song/Video Title and Channel
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = video.title,
                    color = DjTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = video.channelTitle,
                        color = DjAmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "• ${video.durationText}",
                        color = DjTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Play / Pause button
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) DjCrimsonCue else DjAmberGold)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Next button (if playlist / search results available)
            if (onNextClick != null) {
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DjDeepSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Video",
                        tint = DjTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Stop / Close button
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DjDeepSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close and Stop Playback",
                    tint = DjTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Mini Player Bar for local audio tracks.
 * Displays DJ visualizer disc, track title, artist, BPM, Play/Pause, Next, and Stop/Close button.
 */
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
    onCloseClick: (() -> Unit)? = null,
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
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isMasterMuted) DjCrimsonCue else DjDeepSurface)
            ) {
                Icon(
                    imageVector = if (isMasterMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Mute",
                    tint = if (isMasterMuted) Color.White else DjTextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Play / Pause button
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) DjCrimsonCue else DjAmberGold)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Next button
            IconButton(
                onClick = onNextClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DjDeepSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = DjTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Stop / Close button
            if (onCloseClick != null) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DjDeepSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DjTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
