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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kk.mumuchat.navigation.AppNavigation
import com.kk.mumuchat.ui.theme.BackgroundLight
import com.kk.mumuchat.ui.theme.MuMuChatTheme
import com.kk.mumuchat.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val chatViewModel: ChatViewModel = viewModel()
            val isDark = chatViewModel.isDarkMode.value
            MuMuChatTheme(isDarkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDark) {
                        androidx.compose.ui.graphics.Color(0xFF0E1B2D)
                    } else {
                        BackgroundLight
                    }
                ) {
                    AppNavigation(chatViewModel = chatViewModel)
                }
            }
        }
    }
}

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
