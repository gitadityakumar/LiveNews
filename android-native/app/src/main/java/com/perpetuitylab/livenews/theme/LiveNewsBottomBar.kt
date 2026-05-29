package com.perpetuitylab.livenews.theme

import androidx.compose.foundation.layout.size
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

  NavigationBar(modifier = modifier, containerColor = LiveNewsBottomBarBackground, contentColor = LiveNewsTextPrimary) {
    NavigationBarItem(
      selected = selectedTab == LiveNewsBottomTab.Home,
      onClick = onHomeClick,
      icon = { LiveNewsHomeIcon(color = iconColor(selectedTab == LiveNewsBottomTab.Home), modifier = Modifier.size(24.dp)) },
      label = { Text("Home") },
      colors = itemColors,
    )
    NavigationBarItem(
      selected = selectedTab == LiveNewsBottomTab.Settings,
      onClick = onSettingsClick,
      icon = { LiveNewsSettingsIcon(color = iconColor(selectedTab == LiveNewsBottomTab.Settings), modifier = Modifier.size(24.dp)) },
      label = { Text("Settings") },
      colors = itemColors,
    )
  }
}

private fun iconColor(selected: Boolean) = if (selected) LiveNewsTextPrimary else LiveNewsTextTertiary
