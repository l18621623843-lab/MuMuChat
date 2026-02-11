package com.kk.mumuchat.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.kk.mumuchat.model.User
import com.kk.mumuchat.ui.components.GlassCard
import com.kk.mumuchat.ui.theme.*

/**
 * 个人资料页面（"我"标签页）
 * 对应截图第4张：头像、昵称、手机号、操作按钮、动态/已归档
 *
 * @param user 当前登录用户信息
 */
@Composable
fun ProfileScreen(user: User) {
    // Tab 选中状态（动态 / 已归档的动态）
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("动态", "已归档的动态")

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
        // ==================== 顶部操作栏 ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { /* TODO: 搜索 */ }) {
                Icon(Icons.Default.Search, contentDescription = "搜索", tint = SkyBlue)
            }
            IconButton(onClick = { /* TODO: 更多 */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "更多", tint = TextPrimary)
            }
        }

        // ==================== 可滚动内容 ====================
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像区域
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "头像",
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            // 用户名
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = user.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // 手机号
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.phone,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            // 三个操作按钮（设置照片、编辑信息、设置）
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileActionButton(
                        icon = Icons.Default.CameraAlt,
                        label = "设置照片",
                        onClick = { /* TODO: 设置头像 */ }
                    )
                    ProfileActionButton(
                        icon = Icons.Default.Edit,
                        label = "编辑信息",
                        onClick = { /* TODO: 编辑个人信息 */ }
                    )
                    ProfileActionButton(
                        icon = Icons.Default.Settings,
                        label = "设置",
                        onClick = { /* TODO: 打开设置 */ }
                    )
                }
            }

            // 手机号信息卡片
            item {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "+86 / 86-2/62-3843",
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "手机",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // 动态 Tab 栏
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = SkyBlue,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = SkyBlue
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) SkyBlue else TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
            }

            // 空状态提示
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "暂无贴文",
                    fontSize = 18.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "发布照片和视频将显示在您的个人资料上",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
            }

            // "添加贴文" 按钮
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO: 添加贴文 */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(horizontal = 48.dp)
                ) {
                    Text(
                        text = "添加贴文",
                        fontSize = 15.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // 底部留白
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

/**
 * 个人资料页的操作按钮（设置照片、编辑信息、设置）
 * 毛玻璃卡片风格
 */
@Composable
fun ProfileActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.size(width = 100.dp, height = 80.dp),
        cornerRadius = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = SkyBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== 预览 ====================
@Preview(showBackground = true, showSystemUi = true, name = "个人资料")
@Composable
fun ProfileScreenPreview() {
    MuMuChatTheme {
        ProfileScreen(
            user = User(id = "me", name = "devin", phone = "+86 190****2755")
        )
    }
}
