package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.dao.QueueItemWithSong
import com.example.data.database.AppDatabase
import com.example.data.database.DatabaseInitializer
import com.example.data.entity.CategoryEntity
import com.example.data.entity.MusicPackEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.SongEntity
import com.example.data.entity.YouTubeHistoryEntity
import com.example.data.entity.YouTubeQueryHistoryEntity
import com.example.data.repository.SoundOperatorRepository
import com.example.data.youtube.YouTubeApiClient
import com.example.data.youtube.YouTubeSuggestionsProvider
import com.example.data.youtube.YouTubeVideoItem
import com.example.playback.AudioOutputHelper
import com.example.playback.AudioOutputStatus
import com.example.playback.SoundPlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SoundOperatorViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = SoundOperatorRepository(database)
    val playerManager = SoundPlayerManager.getInstance(application)
    val audioOutputHelper = AudioOutputHelper(application)

    val audioOutputStatus: StateFlow<AudioOutputStatus> = audioOutputHelper.outputStatus

    val allSongs: StateFlow<List<SongEntity>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<SongEntity>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongIds: StateFlow<Set<Long>> = repository.favoriteSongIds
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val recentlyPlayedSongs: StateFlow<List<SongEntity>> = repository.recentlyPlayedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val queueWithSongs: StateFlow<List<QueueItemWithSong>> = repository.queueWithSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val musicPacks: StateFlow<List<MusicPackEntity>> = repository.allMusicPacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search & Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<CategoryEntity?>(null)
    val selectedCategoryFilter: StateFlow<CategoryEntity?> = _selectedCategoryFilter.asStateFlow()

    val filteredSearchResults: StateFlow<List<SongEntity>> = combine(
        allSongs,
        _searchQuery,
        _selectedCategoryFilter
    ) { songs, query, selectedCategory ->
        var list = songs
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.artist.lowercase().contains(q) ||
                        it.album.lowercase().contains(q) ||
                        it.cueNotes.lowercase().contains(q) ||
                        it.bpm.toString().contains(q) ||
                        it.musicalKey.lowercase().contains(q)
            }
        }
        if (selectedCategory != null) {
            // Filter by category slug in title or notes if available, or direct mapping
            val catSlug = selectedCategory.slug.lowercase()
            list = list.filter {
                it.title.lowercase().contains(catSlug) ||
                        it.cueNotes.lowercase().contains(catSlug) ||
                        it.album.lowercase().contains(catSlug) ||
                        (catSlug == "mehndi" && (it.title.contains("Mehndi", ignoreCase = true) || it.title.contains("Jalebi", ignoreCase = true))) ||
                        (catSlug == "baraat" && (it.title.contains("Baraat", ignoreCase = true) || it.title.contains("Dulha", ignoreCase = true))) ||
                        (catSlug == "walima" && it.title.contains("Walima", ignoreCase = true)) ||
                        (catSlug == "dance" && (it.title.contains("Dance", ignoreCase = true) || it.title.contains("Remix", ignoreCase = true) || it.bpm >= 120)) ||
                        (catSlug == "slow" && (it.title.contains("Slow", ignoreCase = true) || it.title.contains("Waltz", ignoreCase = true) || it.bpm <= 95)) ||
                        (catSlug == "entry" && (it.title.contains("Entry", ignoreCase = true) || it.title.contains("Entrance", ignoreCase = true))) ||
                        (catSlug == "dj" && (it.title.contains("DJ", ignoreCase = true) || it.title.contains("Bass", ignoreCase = true) || it.title.contains("Remix", ignoreCase = true)))
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Player State
    val currentSong: StateFlow<SongEntity?> = playerManager.currentSong
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val playbackSpeed: StateFlow<Float> = playerManager.playbackSpeed
    val repeatMode: StateFlow<Int> = playerManager.repeatMode
    val isShuffleEnabled: StateFlow<Boolean> = playerManager.isShuffleEnabled
    val isMasterMuted: StateFlow<Boolean> = playerManager.isMasterMuted
    val visualizerBands: StateFlow<List<Float>> = playerManager.visualizerBands

    // Settings
    private val _crossfadeSec = MutableStateFlow(2)
    val crossfadeSec: StateFlow<Int> = _crossfadeSec.asStateFlow()

    private val _gaplessPlayback = MutableStateFlow(true)
    val gaplessPlayback: StateFlow<Boolean> = _gaplessPlayback.asStateFlow()

    private val _djHighContrast = MutableStateFlow(true)
    val djHighContrast: StateFlow<Boolean> = _djHighContrast.asStateFlow()

    init {
        audioOutputHelper.startListening()

        viewModelScope.launch {
            // Seed DB if first run
            DatabaseInitializer.populateInitialData(application, database)

            // Load last settings
            repository.getSetting("crossfade_sec")?.toIntOrNull()?.let { _crossfadeSec.value = it }
            repository.getSetting("gapless_playback")?.let { _gaplessPlayback.value = it.toBoolean() }
            repository.getSetting("dj_high_contrast")?.let { _djHighContrast.value = it.toBoolean() }

            // Resume playback state if available
            val lastSongId = repository.getSetting("last_played_song_id")?.toLongOrNull()
            if (lastSongId != null && playerManager.currentSong.value == null) {
                repository.getSongById(lastSongId)?.let { lastSong ->
                    // Set current song ready without auto-starting sound loudly
                }
            }
        }

        // Record play history when song changes
        playerManager.onSongChanged = { song ->
            viewModelScope.launch {
                repository.recordPlayHistory(song.id, durationPlayedMs = song.durationMs, eventName = "Live Event")
                repository.setSetting("last_played_song_id", song.id.toString())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioOutputHelper.stopListening()
    }

    fun playSong(song: SongEntity, contextList: List<SongEntity> = emptyList()) {
        playerManager.playSong(song, contextList)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun playNext() {
        playerManager.playNext()
    }

    fun playPrevious() {
        playerManager.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun seekRelative(offsetMs: Long) {
        playerManager.seekRelative(offsetMs)
    }

    fun jumpToCue(percentage: Float) {
        playerManager.jumpToCue(percentage)
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }

    fun toggleRepeatMode() {
        playerManager.toggleRepeatMode()
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleMasterMute() {
        playerManager.toggleMasterMute()
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(songId)
        }
    }

    fun addToQueue(songId: Long) {
        viewModelScope.launch {
            repository.addToQueue(songId)
        }
    }

    fun playNextInQueue(songId: Long) {
        viewModelScope.launch {
            repository.playNextInQueue(songId)
        }
    }

    fun removeFromQueue(queueId: Long) {
        viewModelScope.launch {
            repository.removeFromQueue(queueId)
        }
    }

    fun clearQueue() {
        viewModelScope.launch {
            repository.clearQueue()
        }
    }

    fun createPlaylist(name: String, description: String, colorHex: String = "#00E5FF") {
        viewModelScope.launch {
            repository.createPlaylist(name, description, colorHex)
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategoryFilter(category: CategoryEntity?) {
        _selectedCategoryFilter.value = category
    }

    fun togglePackDownload(pack: MusicPackEntity) {
        viewModelScope.launch {
            repository.togglePackDownload(pack)
        }
    }

    fun updateCrossfadeSec(seconds: Int) {
        _crossfadeSec.value = seconds
        viewModelScope.launch {
            repository.setSetting("crossfade_sec", seconds.toString())
        }
    }

    fun updateGapless(enabled: Boolean) {
        _gaplessPlayback.value = enabled
        viewModelScope.launch {
            repository.setSetting("gapless_playback", enabled.toString())
        }
    }

    fun updateDjHighContrast(enabled: Boolean) {
        _djHighContrast.value = enabled
        viewModelScope.launch {
            repository.setSetting("dj_high_contrast", enabled.toString())
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun refreshAudioOutput() {
        audioOutputHelper.refresh()
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.clearQueue()
            repository.clearHistory()
            DatabaseInitializer.populateInitialData(getApplication(), database)
        }
    }

    fun importLocalAudio(title: String, artist: String, filePath: String, durationMs: Long, bpm: Int) {
        viewModelScope.launch {
            val song = SongEntity(
                title = title,
                artist = artist,
                album = "Imported Audio",
                durationMs = durationMs,
                filePath = filePath,
                bpm = bpm,
                musicalKey = "Auto / 8A",
                cueNotes = "Imported from device storage",
                coverColorHex = "#00E5FF",
                isLocal = true
            )
            repository.insertSong(song)
        }
    }

    // --- Official YouTube Search, Suggestions, Pagination & Playback State ---

    private val _youTubeQuery = MutableStateFlow("")
    val youTubeQuery: StateFlow<String> = _youTubeQuery.asStateFlow()

    private val _youTubeSearchResults = MutableStateFlow<List<YouTubeVideoItem>>(emptyList())
    val youTubeSearchResults: StateFlow<List<YouTubeVideoItem>> = _youTubeSearchResults.asStateFlow()

    private val _nextPageToken = MutableStateFlow<String?>(null)
    val nextPageToken: StateFlow<String?> = _nextPageToken.asStateFlow()

    private val _isYouTubeSearching = MutableStateFlow(false)
    val isYouTubeSearching: StateFlow<Boolean> = _isYouTubeSearching.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _youTubeSearchError = MutableStateFlow<String?>(null)
    val youTubeSearchError: StateFlow<String?> = _youTubeSearchError.asStateFlow()

    private val _activeYouTubeVideo = MutableStateFlow<YouTubeVideoItem?>(null)
    val activeYouTubeVideo: StateFlow<YouTubeVideoItem?> = _activeYouTubeVideo.asStateFlow()

    val youTubeHistory: StateFlow<List<YouTubeHistoryEntity>> = repository.youTubeHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val youTubeRecentQueries: StateFlow<List<YouTubeQueryHistoryEntity>> = repository.youTubeRecentQueries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Instant local suggestions based on recent searches, app categories, and curated wedding terms
    val youTubeSuggestions: StateFlow<List<String>> = combine(
        _youTubeQuery,
        youTubeRecentQueries,
        categories
    ) { query, recents, cats ->
        YouTubeSuggestionsProvider.getSuggestions(
            rawQuery = query,
            recentQueries = recents.map { it.query },
            categorySlugs = cats.map { it.slug }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        YouTubeSuggestionsProvider.BUILT_IN_SUGGESTIONS.take(8)
    )

    private var activeSearchJob: Job? = null
    private var debounceTypingJob: Job? = null
    private var lastSearchedQuery: String? = null

    init {
        // Initialize results with curated event videos so operator can test embedded playback immediately
        _youTubeSearchResults.value = YouTubeApiClient.sampleEventVideos
    }

    /**
     * Updates search box input and triggers debounced suggestion/search as user types.
     */
    fun setYouTubeQuery(query: String) {
        _youTubeQuery.value = query
        debounceTypingJob?.cancel()

        val clean = query.trim().replace("\\s+".toRegex(), " ")
        if (clean.length >= 3 && !clean.equals(lastSearchedQuery, ignoreCase = true)) {
            debounceTypingJob = viewModelScope.launch {
                delay(800) // 800ms debounce to conserve quota and avoid request thrashing
                searchYouTube(clean, isManualSubmit = false)
            }
        }
    }

    /**
     * Executes official YouTube search with parameter validation, deduplication, and error mapping.
     */
    fun searchYouTube(rawQuery: String, isManualSubmit: Boolean = false) {
        val clean = rawQuery.trim().replace("\\s+".toRegex(), " ")
        if (clean.isBlank()) return

        _youTubeQuery.value = clean
        debounceTypingJob?.cancel()

        // Prevent redundant identical request unless manually forced
        if (!isManualSubmit && clean.equals(lastSearchedQuery, ignoreCase = true) && _youTubeSearchResults.value.isNotEmpty()) {
            return
        }

        activeSearchJob?.cancel()
        activeSearchJob = viewModelScope.launch {
            _isYouTubeSearching.value = true
            _youTubeSearchError.value = null
            _nextPageToken.value = null
            lastSearchedQuery = clean

            val directVideoId = when {
                clean.matches(Regex("^[a-zA-Z0-9_-]{11}$")) -> clean
                clean.contains("youtu.be/") -> clean.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
                clean.contains("watch?v=") -> clean.substringAfter("watch?v=").substringBefore("&")
                else -> null
            }

            // Direct sample video lookup without requiring external API call
            if (directVideoId != null && directVideoId.equals("M7lc1UVf-VE", ignoreCase = true)) {
                _isYouTubeSearching.value = false
                val sampleItem = YouTubeApiClient.sampleEventVideos.firstOrNull { it.videoId.equals("M7lc1UVf-VE", ignoreCase = true) }
                    ?: YouTubeVideoItem(
                        videoId = "M7lc1UVf-VE",
                        title = "YouTube Developers Official Demo: Submitting Your App",
                        channelTitle = "Google Developers",
                        thumbnailUrl = "https://img.youtube.com/vi/M7lc1UVf-VE/hqdefault.jpg",
                        durationText = "03:49"
                    )
                _youTubeSearchResults.value = listOf(sampleItem) + YouTubeApiClient.sampleEventVideos.filter { it.videoId != "M7lc1UVf-VE" }
                repository.recordYouTubeQuery(clean)
                return@launch
            }

            val apiKey = try {
                BuildConfig.YOUTUBE_API_KEY
            } catch (_: Exception) {
                ""
            }

            if (apiKey.isBlank() || apiKey == "DEFAULT_YOUTUBE_API_KEY" || apiKey == "MY_YOUTUBE_API_KEY") {
                _isYouTubeSearching.value = false
                _youTubeSearchError.value = "YouTube API key required. Please set YOUTUBE_API_KEY in the Secrets panel in AI Studio to enable live catalog search."
                // Provide genuine sample tracks for testing without fake titles (includes M7lc1UVf-VE)
                _youTubeSearchResults.value = YouTubeApiClient.sampleEventVideos
                repository.recordYouTubeQuery(clean)
                return@launch
            }

            try {
                val searchResponse = YouTubeApiClient.api.searchVideos(
                    part = "snippet",
                    query = clean,
                    type = "video",
                    order = "relevance",
                    maxResults = 25,
                    regionCode = "PK",
                    relevanceLanguage = "en",
                    pageToken = null,
                    apiKey = apiKey
                )

                _nextPageToken.value = searchResponse.nextPageToken

                // Use only genuine video IDs, discard non-video items
                val videoIds = searchResponse.items
                    .mapNotNull { it.id?.videoId }
                    .filter { it.isNotBlank() }

                if (videoIds.isEmpty()) {
                    _youTubeSearchResults.value = emptyList()
                    _youTubeSearchError.value = "No YouTube videos found matching \"$clean\"."
                    _isYouTubeSearching.value = false
                    repository.recordYouTubeQuery(clean)
                    return@launch
                }

                // Fetch duration metadata
                val detailsResponse = try {
                    YouTubeApiClient.api.getVideoDetails(
                        part = "contentDetails",
                        videoIds = videoIds.joinToString(","),
                        apiKey = apiKey
                    )
                } catch (_: Exception) {
                    null
                }

                val durationMap = detailsResponse?.items?.associate {
                    it.id to YouTubeApiClient.parseIsoDuration(it.contentDetails?.duration)
                } ?: emptyMap()

                val results = searchResponse.items.mapNotNull { item ->
                    val vId = item.id?.videoId ?: return@mapNotNull null
                    val snippet = item.snippet ?: return@mapNotNull null
                    val thumb = snippet.thumbnails?.high?.url
                        ?: snippet.thumbnails?.medium?.url
                        ?: snippet.thumbnails?.defaultThumb?.url
                        ?: "https://img.youtube.com/vi/$vId/hqdefault.jpg"

                    YouTubeVideoItem(
                        videoId = vId,
                        title = YouTubeApiClient.unescapeHtml(snippet.title),
                        channelTitle = YouTubeApiClient.unescapeHtml(snippet.channelTitle),
                        thumbnailUrl = thumb,
                        durationText = durationMap[vId] ?: "--:--",
                        publishedAt = snippet.publishedAt
                    )
                }

                _youTubeSearchResults.value = results
                repository.recordYouTubeQuery(clean)
            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                val errorBody = try { e.response()?.errorBody()?.string() ?: "" } catch (_: Exception) { "" }
                val message = when {
                    code == 400 || (code == 403 && errorBody.contains("keyInvalid", ignoreCase = true)) -> {
                        "Invalid YouTube API Key. Please verify YOUTUBE_API_KEY in the Secrets panel in AI Studio."
                    }
                    code == 403 && errorBody.contains("quotaExceeded", ignoreCase = true) -> {
                        "YouTube API quota exceeded for today. Please wait for quota reset or update key."
                    }
                    code == 429 -> {
                        "Too many requests sent to YouTube API. Please wait a moment and try again."
                    }
                    code in 500..599 -> {
                        "YouTube service is temporarily unavailable. Please retry in a few moments."
                    }
                    else -> "YouTube API error ($code): ${e.message()}"
                }
                _youTubeSearchError.value = message
            } catch (e: java.io.IOException) {
                _youTubeSearchError.value = "Network unavailable. Please check your internet connection and try again."
            } catch (e: Exception) {
                _youTubeSearchError.value = "Search error: ${e.localizedMessage ?: "Unable to complete YouTube search."}"
            } finally {
                _isYouTubeSearching.value = false
            }
        }
    }

    /**
     * Loads the next page of results using official YouTube Data API nextPageToken.
     */
    fun loadMoreYouTubeResults() {
        val token = _nextPageToken.value
        val query = lastSearchedQuery ?: _youTubeQuery.value.trim()
        if (token.isNullOrBlank() || query.isBlank() || _isLoadingMore.value || _isYouTubeSearching.value) {
            return
        }

        val apiKey = try { BuildConfig.YOUTUBE_API_KEY } catch (_: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "DEFAULT_YOUTUBE_API_KEY" || apiKey == "MY_YOUTUBE_API_KEY") {
            return
        }

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val searchResponse = YouTubeApiClient.api.searchVideos(
                    part = "snippet",
                    query = query,
                    type = "video",
                    order = "relevance",
                    maxResults = 25,
                    regionCode = "PK",
                    relevanceLanguage = "en",
                    pageToken = token,
                    apiKey = apiKey
                )

                _nextPageToken.value = searchResponse.nextPageToken

                val videoIds = searchResponse.items
                    .mapNotNull { it.id?.videoId }
                    .filter { it.isNotBlank() }

                if (videoIds.isNotEmpty()) {
                    val detailsResponse = try {
                        YouTubeApiClient.api.getVideoDetails(
                            part = "contentDetails",
                            videoIds = videoIds.joinToString(","),
                            apiKey = apiKey
                        )
                    } catch (_: Exception) {
                        null
                    }

                    val durationMap = detailsResponse?.items?.associate {
                        it.id to YouTubeApiClient.parseIsoDuration(it.contentDetails?.duration)
                    } ?: emptyMap()

                    val newResults = searchResponse.items.mapNotNull { item ->
                        val vId = item.id?.videoId ?: return@mapNotNull null
                        val snippet = item.snippet ?: return@mapNotNull null
                        val thumb = snippet.thumbnails?.high?.url
                            ?: snippet.thumbnails?.medium?.url
                            ?: snippet.thumbnails?.defaultThumb?.url
                            ?: "https://img.youtube.com/vi/$vId/hqdefault.jpg"

                        YouTubeVideoItem(
                            videoId = vId,
                            title = YouTubeApiClient.unescapeHtml(snippet.title),
                            channelTitle = YouTubeApiClient.unescapeHtml(snippet.channelTitle),
                            thumbnailUrl = thumb,
                            durationText = durationMap[vId] ?: "--:--",
                            publishedAt = snippet.publishedAt
                        )
                    }

                    _youTubeSearchResults.value = _youTubeSearchResults.value + newResults
                }
            } catch (_: Exception) {
                // Ignore transient pagination error or retain token
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * Retries the most recent search query.
     */
    fun retryLastYouTubeSearch() {
        val query = lastSearchedQuery ?: _youTubeQuery.value
        if (query.isNotBlank()) {
            searchYouTube(query, isManualSubmit = true)
        }
    }

    /**
     * Executes YouTube search mapped from a Sound Operator event category.
     */
    fun searchYouTubeCategory(categorySlug: String) {
        val mappedQuery = YouTubeSuggestionsProvider.getCategoryYouTubeQuery(categorySlug)
        searchYouTube(mappedQuery, isManualSubmit = true)
    }

    fun playYouTubeVideo(video: YouTubeVideoItem) {
        // Pause local offline player so audios do not overlap on event PA
        playerManager.pause()
        _activeYouTubeVideo.value = video
        viewModelScope.launch {
            repository.recordYouTubeView(
                videoId = video.videoId,
                title = video.title,
                channelTitle = video.channelTitle,
                thumbnailUrl = video.thumbnailUrl,
                durationText = video.durationText
            )
        }
    }

    fun closeYouTubeVideo() {
        _activeYouTubeVideo.value = null
    }

    fun deleteYouTubeQuery(query: String) {
        viewModelScope.launch {
            repository.deleteYouTubeQuery(query)
        }
    }

    fun clearYouTubeHistory() {
        viewModelScope.launch {
            repository.clearYouTubeHistory()
        }
    }

    fun clearYouTubeQueries() {
        viewModelScope.launch {
            repository.clearYouTubeQueries()
        }
    }
}
