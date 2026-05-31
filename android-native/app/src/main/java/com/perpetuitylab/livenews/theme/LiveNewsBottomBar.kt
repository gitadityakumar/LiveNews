package com.perpetuitylab.livenews.theme

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class LiveNewsBottomTab {
  Home,
  Settings,
}

val LiveNewsBottomBarHeight = 64.dp

@Composable
fun LiveNewsBottomBar(
  selectedTab: LiveNewsBottomTab,
  onHomeClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val itemColors =
    NavigationBarItemDefaults.colors(
      selectedIconColor = LiveNewsTextPrimary,
      selectedTextColor = LiveNewsTextPrimary,
      indicatorColor = LiveNewsAccentSoft,
      unselectedIconColor = LiveNewsTextTertiary,
      unselectedTextColor = LiveNewsTextTertiary,
    )

  NavigationBar(
    modifier = modifier.height(LiveNewsBottomBarHeight),
    containerColor = LiveNewsBottomBarBackground,
    contentColor = LiveNewsTextPrimary,
  ) {
    NavigationBarItem(
      selected = selectedTab == LiveNewsBottomTab.Home,
      onClick = onHomeClick,
      icon = {
        Icon(
          imageVector = if (selectedTab == LiveNewsBottomTab.Home) Icons.Filled.Home else Icons.Outlined.Home,
          contentDescription = "Home",
          modifier = Modifier.size(24.dp),
        )
      },
      label = { Text("Home") },
      colors = itemColors,
    )
    NavigationBarItem(
      selected = selectedTab == LiveNewsBottomTab.Settings,
      onClick = onSettingsClick,
      icon = {
        Icon(
          imageVector = if (selectedTab == LiveNewsBottomTab.Settings) Icons.Filled.Settings else Icons.Outlined.Settings,
          contentDescription = "Settings",
          modifier = Modifier.size(24.dp),
        )
      },
      label = { Text("Settings") },
      colors = itemColors,
    )
  }
}
