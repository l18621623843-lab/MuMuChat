package com.kk.mumuchat.model

/**
 * 聊天消息数据模型
 * @param id 消息唯一标识
 * @param chatId 所属会话 ID
 * @param senderId 发送者 ID
 * @param senderName 发送者昵称（群聊中显示）
 * @param content 消息文本内容（文本消息为文字，图片/视频为描述）
 * @param timestamp 消息时间（如 "上午9:17"）
 * @param isSentByMe 是否是自己发送的消息
 * @param messageType 消息类型（文本、图片、语音、视频等）
 * @param isRead 是否已读
 * @param duration 语音/视频时长（秒），仅语音和视频消息使用
 * @param mediaDescription 媒体描述（图片/视频的简短说明）
 */
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String = "",
    val content: String,
    val timestamp: String,
    val isSentByMe: Boolean = false,
    val messageType: MessageType = MessageType.TEXT,
    val isRead: Boolean = false,
    val duration: Int = 0,
    val mediaDescription: String = ""
)

/**
 * 消息类型枚举
 */
enum class MessageType {
    TEXT,       // 文本消息
    IMAGE,      // 图片消息
    VOICE,      // 语音消息
    VIDEO,      // 视频消息
    EMOJI,      // 表情包消息
    FILE        // 文件消息
}

/**
 * 联系人数据模型（通讯录中使用）
 * @param user 用户信息
 * @param initialLetter 姓名首字母（用于分组索引）
 */
data class Contact(
    val user: User,
    val initialLetter: String = "#"
)
