package com.example.data.database

import android.content.Context
import com.example.data.entity.CategoryEntity
import com.example.data.entity.MusicPackEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.PlaylistSongEntity
import com.example.data.entity.SettingEntity
import com.example.data.entity.SongCategoryEntity
import com.example.data.entity.SongEntity
import com.example.playback.DemoAudioGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun populateInitialData(context: Context, database: AppDatabase) = withContext(Dispatchers.IO) {
        val songCount = database.songDao().getSongCount()
        if (songCount > 0) {
            return@withContext
        }

        // 1. Ensure audio files are synthesized and present
        val audioFiles = DemoAudioGenerator.ensureDemoAudioFiles(context)

        // 2. Insert Categories
        val categories = listOf(
            CategoryEntity(
                id = 1,
                name = "Mehndi",
                slug = "mehndi",
                description = "Folk beats, jalebi grooves, and vibrant sangeet tracks",
                iconName = "Celebration",
                colorHex = "#FFB300",
                displayOrder = 1
            ),
            CategoryEntity(
                id = 2,
                name = "Baraat",
                slug = "baraat",
                description = "Heavy dhol rhythms and high-energy procession fanfares",
                iconName = "Campaign",
                colorHex = "#FF5722",
                displayOrder = 2
            ),
            CategoryEntity(
                id = 3,
                name = "Walima",
                slug = "walima",
                description = "Elegant reception melodies, soft lounge, and dinner strings",
                iconName = "Nightlife",
                colorHex = "#7C4DFF",
                displayOrder = 3
            ),
            CategoryEntity(
                id = 4,
                name = "Dance",
                slug = "dance",
                description = "Floor fillers, modern bhangra beats, and club drops",
                iconName = "SpeakerGroup",
                colorHex = "#00E5FF",
                displayOrder = 4
            ),
            CategoryEntity(
                id = 5,
                name = "Slow",
                slug = "slow",
                description = "Emotional couple dances, waltzes, and gentle acoustic moments",
                iconName = "Favorite",
                colorHex = "#EC407A",
                displayOrder = 5
            ),
            CategoryEntity(
                id = 6,
                name = "Entry",
                slug = "entry",
                description = "Grand bridal entries, groom arrivals, and ceremonial crescendos",
                iconName = "Stars",
                colorHex = "#FFD700",
                displayOrder = 6
            ),
            CategoryEntity(
                id = 7,
                name = "DJ",
                slug = "dj",
                description = "Bass drops, continuous party remixes, sirens, and hypes",
                iconName = "Equalizer",
                colorHex = "#00E676",
                displayOrder = 7
            )
        )
        database.categoryDao().insertCategories(categories)

        // 3. Insert Demo Songs
        val rawSongs = listOf(
            SongEntity(
                title = "Dhol Blast - Baraat Grand Entrance",
                artist = "DJ Sound Ops & Ustad Dholi",
                album = "Royal Wedding Vol. 1",
                durationMs = 20000,
                filePath = audioFiles["demo_baraat_grand_dhol.wav"] ?: "",
                bpm = 130,
                musicalKey = "8A / Am",
                cueNotes = "Cue: Fire pyros at 0:08. Drop bass for groom car door opening.",
                coverColorHex = "#FF5722"
            ),
            SongEntity(
                title = "Mehndi Mayhem - Jalebi Groove",
                artist = "Sangeet Brass & Folk Ensemble",
                album = "Yellow Night Festival",
                durationMs = 20000,
                filePath = audioFiles["demo_mehndi_jalebi.wav"] ?: "",
                bpm = 115,
                musicalKey = "9B / G",
                cueNotes = "Cue: Bride sisters entry dance. High energy clapping segment.",
                coverColorHex = "#FFB300"
            ),
            SongEntity(
                title = "Walima Royale - Elegant Strings",
                artist = "Symphonic Desi Orchestra",
                album = "Grand Palace Reception",
                durationMs = 22000,
                filePath = audioFiles["demo_walima_strings.wav"] ?: "",
                bpm = 85,
                musicalKey = "7B / F",
                cueNotes = "Cue: Dim stage lights to 30%. Stage photo session background.",
                coverColorHex = "#7C4DFF"
            ),
            SongEntity(
                title = "Dance Floor Ignition - Nonstop Remix",
                artist = "DJ Matrix & Sound Crew",
                album = "Club Wedding Mashup",
                durationMs = 20000,
                filePath = audioFiles["demo_dance_ignition.wav"] ?: "",
                bpm = 132,
                musicalKey = "4A / Fm",
                cueNotes = "Cue: Floor lights active. Smoke machine pulse on 0:04.",
                coverColorHex = "#00E5FF"
            ),
            SongEntity(
                title = "Slow Motion Couple Waltz",
                artist = "Acoustic Serenade Strings",
                album = "First Dance Classics",
                durationMs = 24000,
                filePath = audioFiles["demo_slow_couple_waltz.wav"] ?: "",
                bpm = 75,
                musicalKey = "6B / Bb",
                cueNotes = "Cue: First couple dance. Low fog dry ice machine trigger.",
                coverColorHex = "#EC407A"
            ),
            SongEntity(
                title = "Royal Bride Entry - Shehnai & Sitar",
                artist = "Imperial Court Musicians",
                album = "Ceremonial Processions",
                durationMs = 22000,
                filePath = audioFiles["demo_royal_bride_entry.wav"] ?: "",
                bpm = 78,
                musicalKey = "11B / A",
                cueNotes = "Cue: Spotlight on bride walkway. Announce entry on Mic 1.",
                coverColorHex = "#FFD700"
            ),
            SongEntity(
                title = "DJ Party Starter - Bass Drop",
                artist = "Electro Dhol Experiment",
                album = "Midnight Event Bangers",
                durationMs = 20000,
                filePath = audioFiles["demo_dj_starter_bassdrop.wav"] ?: "",
                bpm = 128,
                musicalKey = "2A / Ebm",
                cueNotes = "Cue: Crowd jump signal. Confetti cannons armed.",
                coverColorHex = "#00E676"
            ),
            SongEntity(
                title = "Baraat Heavy Beat - Dulha Arrival",
                artist = "Bhangra Knights",
                album = "Royal Wedding Vol. 1",
                durationMs = 20000,
                filePath = audioFiles["demo_baraat_dulha_swag.wav"] ?: "",
                bpm = 126,
                musicalKey = "5A / C#m",
                cueNotes = "Cue: Gate arrival fanfare. Keep master gain at +2dB for outdoor spill.",
                coverColorHex = "#FF7043"
            ),
            SongEntity(
                title = "Mehndi Sangeet - Boliyan Dholak",
                artist = "Folk Heritage Troupe",
                album = "Yellow Night Festival",
                durationMs = 20000,
                filePath = audioFiles["demo_mehndi_boliyan.wav"] ?: "",
                bpm = 118,
                musicalKey = "8B / C",
                cueNotes = "Cue: Family dance group round 2. Quick fade out when aunties speak.",
                coverColorHex = "#FFCA28"
            ),
            SongEntity(
                title = "Walima Gala - Amber Sunset Ambient",
                artist = "Chillout Lounge Project",
                album = "Grand Palace Reception",
                durationMs = 22000,
                filePath = audioFiles["demo_walima_sunset_lounge.wav"] ?: "",
                bpm = 95,
                musicalKey = "10B / D",
                cueNotes = "Cue: Dinner service background audio. Keep peak SPL at 75dB.",
                coverColorHex = "#9575CD"
            )
        )

        val insertedSongIds = database.songDao().insertSongs(rawSongs)

        // 4. Map Song Categories (One song can belong to multiple categories!)
        // Categories: 1=Mehndi, 2=Baraat, 3=Walima, 4=Dance, 5=Slow, 6=Entry, 7=DJ
        val songCategoryMappings = listOf(
            // Song 0: Dhol Blast -> Baraat (2), Entry (6), DJ (7)
            SongCategoryEntity(insertedSongIds[0], 2),
            SongCategoryEntity(insertedSongIds[0], 6),
            SongCategoryEntity(insertedSongIds[0], 7),

            // Song 1: Mehndi Mayhem -> Mehndi (1), Dance (4)
            SongCategoryEntity(insertedSongIds[1], 1),
            SongCategoryEntity(insertedSongIds[1], 4),

            // Song 2: Walima Royale -> Walima (3), Slow (5), Entry (6)
            SongCategoryEntity(insertedSongIds[2], 3),
            SongCategoryEntity(insertedSongIds[2], 5),
            SongCategoryEntity(insertedSongIds[2], 6),

            // Song 3: Dance Floor Ignition -> Dance (4), DJ (7)
            SongCategoryEntity(insertedSongIds[3], 4),
            SongCategoryEntity(insertedSongIds[3], 7),

            // Song 4: Slow Motion Couple Waltz -> Slow (5), Walima (3)
            SongCategoryEntity(insertedSongIds[4], 5),
            SongCategoryEntity(insertedSongIds[4], 3),

            // Song 5: Royal Bride Entry -> Entry (6), Baraat (2)
            SongCategoryEntity(insertedSongIds[5], 6),
            SongCategoryEntity(insertedSongIds[5], 2),

            // Song 6: DJ Party Starter -> DJ (7), Dance (4)
            SongCategoryEntity(insertedSongIds[6], 7),
            SongCategoryEntity(insertedSongIds[6], 4),

            // Song 7: Baraat Heavy Beat -> Baraat (2), DJ (7)
            SongCategoryEntity(insertedSongIds[7], 2),
            SongCategoryEntity(insertedSongIds[7], 7),

            // Song 8: Mehndi Sangeet -> Mehndi (1), Dance (4)
            SongCategoryEntity(insertedSongIds[8], 1),
            SongCategoryEntity(insertedSongIds[8], 4),

            // Song 9: Walima Gala -> Walima (3), Slow (5)
            SongCategoryEntity(insertedSongIds[9], 3),
            SongCategoryEntity(insertedSongIds[9], 5)
        )
        database.songDao().insertSongCategories(songCategoryMappings)

        // 5. Insert Playlists
        val playlist1Id = database.playlistDao().insertPlaylist(
            PlaylistEntity(
                name = "Main Stage Baraat Hits",
                description = "High energy dhol beats and procession hits for main gate arrival",
                coverColorHex = "#FF5722"
            )
        )
        val playlist2Id = database.playlistDao().insertPlaylist(
            PlaylistEntity(
                name = "Varmala & Royal Entries",
                description = "Curated ceremonial tunes for emotional and majestic couple entries",
                coverColorHex = "#FFD700"
            )
        )
        val playlist3Id = database.playlistDao().insertPlaylist(
            PlaylistEntity(
                name = "Late Night DJ Dancefloor",
                description = "Nonstop uptempo club tracks for the post-dinner celebration",
                coverColorHex = "#00E5FF"
            )
        )

        database.playlistDao().addSongToPlaylist(PlaylistSongEntity(playlist1Id, insertedSongIds[0], 0))
        database.playlistDao().addSongToPlaylist(PlaylistSongEntity(playlist1Id, insertedSongIds[7], 1))
        database.playlistDao().addSongToPlaylist(PlaylistSongEntity(playlist1Id, insertedSongIds[6], 2))

        database.playlistDao().addSongToPlaylist(PlaylistSongEntity(playlist2Id, insertedSongIds[5], 0))
        database.playlistDao().addSongToPlaylist(PlaylistSongEntity(playlist2Id, insertedSongIds[2], 1))
        database.playlistDao().addSongToPlaylist(PlaylistSongEntity(playlist2Id, insertedSongIds[4], 2))

        database.playlistDao().addSongToPlaylist(PlaylistSongEntity(playlist3Id, insertedSongIds[3], 0))
        database.playlistDao().addSongToPlaylist(PlaylistSongEntity(playlist3Id, insertedSongIds[6], 1))
        database.playlistDao().addSongToPlaylist(PlaylistSongEntity(playlist3Id, insertedSongIds[1], 2))

        // 6. Insert Music Packs
        val packs = listOf(
            MusicPackEntity(
                title = "Desi Wedding Essential Pack",
                subtitle = "Complete toolkit for Baraat, Mehndi & Walima ceremonies",
                eventType = "Wedding Master Pack",
                songCount = 10,
                isDownloaded = true,
                colorHex = "#FFB300"
            ),
            MusicPackEntity(
                title = "Grand Entry & Varmala Suite",
                subtitle = "Cinematic shehnai, sitar, and slow-burn royal fanfare",
                eventType = "Ceremonial Entries",
                songCount = 6,
                isDownloaded = true,
                colorHex = "#FFD700"
            ),
            MusicPackEntity(
                title = "High-Energy Baraat Rush",
                subtitle = "Heavy dhol rhythms, brass stabs, and outdoor street procession tunes",
                eventType = "Baraat Procession",
                songCount = 8,
                isDownloaded = true,
                colorHex = "#FF5722"
            ),
            MusicPackEntity(
                title = "Late Night DJ Club Bangers",
                subtitle = "Electronic basslines, synth drops, and fast remixes for floor crowds",
                eventType = "DJ Night",
                songCount = 12,
                isDownloaded = true,
                colorHex = "#00E5FF"
            )
        )
        database.musicPackDao().insertMusicPacks(packs)

        // 7. Initial Settings
        database.settingDao().setSetting(SettingEntity("crossfade_sec", "2"))
        database.settingDao().setSetting(SettingEntity("gapless_playback", "true"))
        database.settingDao().setSetting(SettingEntity("dj_high_contrast", "true"))
        database.settingDao().setSetting(SettingEntity("master_volume", "1.0"))
        database.settingDao().setSetting(SettingEntity("last_played_song_id", insertedSongIds[0].toString()))
        database.settingDao().setSetting(SettingEntity("last_playback_position", "0"))
    }
}
