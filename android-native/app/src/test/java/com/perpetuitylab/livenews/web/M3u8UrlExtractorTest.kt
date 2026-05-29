package com.perpetuitylab.livenews.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class M3u8UrlExtractorTest {
  @Test
  fun extractsRawPlaylistAndPreservesQuery() {
    val url = "https://cdn.example.com/live/master.m3u8?token=a%2Fb&expires=123"

    assertEquals(url, M3u8UrlExtractor.extract(url))
  }

  @Test
  fun extractsPreferredParam8BeforeOtherParameters() {
    val input =
      "https://player.example.com/watch?backup=https%3A%2F%2Fcdn.example.com%2Fbackup.m3u8&param8=https%3A%2F%2Fcdn.example.com%2Fprimary.m3u8%3Ftoken%3Dabc%252F123%26quality%3Dauto"

    assertEquals(
      "https://cdn.example.com/primary.m3u8?token=abc%2F123&quality=auto",
      M3u8UrlExtractor.extract(input),
    )
  }

  @Test
  fun extractsUrlParameter() {
    val input = "https://player.example.com/embed?url=https%3A%2F%2Fmedia.example.com%2Fplaylist.m3u8%3Fsid%3D42"

    assertEquals("https://media.example.com/playlist.m3u8?sid=42", M3u8UrlExtractor.extract(input))
  }

  @Test
  fun extractsStreamUrlParameter() {
    val input = "https://player.example.com/embed?streamUrl=https%3A%2F%2Fmedia.example.com%2Fmaster.m3u8"

    assertEquals("https://media.example.com/master.m3u8", M3u8UrlExtractor.extract(input))
  }

  @Test
  fun extractsAnyQueryParamContainingPlaylist() {
    val input = "https://player.example.com/embed?payload=https%3A%2F%2Fmedia.example.com%2Findex.M3U8%3Fx%3D1"

    assertEquals("https://media.example.com/index.M3U8?x=1", M3u8UrlExtractor.extract(input))
  }

  @Test
  fun returnsNullWhenNoPlaylistCandidateExists() {
    assertNull(M3u8UrlExtractor.extract("https://player.example.com/embed?url=https%3A%2F%2Fexample.com%2Fvideo.mp4"))
  }
}
