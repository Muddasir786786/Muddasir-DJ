package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import com.example.ui.theme.DjThemes
import com.example.ui.theme.LocalDjColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AudioOutputBadge
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjBorderOutline
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjDeepSurface
import com.example.ui.theme.DjElectricCyan
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjNeonEmerald
import com.example.ui.theme.DjObsidianBlack
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.ui.theme.DjTextTertiary
import com.example.viewmodel.SoundOperatorViewModel

@Composable
fun SettingsScreen(
    viewModel: SoundOperatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioOutputStatus by viewModel.audioOutputStatus.collectAsState()
    val crossfadeSec by viewModel.crossfadeSec.collectAsState()
    val gaplessPlayback by viewModel.gaplessPlayback.collectAsState()
    val djHighContrast by viewModel.djHighContrast.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val currentThemeId by viewModel.currentThemeId.collectAsState()
    val activeColors = LocalDjColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(activeColors.background),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Sound Operator Settings",
                    color = activeColors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Professional Stage Deck & Audio Routing Configuration",
                    color = activeColors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // STAGE THEME & VISUAL LIGHTING SYSTEM
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, activeColors.border, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = activeColors.surfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Palette, null, tint = activeColors.primary)
                        Text(
                            text = "STAGE THEMES & VISUAL LIGHTING",
                            color = activeColors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Customize the DJ console appearance, LED accents, and stage contrast for banquet halls and festival booths.",
                        color = activeColors.textSecondary,
                        fontSize = 12.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DjThemes.allThemes.forEach { theme ->
                            val isSelected = currentThemeId == theme.id

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) theme.surfaceHighlight else theme.surface)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) theme.primary else activeColors.border,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setTheme(theme.id) }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Color Swatch Trio
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.primary)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.secondary)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.background)
                                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                            )
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = theme.name,
                                                color = if (isSelected) theme.primary else activeColors.textPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                            )
                                            Text(
                                                text = theme.subtitle,
                                                color = activeColors.textSecondary,
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(theme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Active",
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Audio Output / PA Routing Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DjBorderOutline, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Speaker, null, tint = DjAmberGold)
                            Text(
                                text = "AUDIO OUTPUT ROUTING",
                                color = DjTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    AudioOutputBadge(
                        outputStatus = audioOutputStatus,
                        onRefresh = { viewModel.refreshAudioOutput() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Current: ${audioOutputStatus.description}",
                        color = DjTextSecondary,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DjDeepSurface,
                            contentColor = DjElectricCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Bluetooth, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Bluetooth Devices Menu", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 2. Playback DJ Tuning Card (Crossfade & Gapless)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DjBorderOutline, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, null, tint = DjElectricCyan)
                        Text(
                            text = "DJ PLAYBACK BEHAVIOR",
                            color = DjTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Crossfade Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Crossfade Duration",
                                color = DjTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${crossfadeSec}s",
                                color = DjAmberGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = crossfadeSec.toFloat(),
                            onValueChange = { viewModel.updateCrossfadeSec(it.toInt()) },
                            valueRange = 0f..8f,
                            steps = 7,
                            colors = SliderDefaults.colors(
                                thumbColor = DjAmberGold,
                                activeTrackColor = DjAmberGold,
                                inactiveTrackColor = DjDeepSurface
                            )
                        )
                        Text(
                            text = "Seamlessly blend tracks on stage without awkward silence between events.",
                            color = DjTextTertiary,
                            fontSize = 11.sp
                        )
                    }

                    // Gapless Playback Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gapless Playback",
                                color = DjTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Remove pauses between consecutive wedding tracks.",
                                color = DjTextTertiary,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = gaplessPlayback,
                            onCheckedChange = { viewModel.updateGapless(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = DjNeonEmerald,
                                uncheckedThumbColor = DjTextSecondary,
                                uncheckedTrackColor = DjDeepSurface
                            )
                        )
                    }

                    // High-Contrast DJ Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "High-Contrast DJ Booth Mode",
                                color = DjTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Ultra-bright neon indicators optimized for dark banquet halls.",
                                color = DjTextTertiary,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = djHighContrast,
                            onCheckedChange = { viewModel.updateDjHighContrast(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = DjAmberGold,
                                uncheckedThumbColor = DjTextSecondary,
                                uncheckedTrackColor = DjDeepSurface
                            )
                        )
                    }
                }
            }
        }

        // 3. Maintenance & Stage Reset Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DjBorderOutline, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, null, tint = DjCrimsonCue)
                        Text(
                            text = "STAGE MAINTENANCE",
                            color = DjTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.resetDemoData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DjDeepSurface,
                            contentColor = DjAmberGold
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Demo Audio & Categories", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.clearHistory() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DjDeepSurface,
                            contentColor = DjCrimsonCue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Live Stage History Logs", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 4. Offline Privacy & Policy
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DjBorderOutline, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, null, tint = DjNeonEmerald)
                        Text(
                            text = "100% OFFLINE-FIRST ARCHITECTURE",
                            color = DjTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "• Zero internet required during live gigs\n• No social media features or data trackers\n• Fully compliant with background playback rules\n• Direct local Room database storage",
                        color = DjTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
