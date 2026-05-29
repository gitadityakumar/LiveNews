package com.perpetuitylab.livenews.ui.player

import android.graphics.Rect
import android.util.Rational
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.perpetuitylab.livenews.playback.Media3HlsPlayer
import com.perpetuitylab.livenews.playback.PlayerPictureInPictureController
import com.perpetuitylab.livenews.playback.VideoResizeMode
import com.perpetuitylab.livenews.playback.rememberLiveStreamPlayerController
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DraggableVideoPlayer(
  streamUrl: String,
  isFullscreen: Boolean,
  onFullscreenChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  bottomNavigationHeight: Dp = 88.dp,
  pictureInPictureController: PlayerPictureInPictureController? = null,
) {
  val controller = rememberLiveStreamPlayerController(streamUrl = streamUrl, autoplay = true)
  val density = LocalDensity.current
  val safePadding = WindowInsets.safeDrawing.asPaddingValues()

  var isMinimized by remember { mutableStateOf(false) }
  var miniOffset by remember { mutableStateOf(Offset.Zero) }
  var resizeMode by remember { mutableStateOf(VideoResizeMode.Fit) }
  var showControls by remember { mutableStateOf(true) }
  var accumulatedZoom by remember { mutableFloatStateOf(1f) }
  var isDraggingMinimize by remember { mutableStateOf(false) }
  var dragMinimizeProgress by remember { mutableFloatStateOf(0f) }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val maxWidthDp = maxWidth
    val maxHeightDp = maxHeight
    val expandedHeight = maxWidthDp * 9f / 16f
    val miniWidth = 180.dp
    val miniHeight = miniWidth * 9f / 16f

    val maxWidthPx = with(density) { maxWidthDp.toPx() }
    val maxHeightPx = with(density) { maxHeightDp.toPx() }
    val miniWidthPx = with(density) { miniWidth.toPx() }
    val miniHeightPx = with(density) { miniHeight.toPx() }
    val marginPx = with(density) { 16.dp.toPx() }
    val bottomSafePx = with(density) { safePadding.calculateBottomPadding().toPx() }
    val topSafePx = with(density) { safePadding.calculateTopPadding().toPx() }
    val bottomNavPx = with(density) { bottomNavigationHeight.toPx() }

    fun clampMiniOffset(value: Offset): Offset {
      val minX = marginPx
      val maxX = (maxWidthPx - miniWidthPx - marginPx).coerceAtLeast(minX)
      val minY = topSafePx + marginPx
      val maxY = (maxHeightPx - miniHeightPx - bottomSafePx - bottomNavPx - marginPx).coerceAtLeast(minY)
      return Offset(value.x.coerceIn(minX, maxX), value.y.coerceIn(minY, maxY))
    }

    val defaultMiniOffset =
      clampMiniOffset(
        Offset(
          x = maxWidthPx - miniWidthPx - marginPx,
          y = maxHeightPx - miniHeightPx - bottomSafePx - bottomNavPx - marginPx,
        ),
      )

    LaunchedEffect(maxWidthPx, maxHeightPx, bottomNavPx) {
      miniOffset = clampMiniOffset(if (miniOffset == Offset.Zero) defaultMiniOffset else miniOffset)
    }

    LaunchedEffect(isFullscreen) {
      if (isFullscreen) {
        isMinimized = false
        showControls = true
      } else {
        resizeMode = VideoResizeMode.Fit
      }
    }

    val targetMinimizeProgress =
      when {
        isFullscreen -> 0f
        isDraggingMinimize -> dragMinimizeProgress
        isMinimized -> 1f
        else -> 0f
      }
    val progress by animateFloatAsState(
      targetValue = targetMinimizeProgress,
      animationSpec = if (isDraggingMinimize) snap() else tween(durationMillis = 220),
      label = "minimizeProgress",
    )
    val playerWidth = lerp(maxWidthDp, miniWidth, progress)
    val playerHeight = if (isFullscreen) maxHeightDp else lerp(expandedHeight, miniHeight, progress)
    val playerRadius = lerp(0.dp, 12.dp, progress)
    val playerOffset = if (isFullscreen) Offset.Zero else lerp(Offset.Zero, miniOffset, progress)

    LaunchedEffect(showControls, isMinimized) {
      if (showControls && !isMinimized) {
        delay(3_000)
        showControls = false
      }
    }

    Box(
      modifier =
        Modifier
          .offset { IntOffset(playerOffset.x.roundToInt(), playerOffset.y.roundToInt()) }
          .width(playerWidth)
          .height(playerHeight)
          .zIndex(if (isFullscreen || isMinimized) 100f else 10f)
          .clip(RoundedCornerShape(playerRadius))
          .background(Color.Black)
          .onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            pictureInPictureController?.updateSourceRect(bounds.toAndroidRect(), bounds.toRational())
          },
    ) {
      Media3HlsPlayer(controller = controller, resizeMode = resizeMode, modifier = Modifier.fillMaxSize())

      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .zIndex(if (isMinimized) 4f else 1f)
            .pointerInput(isMinimized, isFullscreen, maxWidthPx, maxHeightPx, resizeMode) {
              detectPlayerGestures(
                onTap = {
                  if (isMinimized) {
                    isMinimized = false
                  } else {
                    showControls = true
                  }
                },
                onDragStart = {
                  if (!isFullscreen && !isMinimized) {
                    isDraggingMinimize = true
                    miniOffset = defaultMiniOffset
                    dragMinimizeProgress = 0f
                  }
                },
                onDrag = { drag, totalDrag ->
                  when {
                    isMinimized -> miniOffset = clampMiniOffset(miniOffset + drag)
                    !isFullscreen && !isMinimized && totalDrag.y > 0f && abs(totalDrag.y) >= abs(totalDrag.x) -> {
                      dragMinimizeProgress = (totalDrag.y / MINIMIZE_DRAG_DISTANCE_PX).coerceIn(0f, 1f)
                    }
                  }
                },
                onDragEnd = { drag, velocity ->
                  val fastUp = velocity.y < -FLING_VELOCITY_PX
                  val fastDown = velocity.y > FLING_VELOCITY_PX
                  when {
                    isFullscreen && (drag.y > FULLSCREEN_EXIT_DRAG_PX || fastDown) -> onFullscreenChange(false)
                    !isFullscreen && !isMinimized && (drag.y < -FULLSCREEN_ENTER_DRAG_PX || fastUp) -> onFullscreenChange(true)
                    !isFullscreen && !isMinimized && (dragMinimizeProgress > 0.36f || drag.y > MINIMIZE_COMMIT_DRAG_PX || fastDown) -> {
                      miniOffset = defaultMiniOffset
                      isMinimized = true
                    }
                    !isFullscreen && !isMinimized -> isMinimized = false
                    isMinimized && (drag.y < MINI_EXPAND_DRAG_PX || fastUp) -> isMinimized = false
                    isMinimized -> miniOffset = clampMiniOffset(miniOffset)
                  }
                  isDraggingMinimize = false
                },
                onGestureCancel = {
                  if (isDraggingMinimize) {
                    isDraggingMinimize = false
                    isMinimized = false
                  }
                },
                onPinch = { zoom ->
                  if (!isFullscreen) return@detectPlayerGestures
                  accumulatedZoom *= zoom
                  if (accumulatedZoom > PINCH_FILL_THRESHOLD && resizeMode != VideoResizeMode.Fill) {
                    resizeMode = VideoResizeMode.Fill
                    accumulatedZoom = 1f
                  } else if (accumulatedZoom < PINCH_FIT_THRESHOLD && resizeMode != VideoResizeMode.Fit) {
                    resizeMode = VideoResizeMode.Fit
                    accumulatedZoom = 1f
                  }
                },
              )
            },
      )

      if (controller.state.isLoading) {
        PlayerLoadingOverlay(Modifier.fillMaxSize().zIndex(2f))
      }

      if (!isMinimized && showControls) {
        PlayerControlsOverlay(
          state = controller.state,
          resizeMode = resizeMode,
          isFullscreen = isFullscreen,
          modifier = Modifier.fillMaxSize().zIndex(3f).background(Color.Black.copy(alpha = 0.18f)),
          onPlayPause = {
            showControls = true
            controller.togglePlayPause()
          },
          onMute = {
            showControls = true
            controller.toggleMuted()
          },
          onJumpToLive = {
            showControls = true
            controller.jumpToLive()
          },
          onFullscreen = {
            showControls = true
            onFullscreenChange(!isFullscreen)
          },
          onResizeMode = {
            showControls = true
            resizeMode = if (resizeMode == VideoResizeMode.Fit) VideoResizeMode.Fill else VideoResizeMode.Fit
          },
        )
      }
    }

    if (isMinimized) {
      Box(
        modifier =
          Modifier
            .offset { IntOffset(playerOffset.x.roundToInt(), playerOffset.y.roundToInt()) }
            .size(playerWidth, playerHeight)
            .zIndex(200f)
            .pointerInput(maxWidthPx, maxHeightPx) {
              detectPlayerGestures(
                onTap = {
                  isMinimized = false
                  showControls = true
                },
                onDragStart = {},
                onDrag = { drag, _ ->
                  miniOffset = clampMiniOffset(miniOffset + drag)
                },
                onDragEnd = { drag, velocity ->
                  val fastUp = velocity.y < -FLING_VELOCITY_PX
                  if (drag.getDistance() < MINI_TAP_EXPAND_DISTANCE_PX || drag.y < MINI_EXPAND_DRAG_PX || fastUp) {
                    isMinimized = false
                    showControls = true
                  } else {
                    miniOffset = clampMiniOffset(miniOffset)
                  }
                },
                onGestureCancel = {
                  miniOffset = clampMiniOffset(miniOffset)
                },
                onPinch = {},
              )
            },
      )
    }
  }
}

