package com.example.playback

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

object DemoAudioGenerator {

    data class AudioTrackSpec(
        val fileName: String,
        val bpm: Int,
        val durationSeconds: Int = 18,
        val baseFreq: Double,
        val pattern: Int // 0: Dhol/Percussive, 1: Strings/Pad, 2: Synth/Club, 3: Fanfare/Ceremonial
    )

    fun ensureDemoAudioFiles(context: Context): Map<String, String> {
        val audioDir = File(context.filesDir, "demo_audio")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        val specs = listOf(
            AudioTrackSpec("demo_baraat_grand_dhol.wav", 130, 20, 110.0, 0),
            AudioTrackSpec("demo_mehndi_jalebi.wav", 115, 20, 220.0, 0),
            AudioTrackSpec("demo_walima_strings.wav", 85, 22, 174.6, 1),
            AudioTrackSpec("demo_dance_ignition.wav", 132, 20, 130.8, 2),
            AudioTrackSpec("demo_slow_couple_waltz.wav", 75, 24, 196.0, 1),
            AudioTrackSpec("demo_royal_bride_entry.wav", 78, 22, 246.9, 3),
            AudioTrackSpec("demo_dj_starter_bassdrop.wav", 128, 20, 116.5, 2),
            AudioTrackSpec("demo_baraat_dulha_swag.wav", 126, 20, 146.8, 0),
            AudioTrackSpec("demo_mehndi_boliyan.wav", 118, 20, 261.6, 0),
            AudioTrackSpec("demo_walima_sunset_lounge.wav", 95, 22, 164.8, 1)
        )

        val resultMap = mutableMapOf<String, String>()

        for (spec in specs) {
            val file = File(audioDir, spec.fileName)
            if (!file.exists() || file.length() < 1000) {
                generateWavFile(file, spec)
            }
            resultMap[spec.fileName] = file.absolutePath
        }

        return resultMap
    }

