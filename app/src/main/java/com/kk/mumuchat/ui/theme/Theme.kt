package com.kk.mumuchat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * MuMuChat 主题配置
 * 使用 Material3 的 lightColorScheme，天蓝色为主色调
 */

// 浅色主题配色方案
private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,                    // 主色 - 天蓝
    onPrimary = Color.White,              // 主色上的文字 - 白色
    primaryContainer = SkyBlueLight,      // 主色容器 - 浅天蓝
    onPrimaryContainer = SkyBlueDark,     // 主色容器上的文字
    secondary = SkyBlueDark,              // 次要色 - 深天蓝
    onSecondary = Color.White,
    background = BackgroundLight,         // 背景色 - 浅灰蓝
    onBackground = TextPrimary,           // 背景上的文字
    surface = SurfaceWhite,              // 表面色 - 白色
    onSurface = TextPrimary,             // 表面上的文字
    surfaceVariant = CardBackground,     // 表面变体 - 半透明白
    outline = DividerColor,              // 轮廓/分割线
    error = UnreadBadge,                 // 错误色 - 红色
)

/**
 * MuMuChat 应用主题
 * @param content 主题包裹的内容
 */
@Composable
fun MuMuChatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