private enum class PlayerGestureMode {
  Undecided,
  Drag,
  Pinch,
}

private suspend fun PointerInputScope.detectPlayerGestures(
  onTap: () -> Unit,
  onDragStart: () -> Unit,
  onDrag: (drag: Offset, totalDrag: Offset) -> Unit,
  onDragEnd: (drag: Offset, velocity: Velocity) -> Unit,
  onGestureCancel: () -> Unit,
  onPinch: (zoom: Float) -> Unit,
) {
  awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false)
    val velocityTracker = VelocityTracker()
    var pointerId = down.id
    var totalDrag = Offset.Zero
    var mode = PlayerGestureMode.Undecided
    velocityTracker.addPosition(down.uptimeMillis, down.position)

    while (true) {
      val event = awaitPointerEvent()
      val pressedChanges = event.changes.filter { it.pressed }
      if (pressedChanges.isEmpty()) break

      if (pressedChanges.size >= 2 || mode == PlayerGestureMode.Pinch) {
        if (mode == PlayerGestureMode.Drag) {
          onGestureCancel()
        }
        mode = PlayerGestureMode.Pinch
        onPinch(event.twoFingerZoom())
        event.changes.forEach { it.consume() }
        continue
      }

      val change = pressedChanges.firstOrNull { it.id == pointerId } ?: pressedChanges.first()
      pointerId = change.id

      val drag = change.positionChange()
      if (drag == Offset.Zero) continue

      totalDrag += drag
      velocityTracker.addPosition(change.uptimeMillis, change.position)

      if (mode == PlayerGestureMode.Undecided && totalDrag.getDistance() > viewConfiguration.touchSlop) {
        mode = PlayerGestureMode.Drag
        onDragStart()
      }

      if (mode == PlayerGestureMode.Drag) {
        onDrag(drag, totalDrag)
        change.consume()
      }
    }

    when (mode) {
      PlayerGestureMode.Undecided -> onTap()
      PlayerGestureMode.Drag -> onDragEnd(totalDrag, velocityTracker.calculateVelocity())
      PlayerGestureMode.Pinch -> Unit
    }
  }
}

