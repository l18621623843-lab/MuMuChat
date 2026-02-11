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

/**
 * 毛玻璃效果卡片组件
 * 半透明白色背景 + 微弱白色边框 + 柔和阴影
 * 模拟 iOS/Telegram 风格的磨砂玻璃质感
 *
 * @param modifier 外部修饰符
 * @param cornerRadius 圆角半径
 * @param backgroundColor 背景色（半透明白色）
 * @param elevation 阴影高度
 * @param content 卡片内容
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.82f),
    elevation: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clip(shape)
            .background(backgroundColor)
            // 微弱白色边框增强玻璃质感
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = shape
            ),
        content = content
    )
}
