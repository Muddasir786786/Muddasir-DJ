package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playback.AudioOutputStatus
import com.example.playback.OutputType
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjNeonEmerald

@Composable
fun AudioOutputBadge(
    outputStatus: AudioOutputStatus,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val (badgeBg, badgeText, statusColor, iconVector) = when (outputStatus.outputType) {
        OutputType.BLUETOOTH -> {
            Quadruple(
                DjElevatedCard,
                outputStatus.deviceName,
                DjNeonEmerald,
                Icons.Default.Bluetooth
            )
        }
        OutputType.AUX_WIRED -> {
            Quadruple(
                DjElevatedCard,
                "Stage AUX / Soundboard",
                DjNeonEmerald,
                Icons.Default.Headphones
            )
        }
        OutputType.USB_AUDIO -> {
            Quadruple(
                DjElevatedCard,
                outputStatus.deviceName,
                DjNeonEmerald,
                Icons.Default.VolumeUp
            )
        }
        else -> {
            Quadruple(
                Color(0xFF3E1E1E),
                "Internal Phone Speaker (NO PA)",
                DjCrimsonCue,
                Icons.Default.Warning
            )
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badgeBg)
            .clickable {
                try {
                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                } catch (_: Exception) {}
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Glowing status indicator dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )

        Icon(
            imageVector = iconVector,
            contentDescription = "Audio Output",
            tint = statusColor,
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = badgeText,
            color = if (outputStatus.isExternalConnected) Color.White else DjCrimsonCue,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )

        IconButton(
            onClick = onRefresh,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh output device",
                tint = DjAmberGold,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
