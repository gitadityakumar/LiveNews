package com.perpetuitylab.livenews.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class M3u8ValidatorTest {
  @Test
  fun acceptsHttpAndHttpsPlaylistUrls() {
    assertTrue(M3u8Validator.isValidPlaylistUrl("http://cdn.example.com/live/playlist.m3u8"))
    assertTrue(M3u8Validator.isValidPlaylistUrl("https://cdn.example.com/live/master.m3u8?token=abc"))
  }

  @Test
  fun acceptsWrappedPingWhenExtractablePlaylistExists() {
    val input = "https://tracker.example.com/ping?url=https%3A%2F%2Fcdn.example.com%2Flive%2Fplaylist.m3u8%3Ftoken%3Dabc"

    assertEquals(
      "https://cdn.example.com/live/playlist.m3u8?token=abc",
      M3u8Validator.normalizeValidPlaylistUrl(input),
    )
  }

  @Test
  fun rejectsNonHttpPlaylistUrls() {
    assertFalse(M3u8Validator.isValidPlaylistUrl("ftp://cdn.example.com/live/playlist.m3u8"))
  }

  @Test
  fun rejectsUrlsWithoutPlaylistPath() {
    assertFalse(M3u8Validator.isValidPlaylistUrl("https://cdn.example.com/live/video.mp4"))
  }

  @Test
  fun rejectsYouboranqsCandidates() {
    assertFalse(M3u8Validator.isValidPlaylistUrl("https://youboranqs.example.com/live/playlist.m3u8?x=1"))
  }

  @Test
  fun rejectsPingCandidatesWithoutExtractablePlaylist() {
    assertFalse(M3u8Validator.isValidPlaylistUrl("https://tracker.example.com/ping?event=playlist.m3u8"))
  }
}
