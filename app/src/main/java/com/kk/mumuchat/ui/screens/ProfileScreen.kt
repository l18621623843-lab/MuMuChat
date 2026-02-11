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

@Composable
fun ProfileScreen(user: User) {
    val colors = LocalMuMuColors.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("动态", "已归档的动态")

    Column(
        modifier = Modifier.fillMaxSize().background(colors.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {}) { Icon(Icons.Default.Search, "搜索", tint = SkyBlue) }
            IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "更多", tint = colors.textPrimary) }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape).background(
                        if (colors.isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.4f)
                    ),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Person, "头像", tint = if (colors.isDark) Color.White.copy(alpha = 0.5f) else Color.Gray, modifier = Modifier.size(50.dp)) }
            }
            item {
                Spacer(Modifier.height(12.dp))
                Text(user.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text(user.phone, fontSize = 14.sp, color = colors.textSecondary)
            }
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileActionButton(Icons.Default.CameraAlt, "设置照片") {}
                    ProfileActionButton(Icons.Default.Edit, "编辑信息") {}
                    ProfileActionButton(Icons.Default.Settings, "设置") {}
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                GlassCard(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Call, null, tint = colors.textSecondary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("+86 / 86-2/62-3843", fontSize = 15.sp, color = colors.textPrimary)
                            Text("手机", fontSize = 13.sp, color = colors.textSecondary)
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
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
                            text = { Text(title, color = if (selectedTab == index) SkyBlue else colors.textSecondary, fontSize = 14.sp) }
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(48.dp))
                Text("暂无贴文", fontSize = 18.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("发布照片和视频将显示在您的个人资料上", fontSize = 14.sp, color = colors.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 48.dp))
            }
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(horizontal = 48.dp)
                ) {
                    Text("添加贴文", fontSize = 15.sp, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun ProfileActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = LocalMuMuColors.current
    GlassCard(modifier = Modifier.size(width = 100.dp, height = 80.dp), cornerRadius = 12.dp) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, label, tint = SkyBlue, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 12.sp, color = colors.textPrimary, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MuMuChatTheme {
        ProfileScreen(user = User(id = "me", name = "devin", phone = "+86 190****2755"))
    }
}
