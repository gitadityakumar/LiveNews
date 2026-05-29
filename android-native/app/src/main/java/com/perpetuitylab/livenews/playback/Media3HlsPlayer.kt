package com.perpetuitylab.livenews.playback

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

enum class VideoResizeMode {
  Fit,
  Fill,
}

@Composable
fun Media3HlsPlayer(
  controller: LiveStreamPlayerController,
  resizeMode: VideoResizeMode,
  modifier: Modifier = Modifier,
) {
  AndroidView(
    modifier = modifier,
    factory = { context ->
      PlayerView(context).apply {
        useController = false
        controllerAutoShow = false
        keepScreenOn = true
        isClickable = false
        isFocusable = false
        player = controller.player
        this.resizeMode = resizeMode.toMedia3ResizeMode()
      }
    },
    update = { view ->
      if (view.player !== controller.player) {
        view.player = controller.player
      }
      if (view.useController) {
        view.useController = false
      }
      if (view.controllerAutoShow) {
        view.controllerAutoShow = false
      }
      if (view.isClickable) {
        view.isClickable = false
      }
      if (view.isFocusable) {
        view.isFocusable = false
      }

      val targetResizeMode = resizeMode.toMedia3ResizeMode()
      if (view.resizeMode != targetResizeMode) {
        view.resizeMode = targetResizeMode
      }
    },
    onRelease = { view ->
      view.player = null
    },
  )
}

private fun VideoResizeMode.toMedia3ResizeMode(): Int =
  when (this) {
    VideoResizeMode.Fit -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    VideoResizeMode.Fill -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
  }
