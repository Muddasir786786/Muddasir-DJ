package com.example.data.youtube

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
import java.util.regex.Pattern

// --- Data Models for YouTube Data API v3 ---

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(
    val nextPageToken: String? = null,
    val prevPageToken: String? = null,
    val items: List<YouTubeSearchItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchItem(
    val id: YouTubeResourceId? = null,
    val snippet: YouTubeSnippet? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeResourceId(
    val kind: String? = null,
    val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSnippet(
    val title: String = "",
    val description: String = "",
    val channelTitle: String = "",
    val publishedAt: String = "",
    val thumbnails: YouTubeThumbnails? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnails(
    @Json(name = "default") val defaultThumb: YouTubeThumbnail? = null,
    val medium: YouTubeThumbnail? = null,
    val high: YouTubeThumbnail? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnail(
    val url: String = ""
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoDetailsResponse(
    val items: List<YouTubeVideoDetailItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoDetailItem(
    val id: String = "",
    val contentDetails: YouTubeContentDetails? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeContentDetails(
    val duration: String? = null
)

// UI Model for Video Cards
data class YouTubeVideoItem(
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val durationText: String,
    val publishedAt: String = ""
)

// --- Retrofit Interface ---

interface YouTubeApi {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("order") order: String = "relevance",
        @Query("maxResults") maxResults: Int = 25,
        @Query("regionCode") regionCode: String = "PK",
        @Query("relevanceLanguage") relevanceLanguage: String = "en",
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    @GET("youtube/v3/videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "contentDetails",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): YouTubeVideoDetailsResponse
}

object YouTubeApiClient {
    private const val BASE_URL = "https://www.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val api: YouTubeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(YouTubeApi::class.java)
    }

    /**
     * Parses ISO-8601 duration string (e.g., "PT3M45S", "PT1H2M30S") into standard MM:SS or HH:MM:SS
     */
    fun parseIsoDuration(duration: String?): String {
        if (duration.isNullOrBlank()) return "--:--"
        try {
            val pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val matcher = pattern.matcher(duration)
            if (matcher.matches()) {
                val hoursStr = matcher.group(1)
                val minutesStr = matcher.group(2)
                val secondsStr = matcher.group(3)

                val hours = hoursStr?.toIntOrNull() ?: 0
                val minutes = minutesStr?.toIntOrNull() ?: 0
                val seconds = secondsStr?.toIntOrNull() ?: 0

                return if (hours > 0) {
                    String.format("%d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%02d:%02d", minutes, seconds)
                }
            }
        } catch (_: Exception) {}
        return "--:--"
    }

    /**
     * Unescapes HTML entities in video titles and descriptions returned by YouTube Data API
     */
    fun unescapeHtml(text: String): String {
        return try {
            android.text.Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
        } catch (_: Exception) {
            text.replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
        }
    }

    /**
     * Curated sample wedding & event tracks for immediate live testing,
     * ensuring sound operators have functional official video IDs even before setting up their API key.
     * Includes the official YouTube Developer sample video M7lc1UVf-VE.
     */
    val sampleEventVideos = listOf(
        YouTubeVideoItem(
            videoId = "M7lc1UVf-VE",
            title = "YouTube Developers Official Demo: Submitting Your App",
            channelTitle = "Google Developers",
            thumbnailUrl = "https://img.youtube.com/vi/M7lc1UVf-VE/hqdefault.jpg",
            durationText = "03:49"
        ),
        YouTubeVideoItem(
            videoId = "jNQXAC9IVRw",
            title = "Me at the zoo - Official First Video on YouTube",
            channelTitle = "jawed",
            thumbnailUrl = "https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg",
            durationText = "00:19"
        ),
        YouTubeVideoItem(
            videoId = "k85mRPqvMbE",
            title = "Big Buck Bunny - Open Movie Celebration Project",
            channelTitle = "Blender Animation Studio",
            thumbnailUrl = "https://img.youtube.com/vi/k85mRPqvMbE/hqdefault.jpg",
            durationText = "09:56"
        ),
        YouTubeVideoItem(
            videoId = "9bZkp7q19f0",
            title = "Celebration Dance Floor Energetic Anthem",
            channelTitle = "Global Dance Rhythms",
            thumbnailUrl = "https://img.youtube.com/vi/9bZkp7q19f0/hqdefault.jpg",
            durationText = "04:13"
        ),
        YouTubeVideoItem(
            videoId = "dQw4w9WgXcQ",
            title = "Grand Reception Celebration Party Anthem",
            channelTitle = "Retro Pop Classics",
            thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
            durationText = "03:33"
        )
    )
}
