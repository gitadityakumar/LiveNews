package com.perpetuitylab.livenews.ui.home

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perpetuitylab.livenews.data.LiveNewsPreferences
import com.perpetuitylab.livenews.data.NewsChannel
import com.perpetuitylab.livenews.data.Region
import com.perpetuitylab.livenews.data.liveNewsDataStore
import com.perpetuitylab.livenews.playback.PlayerPictureInPictureController
import com.perpetuitylab.livenews.theme.LiveNewsAccent
import com.perpetuitylab.livenews.theme.LiveNewsAccentSoft
import com.perpetuitylab.livenews.theme.LiveNewsBackground
import com.perpetuitylab.livenews.theme.LiveNewsBorder
import com.perpetuitylab.livenews.theme.LiveNewsBorderSelected
import com.perpetuitylab.livenews.theme.LiveNewsBottomBar
import com.perpetuitylab.livenews.theme.LiveNewsBottomTab
import com.perpetuitylab.livenews.theme.LiveNewsBottomBarHeight
import com.perpetuitylab.livenews.theme.LiveNewsDragHandleIcon
import com.perpetuitylab.livenews.theme.LiveNewsSurface
import com.perpetuitylab.livenews.theme.LiveNewsSurfaceSelected
import com.perpetuitylab.livenews.theme.LiveNewsSurfaceStrong
import com.perpetuitylab.livenews.theme.LiveNewsTextPrimary
import com.perpetuitylab.livenews.theme.LiveNewsTextSecondary
import com.perpetuitylab.livenews.theme.LiveNewsTextTertiary
import com.perpetuitylab.livenews.ui.player.DraggableVideoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
  onOpenSettings: () -> Unit,
  onOpenNetworkInspector: (channelId: Int, pageUrl: String) -> Unit,
  pictureInPictureController: PlayerPictureInPictureController? = null,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current.applicationContext
  val preferences = remember(context) { LiveNewsPreferences(context.liveNewsDataStore) }
  val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(preferences))
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val reloadRequest = state.reloadRequest

  LaunchedEffect(reloadRequest) {
    if (reloadRequest != null) {
      onOpenNetworkInspector(reloadRequest.channelId, reloadRequest.pageUrl)
      viewModel.consumeReloadRequest()
    }
  }

  HomeScreen(
    state = state,
    onRegionSelected = viewModel::selectRegion,
    onChannelSelected = viewModel::selectChannel,
    onReloadChannel = viewModel::requestReload,
    onMoveChannelToIndex = viewModel::moveChannelToIndex,
    onScrollStart = viewModel::onScrollStart,
    onScrollOffsetChange = viewModel::onScrollOffsetChange,
    onOpenSettings = onOpenSettings,
    pictureInPictureController = pictureInPictureController,
    modifier = modifier,
  )
}

