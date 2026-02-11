package com.kk.mumuchat.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.kk.mumuchat.model.Chat
import com.kk.mumuchat.model.Message
import com.kk.mumuchat.model.MessageType
import com.kk.mumuchat.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class MessageSendStatus { Sending, Sent, Delivered, Read, Failed }

private enum class UploadType { Image, Video, Voice, Camera }

private enum class UploadStatus { Compressing, Uploading, Failed, Completed }

private data class UploadTask(
    val id: String,
    val type: UploadType,
    val label: String,
    val sizeMb: Int,
    val progress: Float,
    val status: UploadStatus,
    val errorMessage: String = ""
)

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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val inputHeight = when {
        screenHeight < 700 -> 42.dp
        screenHeight > 900 -> 52.dp
        else -> 46.dp
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showAttachPanel by remember { mutableStateOf(false) }
    var showEmojiPanel by remember { mutableStateOf(false) }
    var previewImageDesc by remember { mutableStateOf<String?>(null) }
    var playVideoDesc by remember { mutableStateOf<String?>(null) }
    var playingVoiceId by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableIntStateOf(0) }
    var recordCanceled by remember { mutableStateOf(false) }
    var pendingNewCount by remember { mutableIntStateOf(0) }
    var permissionTip by remember { mutableStateOf<String?>(null) }

    val messageStatusMap = remember { mutableStateMapOf<String, MessageSendStatus>() }
    val messageFirstSeenMap = remember { mutableStateMapOf<String, Long>() }
    val hiddenMessageIds = remember { mutableStateMapOf<String, Boolean>() }
    val localSystemMessages = remember { mutableStateListOf<Message>() }
    val uploadTasks = remember { mutableStateListOf<UploadTask>() }

    var audioPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val onSendMessageState = rememberUpdatedState(onSendMessage)
    val onSendVoiceState = rememberUpdatedState(onSendVoice)
    val onSendImageState = rememberUpdatedState(onSendImage)
    val onSendVideoState = rememberUpdatedState(onSendVideo)

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        audioPermissionGranted = granted
        permissionTip = if (granted) null else "录音权限被拒绝"
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val pick = uris.take(9)
        if (uris.size > 9) {
            permissionTip = "最多选择9张图片"
        }
        pick.forEachIndexed { index, _ ->
            val sizeMb = Random.nextInt(1, 8)
            val label = "图片 ${index + 1} · 1080p · 80%"
            enqueueUpload(
                uploadTasks = uploadTasks,
                coroutineScope = coroutineScope,
                type = UploadType.Image,
                label = label,
                sizeMb = sizeMb
            ) { onSendImageState.value.invoke() }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val sizeMb = Random.nextInt(20, 120)
        val label = "视频 · 720p · 1Mbps"
        enqueueUpload(
            uploadTasks = uploadTasks,
            coroutineScope = coroutineScope,
            type = UploadType.Video,
            label = label,
            sizeMb = sizeMb
        ) { onSendVideoState.value.invoke() }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        val label = "拍摄照片 · 1080p · 80%"
        enqueueUpload(
            uploadTasks = uploadTasks,
            coroutineScope = coroutineScope,
            type = UploadType.Camera,
            label = label,
            sizeMb = Random.nextInt(2, 10)
        ) { onSendImageState.value.invoke() }
    }

    val mergedMessages by remember {
        derivedStateOf { (messages + localSystemMessages).filter { !hiddenMessageIds.containsKey(it.id) } }
    }
    val pageSize = 20
    var pageCount by remember { mutableIntStateOf(1) }
    val visibleMessages by remember {
        derivedStateOf { mergedMessages.takeLast(pageCount * pageSize) }
    }
    val isAtBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            visibleMessages.isEmpty() || lastVisible >= visibleMessages.lastIndex
        }
    }

    LaunchedEffect(messages.size) {
        messages.filter { it.isSentByMe }.forEach { msg ->
            if (!messageStatusMap.containsKey(msg.id)) {
                messageStatusMap[msg.id] = if (msg.isRead) MessageSendStatus.Read else MessageSendStatus.Delivered
                if (!messageFirstSeenMap.containsKey(msg.id)) {
                    messageFirstSeenMap[msg.id] = System.currentTimeMillis()
                }
            }
        }
    }

    LaunchedEffect(mergedMessages.size) {
        if (mergedMessages.isEmpty()) return@LaunchedEffect
        if (isAtBottom) {
            listState.animateScrollToItem(visibleMessages.lastIndex.coerceAtLeast(0))
            pendingNewCount = 0
        } else {
            pendingNewCount += 1
        }
        val last = mergedMessages.last()
        if (last.isSentByMe && !messageStatusMap.containsKey(last.id)) {
            messageFirstSeenMap[last.id] = System.currentTimeMillis()
            messageStatusMap[last.id] = MessageSendStatus.Sending
            coroutineScope.launch {
                delay(400)
                if (!hiddenMessageIds.containsKey(last.id)) {
                    messageStatusMap[last.id] = MessageSendStatus.Sent
                }
                delay(500)
                if (!hiddenMessageIds.containsKey(last.id)) {
                    messageStatusMap[last.id] = MessageSendStatus.Delivered
                }
                delay(700)
                if (!hiddenMessageIds.containsKey(last.id) && last.isRead) {
                    messageStatusMap[last.id] = MessageSendStatus.Read
                }
            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, visibleMessages.size, mergedMessages.size) {
        if (listState.firstVisibleItemIndex == 0 && visibleMessages.size < mergedMessages.size) {
            pageCount += 1
        }
    }

    LaunchedEffect(isRecording) {
        if (!isRecording) return@LaunchedEffect
        recordSeconds = 0
        while (isRecording && recordSeconds < 60) {
            delay(1000)
            recordSeconds += 1
        }
        if (isRecording) {
            isRecording = false
            recordCanceled = false
            enqueueVoiceUpload(
                uploadTasks = uploadTasks,
                coroutineScope = coroutineScope,
                duration = recordSeconds.coerceAtLeast(1),
                onComplete = { onSendVoiceState.value.invoke() }
            )
        }
    }

    val bgBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF17293D), Color(0xFF0E1B2D), Color(0xFF142233))
    )

    previewImageDesc?.let { desc ->
        ImagePreviewDialog(description = desc, onDismiss = { previewImageDesc = null })
    }

    playVideoDesc?.let { desc ->
        VideoPlayerDialog(description = desc, onDismiss = { playVideoDesc = null })
    }

    if (isRecording) {
        RecordingOverlay(
            seconds = recordSeconds,
            maxSeconds = 60,
            isCanceling = recordCanceled
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .navigationBarsPadding()
            .imePadding()
            .animateContentSize()
    ) {
        ChatTopBar(chat = chat, onBackClick = onBackClick)

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
                if (visibleMessages.size < mergedMessages.size) {
                    item {
                        LoadMoreBubble(
                            onClick = { pageCount += 1 }
                        )
                    }
                }
                items(visibleMessages) { message ->
                    MessageItem(
                        message = message,
                        isGroup = chat?.isGroup == true,
                        isPlaying = playingVoiceId == message.id,
                        status = messageStatusMap[message.id],
                        onVoiceClick = {
                            playingVoiceId = if (playingVoiceId == message.id) null else message.id
                        },
                        onImageClick = { previewImageDesc = message.mediaDescription },
                        onVideoClick = { playVideoDesc = message.mediaDescription },
                        onCopy = {
                            val text = when (message.messageType) {
                                MessageType.TEXT -> message.content
                                MessageType.VOICE -> "语音 ${message.duration}秒"
                                MessageType.IMAGE, MessageType.VIDEO -> message.mediaDescription
                                else -> message.content
                            }
                            clipboardManager.setText(AnnotatedString(text))
                        },
                        onDelete = { hiddenMessageIds[message.id] = true },
                        onRecall = {
                            hiddenMessageIds[message.id] = true
                            localSystemMessages.add(
                                Message(
                                    id = "sys_${System.currentTimeMillis()}",
                                    chatId = message.chatId,
                                    senderId = "system",
                                    senderName = "",
                                    content = "你撤回了一条消息",
                                    timestamp = "刚刚",
                                    isSentByMe = true
                                )
                            )
                        },
                        canRecall = message.isSentByMe && (System.currentTimeMillis() - (messageFirstSeenMap[message.id]
                            ?: System.currentTimeMillis())) <= 120000
                    )
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }

            if (pendingNewCount > 0) {
                NewMessageIndicator(
                    count = pendingNewCount,
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(visibleMessages.lastIndex.coerceAtLeast(0))
                            pendingNewCount = 0
                        }
                    }
                )
            }
        }

        if (uploadTasks.isNotEmpty()) {
            UploadQueuePanel(
                tasks = uploadTasks,
                onRetry = { taskId ->
                    val task = uploadTasks.firstOrNull { it.id == taskId } ?: return@UploadQueuePanel
                    restartUpload(uploadTasks, coroutineScope, task) {
                        when (task.type) {
                            UploadType.Video -> onSendVideoState.value.invoke()
                            UploadType.Voice -> onSendVoiceState.value.invoke()
                            UploadType.Image, UploadType.Camera -> onSendImageState.value.invoke()
                        }
                    }
                }
            )
        }

        AnimatedVisibility(visible = showEmojiPanel) {
            EmojiPanel(onSelect = { emoji ->
                inputText += emoji
            })
        }

        if (showAttachPanel) {
            AttachmentPanel(
                onVoice = {
                    showAttachPanel = false
                    if (audioPermissionGranted) {
                        isRecording = true
                        recordCanceled = false
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onImage = {
                    showAttachPanel = false
                    imagePickerLauncher.launch("image/*")
                },
                onVideo = {
                    showAttachPanel = false
                    videoPickerLauncher.launch("video/*")
                },
                onCamera = {
                    showAttachPanel = false
                    cameraLauncher.launch(null)
                },
                onDismiss = { showAttachPanel = false }
            )
        }

        if (permissionTip != null) {
            PermissionTip(message = permissionTip ?: "")
        }

        ChatInputBar(
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = {
                val text = inputText
                inputText = ""
                if (text.isNotBlank()) {
                    onSendMessageState.value.invoke(text)
                }
            },
            onAttachClick = {
                showAttachPanel = !showAttachPanel
                showEmojiPanel = false
                keyboardController?.hide()
            },
            onEmojiClick = {
                showEmojiPanel = !showEmojiPanel
                showAttachPanel = false
                if (showEmojiPanel) keyboardController?.hide()
            },
            inputHeight = inputHeight,
            voiceButton = {
                VoiceRecordButton(
                    audioPermissionGranted = audioPermissionGranted,
                    onRequestPermission = {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onStart = {
                        if (audioPermissionGranted) {
                            isRecording = true
                            recordCanceled = false
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onCancelChange = { recordCanceled = it },
                    onFinish = { canceled ->
                        if (isRecording) {
                            isRecording = false
                            if (!canceled) {
                                enqueueVoiceUpload(
                                    uploadTasks = uploadTasks,
                                    coroutineScope = coroutineScope,
                                    duration = recordSeconds.coerceAtLeast(1),
                                    onComplete = { onSendVoiceState.value.invoke() }
                                )
                            }
                        }
                    }
                )
            }
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

@Composable
private fun MessageItem(
    message: Message,
    isGroup: Boolean = false,
    isPlaying: Boolean = false,
    status: MessageSendStatus? = null,
    canRecall: Boolean = false,
    onVoiceClick: () -> Unit = {},
    onImageClick: () -> Unit = {},
    onVideoClick: () -> Unit = {},
    onCopy: () -> Unit = {},
    onDelete: () -> Unit = {},
    onRecall: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val showStatus = message.isSentByMe
    val statusText = when (status) {
        MessageSendStatus.Sending -> "发送中"
        MessageSendStatus.Sent -> "已发送"
        MessageSendStatus.Delivered -> "已送达"
        MessageSendStatus.Read -> "已读"
        MessageSendStatus.Failed -> "发送失败"
        null -> ""
    }
    val statusColor = when (status) {
        MessageSendStatus.Failed -> IconBgRed
        MessageSendStatus.Read -> SkyBlue
        else -> Color.White.copy(alpha = 0.55f)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isSentByMe) {
            AvatarCircle(SkyBlueLight.copy(alpha = 0.3f), SkyBlue)
            Spacer(Modifier.width(8.dp))
        }

        Column(modifier = Modifier.widthIn(max = 260.dp)) {
            if (isGroup && !message.isSentByMe) {
                Text(
                    message.senderName, color = SkyBlue,
                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 10.dp, bottom = 2.dp)
                )
            }
            Box {
                when (message.messageType) {
                    MessageType.VOICE -> VoiceBubble(message, isPlaying, onVoiceClick) { menuExpanded = true }
                    MessageType.IMAGE -> ImageBubble(message, onImageClick) { menuExpanded = true }
                    MessageType.VIDEO -> VideoBubble(message, onVideoClick) { menuExpanded = true }
                    else -> TextBubble(message, onClick = {}, onLongPress = { menuExpanded = true })
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("复制") },
                        onClick = {
                            menuExpanded = false
                            onCopy()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                    if (canRecall) {
                        DropdownMenuItem(
                            text = { Text("撤回") },
                            onClick = {
                                menuExpanded = false
                                onRecall()
                            }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    message.timestamp,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 10.sp
                )
                if (showStatus && statusText.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        statusText,
                        color = statusColor,
                        fontSize = 10.sp
                    )
                }
            }
        }

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
fun TextBubble(message: Message, onClick: () -> Unit, onLongPress: () -> Unit) {
    val shape = RoundedCornerShape(
        topStart = 18.dp, topEnd = 18.dp,
        bottomStart = if (message.isSentByMe) 18.dp else 4.dp,
        bottomEnd = if (message.isSentByMe) 4.dp else 18.dp
    )
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (message.isSentByMe) BubbleSent else Color.White.copy(alpha = 0.9f))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            message.content,
            color = if (message.isSentByMe) Color.White else TextPrimary,
            fontSize = 15.sp, lineHeight = 21.sp
        )
    }
}

@Composable
fun VoiceBubble(message: Message, isPlaying: Boolean, onClick: () -> Unit, onLongPress: () -> Unit) {
    val bgColor = if (message.isSentByMe) BubbleSent else Color.White.copy(alpha = 0.9f)
    val contentColor = if (message.isSentByMe) Color.White else TextPrimary
    val shape = RoundedCornerShape(18.dp)

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
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))

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
            Text(
                "${message.duration}″",
                color = contentColor.copy(alpha = 0.7f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun ImageBubble(message: Message, onClick: () -> Unit, onLongPress: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val gradientColors = if (message.isSentByMe)
        listOf(Color(0xFF4FC3F7), Color(0xFF81D4FA), Color(0xFFB3E5FC))
    else
        listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))

    Column(
        modifier = Modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.1f))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
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

@Composable
fun VideoBubble(message: Message, onClick: () -> Unit, onLongPress: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val gradientColors = listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB))

    Column(
        modifier = Modifier
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Box(
            modifier = Modifier
                .size(width = 220.dp, height = 160.dp)
                .clip(shape)
                .background(Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
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
    onCamera: () -> Unit,
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
        AttachmentItem(Icons.Default.CameraAlt, "拍摄", Color(0xFFFF7043), onCamera)
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
    onAttachClick: () -> Unit = {},
    onEmojiClick: () -> Unit = {},
    inputHeight: androidx.compose.ui.unit.Dp = 46.dp,
    voiceButton: @Composable () -> Unit = {}
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
        voiceButton()
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = { Text("消息", color = Color.White.copy(alpha = 0.35f), fontSize = 14.sp) },
            modifier = Modifier.weight(1f).height(inputHeight),
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
        IconButton(onClick = onEmojiClick, Modifier.size(40.dp)) {
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

@Composable
fun VoiceRecordButton(
    audioPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onStart: () -> Unit,
    onCancelChange: (Boolean) -> Unit,
    onFinish: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .pointerInput(audioPermissionGranted) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startTime = down.uptimeMillis
                    var longPressTriggered = false
                    var canceled = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!longPressTriggered && change.uptimeMillis - startTime >= 300) {
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
                    if (!longPressTriggered) {
                        onRequestPermission()
                    } else {
                        onFinish(canceled)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Mic,
            "语音",
            tint = if (audioPermissionGranted) SkyBlue else Color.White.copy(alpha = 0.55f),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun RecordingOverlay(seconds: Int, maxSeconds: Int, isCanceling: Boolean) {
    val transition = rememberInfiniteTransition(label = "recording")
    val glow by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "recordGlow"
    )
    val remain = (maxSeconds - seconds).coerceAtLeast(0)
    val bars = listOf(0.2f, 0.6f, 0.4f, 0.8f, 0.5f, 0.7f, 0.3f)
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        bars.forEachIndexed { index, h ->
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height((h * (40 + glow * 20)).dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isCanceling) IconBgRed else SkyBlue.copy(alpha = 0.9f)
                                    )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (isCanceling) "松开取消" else "滑动取消",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "剩余 ${remain}s · 16kHz 单声道",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun EmojiPanel(onSelect: (String) -> Unit) {
    val emojis = listOf("😀", "😁", "😂", "🥲", "😍", "😎", "🥳", "🤝", "✨", "🔥", "💬", "🎧")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A3050).copy(alpha = 0.92f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        emojis.forEach { emoji ->
            Text(
                text = emoji,
                fontSize = 20.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(emoji) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

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

@Composable
private fun UploadQueuePanel(tasks: List<UploadTask>, onRetry: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A3050).copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        tasks.forEach { task ->
            UploadTaskRow(task = task, onRetry = onRetry)
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun UploadTaskRow(task: UploadTask, onRetry: (String) -> Unit) {
    val icon = when (task.type) {
        UploadType.Image -> Icons.Default.Image
        UploadType.Video -> Icons.Default.Videocam
        UploadType.Voice -> Icons.Default.Mic
        UploadType.Camera -> Icons.Default.CameraAlt
    }
    val statusText = when (task.status) {
        UploadStatus.Compressing -> "压缩中"
        UploadStatus.Uploading -> "上传中"
        UploadStatus.Failed -> "失败"
        UploadStatus.Completed -> "完成"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, "上传", tint = SkyBlue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.label, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                Text(
                    "${task.sizeMb}MB · $statusText",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
            if (task.status == UploadStatus.Failed) {
                TextButton(onClick = { onRetry(task.id) }) {
                    Text("重试", color = SkyBlue, fontSize = 12.sp)
                }
            }
        }
        if (task.status == UploadStatus.Uploading || task.status == UploadStatus.Compressing) {
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { task.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SkyBlue,
                trackColor = Color.White.copy(alpha = 0.15f)
            )
        }
        if (task.status == UploadStatus.Failed && task.errorMessage.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(task.errorMessage, color = IconBgRed, fontSize = 10.sp)
        }
    }
}

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

private fun enqueueUpload(
    uploadTasks: MutableList<UploadTask>,
    coroutineScope: CoroutineScope,
    type: UploadType,
    label: String,
    sizeMb: Int,
    onComplete: () -> Unit
) {
    val task = UploadTask(
        id = "up_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
        type = type,
        label = label,
        sizeMb = sizeMb,
        progress = 0f,
        status = UploadStatus.Compressing
    )
    uploadTasks.add(task)
    restartUpload(uploadTasks, coroutineScope, task, onComplete)
}

private fun enqueueVoiceUpload(
    uploadTasks: MutableList<UploadTask>,
    coroutineScope: CoroutineScope,
    duration: Int,
    onComplete: () -> Unit
) {
    val sizeMb = if (duration < 4) 1 else duration / 4
    enqueueUpload(
        uploadTasks = uploadTasks,
        coroutineScope = coroutineScope,
        type = UploadType.Voice,
        label = "语音 · ${duration}秒 · AMR",
        sizeMb = sizeMb,
        onComplete = onComplete
    )
}

private fun restartUpload(
    uploadTasks: MutableList<UploadTask>,
    coroutineScope: CoroutineScope,
    task: UploadTask,
    onComplete: () -> Unit
) {
    val taskId = task.id
    updateTask(uploadTasks, taskId) {
        it.copy(progress = 0f, status = UploadStatus.Compressing, errorMessage = "")
    }
    coroutineScope.launch {
        val compressSteps = 4
        for (step in 1..compressSteps) {
            delay(160)
            updateTask(uploadTasks, taskId) {
                it.copy(progress = (step / compressSteps.toFloat()) * 0.3f)
            }
        }
        if (task.type == UploadType.Video && task.sizeMb > 100) {
            updateTask(uploadTasks, taskId) {
                it.copy(status = UploadStatus.Failed, errorMessage = "视频超过100MB")
            }
            return@launch
        }
        updateTask(uploadTasks, taskId) { it.copy(status = UploadStatus.Uploading) }
        val uploadSteps = 7
        for (step in 1..uploadSteps) {
            delay(220)
            updateTask(uploadTasks, taskId) {
                it.copy(progress = 0.3f + (step / uploadSteps.toFloat()) * 0.7f)
            }
        }
        val shouldFail = task.sizeMb > 80 && Random.nextFloat() < 0.25f
        if (shouldFail) {
            updateTask(uploadTasks, taskId) {
                it.copy(status = UploadStatus.Failed, errorMessage = "网络不稳定，请重试")
            }
        } else {
            updateTask(uploadTasks, taskId) { it.copy(status = UploadStatus.Completed, progress = 1f) }
            onComplete()
        }
    }
}

private fun updateTask(
    uploadTasks: MutableList<UploadTask>,
    taskId: String,
    updater: (UploadTask) -> UploadTask
) {
    val index = uploadTasks.indexOfFirst { it.id == taskId }
    if (index >= 0) {
        uploadTasks[index] = updater(uploadTasks[index])
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
