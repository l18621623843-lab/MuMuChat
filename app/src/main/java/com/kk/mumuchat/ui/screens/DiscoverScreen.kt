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

@Composable
fun DiscoverScreen() {
    val colors = LocalMuMuColors.current

    Column(
        modifier = Modifier.fillMaxSize().background(colors.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("设置", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Row {
                IconButton(onClick = {}) { Icon(Icons.Default.Search, "搜索", tint = SkyBlue) }
                IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "更多", tint = colors.textPrimary) }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        SettingsItem(Icons.Default.Person, IconBgBlue, "账号", "账号、用户名、个人简介")
                        SettingsItem(Icons.Default.ChatBubble, IconBgBlue, "聊天设置", "壁纸、深色模式、动画效果")
                        SettingsItem(Icons.Default.Security, IconBgGreen, "隐私与安全", "最后上线时间、登录设备、通行密钥")
                        SettingsItem(Icons.Default.Notifications, IconBgRed, "通知", "声音、通话、标记")
                        SettingsItem(Icons.Default.Storage, IconBgDarkBlue, "数据和储存", "媒体下载设置")
                        SettingsItem(Icons.Default.Folder, IconBgOrange, "聊天文件夹", "将聊天分类到文件夹中")
                        SettingsItem(Icons.Default.Computer, IconBgDarkBlue, "设备", "管理已连接的设备")
                        SettingsItem(Icons.Default.BatteryChargingFull, IconBgGreen, "省电", "低电量时降低功耗")
                        SettingsItem(Icons.Default.Language, IconBgBlue, "语言", "简体中文 @zh_CN")
                    }
                }
            }
            item {
                GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        SettingsItem(Icons.Default.Help, IconBgBlue, "帮助", "")
                        SettingsItem(Icons.Default.QuestionAnswer, IconBgOrange, "向我们提问", "")
                    }
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, iconBgColor: Color, title: String, subtitle: String) {
    val colors = LocalMuMuColors.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable {}.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(iconBgColor),
            contentAlignment = Alignment.Center
        ) { Icon(icon, title, tint = Color.White, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontSize = 16.sp, color = colors.textPrimary)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 13.sp, color = colors.textSecondary)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DiscoverScreenPreview() {
    MuMuChatTheme { DiscoverScreen() }
}
