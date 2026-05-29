package com.perpetuitylab.livenews

import androidx.compose.runtime.Composable
import com.perpetuitylab.livenews.navigation.LiveNewsNavigation
import com.perpetuitylab.livenews.playback.PlayerPictureInPictureController

@Composable
fun MainNavigation(pictureInPictureController: PlayerPictureInPictureController? = null) {
  LiveNewsNavigation(pictureInPictureController = pictureInPictureController)
}
