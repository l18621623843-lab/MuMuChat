package com.kk.mumuchat.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kk.mumuchat.R
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector? = null,
    val useAppIcon: Boolean = false,
    val title: String,
    val subtitle: String,
    val gradientColors: List<Color>
)

private val pages = listOf(
    OnboardingPage(
        useAppIcon = true,
        title = "欢迎使用 MuMuChat",
        subtitle = "一款简洁、安全、高效的即时通讯应用\n让沟通更轻松",
        gradientColors = listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1E88E5))
    ),
    OnboardingPage(
        icon = Icons.Filled.Lock,
        title = "隐私安全",
        subtitle = "支持指纹私密模式\n你的聊天数据只属于你",
        gradientColors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047))
    ),
    OnboardingPage(
        icon = Icons.Filled.Speed,
        title = "极速体验",
        subtitle = "流畅的动画与交互\n让每一次对话都赏心悦目",
        gradientColors = listOf(Color(0xFF4A148C), Color(0xFF6A1B9A), Color(0xFF8E24AA))
    )
)

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon area
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (page.useAppIcon) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(80.dp)
                )
            } else if (page.icon != null) {
                Icon(
                    page.icon,
                    contentDescription = page.title,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Text(
            page.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            page.subtitle,
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(pages[pagerState.currentPage].gradientColors)
            )
    ) {
        // Skip button
        if (pagerState.currentPage < pages.size - 1) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Text("跳过", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingPageContent(pages[pageIndex])
        }

        // Bottom area: indicators + button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(horizontalArrangement = Arrangement.Center) {
                pages.indices.forEach { index ->
                    val isActive = pagerState.currentPage == index
                    val width by animateDpAsState(
                        if (isActive) 24.dp else 8.dp,
                        spring(stiffness = Spring.StiffnessMedium), label = "w"
                    )
                    val color by animateColorAsState(
                        if (isActive) Color.White else Color.White.copy(alpha = 0.35f),
                        tween(200), label = "c"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Next / Get Started button
            val isLast = pagerState.currentPage == pages.size - 1
            Button(
                onClick = {
                    if (isLast) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = pages[pagerState.currentPage].gradientColors[1]
                )
            ) {
                Text(
                    if (isLast) "开始使用" else "下一步",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
