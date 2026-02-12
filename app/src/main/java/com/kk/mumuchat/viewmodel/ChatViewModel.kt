package com.kk.mumuchat.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.kk.mumuchat.model.Chat
import com.kk.mumuchat.model.Contact
import com.kk.mumuchat.model.Message
import com.kk.mumuchat.model.MessageType
import com.kk.mumuchat.model.User

/**
 * 聊天主 ViewModel
 * 管理聊天列表、联系人、消息等所有数据
 * 目前使用模拟数据，后续可替换为真实 API
 */
class ChatViewModel : ViewModel() {

    // ==================== 主题模式 ====================
    val isDarkMode = mutableStateOf(false)

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    // ==================== 当前登录用户 ====================
    val currentUser = mutableStateOf(
        User(
            id = "me",
            name = "devin",
            phone = "+86 190****2755",
            bio = "",
            isOnline = true
        )
    )

    // ==================== 聊天列表数据 ====================
    val chatList = mutableStateListOf(
        Chat(
            id = "chat1",
            name = "绫骨开发进度群",
            lastMessage = "群资料 [图片]",
            lastMessageTime = "09:35",
            isPinned = true,
            isGroup = true,
            lastMessageSender = "清大"
        ),
        Chat(
            id = "chat2",
            name = "龙",
            lastMessage = "好的",
            lastMessageTime = "08:17",
            unreadCount = 1
        ),
        Chat(
            id = "chat3",
            name = "交易临时小分队",
            lastMessage = "自产化测试人群：好",
            lastMessageTime = "07:47",
            isMuted = true,
            isGroup = true,
            lastMessageSender = "清大"
        )
    )

    // ==================== 联系人列表数据 ====================
    val contacts = mutableStateListOf(
        Contact(
            user = User(
                id = "u1",
                name = "龙",
                isOnline = false,
                lastSeen = "近期曾上线"
            ),
            initialLetter = "#"
        ),
        Contact(
            user = User(
                id = "u2",
                name = "190 7542 2755",
                phone = "190 7542 2755",
                isOnline = false,
                lastSeen = "很久前上线"
            ),
            initialLetter = "#"
        ),
        Contact(
            user = User(
                id = "u3",
                name = "蒋龙 应",
                isOnline = false,
                lastSeen = "很久前上线"
            ),
            initialLetter = "#"
        )
    )

    // ==================== 聊天消息数据 ====================
    /** 存储每个会话的消息列表，key 为 chatId */
    val messagesMap = mutableMapOf(
        "chat1" to mutableStateListOf(
            Message(
                id = "m1", chatId = "chat1", senderId = "u1",
                senderName = "清大",
                content = "今天同步一下进度",
                timestamp = "上午9:17", isSentByMe = false
            ),
            Message(
                id = "m2", chatId = "chat1", senderId = "me",
                senderName = "devin",
                content = "收到，我看看",
                timestamp = "上午9:22", isSentByMe = true
            ),
            Message(
                id = "m3", chatId = "chat1", senderId = "u1",
                senderName = "清大",
                content = "这是最新的设计稿",
                timestamp = "上午9:25", isSentByMe = false,
                messageType = MessageType.IMAGE,
                mediaDescription = "UI设计稿_v3.png"
            ),
            Message(
                id = "m4", chatId = "chat1", senderId = "me",
                senderName = "devin",
                content = "",
                timestamp = "上午9:28", isSentByMe = true,
                messageType = MessageType.VOICE,
                duration = 12
            ),
            Message(
                id = "m5", chatId = "chat1", senderId = "u1",
                senderName = "清大",
                content = "录了一段演示视频给你看",
                timestamp = "上午9:30", isSentByMe = false,
                messageType = MessageType.VIDEO,
                duration = 45,
                mediaDescription = "功能演示.mp4"
            ),
            Message(
                id = "m6", chatId = "chat1", senderId = "me",
                senderName = "devin",
                content = "效果不错！我这边也截了个图",
                timestamp = "上午9:32", isSentByMe = true
            ),
            Message(
                id = "m7", chatId = "chat1", senderId = "me",
                senderName = "devin",
                content = "开发进度截图",
                timestamp = "上午9:33", isSentByMe = true,
                messageType = MessageType.IMAGE,
                mediaDescription = "进度截图.jpg"
            ),
            Message(
                id = "m8", chatId = "chat1", senderId = "u1",
                senderName = "清大",
                content = "群资料 [图片]",
                timestamp = "上午9:35", isSentByMe = false
            )
        ),
        "chat2" to mutableStateListOf(
            Message(
                id = "m10", chatId = "chat2", senderId = "u1",
                senderName = "龙",
                content = "你好，最近怎么样？",
                timestamp = "上午8:00", isSentByMe = false
            ),
            Message(
                id = "m11", chatId = "chat2", senderId = "me",
                senderName = "devin",
                content = "挺好的，在忙项目",
                timestamp = "上午8:10", isSentByMe = true
            ),
            Message(
                id = "m12", chatId = "chat2", senderId = "u1",
                senderName = "龙",
                content = "",
                timestamp = "上午8:12", isSentByMe = false,
                messageType = MessageType.VOICE,
                duration = 8
            ),
            Message(
                id = "m13", chatId = "chat2", senderId = "me",
                senderName = "devin",
                content = "看看这个",
                timestamp = "上午8:14", isSentByMe = true,
                messageType = MessageType.IMAGE,
                mediaDescription = "风景照.jpg"
            ),
            Message(
                id = "m14", chatId = "chat2", senderId = "u1",
                senderName = "龙",
                content = "好的",
                timestamp = "上午8:17", isSentByMe = false
            )
        )
    )

