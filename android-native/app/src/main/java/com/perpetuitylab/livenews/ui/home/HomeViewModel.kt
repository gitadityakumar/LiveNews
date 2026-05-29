package com.perpetuitylab.livenews.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.perpetuitylab.livenews.data.CHANNEL_URLS
import com.perpetuitylab.livenews.data.LiveNewsPreferences
import com.perpetuitylab.livenews.data.NEWS_CHANNELS
import com.perpetuitylab.livenews.data.NewsChannel
import com.perpetuitylab.livenews.data.Region
import com.perpetuitylab.livenews.data.STREAMS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeReloadRequest(val channelId: Int, val pageUrl: String)

data class HomeUiState(
  val region: Region = Region.INDIA,
  val selectedStreamIndex: Int = 0,
  val currentUrl: String = STREAMS.getValue(Region.INDIA).first(),
  val channels: List<NewsChannel> = NEWS_CHANNELS.getValue(Region.INDIA),
  val selectedChannel: NewsChannel = NEWS_CHANNELS.getValue(Region.INDIA).first(),
  val isBottomBarVisible: Boolean = true,
  val reloadRequest: HomeReloadRequest? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val preferences: LiveNewsPreferences) : ViewModel() {
  private val selectedRegion = MutableStateFlow(Region.INDIA)
  private val selectedStreamIndex = MutableStateFlow(0)
  private val isBottomBarVisible = MutableStateFlow(true)
  private val reloadRequest = MutableStateFlow<HomeReloadRequest?>(null)

  private var lastScrollOffset = 0
  private var scrollDirection = ScrollDirection.NONE

  private val channels =
    selectedRegion.flatMapLatest { region ->
      preferences.channelOrder(region).map { savedOrder -> orderedChannels(region, savedOrder) }
    }

  private val selectedChannel =
    combine(selectedRegion, selectedStreamIndex) { region, streamIndex ->
      NEWS_CHANNELS.getValue(region).firstOrNull { channel -> channel.streamIndex == streamIndex }
        ?: NEWS_CHANNELS.getValue(region).first()
    }

  private val currentUrl =
    combine(selectedRegion, selectedStreamIndex, selectedChannel) { region, streamIndex, channel ->
        StreamSelection(region = region, streamIndex = streamIndex, channel = channel)
      }
      .flatMapLatest { selection ->
        val staticUrl = STREAMS.getValue(selection.region).getOrElse(selection.streamIndex) { STREAMS.getValue(selection.region).first() }

        if (selection.region == Region.USA) {
          preferences.m3u8Link(selection.channel.id).map { cachedUrl ->
            if (!cachedUrl.isNullOrBlank() && cachedUrl.contains(".m3u8")) cachedUrl else staticUrl
          }
        } else {
          flowOf(staticUrl)
        }
      }

  private val coreState =
    combine(selectedRegion, selectedStreamIndex, currentUrl, channels) { region, streamIndex, url, orderedChannels ->
      HomeCoreState(region = region, selectedStreamIndex = streamIndex, currentUrl = url, channels = orderedChannels)
    }

  private val chromeState =
    combine(selectedChannel, isBottomBarVisible, reloadRequest) { channel, bottomBarVisible, request ->
      HomeChromeState(selectedChannel = channel, isBottomBarVisible = bottomBarVisible, reloadRequest = request)
    }

  val uiState: StateFlow<HomeUiState> =
    combine(coreState, chromeState) { core, chrome ->
        HomeUiState(
          region = core.region,
          selectedStreamIndex = core.selectedStreamIndex,
          currentUrl = core.currentUrl,
          channels = core.channels,
          selectedChannel = chrome.selectedChannel,
          isBottomBarVisible = chrome.isBottomBarVisible,
          reloadRequest = chrome.reloadRequest,
        )
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

  fun selectRegion(region: Region) {
    if (selectedRegion.value == region) return

    selectedRegion.value = region
    selectedStreamIndex.value = 0
    reloadRequest.value = null
    showBottomBar()
  }

  fun selectChannel(channel: NewsChannel) {
    selectedStreamIndex.value = channel.streamIndex
  }

  fun requestReload(channelId: Int) {
    if (selectedRegion.value != Region.USA) return

    val channel = NEWS_CHANNELS.getValue(Region.USA).firstOrNull { it.id == channelId } ?: return
    val pageUrl = pageUrlFor(channel) ?: return
    reloadRequest.value = HomeReloadRequest(channelId = channelId, pageUrl = pageUrl)
  }

  fun consumeReloadRequest() {
    reloadRequest.value = null
  }

  fun moveChannel(channelId: Int, direction: MoveDirection) {
    val region = selectedRegion.value
    val current = uiState.value.channels
    val currentIndex = current.indexOfFirst { channel -> channel.id == channelId }
    val targetIndex =
      when (direction) {
        MoveDirection.UP -> currentIndex - 1
        MoveDirection.DOWN -> currentIndex + 1
      }

    if (currentIndex == -1 || targetIndex !in current.indices) return

    val reordered = current.toMutableList()
    val channel = reordered.removeAt(currentIndex)
    reordered.add(targetIndex, channel)

    viewModelScope.launch { preferences.saveChannelOrder(region, reordered.map { it.id }) }
  }

  fun onScrollStart(offset: Int) {
    lastScrollOffset = offset.coerceAtLeast(0)
    scrollDirection = ScrollDirection.NONE
  }

  fun onScrollOffsetChange(offset: Int) {
    if (offset < 0) return

    val diff = offset - lastScrollOffset
    if (diff > 5 && offset > 50 && scrollDirection != ScrollDirection.DOWN) {
      scrollDirection = ScrollDirection.DOWN
      isBottomBarVisible.value = false
    } else if (diff < -5 && scrollDirection != ScrollDirection.UP) {
      showBottomBar()
      scrollDirection = ScrollDirection.UP
    }

    lastScrollOffset = offset
  }

  private fun showBottomBar() {
    isBottomBarVisible.value = true
  }

  private fun orderedChannels(region: Region, savedOrder: List<Int>): List<NewsChannel> {
    val defaults = NEWS_CHANNELS.getValue(region)
    if (savedOrder.isEmpty()) return defaults

    val ordered = savedOrder.mapNotNull { id -> defaults.firstOrNull { channel -> channel.id == id } }
    val orderedIds = ordered.mapTo(mutableSetOf()) { channel -> channel.id }
    val newDefaults = defaults.filterNot { channel -> channel.id in orderedIds }
    return ordered + newDefaults
  }

  private fun pageUrlFor(channel: NewsChannel): String? {
    val name = channel.name
    return when {
      name.contains("Bloomberg") -> CHANNEL_URLS.BLOOMBERG
      name.contains("ABC News") -> CHANNEL_URLS.ABC_NEWS
      name.contains("Yahoo Finance") -> CHANNEL_URLS.YAHOO_FINANCE
      name.contains("CNN") -> CHANNEL_URLS.CNN
      name.contains("CNBC") -> CHANNEL_URLS.CNBC
      else -> null
    }
  }

  private data class StreamSelection(val region: Region, val streamIndex: Int, val channel: NewsChannel)

  private data class HomeCoreState(
    val region: Region,
    val selectedStreamIndex: Int,
    val currentUrl: String,
    val channels: List<NewsChannel>,
  )

  private data class HomeChromeState(
    val selectedChannel: NewsChannel,
    val isBottomBarVisible: Boolean,
    val reloadRequest: HomeReloadRequest?,
  )

  private enum class ScrollDirection {
    UP,
    DOWN,
    NONE,
  }
}

enum class MoveDirection {
  UP,
  DOWN,
}

class HomeViewModelFactory(private val preferences: LiveNewsPreferences) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
      return HomeViewModel(preferences) as T
    }

    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
  }
}
