package com.perpetuitylab.livenews.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perpetuitylab.livenews.playback.LiveStreamPlayerState
import com.perpetuitylab.livenews.playback.VideoResizeMode

@Composable
internal fun PlayerLoadingOverlay(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.background(Color.Black.copy(alpha = 0.48f)),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(color = Color(0xFF3B82F6), strokeWidth = 3.dp)
  }
}

@Composable
internal fun PlayerControlsOverlay(
  state: LiveStreamPlayerState,
  resizeMode: VideoResizeMode,
  isFullscreen: Boolean,
  modifier: Modifier = Modifier,
  onPlayPause: () -> Unit,
  onMute: () -> Unit,
  onJumpToLive: () -> Unit,
  onFullscreen: () -> Unit,
  onResizeMode: () -> Unit,
) {
  Box(modifier = modifier) {
    PlayerControlButton(
      onClick = onPlayPause,
      modifier = Modifier.align(Alignment.Center).size(60.dp),
      contentDescription = if (state.isPlaying) "Pause" else "Play",
    ) {
      TransportIcon(isPlaying = state.isPlaying)
    }

    Row(
      modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      LiveBadge(isAtLiveEdge = state.isAtLiveEdge)
      PlayerTextButton(text = "GO LIVE", onClick = onJumpToLive)
      PlayerControlButton(onClick = onMute, contentDescription = if (state.isMuted) "Unmute" else "Mute") {
        MuteIcon(isMuted = state.isMuted)
      }
    }

    Row(
      modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      PlayerTextButton(text = if (resizeMode == VideoResizeMode.Fill) "FILL" else "FIT", onClick = onResizeMode)
      PlayerControlButton(
        onClick = onFullscreen,
        contentDescription = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
      ) {
        FullscreenIcon(isFullscreen = isFullscreen)
      }
    }
  }
}

@Composable
private fun LiveBadge(isAtLiveEdge: Boolean) {
  Row(
    modifier =
      Modifier
        .height(36.dp)
        .border(1.dp, Color(0xFFE74C3C), RoundedCornerShape(18.dp))
        .background(Color(0xCC0A0E1A), RoundedCornerShape(18.dp))
        .padding(horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier
          .size(8.dp)
          .background(if (isAtLiveEdge) Color(0xFFE74C3C) else Color.White, CircleShape),
    )
    Spacer(Modifier.width(8.dp))
    Text("LIVE", color = Color(0xFFE74C3C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun PlayerTextButton(text: String, onClick: () -> Unit) {
  Box(
    modifier =
      Modifier
        .defaultMinSize(minWidth = 44.dp, minHeight = 36.dp)
        .background(Color(0xB02A3142), RoundedCornerShape(18.dp))
        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
        .clickable(role = Role.Button, onClick = onClick)
        .padding(horizontal = 12.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun PlayerControlButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier.size(44.dp),
  contentDescription: String,
  content: @Composable () -> Unit,
) {
  Box(
    modifier =
      modifier
        .background(Color(0xB02A3142), CircleShape)
        .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
        .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    content()
  }
}

@Composable
private fun TransportIcon(isPlaying: Boolean) {
  Canvas(Modifier.size(26.dp)) {
    if (isPlaying) {
      val barWidth = size.width * 0.24f
      drawRoundRect(Color.White, topLeft = Offset(size.width * 0.22f, size.height * 0.12f), size = Size(barWidth, size.height * 0.76f))
      drawRoundRect(Color.White, topLeft = Offset(size.width * 0.56f, size.height * 0.12f), size = Size(barWidth, size.height * 0.76f))
    } else {
      val path =
        Path().apply {
          moveTo(size.width * 0.28f, size.height * 0.12f)
          lineTo(size.width * 0.78f, size.height * 0.5f)
          lineTo(size.width * 0.28f, size.height * 0.88f)
          close()
        }
      drawPath(path, Color.White)
    }
  }
}

@Composable
private fun MuteIcon(isMuted: Boolean) {
  Canvas(Modifier.size(24.dp)) {
    val speaker =
      Path().apply {
        moveTo(size.width * 0.12f, size.height * 0.38f)
        lineTo(size.width * 0.32f, size.height * 0.38f)
        lineTo(size.width * 0.52f, size.height * 0.2f)
        lineTo(size.width * 0.52f, size.height * 0.8f)
        lineTo(size.width * 0.32f, size.height * 0.62f)
        lineTo(size.width * 0.12f, size.height * 0.62f)
        close()
      }
    drawPath(speaker, Color.White)
    if (isMuted) {
      drawLine(Color.White, Offset(size.width * 0.68f, size.height * 0.34f), Offset(size.width * 0.9f, size.height * 0.66f), strokeWidth = 3f, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(size.width * 0.9f, size.height * 0.34f), Offset(size.width * 0.68f, size.height * 0.66f), strokeWidth = 3f, cap = StrokeCap.Round)
    } else {
      drawArc(Color.White, startAngle = -38f, sweepAngle = 76f, useCenter = false, topLeft = Offset(size.width * 0.44f, size.height * 0.22f), size = Size(size.width * 0.36f, size.height * 0.56f), style = Stroke(width = 3f, cap = StrokeCap.Round))
      drawArc(Color.White, startAngle = -42f, sweepAngle = 84f, useCenter = false, topLeft = Offset(size.width * 0.48f, size.height * 0.08f), size = Size(size.width * 0.48f, size.height * 0.84f), style = Stroke(width = 2f, cap = StrokeCap.Round))
    }
  }
}

@Composable
private fun FullscreenIcon(isFullscreen: Boolean) {
  Canvas(Modifier.size(24.dp)) {
    val stroke = Stroke(width = 2.5f, cap = StrokeCap.Round)
    val inset = size.width * 0.18f
    val mid = size.width * 0.38f
    if (isFullscreen) {
      drawLine(Color.White, Offset(inset, mid), Offset(mid, mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(mid, inset), Offset(mid, mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(size.width - inset, mid), Offset(size.width - mid, mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(size.width - mid, inset), Offset(size.width - mid, mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(inset, size.height - mid), Offset(mid, size.height - mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(mid, size.height - inset), Offset(mid, size.height - mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(size.width - inset, size.height - mid), Offset(size.width - mid, size.height - mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(size.width - mid, size.height - inset), Offset(size.width - mid, size.height - mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
    } else {
      drawLine(Color.White, Offset(inset, inset), Offset(mid, inset), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(inset, inset), Offset(inset, mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(size.width - inset, inset), Offset(size.width - mid, inset), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(size.width - inset, inset), Offset(size.width - inset, mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(inset, size.height - inset), Offset(mid, size.height - inset), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(inset, size.height - inset), Offset(inset, size.height - mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(size.width - inset, size.height - inset), Offset(size.width - mid, size.height - inset), strokeWidth = stroke.width, cap = StrokeCap.Round)
      drawLine(Color.White, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - mid), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
  }
}
