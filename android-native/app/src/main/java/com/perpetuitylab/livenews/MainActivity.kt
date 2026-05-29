package com.perpetuitylab.livenews

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.perpetuitylab.livenews.playback.PlayerPictureInPictureController
import com.perpetuitylab.livenews.playback.rememberPlayerPictureInPictureController
import com.perpetuitylab.livenews.theme.LiveNewsTheme

class MainActivity : ComponentActivity() {
  private var playerPipController: PlayerPictureInPictureController? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val pipController = rememberPlayerPictureInPictureController()
      LaunchedEffect(pipController) { playerPipController = pipController }

      LiveNewsTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation(pictureInPictureController = pipController)
        }
      }
    }
  }

  override fun onUserLeaveHint() {
    super.onUserLeaveHint()
    playerPipController?.enterPictureInPicture(this)
  }

  override fun onPictureInPictureModeChanged(
    isInPictureInPictureMode: Boolean,
    newConfig: Configuration,
  ) {
    super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    playerPipController?.onPictureInPictureModeChanged(isInPictureInPictureMode)
  }
}
