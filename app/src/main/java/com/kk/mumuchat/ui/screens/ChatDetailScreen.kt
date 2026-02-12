package com.kk.mumuchat.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.kk.mumuchat.model.Chat
import com.kk.mumuchat.model.Message
import com.kk.mumuchat.model.MessageType
import com.kk.mumuchat.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

private enum class MessageSendStatus { Sending, Sent, Delivered, Read, Failed }

internal enum class UploadStatus { Compressing, Uploading, Failed, Completed }

/** 从视频URI获取真实时长（秒） */
private fun getVideoDuration(context: android.content.Context, uri: Uri): Int {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        retriever.release()
        (durationMs / 1000).toInt().coerceAtLeast(1)
    } catch (_: Exception) {
        0
    }
}

/**
 * 聊天详情页面
 */
@Composable
fun ChatDetailScreen(
    chat: Chat?,
    messages: List<Message>,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendVoice: (Int, Uri?) -> Unit = { _, _ -> },
    onSendImage: (Uri) -> Unit = {},
    onSendVideo: (Uri, Int) -> Unit = { _, _ -> }
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
    var isVoiceMode by remember { mutableStateOf(false) }
    var previewImageUri by remember { mutableStateOf<Uri?>(null) }
    var playVideoUri by remember { mutableStateOf<Uri?>(null) }
    var playingVoiceId by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableIntStateOf(0) }
    var recordCanceled by remember { mutableStateOf(false) }
    var pendingNewCount by remember { mutableIntStateOf(0) }
    var permissionTip by remember { mutableStateOf<String?>(null) }
    var forceScrollToBottom by remember { mutableStateOf(false) }

    // 录音相关
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordFile by remember { mutableStateOf<File?>(null) }

    val messageStatusMap = remember { mutableStateMapOf<String, MessageSendStatus>() }
    val messageFirstSeenMap = remember { mutableStateMapOf<String, Long>() }
    val hiddenMessageIds = remember { mutableStateMapOf<String, Boolean>() }
    val localSystemMessages = remember { mutableStateListOf<Message>() }
    val pendingMediaMessages = remember { mutableStateListOf<Message>() }
    val uploadStateMap = remember { mutableStateMapOf<String, Pair<Float, UploadStatus>>() }

    var audioPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
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
    val contentResolver = context.contentResolver
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(9)
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        uris.forEach { uri ->
            val mimeType = contentResolver.getType(uri) ?: ""
            val isVideo = mimeType.startsWith("video/")
            val msgId = "pending_${System.currentTimeMillis()}_${Random.nextInt(1000)}"
            if (isVideo) {
                val duration = getVideoDuration(context, uri)
                val msg = Message(
                    id = msgId,
                    chatId = chat?.id ?: "",
                    senderId = "me",
                    senderName = chat?.name ?: "",
                    content = "",
                    timestamp = "刚刚",
                    isSentByMe = true,
                    messageType = MessageType.VIDEO,
                    mediaUri = uri,
                    duration = duration
                )
                pendingMediaMessages.add(msg)
                forceScrollToBottom = true
                simulateMediaUpload(
                    messageId = msgId,
                    uploadStateMap = uploadStateMap,
                    pendingMessages = pendingMediaMessages,
                    coroutineScope = coroutineScope,
                    sizeMb = Random.nextInt(20, 120)
                ) { onSendVideoState.value.invoke(uri, duration) }
            } else {
                val msg = Message(
                    id = msgId,
                    chatId = chat?.id ?: "",
                    senderId = "me",
                    senderName = chat?.name ?: "",
                    content = "",
                    timestamp = "刚刚",
                    isSentByMe = true,
                    messageType = MessageType.IMAGE,
                    mediaUri = uri
                )
                pendingMediaMessages.add(msg)
                forceScrollToBottom = true
                simulateMediaUpload(
                    messageId = msgId,
                    uploadStateMap = uploadStateMap,
                    pendingMessages = pendingMediaMessages,
                    coroutineScope = coroutineScope,
                    sizeMb = Random.nextInt(1, 8)
                ) { onSendImageState.value.invoke(uri) }
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val duration = getVideoDuration(context, uri)
        val msgId = "pending_${System.currentTimeMillis()}_${Random.nextInt(1000)}"
        val msg = Message(
            id = msgId,
            chatId = chat?.id ?: "",
            senderId = "me",
            senderName = chat?.name ?: "",
            content = "",
            timestamp = "刚刚",
            isSentByMe = true,
            messageType = MessageType.VIDEO,
            mediaUri = uri,
            duration = duration
        )
        pendingMediaMessages.add(msg)
        forceScrollToBottom = true
        simulateMediaUpload(
            messageId = msgId,
            uploadStateMap = uploadStateMap,
            pendingMessages = pendingMediaMessages,
            coroutineScope = coroutineScope,
            sizeMb = Random.nextInt(20, 120)
        ) { onSendVideoState.value.invoke(uri, duration) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        val msgId = "pending_${System.currentTimeMillis()}_${Random.nextInt(1000)}"
        val msg = Message(
            id = msgId,
            chatId = chat?.id ?: "",
            senderId = "me",
            senderName = chat?.name ?: "",
            content = "",
            timestamp = "刚刚",
            isSentByMe = true,
            messageType = MessageType.IMAGE
        )
        pendingMediaMessages.add(msg)
        forceScrollToBottom = true
        simulateMediaUpload(
            messageId = msgId,
            uploadStateMap = uploadStateMap,
            pendingMessages = pendingMediaMessages,
            coroutineScope = coroutineScope,
            sizeMb = Random.nextInt(2, 10)
        ) { }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
        permissionTip = if (granted) null else "相机权限被拒绝"
        if (granted) {
            cameraLauncher.launch(null)
        }
    }

    val mergedMessages by remember {
        derivedStateOf { (messages + localSystemMessages + pendingMediaMessages).filter { !hiddenMessageIds.containsKey(it.id) } }
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
        if (forceScrollToBottom) {
            listState.animateScrollToItem(visibleMessages.lastIndex.coerceAtLeast(0))
            pendingNewCount = 0
            forceScrollToBottom = false
        } else if (isAtBottom) {
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

    // 录音计时 + 真实录音
    LaunchedEffect(isRecording) {
        if (!isRecording) return@LaunchedEffect
        recordSeconds = 0
        // 开始真实录音
        try {
            val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            recordFile = file
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioChannels(1)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
        } catch (_: Exception) {
            mediaRecorder = null
            recordFile = null
        }

        while (isRecording && recordSeconds < 60) {
            delay(1000)
            recordSeconds += 1
        }
        if (isRecording) {
            isRecording = false
            recordCanceled = false
            // 停止录音
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
            } catch (_: Exception) { }
            mediaRecorder = null
            val duration = recordSeconds.coerceAtLeast(1)
            val voiceUri = recordFile?.let { Uri.fromFile(it) }
            onSendVoiceState.value.invoke(duration, voiceUri)
            forceScrollToBottom = true
        }
    }

    val themeColors = LocalMuMuColors.current
    val bgBrush = themeColors.chatBgBrush

    previewImageUri?.let { uri ->
        ImagePreviewDialog(uri = uri, onDismiss = { previewImageUri = null })
    }

    playVideoUri?.let { uri ->
        VideoPlayerDialog(uri = uri, onDismiss = { playVideoUri = null })
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
                    val uploadState = uploadStateMap[message.id]
                    MessageItem(
                        message = message,
                        isGroup = chat?.isGroup == true,
                        isPlaying = playingVoiceId == message.id,
                        status = messageStatusMap[message.id],
                        uploadProgress = uploadState?.first,
                        uploadStatus = uploadState?.second,
                        onVoiceClick = {
                            playingVoiceId = if (playingVoiceId == message.id) null else message.id
                        },
                        onImageClick = {
                            message.mediaUri?.let { previewImageUri = it }
                        },
                        onVideoClick = {
                            message.mediaUri?.let { playVideoUri = it }
                        },
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

        if (permissionTip != null) {
            PermissionTip(message = permissionTip ?: "")
        }

        ChatInputBar(
            inputText = inputText,
            onInputChange = { inputText = it },
            isVoiceMode = isVoiceMode,
            onSendText = {
                val text = inputText
                inputText = ""
                if (text.isNotBlank()) {
                    onSendMessageState.value.invoke(text)
                    forceScrollToBottom = true
                }
            },
            onToggleVoice = {
                isVoiceMode = !isVoiceMode
                showAttachPanel = false
                showEmojiPanel = false
                if (isVoiceMode) {
                    keyboardController?.hide()
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
                if (showEmojiPanel) {
                    isVoiceMode = false
                    keyboardController?.hide()
                }
            },
            onRequestPermission = {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onStartRecord = {
                if (audioPermissionGranted) {
                    isRecording = true
                    recordCanceled = false
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onCancelChange = { recordCanceled = it },
            onFinishRecord = { canceled ->
                if (isRecording) {
                    isRecording = false
                    // 停止录音
                    try {
                        mediaRecorder?.stop()
                        mediaRecorder?.release()
                    } catch (_: Exception) { }
                    mediaRecorder = null
                    if (!canceled) {
                        val duration = recordSeconds.coerceAtLeast(1)
                        val voiceUri = recordFile?.let { Uri.fromFile(it) }
                        onSendVoiceState.value.invoke(duration, voiceUri)
                        forceScrollToBottom = true
                    } else {
                        // 取消录音，删除文件
                        recordFile?.delete()
                        recordFile = null
                    }
                }
            },
            inputHeight = inputHeight,
            audioPermissionGranted = audioPermissionGranted,
            inputTextNotBlank = inputText.isNotBlank()
        )

        AnimatedVisibility(visible = showEmojiPanel) {
            EmojiPanel(onSelect = { emoji ->
                inputText += emoji
            })
        }

        AnimatedVisibility(visible = showAttachPanel) {
            AttachmentPanel(
                onVoice = {
                    showAttachPanel = false
                    showEmojiPanel = false
                    isVoiceMode = true
                    keyboardController?.hide()
                },
                onImage = {
                    showAttachPanel = false
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                onVideo = {
                    showAttachPanel = false
                    videoPickerLauncher.launch("video/*")
                },
                onCamera = {
                    showAttachPanel = false
                    if (cameraPermissionGranted) {
                        cameraLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onDismiss = { showAttachPanel = false }
            )
        }
    }
}

// ==================== 顶部栏 ====================
@Composable
fun ChatTopBar(chat: Chat?, onBackClick: () -> Unit) {
    val colors = LocalMuMuColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.4f else 0.1f))
            .background(
                if (colors.isDark) Brush.horizontalGradient(listOf(Color(0xFF1A3050), Color(0xFF1E3A55)))
                else Brush.horizontalGradient(listOf(Color(0xFFF2F6FA), Color(0xFFE8EFF6)))
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
                chat?.name ?: "聊天", color = colors.textPrimary,
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold
            )
            if (chat?.isGroup == true) {
                Text("群组·点击查看", color = colors.textSecondary, fontSize = 12.sp)
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

// ==================== 消息项组件 ====================
@Composable
private fun MessageItem(
    message: Message,
    isGroup: Boolean = false,
    isPlaying: Boolean = false,
    status: MessageSendStatus? = null,
    uploadProgress: Float? = null,
    uploadStatus: UploadStatus? = null,
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!message.isSentByMe) {
            AvatarCircle(
                SkyBlueLight.copy(alpha = 0.3f),
                SkyBlue,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.width(4.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 260.dp),
            horizontalAlignment = if (message.isSentByMe) Alignment.End else Alignment.Start
        ) {
            Text(
                message.senderName,
                color = if (message.isSentByMe) Color.LightGray.copy(alpha = 0.85f) else SkyBlue.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            )

            Box {
                when (message.messageType) {
                    MessageType.VOICE -> VoiceBubble(
                        message = message,
                        isPlaying = isPlaying,
                        onClick = onVoiceClick,
                        onLongPress = { menuExpanded = true }
                    )
                    MessageType.IMAGE -> ImageBubble(
                        message = message,
                        uploadProgress = uploadProgress,
                        uploadStatus = uploadStatus,
                        onClick = onImageClick,
                        onLongPress = { menuExpanded = true }
                    )
                    MessageType.VIDEO -> VideoBubble(
                        message = message,
                        uploadProgress = uploadProgress,
                        uploadStatus = uploadStatus,
                        onClick = onVideoClick,
                        onLongPress = { menuExpanded = true }
                    )
                    else -> TextBubble(
                        message = message,
                        onClick = {},
                        onLongPress = { menuExpanded = true }
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("复制") },
                        onClick = { menuExpanded = false; onCopy() }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                    if (canRecall) {
                        DropdownMenuItem(
                            text = { Text("撤回") },
                            onClick = { menuExpanded = false; onRecall() }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(message.timestamp, color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
                if (showStatus && statusText.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Text(statusText, color = statusColor, fontSize = 10.sp)
                }
            }
        }

        if (message.isSentByMe) {
            Spacer(Modifier.width(4.dp))
            AvatarCircle(
                Color.LightGray.copy(alpha = 0.12f),
                Color.LightGray.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ==================== 头像 ====================
@Composable
fun AvatarCircle(bgColor: Color, iconColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, "头像", tint = iconColor, modifier = Modifier.size(22.dp))
    }
}

// ==================== 文本气泡 ====================
@Composable
private fun TextBubble(message: Message, onClick: () -> Unit, onLongPress: () -> Unit) {
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
    val textColor = if (message.isSentByMe) Color.White else Color(0xFF1A1A1A)

    Box(
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
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(message.content, color = textColor, fontSize = 15.sp, lineHeight = 22.sp)
    }
}

// ==================== 图片气泡 ====================
@Composable
private fun ImageBubble(
    message: Message,
    uploadProgress: Float? = null,
    uploadStatus: UploadStatus? = null,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val shape = RoundedCornerShape(
        topStart = if (message.isSentByMe) 12.dp else 4.dp,
        topEnd = if (message.isSentByMe) 4.dp else 12.dp,
        bottomStart = 12.dp,
        bottomEnd = 12.dp
    )

    Column(
        modifier = Modifier
            .shadow(4.dp, shape, spotColor = Color.Black.copy(alpha = 0.2f))
            .clip(shape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                if (message.isSentByMe) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f),
                shape
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
    ) {
        Box {
            if (message.mediaUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(message.mediaUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "图片",
                    modifier = Modifier
                        .size(width = 200.dp, height = 140.dp)
                        .clip(shape),
                    contentScale = ContentScale.Crop
                )
            } else {
                val gradientColors = if (message.isSentByMe) {
                    listOf(Color(0xFF29B6F6), Color(0xFF4FC3F7), Color(0xFF81D4FA))
                } else {
                    listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFAED581))
                }
                Box(
                    modifier = Modifier
                        .size(width = 200.dp, height = 140.dp)
                        .clip(shape)
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, "图片", tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(44.dp))
                }
            }

            if (uploadProgress != null && uploadStatus != null && uploadStatus != UploadStatus.Completed) {
                UploadProgressOverlay(progress = uploadProgress, status = uploadStatus, modifier = Modifier.size(width = 200.dp, height = 140.dp))
            }
        }

        if (message.mediaDescription.isNotEmpty()) {
            Text(
                message.mediaDescription,
                color = if (message.isSentByMe) Color.White.copy(alpha = 0.8f) else Color(0xFF424242),
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

// ==================== 视频气泡 ====================
@Composable
private fun VideoBubble(
    message: Message,
    uploadProgress: Float? = null,
    uploadStatus: UploadStatus? = null,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val shape = RoundedCornerShape(
        topStart = if (message.isSentByMe) 12.dp else 4.dp,
        topEnd = if (message.isSentByMe) 4.dp else 12.dp,
        bottomStart = 12.dp,
        bottomEnd = 12.dp
    )

    Column(
        modifier = Modifier
            .shadow(4.dp, shape, spotColor = Color.Black.copy(alpha = 0.2f))
            .clip(shape)
            .border(
                1.dp,
                if (message.isSentByMe) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.12f),
                shape
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
    ) {
        Box(
            modifier = Modifier
                .size(width = 210.dp, height = 150.dp)
                .clip(shape)
        ) {
            if (message.mediaUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(message.mediaUri)
                        .videoFrameMillis(0)
                        .crossfade(true)
                        .build(),
                    contentDescription = "视频",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB))))
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, "播放视频", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val min = message.duration / 60
                val sec = message.duration % 60
                Text(
                    "${min}:${sec.toString().padStart(2, '0')}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (uploadProgress != null && uploadStatus != null && uploadStatus != UploadStatus.Completed) {
                UploadProgressOverlay(progress = uploadProgress, status = uploadStatus, modifier = Modifier.size(width = 210.dp, height = 150.dp))
            }
        }
    }
}

private fun simulateMediaUpload(
    messageId: String,
    uploadStateMap: MutableMap<String, Pair<Float, UploadStatus>>,
    pendingMessages: MutableList<Message>,
    coroutineScope: CoroutineScope,
    sizeMb: Int,
    onComplete: () -> Unit
) {
    uploadStateMap[messageId] = Pair(0f, UploadStatus.Compressing)
    coroutineScope.launch {
        val compressSteps = 4
        for (step in 1..compressSteps) {
            delay(160)
            uploadStateMap[messageId] = Pair((step / compressSteps.toFloat()) * 0.3f, UploadStatus.Compressing)
        }
        uploadStateMap[messageId] = Pair(0.3f, UploadStatus.Uploading)
        val uploadSteps = 7
        for (step in 1..uploadSteps) {
            delay(220)
            uploadStateMap[messageId] = Pair(0.3f + (step / uploadSteps.toFloat()) * 0.7f, UploadStatus.Uploading)
        }
        val shouldFail = sizeMb > 80 && Random.nextFloat() < 0.25f
        if (shouldFail) {
            uploadStateMap[messageId] = Pair(0f, UploadStatus.Failed)
        } else {
            uploadStateMap.remove(messageId)
            pendingMessages.removeAll { it.id == messageId }
            onComplete()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "聊天详情")
@Composable
fun ChatDetailScreenPreview() {
    val mockMessages = listOf(
        Message("m1", "c1", "u1", "清大", "今天同步一下进度", "上午9:17"),
        Message("m2", "c1", "me", "devin", "收到，我看看", "上午9:22", isSentByMe = true),
        Message("m3", "c1", "u1", "清大", "", "上午9:25", messageType = MessageType.IMAGE, mediaDescription = "设计稿.png"),
        Message("m4", "c1", "me", "devin", "", "上午9:28", isSentByMe = true, messageType = MessageType.VOICE, duration = 12),
        Message("m5", "c1", "u1", "清大", "", "上午9:30", messageType = MessageType.VIDEO, duration = 45, mediaDescription = "演示.mp4"),
        Message("m6", "c1", "me", "devin", "效果不错!", "上午9:32", isSentByMe = true)
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