@Composable
private fun HomeScreen(
  state: HomeUiState,
  onRegionSelected: (Region) -> Unit,
  onChannelSelected: (NewsChannel) -> Unit,
  onReloadChannel: (Int) -> Unit,
  onMoveChannelToIndex: (Int, Int) -> Unit,
  onScrollStart: (Int) -> Unit,
  onScrollOffsetChange: (Int) -> Unit,
  onOpenSettings: () -> Unit,
  pictureInPictureController: PlayerPictureInPictureController?,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  var isFullscreen by remember { mutableStateOf(false) }
  var playerCollapseProgress by remember { mutableStateOf(0f) }
  val isInPipMode = pictureInPictureController?.state?.isInPictureInPictureMode == true
  val safeTopInset = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()

  LaunchedEffect(state.region) { onScrollStart(0) }
  LaunchedEffect(listState) {
    snapshotFlow { listState.firstVisibleItemIndex * 1_000 + listState.firstVisibleItemScrollOffset }
      .distinctUntilChanged()
      .collect { offset -> onScrollOffsetChange(offset) }
  }
  ApplyFullscreenChrome(isFullscreen)

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = LiveNewsBackground,
    bottomBar = {
      AnimatedVisibility(visible = state.isBottomBarVisible && !isFullscreen && !isInPipMode) {
        LiveNewsBottomBar(selectedTab = LiveNewsBottomTab.Home, onHomeClick = {}, onSettingsClick = onOpenSettings)
      }
    },
  ) { padding ->
    Box(modifier = Modifier.fillMaxSize()) {
      BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
        val expandedVideoHeight = maxWidth * 9f / 16f
        val listTopPadding by animateDpAsState(
          targetValue = lerp(expandedVideoHeight + safeTopInset + 10.dp, safeTopInset + 12.dp, playerCollapseProgress),
          animationSpec = tween(durationMillis = 180),
          label = "listTopPadding",
        )

        if (!isInPipMode) {
          AnimatedContent(
            targetState = state.region,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
              val toStart = targetState.ordinal > initialState.ordinal
              val enter = slideInHorizontally(animationSpec = tween(220)) { fullWidth -> if (toStart) fullWidth / 4 else -fullWidth / 4 } + fadeIn(
                animationSpec = tween(220),
              )
              val exit = slideOutHorizontally(animationSpec = tween(220)) { fullWidth -> if (toStart) -fullWidth / 4 else fullWidth / 4 } + fadeOut(
                animationSpec = tween(220),
              )
              enter togetherWith exit
            },
            label = "regionTransition",
          ) { region ->
            LazyColumn(
              state = listState,
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = listTopPadding, bottom = 0.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              item { RegionSelector(selectedRegion = region, onRegionSelected = onRegionSelected) }

              itemsIndexed(state.channels, key = { _, channel -> channel.id }) { index, channel ->
                var isVisible by remember(region, channel.id) { mutableStateOf(false) }
                LaunchedEffect(region, channel.id) {
                  delay(index * REGION_CARD_STAGGER_MS)
                  isVisible = true
                }

                AnimatedVisibility(
                  visible = isVisible,
                  enter =
                    fadeIn(animationSpec = tween(durationMillis = 260)) +
                      slideInVertically(animationSpec = tween(durationMillis = 320)) { height -> height / 3 },
                ) {
                  ChannelCard(
                    channel = channel,
                    isSelected = channel.streamIndex == state.selectedStreamIndex,
                    showReload = region == Region.USA,
                    onClick = { onChannelSelected(channel) },
                    onReload = { onReloadChannel(channel.id) },
                    onMoveToIndex = { targetIndex -> onMoveChannelToIndex(channel.id, targetIndex) },
                    index = index,
                    totalItems = state.channels.size,
                  )
                }
              }
            }
          }
        }
      }

      DraggableVideoPlayer(
        streamUrl = state.currentUrl,
        isFullscreen = isFullscreen,
        onFullscreenChange = { isFullscreen = it },
        onCollapseProgressChange = { playerCollapseProgress = it },
        bottomNavigationHeight = if (state.isBottomBarVisible && !isFullscreen && !isInPipMode) LiveNewsBottomBarHeight else 0.dp,
        pictureInPictureController = pictureInPictureController,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}

@Composable
private fun ApplyFullscreenChrome(isFullscreen: Boolean) {
  val activity = LocalContext.current.findActivity()

  DisposableEffect(activity, isFullscreen) {
    if (activity != null) {
      val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
      if (isFullscreen) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      } else {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        controller.show(WindowInsetsCompat.Type.systemBars())
      }
    }

    onDispose {
      if (activity != null) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).show(WindowInsetsCompat.Type.systemBars())
      }
    }
  }
}

@Composable
private fun RegionSelector(selectedRegion: Region, onRegionSelected: (Region) -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(LiveNewsSurface)
        .border(1.dp, LiveNewsBorder, RoundedCornerShape(8.dp))
        .padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Region.entries.forEach { region ->
      Button(
        onClick = { onRegionSelected(region) },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = if (selectedRegion == region) LiveNewsAccent else Color.Transparent,
            contentColor = LiveNewsTextPrimary,
          ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
      ) {
        Text(text = region.label, maxLines = 1)
      }
    }
  }
}

