package com.kk.mumuchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kk.mumuchat.ui.theme.LocalMuMuColors

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    backgroundColor: Color? = null,
    elevation: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalMuMuColors.current
    val bg = backgroundColor ?: colors.cardBg
    val borderColor = colors.cardBorder
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (colors.isDark) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.04f),
                spotColor = if (colors.isDark) Color.Black.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.06f)
            )
            .clip(shape)
            .background(bg)
            .border(width = 0.5.dp, color = borderColor, shape = shape),
        content = content
    )
}
