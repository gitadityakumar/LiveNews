package com.perpetuitylab.livenews.playback

import android.app.Activity
import android.app.PictureInPictureParams
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class PlayerPictureInPictureState(
  val sourceRectHint: Rect? = null,
  val aspectRatio: Rational = Rational(16, 9),
  val isInPictureInPictureMode: Boolean = false,
)

@Stable
interface PlayerPictureInPictureController {
  val state: PlayerPictureInPictureState

  fun updateSourceRect(sourceRectHint: Rect?, aspectRatio: Rational = Rational(16, 9))

  fun buildPictureInPictureParams(autoEnterEnabled: Boolean = true): PictureInPictureParams?

  fun enterPictureInPicture(activity: Activity, autoEnterEnabled: Boolean = true): Boolean

  fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean)
}

@Stable
private class DefaultPlayerPictureInPictureController : PlayerPictureInPictureController {
  override var state by mutableStateOf(PlayerPictureInPictureState())
    private set

  override fun updateSourceRect(sourceRectHint: Rect?, aspectRatio: Rational) {
    state = state.copy(sourceRectHint = sourceRectHint, aspectRatio = aspectRatio)
  }

  override fun buildPictureInPictureParams(autoEnterEnabled: Boolean): PictureInPictureParams? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null

    val params =
      PictureInPictureParams.Builder()
        .setAspectRatio(state.aspectRatio)
        .apply {
          state.sourceRectHint?.let(::setSourceRectHint)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setAutoEnterEnabled(autoEnterEnabled)
          }
        }

    return params.build()
  }

  override fun enterPictureInPicture(activity: Activity, autoEnterEnabled: Boolean): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    val params = buildPictureInPictureParams(autoEnterEnabled) ?: return false
    return activity.enterPictureInPictureMode(params)
  }

  override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
    state = state.copy(isInPictureInPictureMode = isInPictureInPictureMode)
  }
}

@Composable
fun rememberPlayerPictureInPictureController(): PlayerPictureInPictureController =
  remember { DefaultPlayerPictureInPictureController() }
