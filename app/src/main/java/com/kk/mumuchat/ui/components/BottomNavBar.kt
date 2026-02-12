package com.kk.mumuchat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kk.mumuchat.ui.theme.LocalMuMuColors
import com.kk.mumuchat.ui.theme.SkyBlue

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val route: String,
    val badgeCount: Int = 0
)

val bottomNavItems = listOf(
    BottomNavItem("聊天", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble, "chat_list", badgeCount = 1),
    BottomNavItem("联系人", Icons.Outlined.Contacts, Icons.Filled.Contacts, "contacts"),
    BottomNavItem("设置", Icons.Outlined.Settings, Icons.Filled.Settings, "discover"),
    BottomNavItem("个人资料", Icons.Outlined.PersonOutline, Icons.Filled.Person, "profile")
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onItemClick: (String) -> Unit,
    userName: String = "D"
) {
    val colors = LocalMuMuColors.current
    val unselectedColor = colors.textSecondary

    // 磨砂玻璃胶囊容器
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // 磨砂底层（模糊背景模拟）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(33.dp),
                    ambientColor = if (colors.isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.08f),
                    spotColor = if (colors.isDark) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.12f)
                )
                .clip(RoundedCornerShape(33.dp))
                .background(
                    if (colors.isDark)
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1E2D40).copy(alpha = 0.85f),
                                Color(0xFF162232).copy(alpha = 0.92f)
                            )
                        )
                    else
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.82f),
                                Color(0xFFF8FAFE).copy(alpha = 0.90f)
                            )
                        )
                )
                .graphicsLayer { alpha = 0.98f }
        )

        // 内容层
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route

                // 弹性缩放
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale_${item.route}"
                )

                // 选中上浮
                val yOffset by animateDpAsState(
                    targetValue = if (isSelected) (-1).dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "yOff_${item.route}"
                )

                // 选中高亮背景透明度
                val pillAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = tween(200),
                    label = "pill_${item.route}"
                )

                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) SkyBlue else unselectedColor,
                    animationSpec = tween(150),
                    label = "tint_${item.route}"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) SkyBlue else unselectedColor,
                    animationSpec = tween(150),
                    label = "txt_${item.route}"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = yOffset)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemClick(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 整体容器：pill 高亮包裹图标 + 文字
                    Box(
                        modifier = Modifier
                            .graphicsLayer(scaleX = scale, scaleY = scale),
                        contentAlignment = Alignment.Center
                    ) {
                        // 选中时的胶囊高亮背景（覆盖图标+文字）
                        if (pillAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .size(width = 54.dp, height = 46.dp)
                                    .graphicsLayer { alpha = pillAlpha }
                                    .clip(RoundedCornerShape(23.dp))
                                    .background(
                                        if (colors.isDark)
                                            SkyBlue.copy(alpha = 0.18f)
                                        else
                                            SkyBlue.copy(alpha = 0.12f)
                                    )
                            )
                        }

                        // 图标 + 文字纵向排列
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            if (item.route == "profile") {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) SkyBlue else unselectedColor.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        userName.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Icon(
                                    if (isSelected) item.selectedIcon else item.icon,
                                    contentDescription = item.label,
                                    tint = iconTint,
                                    modifier = Modifier.size(21.dp)
                                )
                            }

                            Spacer(Modifier.height(2.dp))

                            Text(
                                item.label,
                                fontSize = 9.sp,
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                lineHeight = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
