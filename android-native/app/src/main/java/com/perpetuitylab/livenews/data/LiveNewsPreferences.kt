package com.perpetuitylab.livenews.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.liveNewsDataStore: DataStore<Preferences> by preferencesDataStore(name = "live_news_preferences")

class LiveNewsPreferences(private val dataStore: DataStore<Preferences>) {
  fun m3u8Link(channelId: Int): Flow<String?> =
    dataStore.data.catch { emit(emptyPreferences()) }.map { preferences -> preferences[m3u8Key(channelId)] }

  suspend fun getM3u8Link(channelId: Int): String? = m3u8Link(channelId).first()

  suspend fun saveM3u8Link(channelId: Int, url: String) {
    if (!url.contains(".m3u8")) return

    dataStore.edit { preferences -> preferences[m3u8Key(channelId)] = url }
  }

  suspend fun clearM3u8Link(channelId: Int) {
    dataStore.edit { preferences -> preferences.remove(m3u8Key(channelId)) }
  }

  fun channelOrder(region: Region): Flow<List<Int>> =
    dataStore.data.catch { emit(emptyPreferences()) }.map { preferences -> decodeChannelOrder(preferences[channelOrderKey(region)]) }

  suspend fun getChannelOrder(region: Region): List<Int> = channelOrder(region).first()

  suspend fun saveChannelOrder(region: Region, orderedIds: List<Int>) {
    dataStore.edit { preferences -> preferences[channelOrderKey(region)] = orderedIds.joinToString(separator = ",") }
  }

  companion object {
    fun m3u8Key(channelId: Int): Preferences.Key<String> = stringPreferencesKey("m3u8_$channelId")

    fun channelOrderKey(region: Region): Preferences.Key<String> = stringPreferencesKey("channel_order_${region.key}")

    fun decodeChannelOrder(value: String?): List<Int> =
      value
        ?.split(",")
        ?.mapNotNull { part -> part.toIntOrNull() }
        .orEmpty()
  }
}
