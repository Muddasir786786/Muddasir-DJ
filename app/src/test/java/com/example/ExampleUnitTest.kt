package com.example

import com.example.data.online.OnlineMusicTrack
import com.example.data.online.toSongEntity
import com.example.data.youtube.YouTubeApiClient
import com.example.data.youtube.YouTubeSuggestionsProvider
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun official_sample_video_is_present() {
    val sampleVideo = YouTubeApiClient.sampleEventVideos.find { it.videoId == "M7lc1UVf-VE" }
    assertNotNull("M7lc1UVf-VE official sample video must be present in sampleEventVideos", sampleVideo)
    assertEquals("M7lc1UVf-VE", sampleVideo?.videoId)
  }

  @Test
  fun duration_parser_formats_correctly() {
    assertEquals("03:45", YouTubeApiClient.parseIsoDuration("PT3M45S"))
    assertEquals("1:02:30", YouTubeApiClient.parseIsoDuration("PT1H2M30S"))
    assertEquals("--:--", YouTubeApiClient.parseIsoDuration(null))
    assertEquals("--:--", YouTubeApiClient.parseIsoDuration(""))
  }

  @Test
  fun built_in_suggestions_contains_official_sample() {
    assertTrue(YouTubeSuggestionsProvider.BUILT_IN_SUGGESTIONS.contains("M7lc1UVf-VE"))
  }

  @Test
  fun online_track_converts_to_playable_song_entity() {
    val track = OnlineMusicTrack(
      trackId = 12345L,
      trackName = "Bhangra Beats",
      artistName = "DJ Star",
      collectionName = "Wedding Collection",
      previewUrl = "https://audio-ssl.itunes.apple.com/preview.m4a",
      trackTimeMillis = 30000L
    )
    val song = track.toSongEntity()
    assertFalse("Online song must have isLocal set to false", song.isLocal)
    assertEquals("https://audio-ssl.itunes.apple.com/preview.m4a", song.filePath)
    assertEquals("Bhangra Beats", song.title)
    assertTrue("Online song must have negative ID to avoid Room local collision", song.id < 0)
  }
}