    /**
     * 发送一条新消息
     */
    fun sendMessage(chatId: String, content: String) {
        if (content.isBlank()) return
        addMessage(chatId, content, MessageType.TEXT)
        simulateReply(chatId)
    }

    /**
     * 发送语音消息
     * @param chatId 目标会话
     * @param duration 语音时长（秒）
     * @param uri 语音文件URI
     */
    fun sendVoiceMessage(chatId: String, duration: Int = (3..30).random(), uri: Uri? = null) {
        addMessage(chatId, "", MessageType.VOICE, duration = duration, mediaUri = uri)
        simulateReply(chatId)
    }

    /**
     * 发送图片消息
     * @param chatId 目标会话
     * @param uri 图片URI
     */
    fun sendImageMessage(chatId: String, uri: Uri) {
        addMessage(
            chatId,
            "图片",
            MessageType.IMAGE,
            mediaDescription = "IMG_${System.currentTimeMillis()}.jpg",
            mediaUri = uri  // 传递真实URI
        )
        simulateReply(chatId)
    }

    /**
     * 发送视频消息
     * @param chatId 目标会话
     * @param uri 视频URI
     * @param duration 视频时长
     */
    fun sendVideoMessage(chatId: String, uri: Uri, duration: Int) {
        addMessage(
            chatId,
            "视频",
            MessageType.VIDEO,
            duration = duration,
            mediaDescription = "VID_${System.currentTimeMillis()}.mp4",
            mediaUri = uri  // 传递真实URI
        )
        simulateReply(chatId)
    }

    /**
     * 通用消息添加方法
     */
    private fun addMessage(
        chatId: String,
        content: String,
        type: MessageType,
        duration: Int = 0,
        mediaDescription: String = "",
        mediaUri: Uri? = null  // 添加URI参数
    ) {
        val messages = messagesMap.getOrPut(chatId) { mutableStateListOf() }
        messages.add(
            Message(
                id = "m${System.currentTimeMillis()}",
                chatId = chatId,
                senderId = "me",
                senderName = currentUser.value.name,
                content = content,
                timestamp = getCurrentTime(),
                isSentByMe = true,
                messageType = type,
                isRead = true,
                duration = duration,
                mediaDescription = mediaDescription,
                mediaUri = mediaUri  // 保存URI
            )
        )
        // 更新聊天列表最后消息
        val preview = when (type) {
            MessageType.VOICE -> "[语音消息] ${duration}″"
            MessageType.IMAGE -> "[图片]"
            MessageType.VIDEO -> "[视频]"
            else -> content
        }
        val chatIndex = chatList.indexOfFirst { it.id == chatId }
        if (chatIndex >= 0) {
            chatList[chatIndex] = chatList[chatIndex].copy(
                lastMessage = preview,
                lastMessageTime = getCurrentTime(),
                lastMessageSender = currentUser.value.name
            )
        }
    }

    /**
     * 模拟对方自动回复
     */
    private fun simulateReply(chatId: String) {
        val replies = listOf(
            "收到！", "好的，我知道了", "没问题 👍",
            "稍等，我看看", "OK", "明白了",
            "这个方案不错", "我一会儿回复你"
        )
        val messages = messagesMap[chatId] ?: return
        val chat = chatList.find { it.id == chatId } ?: return
        val replyContent = replies.random()

        messages.add(
            Message(
                id = "m${System.currentTimeMillis() + 1}",
                chatId = chatId,
                senderId = "other",
                senderName = if (chat.isGroup) "清大" else chat.name,
                content = replyContent,
                timestamp = getCurrentTime(),
                isSentByMe = false
            )
        )
    }

    /**
     * 获取当前时间字符串
     */
    private fun getCurrentTime(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val period = if (hour < 12) "上午" else "下午"
        val displayHour = if (hour > 12) hour - 12 else hour
        return "$period$displayHour:${minute.toString().padStart(2, '0')}"
    }

    /**
     * 获取指定会话的消息列表
     */
    fun getMessages(chatId: String): List<Message> {
        return messagesMap.getOrPut(chatId) { mutableStateListOf() }
    }

    /**
     * 根据 chatId 获取会话信息
     */
    fun getChat(chatId: String): Chat? {
        return chatList.find { it.id == chatId }
    }
}