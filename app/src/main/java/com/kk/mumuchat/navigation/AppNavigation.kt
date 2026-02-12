package com.kk.mumuchat.navigation

import android.net.Uri
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

@Composable
fun AppNavigation(chatViewModel: ChatViewModel = viewModel()) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(Routes.CHAT_LIST) }

    val showBottomBar = currentRoute in listOf(
        Routes.CHAT_LIST, Routes.CONTACTS,
        Routes.DISCOVER, Routes.PROFILE
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.CHAT_LIST,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.CHAT_LIST) {
                currentRoute = Routes.CHAT_LIST
                ChatListScreen(
                    chatList = chatViewModel.chatList,
                    contacts = chatViewModel.contacts,
                    onChatClick = { chatId ->
                        currentRoute = "chat_detail"
                        navController.navigate(Routes.chatDetail(chatId))
                    },
                    isDarkMode = chatViewModel.isDarkMode.value,
                    onToggleDarkMode = { chatViewModel.toggleDarkMode() }
                )
            }

            composable(Routes.CONTACTS) {
                currentRoute = Routes.CONTACTS
                ContactsScreen(contacts = chatViewModel.contacts)
            }

            composable(Routes.DISCOVER) {
                currentRoute = Routes.DISCOVER
                DiscoverScreen()
            }

            composable(Routes.PROFILE) {
                currentRoute = Routes.PROFILE
                ProfileScreen(user = chatViewModel.currentUser.value)
            }

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
                    onSendVoice = { duration, uri ->
                        chatViewModel.sendVoiceMessage(chatId, duration, uri)
                    },
                    onSendImage = { uri ->
                        chatViewModel.sendImageMessage(chatId, uri)
                    },
                    onSendVideo = { uri, duration ->
                        chatViewModel.sendVideoMessage(chatId, uri, duration)
                    }
                )
            }
        }

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