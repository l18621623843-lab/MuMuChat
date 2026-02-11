package com.kk.mumuchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kk.mumuchat.navigation.AppNavigation
import com.kk.mumuchat.ui.theme.BackgroundLight
import com.kk.mumuchat.ui.theme.MuMuChatTheme

/**
 * MuMuChat 应用主入口 Activity
 *
 * 使用 Jetpack Compose 构建 UI，整体架构：
 * - MuMuChatTheme：天蓝色主题
 * - AppNavigation：管理所有页面的导航路由
 * - ChatViewModel：共享数据层（聊天、联系人、消息）
 *
 * 页面结构：
 * 1. 聊天列表页（ChatListScreen）- 主页
 * 2. 联系人页（ContactsScreen）
 * 3. 发现/设置页（DiscoverScreen）
 * 4. 个人资料页（ProfileScreen）
 * 5. 聊天详情页（ChatDetailScreen）
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启用边到边显示（沉浸式状态栏）
        enableEdgeToEdge()
        // 使用 Compose 设置界面内容
        setContent {
            MuMuChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundLight
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * 全局预览 - 完整应用入口
 */
@Preview(showBackground = true, showSystemUi = true, name = "MuMuChat 主界面")
@Composable
fun AppPreview() {
    MuMuChatTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundLight
        ) {
            AppNavigation()
        }
    }
}
