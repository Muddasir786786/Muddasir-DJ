package com.example.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioOutputStatus(
    val outputType: OutputType,
    val deviceName: String,
    val isExternalConnected: Boolean,
    val description: String
)

enum class OutputType {
    BLUETOOTH,
    AUX_WIRED,
    USB_AUDIO,
    BUILT_IN_SPEAKER,
    UNKNOWN
}

class AudioOutputHelper(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _outputStatus = MutableStateFlow(detectCurrentOutput())
    val outputStatus: StateFlow<AudioOutputStatus> = _outputStatus.asStateFlow()

    private val audioReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            _outputStatus.value = detectCurrentOutput()
        }
    }

    fun startListening() {
        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(AudioManager.ACTION_HEADSET_PLUG)
            addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED")
        }
        try {
            context.registerReceiver(audioReceiver, filter)
        } catch (_: Exception) {}
        _outputStatus.value = detectCurrentOutput()
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(audioReceiver)
        } catch (_: Exception) {}
    }

    fun refresh() {
        _outputStatus.value = detectCurrentOutput()
    }

    fun detectCurrentOutput(): AudioOutputStatus {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                        val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !device.productName.isNullOrBlank()) {
                            device.productName.toString()
                        } else {
                            "Bluetooth Audio / PA"
                        }
                        return AudioOutputStatus(
                            outputType = OutputType.BLUETOOTH,
                            deviceName = name,
                            isExternalConnected = true,
                            description = "Connected via Bluetooth A2DP"
                        )
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_AUX_LINE -> {
                        return AudioOutputStatus(
                            outputType = OutputType.AUX_WIRED,
                            deviceName = "Stage AUX Cable (3.5mm)",
                            isExternalConnected = true,
                            description = "Connected directly to Mixer / Soundboard"
                        )
                    }
                    AudioDeviceInfo.TYPE_USB_DEVICE,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        return AudioOutputStatus(
                            outputType = OutputType.USB_AUDIO,
                            deviceName = "USB Audio Interface",
                            isExternalConnected = true,
                            description = "Studio Grade USB Audio Output"
                        )
                    }
                }
            }
        }

        // Fallback for older or default
        val isWired = audioManager.isWiredHeadsetOn
        val isBt = audioManager.isBluetoothA2dpOn

        return when {
            isBt -> AudioOutputStatus(
                outputType = OutputType.BLUETOOTH,
                deviceName = "Bluetooth Sound Output",
                isExternalConnected = true,
                description = "Active Wireless Soundboard"
            )
            isWired -> AudioOutputStatus(
                outputType = OutputType.AUX_WIRED,
                deviceName = "AUX Cable (3.5mm)",
                isExternalConnected = true,
                description = "Direct Line-In"
            )
            else -> AudioOutputStatus(
                outputType = OutputType.BUILT_IN_SPEAKER,
                deviceName = "Internal Speaker",
                isExternalConnected = false,
                description = "Warning: Not connected to external PA"
            )
        }
    }
}
