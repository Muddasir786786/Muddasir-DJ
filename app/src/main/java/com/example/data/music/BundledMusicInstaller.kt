package com.example.data.music

import android.content.Context
import android.content.res.AssetManager
import android.media.MediaMetadataRetriever
import android.os.StatFs
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.entity.SongCategoryEntity
import com.example.data.entity.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream

object BundledMusicInstaller {
    private const val TAG = "BundledMusicInstaller"
    private const val INSTALL_VERSION = 2
    private const val TRACK_COUNT = 70
    private const val TOTAL_BYTES = 481630104L
    private const val MARKER = ".bundled_music_v2"

    private val extensions = setOf("mp3", "m4a", "aac", "wav", "ogg", "flac", "opus", "mp4", "3gp")
    private val categoryOrder = listOf("mehndi", "baraat", "walima", "dance", "slow", "entry", "dj")

    suspend fun installIfAvailable(context: Context, database: AppDatabase) = withContext(Dispatchers.IO) {
        if (database.songDao().getBundledSongCount() >= TRACK_COUNT &&
            File(context.filesDir, MARKER).exists()
        ) return@withContext

        val root = File(context.filesDir, "bundled-music/library").apply { mkdirs() }
        val free = StatFs(context.filesDir.absolutePath).availableBytes
        if (free < (TOTAL_BYTES * 1.08).toLong()) {
            Log.w(TAG, "Not enough free storage for bundled library")
            return@withContext
        }

        val archives = discoverArchives(context.assets)
        if (archives.isEmpty()) {
            Log.i(TAG, "No bundled ZIP payload found in installed assets")
            return@withContext
        }

        var imported = 0
        var failed = 0

        for (archive in archives) {
            val input = openArchive(context.assets, archive) ?: continue
            input.use { stream ->
                ZipInputStream(stream.buffered(64 * 1024)).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        if (entry.isDirectory) continue

                        val entryName = entry.name.replace('\\', '/')
                        val name = entryName.substringAfterLast('/')
                        val ext = name.substringAfterLast('.', "").lowercase()
                        if (ext !in extensions || !safePath(entryName)) continue

                        var temp: File? = null
                        try {
                            temp = File.createTempFile("music_", ".part", root)
                            val digest = MessageDigest.getInstance("SHA-256")
                            var size = 0L
                            FileOutputStream(temp).use { out ->
                                val buffer = ByteArray(64 * 1024)
                                while (true) {
                                    val n = zip.read(buffer)
                                    if (n <= 0) break
                                    out.write(buffer, 0, n)
                                    digest.update(buffer, 0, n)
                                    size += n
                                }
                            }
                            if (size == 0L) continue

                            val hash = digest.digest().joinToString("") { "%02x".format(it) }
                            val existing = database.songDao().getBundledSongByHash(hash)
                            if (existing != null && File(existing.filePath).exists()) continue

                            val target = File(root, hash + "." + ext)
                            if (!target.exists()) {
                                if (!temp.renameTo(target)) {
                                    temp.copyTo(target, overwrite = true)
                                    temp.delete()
                                }
                            } else {
                                temp.delete()
                            }
                            temp = null

                            val meta = readMetadata(target)
                            val title = meta.title?.takeIf { it.isNotBlank() }
                                ?: name.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim()
                            if (title.isBlank()) {
                                target.delete()
                                continue
                            }

                            val artist = meta.artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
                            val album = meta.album?.takeIf { it.isNotBlank() } ?: "Bundled Music Library"
                            val categories = classify(entryName, title, artist, album)
                            val ids = categories.mapNotNull {
                                database.categoryDao().getCategoryBySlug(it)?.id
                            }

                            val song = SongEntity(
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = meta.durationMs.coerceAtLeast(0L),
                                filePath = target.absolutePath,
                                bpm = inferBpm(categories),
                                musicalKey = "Unknown",
                                cueNotes = "Bundled offline library • " + categories.joinToString(", "),
                                coverColorHex = colorFor(categories.firstOrNull()),
                                isLocal = true,
                                contentHash = hash,
                                source = "bundled"
                            )
                            val songId = database.songDao().insertSong(song)
                            database.songDao().insertSongCategories(
                                ids.map { SongCategoryEntity(songId, it) }
                            )
                            imported++
                        } catch (e: Exception) {
                            failed++
                            Log.e(TAG, "Bundled track import failed: " + entryName, e)
                        } finally {
                            temp?.delete()
                            zip.closeEntry()
                        }
                    }
                }
            }
        }

        if (failed == 0 && database.songDao().getBundledSongCount() >= TRACK_COUNT) {
            File(context.filesDir, MARKER).writeText(
                "version=" + INSTALL_VERSION + "\ntracks=" + TRACK_COUNT + "\n"
            )
        }
        Log.i(TAG, "Bundled music install: imported=" + imported + ", failed=" + failed)
    }

    private fun discoverArchives(assetManager: AssetManager): List<String> {
        val found = mutableListOf<String>()
        fun visit(path: String) {
            val children = runCatching { assetManager.list(path) ?: emptyArray() }.getOrDefault(emptyArray())
            for (child in children) {
                val full = if (path.isEmpty()) child else "$path/$child"
                val nested = runCatching { assetManager.list(full) ?: emptyArray() }.getOrDefault(emptyArray())
                if (nested.isNotEmpty()) {
                    visit(full)
                } else if (child.endsWith(".zip", ignoreCase = true)) {
                    found += full
                }
            }
        }
        visit("")
        return found.distinct().sorted()
    }

    private fun openArchive(assetManager: AssetManager, name: String): java.io.InputStream? {
        return runCatching { assetManager.open(name) }.getOrNull()
    }

    private fun safePath(path: String): Boolean =
        path.isNotBlank() && !path.startsWith("/") &&
            !path.contains("../") && !path.contains("..\\") && !path.contains(":")

    private data class Meta(val title: String?, val artist: String?, val album: String?, val durationMs: Long)

    private fun readMetadata(file: File): Meta {
        val r = MediaMetadataRetriever()
        return try {
            r.setDataSource(file.absolutePath)
            Meta(
                r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            )
        } catch (_: Exception) {
            Meta(null, null, null, 0L)
        } finally {
            runCatching { r.release() }
        }
    }

    private fun classify(path: String, title: String, artist: String, album: String): List<String> {
        val s = (path + " " + title + " " + artist + " " + album).lowercase()
        val out = linkedSetOf<String>()
        fun has(vararg words: String) = words.any { s.contains(it) }

        if (has("mehndi","mehendi","sangeet","dholki","boliyan","chogada","bindiya","lathe di chaddar","phoolon","badri ki dulhania","laung","shakar wandaan")) out += "mehndi"
        if (has("baraat","barat","dulha","dhol","procession","sehra","groom","dhamaal")) out += "baraat"
        if (has("walima","reception","lounge","elegant","dinner") || path.startsWith("Old Songs", true)) out += "walima"
        if (has("dance","bhangra","remix","mashup","party","club","swag","jaguar","expert jatt","coca cola","abusadamente","oh nana") ||
            path.startsWith("sngs 2018", true) || path.startsWith("Dhamaal", true)) out += "dance"
        if (has("slow","sad","love","romantic","wafa","bewafa","kaash","majboor","juda","ishq","pyaar","pyar","intezar","umeed","meri zaat","khaani","khair mangda","qismat","manzil","waltz","acoustic","sufi","dhamal") ||
            path.startsWith("Old Songs", true)) out += "slow"
        if (has("entry","entrance","arrival","bride","bridal","palki","ghoonghat","dulhania") ||
            path.startsWith("Entry mhendi", true)) out += "entry"
        if (has("dj","remix","mashup","bass","drop","hype")) out += "dj"
        if (path.startsWith("Entry mhendi", true)) out += "mehndi"
        if (out.isEmpty()) out += "walima"
        return categoryOrder.filter { it in out }
    }

    private fun inferBpm(categories: List<String>): Int = when {
        "dj" in categories -> 128
        "dance" in categories || "baraat" in categories -> 120
        "slow" in categories || "walima" in categories -> 90
        else -> 110
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
}
