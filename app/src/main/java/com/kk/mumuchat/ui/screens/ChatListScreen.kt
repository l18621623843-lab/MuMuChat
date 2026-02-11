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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.kk.mumuchat.model.Chat
import com.kk.mumuchat.model.Contact
import com.kk.mumuchat.model.User
import com.kk.mumuchat.ui.components.GlassCard
import com.kk.mumuchat.ui.theme.*

/**
 * 聊天列表页面（主页）
 * 毛玻璃卡片风格，浅灰蓝渐变背景
 */
@Composable
fun ChatListScreen(
    chatList: List<Chat>,
    contacts: List<Contact>,
    onChatClick: (String) -> Unit
) {
    // 渐变背景
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF2F6FA),
            Color(0xFFE8EFF6),
            Color(0xFFF0F4F8)
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
                text = "MuMuChat",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SkyBlue
            )
            Row {
                IconButton(onClick = { /* TODO: 搜索 */ }) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = SkyBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = { /* TODO: 锁定 */ }) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "锁定",
                        tint = SkyBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = { /* TODO: 更多 */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "更多",
                        tint = SkyBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ==================== 可滚动内容 ====================
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 聊天会话列表（毛玻璃卡片包裹）
            item {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    cornerRadius = 20.dp
                ) {
                    Column {
                        chatList.forEachIndexed { index, chat ->
                            ChatListItem(
                                chat = chat,
                                onClick = { onChatClick(chat.id) }
                            )
                            // 分割线（最后一项不加）
                            if (index < chatList.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 76.dp, end = 16.dp)
                                        .height(0.5.dp)
                                        .background(DividerColor)
                                )
                            }
                        }
                    }
                }
            }

            // 分隔
            item { Spacer(modifier = Modifier.height(12.dp)) }

            // "您在 MuMuChat 上的联系人"
            item {
                Text(
                    text = "您在 MuMuChat 上的联系人",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // 联系人列表（毛玻璃卡片）
            item {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    cornerRadius = 20.dp
                ) {
                    Column {
                        contacts.forEachIndexed { index, contact ->
                            ContactInChatList(
                                contact = contact,
                                onClick = { /* TODO */ }
                            )
                            if (index < contacts.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 72.dp, end = 16.dp)
                                        .height(0.5.dp)
                                        .background(DividerColor)
                                )
                            }
                        }
                    }
                }
            }

            // 底部留白（给浮动导航栏留空间）
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

/**
 * 聊天列表单项
 */
@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像 + 未读红点
        Box {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                SkyBlueLight,
                                SkyBlueVeryLight
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(26.dp)
                )
            }
            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(UnreadBadge)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 名称 + 最后消息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = if (chat.isGroup && chat.lastMessageSender.isNotEmpty())
                    "${chat.lastMessageSender}：${chat.lastMessage}"
                else chat.lastMessage,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 时间 + 图标
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = chat.lastMessageTime,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (chat.isPinned) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "置顶",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (chat.isMuted) {
                    Icon(
                        Icons.Default.NotificationsOff,
                        contentDescription = "静音",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * 聊天列表底部联系人项
 */
@Composable
fun ContactInChatList(
    contact: Contact,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(SkyBlueLight, SkyBlueVeryLight)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = SkyBlue,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = contact.user.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = contact.user.lastSeen,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

// ==================== 预览 ====================
@Preview(showBackground = true, showSystemUi = true, name = "聊天列表")
@Composable
fun ChatListScreenPreview() {
    MuMuChatTheme {
        ChatListScreen(
            chatList = listOf(
                Chat("1", "绫骨开发进度群", lastMessage = "群资料 [图片]", lastMessageTime = "09:35", isPinned = true, isGroup = true, lastMessageSender = "清大"),
                Chat("2", "龙", lastMessage = "好的", lastMessageTime = "08:17", unreadCount = 1),
                Chat("3", "交易临时小分队", lastMessage = "自产化测试人群：好", lastMessageTime = "07:47", isMuted = true, isGroup = true, lastMessageSender = "清大")
            ),
            contacts = listOf(
                Contact(User("u1", "龙", lastSeen = "近期曾上线")),
                Contact(User("u2", "190 7542 2755", lastSeen = "很久前上线"))
            ),
            onChatClick = {}
        )
    }
}