    private fun generateWavFile(file: File, spec: AudioTrackSpec) {
        val sampleRate = 22050 // Clean, lightweight 22.05kHz mono WAV
        val totalSamples = sampleRate * spec.durationSeconds
        val numChannels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * bitsPerSample / 8
        val blockAlign = numChannels * bitsPerSample / 8
        val dataSize = totalSamples * blockAlign
        val totalSize = 36 + dataSize

        val beatDurationSec = 60.0 / spec.bpm
        val samplesPerBeat = (sampleRate * beatDurationSec).toInt()

        val pcmData = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val beatIndex = (i / samplesPerBeat)
            val beatPhase = (i % samplesPerBeat).toDouble() / samplesPerBeat

            val sampleVal: Double = when (spec.pattern) {
                0 -> { // Energetic Dhol / Percussion with bass thump & sharp snap
                    val kickEnvelope = Math.exp(-beatPhase * 12.0)
                    val kickFreq = 55.0 + 80.0 * Math.exp(-beatPhase * 25.0)
                    val kick = sin(2.0 * PI * kickFreq * t) * kickEnvelope

                    val isSnareBeat = (beatIndex % 2 == 1)
                    val snareEnvelope = if (isSnareBeat) Math.exp(-beatPhase * 8.0) else 0.0
                    val snareTone = sin(2.0 * PI * 220.0 * t) + sin(2.0 * PI * 440.0 * t)
                    val snareNoise = ((i * 1103515245 + 12345) % 65536 / 32768.0 - 1.0)
                    val snare = (snareTone * 0.4 + snareNoise * 0.6) * snareEnvelope

                    val melodicNote = when (beatIndex % 8) {
                        0, 1 -> spec.baseFreq
                        2, 3 -> spec.baseFreq * 1.25 // Major 3rd
                        4, 5 -> spec.baseFreq * 1.5  // Perfect 5th
                        else -> spec.baseFreq * 1.334 // 4th
                    }
                    val melody = sin(2.0 * PI * melodicNote * t) * 0.25

                    (kick * 0.6 + snare * 0.4 + melody * 0.3)
                }
                1 -> { // Romantic Strings / Gentle chords
                    val chordRoot = when ((beatIndex / 4) % 4) {
                        0 -> spec.baseFreq
                        1 -> spec.baseFreq * 1.334 // IV chord
                        2 -> spec.baseFreq * 1.5   // V chord
                        else -> spec.baseFreq * 1.122 // ii chord
                    }
                    val tone1 = sin(2.0 * PI * chordRoot * t)
                    val tone2 = sin(2.0 * PI * (chordRoot * 1.2599) * t) // major 3rd
                    val tone3 = sin(2.0 * PI * (chordRoot * 1.4983) * t) // 5th
                    val shimmer = sin(2.0 * PI * 4.0 * t) * 0.15

                    val pulse = 0.7 + 0.3 * sin(2.0 * PI * (1.0 / beatDurationSec) * t)
                    ((tone1 + tone2 + tone3) / 3.0 * (1.0 + shimmer)) * pulse * 0.65
                }
                2 -> { // High-Energy Club / DJ Bass drop
                    val kickEnvelope = Math.exp(-beatPhase * 14.0)
                    val kick = sin(2.0 * PI * 50.0 * t) * kickEnvelope

                    val hatPhase = (i % (samplesPerBeat / 2)).toDouble() / (samplesPerBeat / 2)
                    val hatEnvelope = Math.exp(-hatPhase * 25.0)
                    val hatNoise = ((i * 1103515245 + 12345) % 65536 / 32768.0 - 1.0)
                    val hiHat = hatNoise * hatEnvelope * 0.25

                    val bassFreq = when (beatIndex % 4) {
                        0 -> spec.baseFreq
                        1 -> spec.baseFreq * 1.122
                        2 -> spec.baseFreq * 1.2599
                        else -> spec.baseFreq * 0.89
                    }
                    val bass = sin(2.0 * PI * bassFreq * t) * 0.45

                    (kick * 0.55 + hiHat * 0.25 + bass * 0.4)
                }
                else -> { // Majestic Ceremonial Fanfare (Entry / Shehnai)
                    val drone = sin(2.0 * PI * (spec.baseFreq / 2.0) * t) * 0.3
                    val melodyFreq = when (beatIndex % 8) {
                        0 -> spec.baseFreq
                        1 -> spec.baseFreq * 1.122
                        2 -> spec.baseFreq * 1.2599
                        3 -> spec.baseFreq * 1.4983
                        4 -> spec.baseFreq * 1.6817
                        5 -> spec.baseFreq * 1.4983
                        6 -> spec.baseFreq * 1.3348
                        else -> spec.baseFreq
                    }
                    val vibrato = 1.0 + 0.03 * sin(2.0 * PI * 6.0 * t)
                    val shehnai = sin(2.0 * PI * (melodyFreq * vibrato) * t) * 0.5 +
                            sin(2.0 * PI * (melodyFreq * 2.0 * vibrato) * t) * 0.25

                    (drone + shehnai) * 0.6
                }
            }

            val clamped = (sampleVal.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
            pcmData[i] = clamped
        }

        FileOutputStream(file).use { fos ->
            val header = ByteBuffer.allocate(44).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put("RIFF".toByteArray())
                putInt(totalSize)
                put("WAVE".toByteArray())
                put("fmt ".toByteArray())
                putInt(16) // Subchunk1Size for PCM
                putShort(1.toShort()) // AudioFormat 1 = PCM
                putShort(numChannels.toShort())
                putInt(sampleRate)
                putInt(byteRate)
                putShort(blockAlign.toShort())
                putShort(bitsPerSample.toShort())
                put("data".toByteArray())
                putInt(dataSize)
            }
            fos.write(header.array())

            val byteBuffer = ByteBuffer.allocate(pcmData.size * 2).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                for (s in pcmData) {
                    putShort(s)
                }
            }
            fos.write(byteBuffer.array())
        }
    }
}
