package com.kk.mumuchat.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==================== 主色调（天蓝色系列）====================
val SkyBlue = Color(0xFF4FC3F7)
val SkyBlueDark = Color(0xFF0288D1)
val SkyBlueLight = Color(0xFFB3E5FC)
val SkyBlueVeryLight = Color(0xFFE1F5FE)

// ==================== 背景色 ====================
val BackgroundLight = Color(0xFFF0F4F8)
val CardBackground = Color(0xCCFFFFFF)
val SurfaceWhite = Color(0xFFFAFAFA)

// ==================== 聊天气泡颜色 ====================
val BubbleSent = Color(0xFF6AD06A)
val BubbleReceived = Color(0xFFFAFAFA)
val ChatDarkBg = Color(0xFF1B2838)

// ==================== 文字颜色 ====================
val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF9E9E9E)
val TextBlue = Color(0xFF2196F3)
val TextWhite = Color(0xFFFFFFFF)

// ==================== 功能色 ====================
val UnreadBadge = Color(0xFFFF5252)
val OnlineGreen = Color(0xFF4CAF50)
val DividerColor = Color(0xFFEEEEEE)

// ==================== 设置页图标背景色 ====================
val IconBgBlue = Color(0xFF42A5F5)
val IconBgGreen = Color(0xFF66BB6A)
val IconBgOrange = Color(0xFFFF9800)
val IconBgYellow = Color(0xFFFFCA28)
val IconBgRed = Color(0xFFEF5350)
val IconBgDarkBlue = Color(0xFF5C6BC0)

// ==================== 应用主题色系统 ====================
data class MuMuColors(
    val isDark: Boolean,
    val background: Brush,
    val cardBg: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val topBarBg: Color,
    val inputBarBg: Color,
    val inputFieldBg: Color,
    val inputFieldBorder: Color,
    val inputFieldText: Color,
    val iconColor: Color,
    val panelBg: Color,
    val panelItemBg: Color,
    val navBarBg: Color,
    val chatBgBrush: Brush,
    val menuBg: Color,
    val menuText: Color,
)

val LightMuMuColors = MuMuColors(
    isDark = false,
    background = Brush.verticalGradient(listOf(Color(0xFFF2F6FA), Color(0xFFE8EFF6), Color(0xFFF0F4F8))),
    cardBg = Color.White.copy(alpha = 0.82f),
    cardBorder = Color.White.copy(alpha = 0.5f),
    textPrimary = Color(0xFF212121),
    textSecondary = Color(0xFF9E9E9E),
    divider = Color(0xFFEEEEEE),
    topBarBg = Color.Transparent,
    inputBarBg = Color(0xFFF7F7F7),
    inputFieldBg = Color.White,
    inputFieldBorder = Color(0xFFDDDDDD),
    inputFieldText = Color(0xFF1C1C1C),
    iconColor = Color(0xFF1C1C1C),
    panelBg = Color(0xFFF7F7F7),
    panelItemBg = Color.White,
    navBarBg = Color.White.copy(alpha = 0.92f),
    chatBgBrush = Brush.verticalGradient(listOf(Color(0xFFF2F6FA), Color(0xFFE8EFF6), Color(0xFFF0F4F8))),
    menuBg = Color.White,
    menuText = Color(0xFF212121),
)

val DarkMuMuColors = MuMuColors(
    isDark = true,
    background = Brush.verticalGradient(listOf(Color(0xFF0E1B2D), Color(0xFF142233), Color(0xFF17293D))),
    cardBg = Color.White.copy(alpha = 0.08f),
    cardBorder = Color.White.copy(alpha = 0.12f),
    textPrimary = Color.White.copy(alpha = 0.92f),
    textSecondary = Color.White.copy(alpha = 0.5f),
    divider = Color.White.copy(alpha = 0.08f),
    topBarBg = Color.Transparent,
    inputBarBg = Color(0xFF1A3050).copy(alpha = 0.85f),
    inputFieldBg = Color.White.copy(alpha = 0.07f),
    inputFieldBorder = Color.White.copy(alpha = 0.15f),
    inputFieldText = Color.White,
    iconColor = Color.White.copy(alpha = 0.75f),
    panelBg = Color(0xFF1A3050).copy(alpha = 0.95f),
    panelItemBg = Color.White.copy(alpha = 0.08f),
    navBarBg = Color(0xFF1A2A3E).copy(alpha = 0.92f),
    chatBgBrush = Brush.verticalGradient(listOf(Color(0xFF17293D), Color(0xFF0E1B2D), Color(0xFF142233))),
    menuBg = Color(0xFF1E3248),
    menuText = Color.White.copy(alpha = 0.9f),
)

val LocalMuMuColors = staticCompositionLocalOf { LightMuMuColors }
