package com.example.data.music

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.data.entity.SongEntity
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream

data class ImportedTrack(
    val song: SongEntity,
    val categorySlugs: List<String>
)

object MusicPackImporter {

    private val audioExtensions = setOf("mp3", "m4a", "aac", "wav", "ogg", "flac", "opus", "mp4", "3gp")

    suspend fun importZip(context: Context, zipUri: Uri): List<ImportedTrack> {
        val root = File(context.filesDir, "music-library")
        if (!root.exists()) root.mkdirs()

        val extracted = mutableListOf<File>()
        context.contentResolver.openInputStream(zipUri)?.use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (entry.isDirectory) continue

                    val rawName = entry.name.replace('\\', '/')
                    if (rawName.startsWith("/") || rawName.contains("../") || rawName.contains("..\\") || rawName.contains(":")) {
                        zip.closeEntry()
                        continue
                    }

                    val cleanName = rawName.substringAfterLast('/')
                    val ext = cleanName.substringAfterLast('.', "").lowercase()
                    if (ext !in audioExtensions) {
                        zip.closeEntry()
                        continue
                    }

                    val target = File(root, cleanName).canonicalFile
                    if (!target.path.startsWith(root.canonicalPath + File.separator)) {
                        zip.closeEntry()
                        continue
                    }

                    target.parentFile?.mkdirs()
                    FileOutputStream(target).use { output -> zip.copyTo(output) }
                    if (target.length() > 0L) extracted += target
                    zip.closeEntry()
                }
            }
        } ?: return emptyList()

        return extracted.distinctBy { sha256(it) }.mapNotNull { file ->
            buildImportedTrack(file)
        }
    }

    private fun buildImportedTrack(file: File): ImportedTrack? {
        val meta = readMetadata(file)
        val title = meta.title?.trim()?.takeIf { it.isNotBlank() }
            ?: file.nameWithoutExtension.replace('_', ' ').replace('-', ' ').trim()
        if (title.isBlank()) return null

        val artist = meta.artist?.trim()?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
        val album = meta.album?.trim()?.takeIf { it.isNotBlank() } ?: "Imported Music Pack"
        val duration = meta.durationMs.coerceAtLeast(0L)

        val haystack = (title + " " + artist + " " + album + " " + file.name).lowercase()
        val categories = classify(haystack, duration)

        val song = SongEntity(
            title = title,
            artist = artist,
            album = album,
            durationMs = duration,
            filePath = file.absolutePath,
            bpm = estimatedBpm(haystack),
            musicalKey = "Unknown",
            cueNotes = "Imported music pack • ${categories.joinToString(", ")}",
            coverColorHex = colorFor(categories.firstOrNull()),
            isLocal = true
        )
        return ImportedTrack(song, categories)
    }

    private data class Metadata(val title: String?, val artist: String?, val album: String?, val durationMs: Long)

    private fun readMetadata(file: File): Metadata {
        val r = MediaMetadataRetriever()
        return try {
            r.setDataSource(file.absolutePath)
            Metadata(
                title = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                durationMs = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            )
        } catch (_: Exception) {
            Metadata(null, null, null, 0L)
        } finally {
            try { r.release() } catch (_: Exception) {}
        }
    }

    private fun classify(text: String, durationMs: Long): List<String> {
        val result = linkedSetOf<String>()

        fun has(vararg words: String) = words.any { text.contains(it) }

        if (has("mehndi", "mehendi", "mayun", "dholki", "boliyan", "sangeet", "jalebi")) result += "mehndi"
        if (has("baraat", "barat", "dulha", "groom", "procession", "dhol", "sehra")) result += "baraat"
        if (has("walima", "reception", "wedding dinner", "lounge")) result += "walima"
        if (has("entry", "entrance", "arrival", "bridal", "bride", "groom entry")) result += "entry"
        if (has("dj", "remix", "mashup", "bass", "club", "party", "drop")) result += "dj"
        if (has("slow", "romantic", "love", "waltz", "acoustic", "instrumental") || (durationMs > 0 && estimatedBpm(text) <= 95)) result += "slow"
        if (has("dance", "bhangra", "party", "remix", "mashup", "bass") || estimatedBpm(text) >= 120) result += "dance"

        if (result.isEmpty()) {
            result += if (estimatedBpm(text) <= 95) "slow" else "dance"
        }
        return result.toList()
    }

    private fun estimatedBpm(text: String): Int {
        val match = Regex("""(?<!\d)([6-9]\d|1[0-9]{2}|2[0-2]\d)(?:\s*bpm)?(?!\d)""").find(text)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 110
    }

    private fun colorFor(category: String?): String = when (category) {
        "mehndi" -> "#FFB300"
        "baraat" -> "#FF5722"
        "walima" -> "#7C4DFF"
        "dance" -> "#00E5FF"
        "slow" -> "#EC407A"
        "entry" -> "#FFD700"
        "dj" -> "#00E676"
        else -> "#FF9800"
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
