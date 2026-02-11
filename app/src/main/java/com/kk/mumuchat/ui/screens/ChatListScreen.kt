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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun ChatListScreen(
    chatList: List<Chat>,
    contacts: List<Contact>,
    onChatClick: (String) -> Unit,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    val colors = LocalMuMuColors.current
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MuMuChat", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SkyBlue)
            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, "搜索", tint = SkyBlue, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Lock, "锁定", tint = SkyBlue, modifier = Modifier.size(22.dp))
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "更多", tint = SkyBlue, modifier = Modifier.size(22.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(colors.menuBg)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DarkMode, null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("夜间模式", color = colors.textPrimary, fontSize = 15.sp)
                                }
                            },
                            onClick = { showMenu = false; onToggleDarkMode() }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.GroupAdd, null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("新建群组", color = colors.textPrimary, fontSize = 15.sp)
                                }
                            },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bookmark, null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("收藏夹", color = colors.textPrimary, fontSize = 15.sp)
                                }
                            },
                            onClick = { showMenu = false }
                        )
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    cornerRadius = 20.dp,
                    backgroundColor = colors.cardBg
                ) {
                    Column {
                        chatList.forEachIndexed { index, chat ->
                            ChatListItem(chat = chat, onClick = { onChatClick(chat.id) })
                            if (index < chatList.lastIndex) {
                                Box(Modifier.fillMaxWidth().padding(start = 76.dp, end = 16.dp).height(0.5.dp).background(colors.divider))
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                Text("您在 MuMuChat 上的联系人", fontSize = 13.sp, color = colors.textSecondary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }
            item {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    cornerRadius = 20.dp,
                    backgroundColor = colors.cardBg
                ) {
                    Column {
                        contacts.forEachIndexed { index, contact ->
                            ContactInChatList(contact = contact, onClick = {})
                            if (index < contacts.lastIndex) {
                                Box(Modifier.fillMaxWidth().padding(start = 72.dp, end = 16.dp).height(0.5.dp).background(colors.divider))
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun ChatListItem(chat: Chat, onClick: () -> Unit) {
    val colors = LocalMuMuColors.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(
                    if (colors.isDark) Brush.linearGradient(listOf(SkyBlue.copy(alpha = 0.2f), SkyBlue.copy(alpha = 0.1f)))
                    else Brush.linearGradient(listOf(SkyBlueLight, SkyBlueVeryLight))
                ),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Person, null, tint = SkyBlue, modifier = Modifier.size(26.dp)) }
            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier.size(20.dp).clip(CircleShape).background(UnreadBadge).align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) { Text(chat.unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(chat.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text(
                if (chat.isGroup && chat.lastMessageSender.isNotEmpty()) "${chat.lastMessageSender}：${chat.lastMessage}" else chat.lastMessage,
                fontSize = 13.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(chat.lastMessageTime, fontSize = 11.sp, color = colors.textSecondary)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (chat.isPinned) Icon(Icons.Default.PushPin, "置顶", tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                if (chat.isMuted) Icon(Icons.Default.NotificationsOff, "静音", tint = colors.textSecondary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun ContactInChatList(contact: Contact, onClick: () -> Unit) {
    val colors = LocalMuMuColors.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(
                if (colors.isDark) Brush.linearGradient(listOf(SkyBlue.copy(alpha = 0.2f), SkyBlue.copy(alpha = 0.1f)))
                else Brush.linearGradient(listOf(SkyBlueLight, SkyBlueVeryLight))
            ),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Person, null, tint = SkyBlue, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(contact.user.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
            Text(contact.user.lastSeen, fontSize = 12.sp, color = colors.textSecondary)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatListScreenPreview() {
    MuMuChatTheme {
        ChatListScreen(
            chatList = listOf(
                Chat("1", "绫骨开发进度群", lastMessage = "群资料 [图片]", lastMessageTime = "09:35", isPinned = true, isGroup = true, lastMessageSender = "清大"),
                Chat("2", "龙", lastMessage = "好的", lastMessageTime = "08:17", unreadCount = 1),
            ),
            contacts = listOf(Contact(User("u1", "龙", lastSeen = "近期曾上线"))),
            onChatClick = {}
        )
    }
}
