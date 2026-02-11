package com.kk.mumuchat.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import com.kk.mumuchat.model.Chat
import com.kk.mumuchat.model.Message
import com.kk.mumuchat.model.MessageType
import com.kk.mumuchat.ui.theme.*

/**
 * 聊天详情页面
 * 固定顶栏 + 可滚动消息列表 + 固定底部输入栏
 * 支持文本、语音、图片、视频消息的展示和交互
 */
@Composable
fun ChatDetailScreen(
    chat: Chat?,
    messages: List<Message>,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendVoice: () -> Unit = {},
    onSendImage: () -> Unit = {},
    onSendVideo: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    // 是否显示附件面板
    var showAttachPanel by remember { mutableStateOf(false) }
    // 图片预览弹窗
    var previewImageDesc by remember { mutableStateOf<String?>(null) }
    // 视频播放弹窗
    var playVideoDesc by remember { mutableStateOf<String?>(null) }
    // 语音播放状态（记录正在播放的消息 ID）
    var playingVoiceId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val bgBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF17293D), Color(0xFF0E1B2D), Color(0xFF142233))
    )

    // 图片预览弹窗
    previewImageDesc?.let { desc ->
        ImagePreviewDialog(description = desc, onDismiss = { previewImageDesc = null })
    }

    // 视频播放弹窗
    playVideoDesc?.let { desc ->
        VideoPlayerDialog(description = desc, onDismiss = { playVideoDesc = null })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // ==================== 固定顶部栏 ====================
        ChatTopBar(chat = chat, onBackClick = onBackClick)

        // ==================== 消息列表 ====================
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    isGroup = chat?.isGroup == true,
                    isPlaying = playingVoiceId == message.id,
                    onVoiceClick = {
                        playingVoiceId = if (playingVoiceId == message.id) null else message.id
                    },
                    onImageClick = { previewImageDesc = message.mediaDescription },
                    onVideoClick = { playVideoDesc = message.mediaDescription }
                )
            }
            item { Spacer(modifier = Modifier.height(10.dp)) }
        }

        // ==================== 附件面板 ====================
        if (showAttachPanel) {
            AttachmentPanel(
                onVoice = { onSendVoice(); showAttachPanel = false },
                onImage = { onSendImage(); showAttachPanel = false },
                onVideo = { onSendVideo(); showAttachPanel = false },
                onDismiss = { showAttachPanel = false }
            )
        }

        // ==================== 底部输入栏 ====================
        ChatInputBar(
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = { onSendMessage(inputText); inputText = "" },
            onAttachClick = { showAttachPanel = !showAttachPanel }
        )
    }
}

// ==================== 顶部栏 ====================
@Composable
fun ChatTopBar(chat: Chat?, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, ambientColor = Color.Black.copy(alpha = 0.3f))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1A3050), Color(0xFF1E3A55))
                )
            )
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = SkyBlue, modifier = Modifier.size(22.dp))
        }
        Text("返回", color = SkyBlue, fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                chat?.name ?: "聊天", color = Color.White,
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold
            )
            if (chat?.isGroup == true) {
                Text("群组·点击查看", color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = {}) {
            Icon(Icons.Default.Add, "添加", tint = SkyBlue, modifier = Modifier.size(22.dp))
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, "更多", tint = SkyBlue, modifier = Modifier.size(22.dp))
        }
    }
}

// ==================== 消息气泡（根据类型分发）====================
@Composable
fun MessageBubble(
    message: Message,
    isGroup: Boolean = false,
    isPlaying: Boolean = false,
    onVoiceClick: () -> Unit = {},
    onImageClick: () -> Unit = {},
    onVideoClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // 左侧头像（接收消息）
        if (!message.isSentByMe) {
            AvatarCircle(SkyBlueLight.copy(alpha = 0.3f), SkyBlue)
            Spacer(Modifier.width(8.dp))
        }

        Column(modifier = Modifier.widthIn(max = 260.dp)) {
            // 群聊发送者名称
            if (isGroup && !message.isSentByMe) {
                Text(
                    message.senderName, color = SkyBlue,
                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 10.dp, bottom = 2.dp)
                )
            }
            // 根据消息类型渲染不同气泡
            when (message.messageType) {
                MessageType.VOICE -> VoiceBubble(message, isPlaying, onVoiceClick)
                MessageType.IMAGE -> ImageBubble(message, onImageClick)
                MessageType.VIDEO -> VideoBubble(message, onVideoClick)
                else -> TextBubble(message)
            }
        }

        // 右侧头像（发送消息）
        if (message.isSentByMe) {
            Spacer(Modifier.width(8.dp))
            AvatarCircle(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.6f))
        }
    }
}

