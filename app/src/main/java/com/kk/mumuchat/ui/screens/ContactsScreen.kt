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
import androidx.compose.ui.graphics.Brush
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

@Composable
fun ContactsScreen(contacts: List<Contact>) {
    val colors = LocalMuMuColors.current
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("联系人", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, "更多", tint = colors.textPrimary)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("搜索联系人", color = colors.textSecondary)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        singleLine = true
                    )
                }
            }
            item {
                GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        ContactActionItem(Icons.Default.PersonAdd, IconBgBlue, "邀请朋友") {}
                        ContactActionItem(Icons.Default.Call, IconBgGreen, "最近的通话") {}
                        ContactActionItem(Icons.Default.GroupAdd, IconBgOrange, "新建群组") {}
                    }
                }
            }
            item {
                Text("#", fontSize = 14.sp, color = SkyBlue, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp))
            }
            item {
                GlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        val filtered = if (searchQuery.isBlank()) contacts
                        else contacts.filter { it.user.name.contains(searchQuery, ignoreCase = true) }
                        filtered.forEach { ContactListItem(it) }
                    }
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun ContactActionItem(icon: ImageVector, iconBgColor: Color, title: String, onClick: () -> Unit) {
    val colors = LocalMuMuColors.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(iconBgColor),
            contentAlignment = Alignment.Center
        ) { Icon(icon, title, tint = Color.White, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = colors.textPrimary)
    }
}

@Composable
fun ContactListItem(contact: Contact) {
    val colors = LocalMuMuColors.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable {}.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(
                if (colors.isDark) SkyBlue.copy(alpha = 0.15f) else SkyBlueLight
            ),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Person, null, tint = SkyBlue, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(contact.user.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
            Text(contact.user.lastSeen, fontSize = 13.sp, color = colors.textSecondary)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ContactsScreenPreview() {
    MuMuChatTheme {
        ContactsScreen(
            contacts = listOf(
                Contact(User("u1", "龙", lastSeen = "近期曾上线")),
                Contact(User("u2", "190 7542 2755", lastSeen = "很久前上线"))
            )
        )
    }
}