private fun PointerEvent.twoFingerZoom(): Float {
  val pressed = changes.filter { it.pressed }.take(2)
  if (pressed.size < 2) return 1f

  val previousDistance = (pressed[0].previousPosition - pressed[1].previousPosition).getDistance()
  if (previousDistance == 0f) return 1f

  val currentDistance = (pressed[0].position - pressed[1].position).getDistance()
  return currentDistance / previousDistance
}

private fun lerp(start: Dp, stop: Dp, fraction: Float): Dp =
  start + (stop - start) * fraction.coerceIn(0f, 1f)

private fun lerp(start: Offset, stop: Offset, fraction: Float): Offset {
  val coerced = fraction.coerceIn(0f, 1f)
  return Offset(
    x = start.x + (stop.x - start.x) * coerced,
    y = start.y + (stop.y - start.y) * coerced,
  )
}

private const val FLING_VELOCITY_PX = 700f
private const val FULLSCREEN_ENTER_DRAG_PX = 64f
private const val FULLSCREEN_EXIT_DRAG_PX = 64f
private const val MINI_EXPAND_DRAG_PX = -48f
private const val MINI_TAP_EXPAND_DISTANCE_PX = 36f
private const val MINIMIZE_COMMIT_DRAG_PX = 72f
private const val MINIMIZE_DRAG_DISTANCE_PX = 260f
private const val PINCH_FILL_THRESHOLD = 1.18f
private const val PINCH_FIT_THRESHOLD = 0.84f

private fun ComposeRect.toAndroidRect(): Rect =
  Rect(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())

private fun ComposeRect.toRational(): Rational {
  val width = width.roundToInt().coerceAtLeast(1)
  val height = height.roundToInt().coerceAtLeast(1)
  return Rational(width, height)
}
