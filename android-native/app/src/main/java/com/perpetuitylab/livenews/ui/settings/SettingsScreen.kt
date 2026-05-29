package com.perpetuitylab.livenews.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.perpetuitylab.livenews.theme.LiveNewsAccent
import com.perpetuitylab.livenews.theme.LiveNewsBackground
import com.perpetuitylab.livenews.theme.LiveNewsBorder
import com.perpetuitylab.livenews.theme.LiveNewsBottomBar
import com.perpetuitylab.livenews.theme.LiveNewsBottomTab
import com.perpetuitylab.livenews.theme.LiveNewsSurface
import com.perpetuitylab.livenews.theme.LiveNewsSurfaceStrong
import com.perpetuitylab.livenews.theme.LiveNewsTextPrimary
import com.perpetuitylab.livenews.theme.LiveNewsTextSecondary
import com.perpetuitylab.livenews.theme.LiveNewsTextTertiary

@Composable
fun SettingsScreen(onOpenHome: () -> Unit, modifier: Modifier = Modifier) {
  var autoplay by rememberSaveable { mutableStateOf(true) }
  var dataSaver by rememberSaveable { mutableStateOf(false) }
  var syncStreams by rememberSaveable { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = LiveNewsBackground,
    bottomBar = {
      LiveNewsBottomBar(selectedTab = LiveNewsBottomTab.Settings, onHomeClick = onOpenHome, onSettingsClick = {})
    },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 28.dp),
    ) {
      Text(text = "Settings", color = LiveNewsTextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(32.dp))

      SettingsSection(title = "Playback") {
        SettingsSwitchRow(title = "Autoplay", checked = autoplay, onCheckedChange = { autoplay = it })
        SettingsSwitchRow(title = "Data Saver", checked = dataSaver, onCheckedChange = { dataSaver = it })
      }

      SettingsSection(title = "Advance") {
        SettingsSwitchRow(title = "Sync Streams", checked = syncStreams, onCheckedChange = { syncStreams = it })
      }

      SettingsSection(title = "About") {
        SettingsButton(title = "Privacy Policy", onClick = {})
        SettingsButton(title = "Terms of Service", onClick = {})
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Version 1.0.1", modifier = Modifier.fillMaxWidth(), color = LiveNewsTextTertiary, style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
  Column(modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
    Text(text = title, color = LiveNewsTextTertiary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 20.dp, bottom = 12.dp))
    content()
  }
}

@Composable
private fun SettingsSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .padding(bottom = 12.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(LiveNewsSurface)
        .border(1.dp, LiveNewsBorder, RoundedCornerShape(8.dp))
        .padding(horizontal = 20.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(text = title, color = LiveNewsTextPrimary, fontWeight = FontWeight.Medium)
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = LiveNewsTextPrimary,
          checkedTrackColor = LiveNewsAccent,
          uncheckedThumbColor = LiveNewsTextSecondary,
          uncheckedTrackColor = LiveNewsSurfaceStrong,
          uncheckedBorderColor = LiveNewsBorder,
        ),
    )
  }
}

@Composable
private fun SettingsButton(title: String, onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .padding(bottom = 12.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(LiveNewsSurface)
        .border(1.dp, LiveNewsBorder, RoundedCornerShape(8.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 18.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = title, color = LiveNewsTextPrimary, fontWeight = FontWeight.Medium)
  }
}