// ==================== 头像圆形组件 ====================
@Composable
fun AvatarCircle(bgColor: Color, iconColor: Color) {
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape).background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, null, tint = iconColor, modifier = Modifier.size(20.dp))
    }
}

// ==================== 文本气泡 ====================
@Composable
fun TextBubble(message: Message) {
    val shape = RoundedCornerShape(
        topStart = 18.dp, topEnd = 18.dp,
        bottomStart = if (message.isSentByMe) 18.dp else 4.dp,
        bottomEnd = if (message.isSentByMe) 4.dp else 18.dp
    )
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (message.isSentByMe) BubbleSent else Color.White.copy(alpha = 0.9f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            message.content,
            color = if (message.isSentByMe) Color.White else TextPrimary,
            fontSize = 15.sp, lineHeight = 21.sp
        )
    }
}

// ==================== 语音气泡（可点击播放/暂停）====================
@Composable
fun VoiceBubble(message: Message, isPlaying: Boolean, onClick: () -> Unit) {
    val bgColor = if (message.isSentByMe) BubbleSent else Color.White.copy(alpha = 0.9f)
    val contentColor = if (message.isSentByMe) Color.White else TextPrimary
    val shape = RoundedCornerShape(18.dp)

    // 播放动画
    val transition = rememberInfiniteTransition(label = "voice")
    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "voiceProgress"
    )

    Row(
        modifier = Modifier
            .clip(shape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 播放/暂停按钮
        Icon(
            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))

        // 波形条模拟
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val bars = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.3f, 0.7f, 0.5f, 0.6f)
                bars.forEachIndexed { i, h ->
                    val barAlpha = if (isPlaying && (i.toFloat() / bars.size) < progress) 1f else 0.4f
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height((h * 18).dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(contentColor.copy(alpha = barAlpha))
                    )
                }
            }
            // 时长
            Text(
                "${message.duration}″",
                color = contentColor.copy(alpha = 0.7f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ==================== 图片气泡（可点击预览）====================
@Composable
fun ImageBubble(message: Message, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    // 模拟图片缩略图（渐变色块）
    val gradientColors = if (message.isSentByMe)
        listOf(Color(0xFF4FC3F7), Color(0xFF81D4FA), Color(0xFFB3E5FC))
    else
        listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))

    Column(
        modifier = Modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
    ) {
        // 图片区域（渐变模拟）
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 150.dp)
                .clip(shape)
                .background(Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Image,
                contentDescription = "图片",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(40.dp)
            )
        }
        // 图片描述
        if (message.mediaDescription.isNotEmpty()) {
            Text(
                message.mediaDescription,
                color = if (message.isSentByMe) Color.White.copy(alpha = 0.7f)
                else TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// ==================== 视频气泡（可点击播放）====================
@Composable
fun VideoBubble(message: Message, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val gradientColors = listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB))

    Column(
        modifier = Modifier
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        // 视频封面（深色渐变 + 播放按钮）
        Box(
            modifier = Modifier
                .size(width = 220.dp, height = 160.dp)
                .clip(shape)
                .background(Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            // 半透明播放按钮
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "播放视频",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            // 时长标签
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                val min = message.duration / 60
                val sec = message.duration % 60
                Text(
                    "${min}:${sec.toString().padStart(2, '0')}",
                    color = Color.White, fontSize = 11.sp
                )
            }
        }
        if (message.mediaDescription.isNotEmpty()) {
            Text(
                message.mediaDescription,
                color = if (message.isSentByMe) Color.White.copy(alpha = 0.7f)
                else TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// ==================== 图片预览弹窗 ====================
@Composable
fun ImagePreviewDialog(description: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 模拟大图预览
                Box(
                    modifier = Modifier
                        .size(width = 300.dp, height = 400.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF4FC3F7), Color(0xFF81D4FA), Color(0xFFB3E5FC))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image, "预览",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(80.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(description, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                // 关闭按钮
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close, "关闭",
                        tint = Color.White, modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// ==================== 视频播放弹窗 ====================
@Composable
fun VideoPlayerDialog(description: String, onDismiss: () -> Unit) {
    // 模拟播放进度
    val transition = rememberInfiniteTransition(label = "video")
    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "videoProgress"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 视频画面模拟
                Box(
                    modifier = Modifier
                        .size(width = 320.dp, height = 220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1A237E), Color(0xFF283593))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Videocam, "播放中",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    )
                }
                // 进度条
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(320.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = SkyBlue,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(Modifier.height(12.dp))
                Text(description, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "关闭", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

// ==================== 附件面板（语音/图片/视频）====================
@Composable
fun AttachmentPanel(
    onVoice: () -> Unit,
    onImage: () -> Unit,
    onVideo: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A3050).copy(alpha = 0.95f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AttachmentItem(Icons.Default.Mic, "语音", Color(0xFF66BB6A), onVoice)
        AttachmentItem(Icons.Default.Image, "图片", Color(0xFF42A5F5), onImage)
        AttachmentItem(Icons.Default.Videocam, "视频", Color(0xFFAB47BC), onVideo)
        AttachmentItem(Icons.Default.CameraAlt, "拍摄", Color(0xFFFF7043), {})
    }
}

@Composable
fun AttachmentItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

// ==================== 底部输入栏 ====================
@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A3050).copy(alpha = 0.85f), Color(0xFF1A3050))
                )
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}, Modifier.size(40.dp)) {
            Icon(Icons.Default.Mic, "语音", tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(22.dp))
        }
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = { Text("消息", color = Color.White.copy(alpha = 0.35f), fontSize = 14.sp) },
            modifier = Modifier.weight(1f).height(46.dp),
            shape = RoundedCornerShape(23.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White.copy(alpha = 0.15f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                focusedContainerColor = Color.White.copy(alpha = 0.07f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = SkyBlue
            ),
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp)
        )
        IconButton(onClick = {}, Modifier.size(40.dp)) {
            Icon(Icons.Default.EmojiEmotions, "表情", tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(22.dp))
        }
        IconButton(
            onClick = { if (inputText.isNotBlank()) onSend() else onAttachClick() },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                if (inputText.isNotBlank()) Icons.Default.Send else Icons.Default.Add,
                if (inputText.isNotBlank()) "发送" else "附件",
                tint = if (inputText.isNotBlank()) SkyBlue else Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ==================== 预览 ====================
@Preview(showBackground = true, showSystemUi = true, name = "聊天详情")
@Composable
fun ChatDetailScreenPreview() {
    val mockMessages = listOf(
        Message("m1", "c1", "u1", "清大", "今天同步一下进度", "上午9:17"),
        Message("m2", "c1", "me", "devin", "收到，我看看", "上午9:22", isSentByMe = true),
        Message("m3", "c1", "u1", "清大", "", "上午9:25", messageType = MessageType.IMAGE, mediaDescription = "设计稿.png"),
        Message("m4", "c1", "me", "devin", "", "上午9:28", isSentByMe = true, messageType = MessageType.VOICE, duration = 12),
        Message("m5", "c1", "u1", "清大", "", "上午9:30", messageType = MessageType.VIDEO, duration = 45, mediaDescription = "演示.mp4"),
        Message("m6", "c1", "me", "devin", "效果不错！", "上午9:32", isSentByMe = true)
    )
    MuMuChatTheme {
        ChatDetailScreen(
            chat = Chat("c1", "绫骨开发进度群", isGroup = true),
            messages = mockMessages,
            onBackClick = {},
            onSendMessage = {}
        )
    }
}
