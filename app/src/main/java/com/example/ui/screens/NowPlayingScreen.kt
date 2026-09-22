package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.ui.components.AudioOutputBadge
import com.example.ui.components.DJVisualizer
import com.example.ui.components.formatDuration
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjBorderOutline
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjDeepSurface
import com.example.ui.theme.DjElectricCyan
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjHighlightCard
import com.example.ui.theme.DjNeonEmerald
import com.example.ui.theme.DjObsidianBlack
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.ui.theme.DjTextTertiary
import com.example.viewmodel.SoundOperatorViewModel

@Composable
fun NowPlayingScreen(
    viewModel: SoundOperatorViewModel,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isShuffle by viewModel.isShuffleEnabled.collectAsState()
    val isMasterMuted by viewModel.isMasterMuted.collectAsState()
    val visualizerBands by viewModel.visualizerBands.collectAsState()
    val audioOutputStatus by viewModel.audioOutputStatus.collectAsState()
    val favoriteIds by viewModel.favoriteSongIds.collectAsState()

    val song = currentSong ?: return

    val isFav = favoriteIds.contains(song.id)

    val coverColor = try {
        Color(android.graphics.Color.parseColor(song.coverColorHex))
    } catch (_: Exception) {
        DjAmberGold
    }

    // Vinyl spinning rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    var isUserSeeking by remember { androidx.compose.runtime.mutableStateOf(false) }
    var userSeekPos by remember { mutableFloatStateOf(0f) }

    val effectivePosition = if (isUserSeeking) userSeekPos.toLong() else currentPositionMs

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjObsidianBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header: Collapse button, Title, Mute Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DjElevatedCard)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse Deck",
                    tint = DjTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LIVE STAGE DECK",
                    color = DjAmberGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (isPlaying) "BROADCASTING" else "CUE READY",
                    color = if (isPlaying) DjNeonEmerald else DjTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { viewModel.toggleMasterMute() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isMasterMuted) DjCrimsonCue else DjElevatedCard)
            ) {
                Icon(
                    imageVector = if (isMasterMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Mute",
                    tint = if (isMasterMuted) Color.White else DjAmberGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Live Audio Output Routing Badge
        AudioOutputBadge(
            outputStatus = audioOutputStatus,
            onRefresh = { viewModel.refreshAudioOutput() },
            modifier = Modifier.fillMaxWidth()
        )

        // Center DJ Turntable Vinyl Disc
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F1015))
                .border(6.dp, Color(0xFF1E212B), CircleShape)
                .border(1.5.dp, coverColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Concentric vinyl grooves
            Box(
                modifier = Modifier
                    .size(175.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF282C3A), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(135.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF33384A), CircleShape)
            )

            // Center Spindle Label (spins when playing)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(coverColor, coverColor.copy(alpha = 0.7f), Color.Black)
                        )
                    )
                    .rotate(if (isPlaying) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )
            }
        }

        // DJ Spectrum Visualizer
        DJVisualizer(
            bands = visualizerBands,
            height = 42.dp,
            barWidth = 8.dp,
            spacing = 6.dp
        )

        // Song Info: Title, Artist, Album
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = song.title,
                color = DjTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${song.artist} • ${song.album}",
                color = DjTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // DJ Telemetry: BPM + Key Scale
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DjElevatedCard)
                        .border(1.dp, DjAmberGold, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${song.bpm} BPM",
                        color = DjAmberGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DjElevatedCard)
                        .border(1.dp, DjElectricCyan, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "KEY: ${song.musicalKey}",
                        color = DjElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleFavorite(song.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) DjCrimsonCue else DjTextTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Live Event Cue Notes Card
        if (song.cueNotes.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Event Cue",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = song.cueNotes,
                        color = Color(0xFFFFD54F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Track Progress & Seek Slider
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Slider(
                value = effectivePosition.toFloat().coerceIn(0f, durationMs.coerceAtLeast(1L).toFloat()),
                onValueChange = {
                    isUserSeeking = true
                    userSeekPos = it
                },
                onValueChangeFinished = {
                    isUserSeeking = false
                    viewModel.seekTo(userSeekPos.toLong())
                },
                valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = DjNeonEmerald,
                    activeTrackColor = DjNeonEmerald,
                    inactiveTrackColor = DjDeepSurface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(effectivePosition),
                    color = DjTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDuration(durationMs),
                    color = DjTextTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Hot Cue Jump Pads (0%, 25%, 55%, 85%)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HotCueButton("INTRO", 0.0f, DjElectricCyan) { viewModel.jumpToCue(0.0f) }
            HotCueButton("VERSE", 0.25f, DjAmberGold) { viewModel.jumpToCue(0.25f) }
            HotCueButton("DROP", 0.55f, DjCrimsonCue) { viewModel.jumpToCue(0.55f) }
            HotCueButton("OUTRO", 0.85f, DjNeonEmerald) { viewModel.jumpToCue(0.85f) }
        }

        // Large Primary Playback Controls Row (72dp Play/Pause)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(
                onClick = { viewModel.toggleShuffle() },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isShuffle) DjAmberGold else DjElevatedCard)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) Color.Black else DjTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Seek -10s
            IconButton(
                onClick = { viewModel.seekRelative(-10000L) },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(DjElevatedCard)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "-10s",
                    tint = DjTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Previous
            IconButton(
                onClick = { viewModel.playPrevious() },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DjElevatedCard)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = DjTextPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Large Center Play / Pause Button (72dp) with illuminated ring
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) DjCrimsonCue else DjAmberGold)
                    .clickable { viewModel.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(42.dp)
                )
            }

            // Next
            IconButton(
                onClick = { viewModel.playNext() },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DjElevatedCard)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = DjTextPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Seek +10s
            IconButton(
                onClick = { viewModel.seekRelative(10000L) },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(DjElevatedCard)
            ) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "+10s",
                    tint = DjTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Repeat Mode
            IconButton(
                onClick = { viewModel.toggleRepeatMode() },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (repeatMode != Player.REPEAT_MODE_OFF) DjElectricCyan else DjElevatedCard)
            ) {
                Icon(
                    imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) Color.Black else DjTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // DJ Pitch / Tempo Fader Slider (0.8x - 1.2x)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, DjBorderOutline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Speed, null, tint = DjAmberGold, modifier = Modifier.size(18.dp))
                        Text(
                            text = "DJ TEMPO PITCH",
                            color = DjTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = String.format("%.2fx (%.0f BPM)", playbackSpeed, song.bpm * playbackSpeed),
                            color = DjAmberGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { viewModel.setPlaybackSpeed(1.0f) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DjDeepSurface,
                                contentColor = DjTextSecondary
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("1.0x", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Slider(
                    value = playbackSpeed,
                    onValueChange = { viewModel.setPlaybackSpeed(it) },
                    valueRange = 0.85f..1.15f,
                    colors = SliderDefaults.colors(
                        thumbColor = DjAmberGold,
                        activeTrackColor = DjAmberGold,
                        inactiveTrackColor = DjDeepSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun HotCueButton(
    title: String,
    percentage: Float,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DjElevatedCard)
            .border(1.dp, accentColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                color = DjTextTertiary,
                fontSize = 9.sp
            )
        }
    }
}
