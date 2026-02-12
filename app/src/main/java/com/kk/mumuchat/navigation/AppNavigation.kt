package com.kk.mumuchat.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

/** Tab切换动画时长 */
private const val TAB_ANIM_MS = 200
/** 详情页动画时长 */
private const val DETAIL_ANIM_MS = 180

@Composable
fun AppNavigation(chatViewModel: ChatViewModel = viewModel()) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(Routes.CHAT_LIST) }
    var previousTabIndex by remember { mutableIntStateOf(0) }

    val showBottomBar = currentRoute in Routes.tabOrder

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
            // ==================== Tab页面（左右滑动切换）====================
            Routes.tabOrder.forEach { route ->
                composable(
                    route = route,
                    enterTransition = {
                        val fromIndex = Routes.tabIndex(initialState.destination.route ?: "")
                        val toIndex = Routes.tabIndex(route)
                        if (fromIndex >= 0 && toIndex >= 0) {
                            // Tab间切换：根据索引方向左右滑入
                            val direction = if (toIndex > fromIndex)
                                AnimatedContentTransitionScope.SlideDirection.Left
                            else
                                AnimatedContentTransitionScope.SlideDirection.Right
                            slideIntoContainer(direction, tween(TAB_ANIM_MS)) + fadeIn(tween(TAB_ANIM_MS / 2))
                        } else {
                            // 从详情页返回：快速淡入
                            fadeIn(tween(DETAIL_ANIM_MS))
                        }
                    },
                    exitTransition = {
                        val fromIndex = Routes.tabIndex(route)
                        val toIndex = Routes.tabIndex(targetState.destination.route ?: "")
                        if (fromIndex >= 0 && toIndex >= 0) {
                            val direction = if (toIndex > fromIndex)
                                AnimatedContentTransitionScope.SlideDirection.Left
                            else
                                AnimatedContentTransitionScope.SlideDirection.Right
                            slideOutOfContainer(direction, tween(TAB_ANIM_MS)) + fadeOut(tween(TAB_ANIM_MS / 2))
                        } else {
                            // 进入详情页：快速淡出
                            fadeOut(tween(DETAIL_ANIM_MS))
                        }
                    },
                    popEnterTransition = {
                        fadeIn(tween(DETAIL_ANIM_MS))
                    },
                    popExitTransition = {
                        fadeOut(tween(DETAIL_ANIM_MS))
                    }
                ) {
                    currentRoute = route
                    previousTabIndex = Routes.tabIndex(route)
                    when (route) {
                        Routes.CHAT_LIST -> ChatListScreen(
                            chatList = chatViewModel.chatList,
                            contacts = chatViewModel.contacts,
                            onChatClick = { chatId ->
                                currentRoute = "chat_detail"
                                navController.navigate(Routes.chatDetail(chatId))
                            },
                            isDarkMode = chatViewModel.isDarkMode.value,
                            onToggleDarkMode = { chatViewModel.toggleDarkMode() }
                        )
                        Routes.CONTACTS -> ContactsScreen(contacts = chatViewModel.contacts)
                        Routes.DISCOVER -> DiscoverScreen()
                        Routes.PROFILE -> ProfileScreen(user = chatViewModel.currentUser.value)
                    }
                }
            }

            // ==================== 聊天详情页（快速右滑进出）====================
            composable(
                route = Routes.CHAT_DETAIL,
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType }
                ),
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        tween(DETAIL_ANIM_MS)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        tween(DETAIL_ANIM_MS)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        tween(DETAIL_ANIM_MS)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        tween(DETAIL_ANIM_MS)
                    )
                }
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
