package com.kk.mumuchat.ui.screens

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.kk.mumuchat.model.Message
import com.kk.mumuchat.ui.theme.*
import kotlinx.coroutines.delay

// ==================== 语音气泡（真实播放 + 进度同步）====================
@Composable
fun VoiceBubble(message: Message, isPlaying: Boolean, onClick: () -> Unit, onLongPress: () -> Unit) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(
        topStart = if (message.isSentByMe) 16.dp else 4.dp,
        topEnd = if (message.isSentByMe) 4.dp else 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )
    val bgBrush = if (message.isSentByMe) {
        Brush.linearGradient(listOf(Color(0xFF4A90E2), Color(0xFF5BA3F5)))
    } else {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.98f), Color.White.copy(alpha = 0.95f)))
    }
    val contentColor = if (message.isSentByMe) Color.White else Color(0xFF1A1A1A)

    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var progress by remember { mutableFloatStateOf(0f) }
    var hasEnded by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying && message.mediaUri != null) {
            hasEnded = false
            exoPlayer.setMediaItem(MediaItem.fromUri(message.mediaUri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            while (isPlaying && !hasEnded) {
                val dur = exoPlayer.duration.coerceAtLeast(1L)
                val pos = exoPlayer.currentPosition
                progress = (pos.toFloat() / dur).coerceIn(0f, 1f)
                if (exoPlayer.playbackState == Player.STATE_ENDED) {
                    progress = 1f
                    hasEnded = true
                    break
                }
                delay(80)
            }
        } else {
            exoPlayer.stop()
            progress = 0f
        }
    }

    // 播放结束自动回调
    LaunchedEffect(hasEnded) {
        if (hasEnded) {
            delay(200)
            onClick() // 触发外部停止播放状态
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Row(
        modifier = Modifier
            .shadow(3.dp, shape, spotColor = Color.Black.copy(alpha = 0.15f))
            .clip(shape)
            .background(bgBrush)
            .border(
                0.5.dp,
                if (message.isSentByMe) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.08f),
                shape
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val bars = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.3f, 0.7f, 0.5f, 0.6f)
                bars.forEachIndexed { i, h ->
                    val barAlpha = if (isPlaying && (i.toFloat() / bars.size) < progress) 1f else 0.35f
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height((h * 20).dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(contentColor.copy(alpha = barAlpha))
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text("${message.duration}″", color = contentColor.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    }
}

// ==================== 图片预览（微信风格：全屏黑底 + 缩放手势 + 点击关闭）====================
@Composable
fun ImagePreviewDialog(uri: Uri, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { if (scale <= 1f) onDismiss() }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "预览图片",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// ==================== 视频播放器（ExoPlayer 真实播放）====================
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayerDialog(uri: Uri, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Dialog(
        onDismissRequest = {
            exoPlayer.release()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 关闭按钮
            IconButton(
                onClick = {
                    exoPlayer.release()
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Close, "关闭", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

// ==================== 录音提示框（微信风格：快速弹出 + 麦克风图标 + 波纹）====================
@Composable
fun RecordingOverlay(seconds: Int, maxSeconds: Int, isCanceling: Boolean) {
    val transition = rememberInfiniteTransition(label = "recording")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 主圆形区域
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isCanceling) Color(0xCC960A0A)
                        else Color(0xCC2B2B2B)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 麦克风图标 + 脉冲
                    Box(contentAlignment = Alignment.Center) {
                        // 脉冲圆环
                        if (!isCanceling) {
                            Box(
                                modifier = Modifier
                                    .size((60 * pulse).dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF07C160).copy(alpha = 0.2f * pulse))
                            )
                        }
                        Icon(
                            if (isCanceling) Icons.Default.Close else Icons.Default.Mic,
                            contentDescription = "录音",
                            tint = if (isCanceling) Color.White else Color(0xFF07C160),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // 时长显示
                    val min = seconds / 60
                    val sec = seconds % 60
                    Text(
                        "${min}:${sec.toString().padStart(2, '0')}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (isCanceling) "松开 取消" else "上滑 取消",
                color = if (isCanceling) Color(0xFFFF5252) else Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==================== 附件面板 ====================
@Composable
fun AttachmentPanel(
    onVoice: () -> Unit = {},
    onImage: () -> Unit = {},
    onVideo: () -> Unit = {},
    onCamera: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val colors = LocalMuMuColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.panelBg)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AttachmentGridItem(Icons.Default.Image, "相册", onImage)
            AttachmentGridItem(Icons.Default.CameraAlt, "拍摄", onCamera)
            AttachmentGridItem(Icons.Default.VideoCall, "视频", onVideo)
            AttachmentGridItem(Icons.Default.LocationOn, "位置") {}
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AttachmentGridItem(Icons.Default.MonetizationOn, "红包") {}
            AttachmentGridItem(Icons.Default.Redeem, "转账") {}
            AttachmentGridItem(Icons.Default.Mic, "语音", onVoice)
            AttachmentGridItem(Icons.Default.CardGiftcard, "收藏") {}
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF888888))
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCCCCCC))
            )
        }
    }
}

@Composable
fun AttachmentGridItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = LocalMuMuColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(62.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.panelItemBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = colors.iconColor, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = colors.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ==================== 底部输入栏 ====================
@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendText: () -> Unit,
    onToggleVoice: () -> Unit,
    onAttachClick: () -> Unit = {},
    onEmojiClick: () -> Unit = {},
    onRequestPermission: () -> Unit,
    onStartRecord: () -> Unit,
    onCancelChange: (Boolean) -> Unit,
    onFinishRecord: (Boolean) -> Unit,
    isVoiceMode: Boolean,
    audioPermissionGranted: Boolean,
    inputTextNotBlank: Boolean,
    inputHeight: androidx.compose.ui.unit.Dp = 46.dp,
) {
    val colors = LocalMuMuColors.current
    val barBg = colors.inputBarBg
    val iconColor = colors.iconColor

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.divider)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(barBg)
                .padding(horizontal = 6.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleVoice, Modifier.size(38.dp)) {
                if (isVoiceMode) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .border(1.5.dp, iconColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Keyboard, "切换键盘", tint = iconColor, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Icon(Icons.Default.Mic, "语音输入", tint = iconColor, modifier = Modifier.size(28.dp))
                }
            }

            if (isVoiceMode) {
                HoldToTalkButton(
                    audioPermissionGranted = audioPermissionGranted,
                    onRequestPermission = onRequestPermission,
                    onStart = onStartRecord,
                    onCancelChange = onCancelChange,
                    onFinish = onFinishRecord,
                    modifier = Modifier
                        .weight(1f)
                        .height(inputHeight)
                )
            } else {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text("", color = Color(0xFFBBBBBB), fontSize = 15.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = inputHeight),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.inputFieldBorder,
                        unfocusedBorderColor = colors.inputFieldBorder,
                        focusedContainerColor = colors.inputFieldBg,
                        unfocusedContainerColor = colors.inputFieldBg,
                        focusedTextColor = colors.inputFieldText,
                        unfocusedTextColor = colors.inputFieldText,
                        cursorColor = Color(0xFF07C160)
                    ),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp)
                )
            }

            IconButton(onClick = onEmojiClick, Modifier.size(38.dp)) {
                Icon(Icons.Outlined.EmojiEmotions, "表情", tint = iconColor, modifier = Modifier.size(28.dp))
            }

            if (inputTextNotBlank) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF07C160))
                        .clickable { onSendText() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("发送", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                IconButton(onClick = onAttachClick, Modifier.size(38.dp)) {
                    Icon(Icons.Default.AddCircle, "更多", tint = iconColor, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

// ==================== 按住说话按钮（优化响应速度）====================
@Composable
fun HoldToTalkButton(
    audioPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onStart: () -> Unit,
    onCancelChange: (Boolean) -> Unit,
    onFinish: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMuMuColors.current
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) colors.inputFieldBorder else colors.inputFieldBg)
            .border(1.dp, colors.inputFieldBorder, RoundedCornerShape(6.dp))
            .pointerInput(audioPermissionGranted) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (!audioPermissionGranted) {
                        onRequestPermission()
                        return@awaitEachGesture
                    }
                    isPressed = true
                    // 立即触发录音（减少延迟，从200ms降到100ms）
                    val startTime = down.uptimeMillis
                    var longPressTriggered = false
                    var canceled = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!longPressTriggered && change.uptimeMillis - startTime >= 100) {
                            longPressTriggered = true
                            onStart()
                        }
                        if (!change.pressed) break
                        val dragY = change.position.y - down.position.y
                        if (longPressTriggered) {
                            val canceling = dragY < -80f
                            if (canceling != canceled) {
                                canceled = canceling
                                onCancelChange(canceled)
                            }
                        }
                    }
                    isPressed = false
                    if (longPressTriggered) {
                        onFinish(canceled)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isPressed) "松开 结束" else "按住 说话",
            color = colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==================== Emoji面板 ====================
@Composable
fun EmojiPanel(onSelect: (String) -> Unit) {
    val colors = LocalMuMuColors.current
    val emojis = listOf("😀", "😁", "😂", "🥲", "😍", "😎", "🥳", "🤝", "✨", "🔥", "💬", "🎧")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.panelBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        emojis.forEach { emoji ->
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(emoji) }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
    }
}

// ==================== 权限提示 ====================
@Composable
fun PermissionTip(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
    }
}

// ==================== 上传进度遮罩 ====================
@Composable
internal fun UploadProgressOverlay(
    progress: Float,
    status: UploadStatus,
    modifier: Modifier = Modifier
) {
    val statusText = when (status) {
        UploadStatus.Compressing -> "压缩中"
        UploadStatus.Uploading -> "上传中"
        UploadStatus.Failed -> "发送失败"
        UploadStatus.Completed -> ""
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (status == UploadStatus.Failed) {
                Icon(Icons.Default.Close, "失败", tint = IconBgRed, modifier = Modifier.size(36.dp))
            } else {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(40.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        strokeWidth = 3.dp
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (statusText.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(statusText, color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
            }
        }
    }
}

// ==================== 加载更多 ====================
@Composable
fun LoadMoreBubble(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text("加载更多", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

// ==================== 新消息提示 ====================
@Composable
fun NewMessageIndicator(count: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SkyBlue.copy(alpha = 0.9f))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text("新消息 $count", color = Color.White, fontSize = 12.sp)
        }
    }
}
