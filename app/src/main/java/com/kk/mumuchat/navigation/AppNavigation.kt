package com.kk.mumuchat.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kk.mumuchat.ui.components.BottomNavBar
import com.kk.mumuchat.ui.screens.ChatDetailScreen
import com.kk.mumuchat.ui.screens.ChatListScreen
import com.kk.mumuchat.ui.screens.ContactsScreen
import com.kk.mumuchat.ui.screens.DiscoverScreen
import com.kk.mumuchat.ui.screens.ProfileScreen
import com.kk.mumuchat.viewmodel.ChatViewModel

/**
 * 应用主导航组件
 * 使用 Box 布局让底部导航栏浮动在内容上方（毛玻璃效果）
 * 不使用 Scaffold，避免底部栏占据固定空间
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val chatViewModel: ChatViewModel = viewModel()
    var currentRoute by remember { mutableStateOf(Routes.CHAT_LIST) }

    // 聊天详情页不显示底部导航栏
    val showBottomBar = currentRoute in listOf(
        Routes.CHAT_LIST, Routes.CONTACTS,
        Routes.DISCOVER, Routes.PROFILE
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // 页面内容层
        NavHost(
            navController = navController,
            startDestination = Routes.CHAT_LIST,
            modifier = Modifier.fillMaxSize()
        ) {
            // 聊天列表页
            composable(Routes.CHAT_LIST) {
                currentRoute = Routes.CHAT_LIST
                ChatListScreen(
                    chatList = chatViewModel.chatList,
                    contacts = chatViewModel.contacts,
                    onChatClick = { chatId ->
                        currentRoute = "chat_detail"
                        navController.navigate(Routes.chatDetail(chatId))
                    }
                )
            }

            // 联系人页
            composable(Routes.CONTACTS) {
                currentRoute = Routes.CONTACTS
                ContactsScreen(contacts = chatViewModel.contacts)
            }

            // 发现/设置页
            composable(Routes.DISCOVER) {
                currentRoute = Routes.DISCOVER
                DiscoverScreen()
            }

            // 个人资料页
            composable(Routes.PROFILE) {
                currentRoute = Routes.PROFILE
                ProfileScreen(user = chatViewModel.currentUser.value)
            }

            // 聊天详情页
            composable(
                route = Routes.CHAT_DETAIL,
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                currentRoute = "chat_detail"
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val chat = chatViewModel.getChat(chatId)
                val messages = chatViewModel.getMessages(chatId)

                ChatDetailScreen(
                    chat = chat,
                    messages = messages,
                    onBackClick = {
                        currentRoute = Routes.CHAT_LIST
                        navController.popBackStack()
                    },
                    onSendMessage = { content ->
                        chatViewModel.sendMessage(chatId, content)
                    },
                    onSendVoice = {
                        chatViewModel.sendVoiceMessage(chatId)
                    },
                    onSendImage = {
                        chatViewModel.sendImageMessage(chatId)
                    },
                    onSendVideo = {
                        chatViewModel.sendVideoMessage(chatId)
                    }
                )
            }
        }

        // 浮动底部导航栏（覆盖在内容上方）
        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            ) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        currentRoute = route
                        navController.navigate(route) {
                            popUpTo(Routes.CHAT_LIST) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    userName = chatViewModel.currentUser.value.name
                )
            }
        }
    }
}