@Composable
private fun ChannelCard(
  channel: NewsChannel,
  isSelected: Boolean,
  showReload: Boolean,
  onClick: () -> Unit,
  onReload: () -> Unit,
  onMoveToIndex: (Int) -> Unit,
  index: Int,
  totalItems: Int,
) {
  val borderColor = if (isSelected) LiveNewsBorderSelected else LiveNewsBorder
  val background = if (isSelected) LiveNewsSurfaceSelected else LiveNewsSurface
  val density = LocalDensity.current.density
  var dragDistanceDp by remember { mutableFloatStateOf(0f) }
  var isDraggingCard by remember { mutableStateOf(false) }
  val estimatedCardHeightDp = 98f

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .offset { IntOffset(0, if (isDraggingCard) (dragDistanceDp * density).roundToInt() else 0) }
        .zIndex(if (isDraggingCard) 50f else 0f)
        .graphicsLayer {
          if (isDraggingCard) {
            scaleX = 1.02f
            scaleY = 1.02f
            alpha = 0.96f
            shadowElevation = 18.dp.toPx()
          } else {
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
            shadowElevation = 0f
          }
        }
        .clip(RoundedCornerShape(10.dp))
        .background(background)
        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
        .clickable(onClick = onClick)
        .pointerInput(index, totalItems) {
          detectDragGesturesAfterLongPress(
            onDragStart = {
              isDraggingCard = true
            },
            onDrag = { change, dragAmount ->
              change.consume()
              dragDistanceDp += dragAmount.y / density
            },
            onDragEnd = {
              val deltaItems = (dragDistanceDp / estimatedCardHeightDp).roundToInt()
              dragDistanceDp = 0f
              isDraggingCard = false
              if (deltaItems == 0) return@detectDragGesturesAfterLongPress
              val targetIndex = (index + deltaItems).coerceIn(0, totalItems - 1)
              if (targetIndex != index) {
                onMoveToIndex(targetIndex)
              }
            },
            onDragCancel = {
              dragDistanceDp = 0f
              isDraggingCard = false
            },
          )
        }
        .padding(horizontal = 10.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.width(3.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(if (isSelected) LiveNewsAccent else Color.Transparent),
    )
    Spacer(modifier = Modifier.width(8.dp))
    LiveNewsDragHandleIcon(color = LiveNewsTextTertiary, modifier = Modifier.size(18.dp))
    Spacer(modifier = Modifier.width(8.dp))
    Box(
      modifier =
        Modifier.size(44.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(if (isSelected) LiveNewsAccentSoft else LiveNewsSurfaceStrong)
          .border(1.dp, if (isSelected) LiveNewsBorderSelected else LiveNewsBorder, RoundedCornerShape(8.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Text(text = initials(channel.name), color = LiveNewsTextPrimary, fontWeight = FontWeight.SemiBold)
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = channel.name, color = LiveNewsTextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = channel.category, color = LiveNewsTextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    if (showReload) {
      IconButton(
        onClick = onReload,
        modifier =
          Modifier
            .size(36.dp)
            .background(LiveNewsAccentSoft, CircleShape)
            .border(1.dp, LiveNewsBorderSelected, CircleShape)
            .semantics { contentDescription = "Refresh stream" },
      ) {
        Icon(
          imageVector = Icons.Rounded.Refresh,
          contentDescription = null,
          tint = LiveNewsTextSecondary,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

private fun initials(name: String): String = name.split(" ").mapNotNull { word -> word.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)

private const val REGION_CARD_STAGGER_MS = 55L

private fun lerp(start: androidx.compose.ui.unit.Dp, stop: androidx.compose.ui.unit.Dp, fraction: Float): androidx.compose.ui.unit.Dp {
  val clamped = fraction.coerceIn(0f, 1f)
  return start + (stop - start) * clamped
}

private tailrec fun Context.findActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }
