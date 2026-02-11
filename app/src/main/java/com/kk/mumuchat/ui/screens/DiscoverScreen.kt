package com.kk.mumuchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.kk.mumuchat.ui.components.GlassCard
import com.kk.mumuchat.ui.theme.*

/**
 * 发现/设置页面
 * 对应截图第3张：各种设置项，带彩色图标
 */
@Composable
fun DiscoverScreen() {
    val bgBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(
            androidx.compose.ui.graphics.Color(0xFFF2F6FA),
            androidx.compose.ui.graphics.Color(0xFFE8EFF6),
            androidx.compose.ui.graphics.Color(0xFFF0F4F8)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        // ==================== 顶部标题栏 ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "设置",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row {
                IconButton(onClick = { /* TODO: 搜索设置 */ }) {
                    Icon(Icons.Default.Search, contentDescription = "搜索", tint = SkyBlue)
                }
                IconButton(onClick = { /* TODO: 更多 */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多", tint = TextPrimary)
                }
            }
        }

        // ==================== 设置项列表 ====================
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 主要设置项（毛玻璃卡片）
            item {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItem(
                            icon = Icons.Default.Person,
                            iconBgColor = IconBgBlue,
                            title = "账号",
                            subtitle = "账号、用户名、个人简介"
                        )
                        SettingsItem(
                            icon = Icons.Default.ChatBubble,
                            iconBgColor = IconBgBlue,
                            title = "聊天设置",
                            subtitle = "壁纸、深色模式、动画效果"
                        )
                        SettingsItem(
                            icon = Icons.Default.Security,
                            iconBgColor = IconBgGreen,
                            title = "隐私与安全",
                            subtitle = "最后上线时间、登录设备、通行密钥"
                        )
                        SettingsItem(
                            icon = Icons.Default.Notifications,
                            iconBgColor = IconBgRed,
                            title = "通知",
                            subtitle = "声音、通话、标记"
                        )
                        SettingsItem(
                            icon = Icons.Default.Storage,
                            iconBgColor = IconBgDarkBlue,
                            title = "数据和储存",
                            subtitle = "媒体下载设置"
                        )
                        SettingsItem(
                            icon = Icons.Default.Folder,
                            iconBgColor = IconBgOrange,
                            title = "聊天文件夹",
                            subtitle = "将聊天分类到文件夹中"
                        )
                        SettingsItem(
                            icon = Icons.Default.Computer,
                            iconBgColor = IconBgDarkBlue,
                            title = "设备",
                            subtitle = "管理已连接的设备"
                        )
                        SettingsItem(
                            icon = Icons.Default.BatteryChargingFull,
                            iconBgColor = IconBgGreen,
                            title = "省电",
                            subtitle = "低电量时降低功耗"
                        )
                        SettingsItem(
                            icon = Icons.Default.Language,
                            iconBgColor = IconBgBlue,
                            title = "语言",
                            subtitle = "简体中文 @zh_CN"
                        )
                    }
                }
            }

            // 帮助和反馈（毛玻璃卡片）
            item {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItem(
                            icon = Icons.Default.Help,
                            iconBgColor = IconBgBlue,
                            title = "帮助",
                            subtitle = ""
                        )
                        SettingsItem(
                            icon = Icons.Default.QuestionAnswer,
                            iconBgColor = IconBgOrange,
                            title = "向我们提问",
                            subtitle = ""
                        )
                    }
                }
            }

            // 底部留白
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

/**
 * 设置项组件
 * 左侧彩色图标 + 标题 + 副标题
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: 打开对应设置页 */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 彩色圆角方形图标
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                color = TextPrimary
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// ==================== 预览 ====================
@Preview(showBackground = true, showSystemUi = true, name = "设置")
@Composable
fun DiscoverScreenPreview() {
    MuMuChatTheme {
        DiscoverScreen()
    }
}
