package com.perpetuitylab.livenews.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LiveNewsHomeIcon(color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val strokeWidth = 2.dp.toPx()
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.18f, size.height * 0.48f), Offset(size.width * 0.5f, size.height * 0.2f), strokeWidth, StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.82f, size.height * 0.48f), strokeWidth, StrokeCap.Round)
    drawRoundRect(
      color = color,
      topLeft = Offset(size.width * 0.28f, size.height * 0.46f),
      size = Size(size.width * 0.44f, size.height * 0.34f),
      style = stroke,
    )
  }
}

@Composable
fun LiveNewsSettingsIcon(color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val strokeWidth = 2.dp.toPx()
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    val center = Offset(size.width / 2f, size.height / 2f)
    val outer = size.minDimension * 0.28f
    val inner = size.minDimension * 0.09f
    drawCircle(color = color, radius = outer, center = center, style = stroke)
    drawCircle(color = color, radius = inner, center = center, style = stroke)
    for (i in 0 until 8) {
      val angle = Math.toRadians((i * 45).toDouble())
      val start = Offset(center.x + kotlin.math.cos(angle).toFloat() * outer, center.y + kotlin.math.sin(angle).toFloat() * outer)
      val end = Offset(center.x + kotlin.math.cos(angle).toFloat() * outer * 1.32f, center.y + kotlin.math.sin(angle).toFloat() * outer * 1.32f)
      drawLine(color, start, end, strokeWidth, StrokeCap.Round)
    }
  }
}

@Composable
fun LiveNewsRefreshIcon(color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val strokeWidth = 2.dp.toPx()
    val arcSize = Size(size.width * 0.68f, size.height * 0.68f)
    val topLeft = Offset(size.width * 0.16f, size.height * 0.16f)
    drawArc(color = color, startAngle = 38f, sweepAngle = 278f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
    drawLine(color, Offset(size.width * 0.72f, size.height * 0.12f), Offset(size.width * 0.92f, size.height * 0.12f), strokeWidth, StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.92f, size.height * 0.12f), Offset(size.width * 0.92f, size.height * 0.32f), strokeWidth, StrokeCap.Round)
  }
}

@Composable
fun LiveNewsDragHandleIcon(color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val radius = 1.45.dp.toPx()
    val xs = listOf(size.width * 0.38f, size.width * 0.62f)
    val ys = listOf(size.height * 0.28f, size.height * 0.5f, size.height * 0.72f)
    ys.forEach { y -> xs.forEach { x -> drawCircle(color = color, radius = radius, center = Offset(x, y)) } }
  }
}

@Composable
fun LiveNewsChevronIcon(direction: LiveNewsChevronDirection, color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val strokeWidth = 2.dp.toPx()
    val topY = if (direction == LiveNewsChevronDirection.Up) size.height * 0.38f else size.height * 0.62f
    val bottomY = if (direction == LiveNewsChevronDirection.Up) size.height * 0.62f else size.height * 0.38f
    drawLine(color, Offset(size.width * 0.26f, bottomY), Offset(size.width * 0.5f, topY), strokeWidth, StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.5f, topY), Offset(size.width * 0.74f, bottomY), strokeWidth, StrokeCap.Round)
  }
}

enum class LiveNewsChevronDirection {
  Up,
  Down,
}
