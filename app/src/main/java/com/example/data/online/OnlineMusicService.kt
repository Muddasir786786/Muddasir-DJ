package com.example.data.online

import com.example.data.entity.SongEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OnlineMusicSearchResponse(
    val resultCount: Int = 0,
    val results: List<OnlineMusicTrack> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OnlineMusicTrack(
    val trackId: Long = 0,
    val trackName: String? = null,
    val artistName: String? = null,
    val collectionName: String? = null,
    val previewUrl: String? = null,
    val artworkUrl100: String? = null,
    val trackTimeMillis: Long? = 30000L,
    val primaryGenreName: String? = null,
    val releaseDate: String? = null
)

fun OnlineMusicTrack.toSongEntity(): SongEntity {
    // Generate negative ID for online tracks to avoid Room primary key collision with positive local IDs
    val safeId = if (trackId != 0L) -trackId else -(System.nanoTime() % 100000000L)
    return SongEntity(
        id = safeId,
        title = trackName ?: "Online Track",
        artist = artistName ?: "Online Artist",
        album = collectionName ?: (primaryGenreName ?: "Online Streaming"),
        durationMs = trackTimeMillis ?: 30000L,
        filePath = previewUrl ?: "",
        bpm = 128,
        musicalKey = "8A / Am",
        cueNotes = "Legitimate Online Stream • ${primaryGenreName ?: "Music"} • Background & Screen-Off Ready",
        coverColorHex = "#00E5FF",
        isLocal = false
    )
}

interface OnlineMusicApi {
    @GET("search")
    suspend fun searchTracks(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 30
    ): OnlineMusicSearchResponse
}

object OnlineMusicClient {
    private const val BASE_URL = "https://itunes.apple.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            }
        )
        .build()

    val api: OnlineMusicApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OnlineMusicApi::class.java)
    }

    /**
     * Curated legitimate wedding and DJ presets for sound operators when searching common event categories.
     */
    val curatedEventPresets = listOf(
        "Punjabi Wedding Dhol",
        "Bhangra Remix",
        "Mehndi Night Dance",
        "Baraat High Energy",
        "Walima Reception Instrumental",
        "Couple Romantic First Dance",
        "Grand Bridal Entrance",
        "DJ Bass Drop Party Starter"
    )
}
