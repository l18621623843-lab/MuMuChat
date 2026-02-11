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
import com.kk.mumuchat.ui.theme.LocalMuMuColors
import com.kk.mumuchat.ui.theme.SkyBlue

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0
)

val bottomNavItems = listOf(
    BottomNavItem("聊天", Icons.Outlined.ChatBubbleOutline, "chat_list", badgeCount = 1),
    BottomNavItem("联系人", Icons.Outlined.Contacts, "contacts"),
    BottomNavItem("设置", Icons.Outlined.Settings, "discover"),
    BottomNavItem("个人资料", Icons.Outlined.PersonOutline, "profile")
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onItemClick: (String) -> Unit,
    userName: String = "D"
) {
    val colors = LocalMuMuColors.current
    val unselectedColor = colors.textSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = if (colors.isDark) Color.Black.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.06f),
                    spotColor = if (colors.isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(colors.navBarBg)
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemClick(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isSelected && item.route == "profile") {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(SkyBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                userName.take(1).uppercase(),
                                color = Color.White, fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Icon(
                            item.icon, item.label,
                            tint = if (isSelected) SkyBlue else unselectedColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        item.label, fontSize = 10.sp,
                        color = if (isSelected) SkyBlue else unselectedColor,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
