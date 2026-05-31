package com.perpetuitylab.livenews.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LiveNewsDragHandleIcon(color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val radius = 1.45.dp.toPx()
    val xs = listOf(size.width * 0.38f, size.width * 0.62f)
    val ys = listOf(size.height * 0.28f, size.height * 0.5f, size.height * 0.72f)
    ys.forEach { y -> xs.forEach { x -> drawCircle(color = color, radius = radius, center = Offset(x, y)) } }
  }
}
