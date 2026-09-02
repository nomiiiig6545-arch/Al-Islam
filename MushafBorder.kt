package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun MushafBorder(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val outerStroke = 4f
        val innerStroke = 2f
        val padding = 16f
        val cornerRadius = 32f

        // Outer border
        drawRoundRect(
            color = Color(0xFF6A8696),
            topLeft = Offset(padding, padding),
            size = Size(size.width - padding * 2, size.height - padding * 2),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = outerStroke)
        )

        // Inner border
        val innerPadding = padding + 12f
        drawRoundRect(
            color = Color(0xFFC04F46),
            topLeft = Offset(innerPadding, innerPadding),
            size = Size(size.width - innerPadding * 2, size.height - innerPadding * 2),
            cornerRadius = CornerRadius(cornerRadius - 8f, cornerRadius - 8f),
            style = Stroke(width = innerStroke)
        )

        // Corner decorations
        val decorations = listOf(
            Offset(innerPadding, innerPadding),
            Offset(size.width - innerPadding, innerPadding),
            Offset(innerPadding, size.height - innerPadding),
            Offset(size.width - innerPadding, size.height - innerPadding)
        )

        decorations.forEach { center ->
            drawCircle(
                color = Color(0xFFFAF7EE),
                radius = 16f,
                center = center
            )
            drawCircle(
                color = Color(0xFF6A8696),
                radius = 12f,
                center = center,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = Color(0xFFC04F46),
                radius = 6f,
                center = center
            )
        }
    }
}
