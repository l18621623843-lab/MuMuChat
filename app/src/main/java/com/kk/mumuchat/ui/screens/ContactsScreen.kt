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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.kk.mumuchat.model.Contact
import com.kk.mumuchat.model.User
import com.kk.mumuchat.ui.components.GlassCard
import com.kk.mumuchat.ui.theme.*

/**
 * 联系人页面
 * 对应截图第2张：搜索栏 + 功能入口（邀请/通话/建群）+ 联系人列表
 *
 * @param contacts 联系人列表
 */
@Composable
fun ContactsScreen(
    contacts: List<Contact>
) {
    // 搜索关键词状态
    var searchQuery by remember { mutableStateOf("") }

    // 渐变背景
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
                text = "联系人",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = { /* TODO: 更多选项 */ }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = TextPrimary
                )
            }
        }

        // ==================== 可滚动内容 ====================
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 搜索栏（毛玻璃卡片）
            item {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("搜索联系人", color = TextSecondary)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            // 功能入口卡片（邀请朋友、最近通话、新建群组）
            item {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ContactActionItem(
                            icon = Icons.Default.PersonAdd,
                            iconBgColor = IconBgBlue,
                            title = "邀请朋友",
                            onClick = { /* TODO: 邀请朋友 */ }
                        )
                        ContactActionItem(
                            icon = Icons.Default.Call,
                            iconBgColor = IconBgGreen,
                            title = "最近的通话",
                            onClick = { /* TODO: 最近通话 */ }
                        )
                        ContactActionItem(
                            icon = Icons.Default.GroupAdd,
                            iconBgColor = IconBgOrange,
                            title = "新建群组",
                            onClick = { /* TODO: 新建群组 */ }
                        )
                    }
                }
            }

            // 联系人分组标题
            item {
                Text(
                    text = "#",
                    fontSize = 14.sp,
                    color = SkyBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            // 联系人列表（毛玻璃卡片）
            item {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        // 根据搜索关键词过滤联系人
                        val filteredContacts = if (searchQuery.isBlank()) contacts
                        else contacts.filter {
                            it.user.name.contains(searchQuery, ignoreCase = true)
                        }
                        filteredContacts.forEach { contact ->
                            ContactListItem(contact = contact)
                        }
                    }
                }
            }

            // 底部留白
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

/**
 * 联系人页面的功能入口项（邀请朋友、最近通话、新建群组）
 */
@Composable
fun ContactActionItem(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 彩色圆角方形图标背景
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
        Text(
            text = title,
            fontSize = 16.sp,
            color = TextPrimary
        )
    }
}

/**
 * 联系人列表中的单个联系人项
 */
@Composable
fun ContactListItem(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: 打开联系人详情 */ }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 圆形头像
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SkyBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = SkyBlue,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = contact.user.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = contact.user.lastSeen,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

// ==================== 预览 ====================
@Preview(showBackground = true, showSystemUi = true, name = "联系人")
@Composable
fun ContactsScreenPreview() {
    MuMuChatTheme {
        ContactsScreen(
            contacts = listOf(
                Contact(User("u1", "龙", lastSeen = "近期曾上线")),
                Contact(User("u2", "190 7542 2755", lastSeen = "很久前上线")),
                Contact(User("u3", "蒋龙 应", lastSeen = "很久前上线"))
            )
        )
    }
}
