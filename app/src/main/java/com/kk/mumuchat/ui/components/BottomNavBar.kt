package com.kk.mumuchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kk.mumuchat.ui.theme.SkyBlue
import com.kk.mumuchat.ui.theme.TextSecondary

/**
 * 底部导航栏标签页数据
 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0
)

// 四个标签页配置（参考截图：聊天、联系人、设置、个人资料）
val bottomNavItems = listOf(
    BottomNavItem(
        label = "聊天",
        icon = Icons.Outlined.ChatBubbleOutline,
        route = "chat_list",
        badgeCount = 1
    ),
    BottomNavItem(
        label = "联系人",
        icon = Icons.Outlined.Contacts,
        route = "contacts"
    ),
    BottomNavItem(
        label = "设置",
        icon = Icons.Outlined.Settings,
        route = "discover"
    ),
    BottomNavItem(
        label = "个人资料",
        icon = Icons.Outlined.PersonOutline,
        route = "profile"
    )
)

/**
 * 毛玻璃浮动底部导航栏
 * 参考截图：胶囊形圆角、半透明白色背景、浮动阴影
 * 选中项显示蓝色圆形背景 + 首字母
 *
 * @param currentRoute 当前选中的路由
 * @param onItemClick 点击标签页回调
 * @param userName 当前用户名（个人资料 tab 显示首字母）
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onItemClick: (String) -> Unit,
    userName: String = "D"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        // 毛玻璃胶囊形容器
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.06f),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.92f))
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                // 单个导航项
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null  // 去掉点击水波纹
                        ) { onItemClick(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isSelected && item.route == "profile") {
                        // 个人资料选中态：蓝色圆形 + 首字母
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SkyBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // 图标
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) SkyBlue else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // 标签文字
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        color = if (isSelected) SkyBlue else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
