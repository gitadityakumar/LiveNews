package com.perpetuitylab.livenews.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.perpetuitylab.livenews.Home
import com.perpetuitylab.livenews.NetworkInspector
import com.perpetuitylab.livenews.Settings
import com.perpetuitylab.livenews.Welcome
import com.perpetuitylab.livenews.data.LiveNewsPreferences
import com.perpetuitylab.livenews.data.liveNewsDataStore
import com.perpetuitylab.livenews.playback.PlayerPictureInPictureController
import com.perpetuitylab.livenews.playback.createPlayerController
import com.perpetuitylab.livenews.playback.rememberLifecycleObserver
import com.perpetuitylab.livenews.ui.home.HomeScreen
import com.perpetuitylab.livenews.ui.network.NetworkInspectorScreen
import com.perpetuitylab.livenews.ui.settings.SettingsScreen
import com.perpetuitylab.livenews.ui.welcome.WelcomeScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LiveNewsNavigation(
  modifier: Modifier = Modifier,
  pictureInPictureController: PlayerPictureInPictureController? = null,
) {
  val backStack = rememberNavBackStack(Welcome)
  val context = LocalContext.current.applicationContext
  val preferences = remember(context) { LiveNewsPreferences(context.liveNewsDataStore) }
  val scope = rememberCoroutineScope()

  val playerController = remember(context) { createPlayerController(context) }
  playerController.rememberLifecycleObserver()

  NavDisplay(
    backStack = backStack,
    modifier = modifier,
    onBack = { backStack.removeLastOrNull() },
      entryProvider =
      entryProvider {
        entry<Welcome> {
          LaunchedEffect(Unit) {
            delay(900)
            backStack.removeLastOrNull()
            backStack.add(Home)
          }
          WelcomeScreen(modifier = Modifier.fillMaxSize())
        }

        entry<Home> {
          HomeScreen(
            playerController = playerController,
            onOpenSettings = { backStack.add(Settings) },
            onOpenNetworkInspector = { channelId, pageUrl -> backStack.add(NetworkInspector(channelId, pageUrl)) },
            pictureInPictureController = pictureInPictureController,
            modifier = Modifier,
          )
        }

        entry<Settings> {
          SettingsScreen(
            onOpenHome = { backStack.add(Home) },
            modifier = Modifier.safeDrawingPadding(),
          )
        }

        entry<NetworkInspector> { route ->
          NetworkInspectorScreen(
            channelId = route.channelId.toString(),
            pageUrl = route.pageUrl,
            onPlaylistCaptured = { channelId, url ->
              val numericChannelId = channelId.toIntOrNull() ?: return@NetworkInspectorScreen
              scope.launch {
                preferences.saveM3u8Link(numericChannelId, url)
                backStack.removeLastOrNull()
              }
            },
            modifier = Modifier.fillMaxSize(),
          )
        }
      },
  )
}
