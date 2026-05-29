package com.perpetuitylab.livenews.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.perpetuitylab.livenews.R
import com.perpetuitylab.livenews.theme.LiveNewsAccent
import com.perpetuitylab.livenews.theme.LiveNewsBackground
import com.perpetuitylab.livenews.theme.LiveNewsSurfaceStrong
import com.perpetuitylab.livenews.theme.LiveNewsTextPrimary
import com.perpetuitylab.livenews.theme.LiveNewsTextSecondary

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize().background(LiveNewsBackground), contentAlignment = Alignment.Center) {
    Column(
      modifier = Modifier.padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "LiveNews logo",
        modifier = Modifier.size(104.dp).clip(CircleShape),
      )
      Spacer(modifier = Modifier.height(22.dp))
      Text(
        text = "LiveNews",
        color = LiveNewsTextPrimary,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Opening live channels",
        color = LiveNewsTextSecondary,
        style = MaterialTheme.typography.bodyMedium,
      )
      Spacer(modifier = Modifier.height(22.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(LiveNewsAccent))
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(LiveNewsSurfaceStrong))
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(LiveNewsSurfaceStrong))
      }
    }
  }
}
