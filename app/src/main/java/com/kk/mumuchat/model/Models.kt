package com.kk.mumuchat.model

/**
 * 用户数据模型
 * @param id 用户唯一标识
 * @param name 用户昵称
 * @param avatar 头像 URL（null 则显示默认头像）
 * @param phone 手机号
 * @param bio 个人简介
 * @param isOnline 是否在线
 * @param lastSeen 最后在线时间描述（如 "近期曾上线"）
 */
data class User(
    val id: String,
    val name: String,
    val avatar: String? = null,
    val phone: String = "",
    val bio: String = "",
    val isOnline: Boolean = false,
    val lastSeen: String = ""
)

/**
 * 聊天会话数据模型（出现在聊天列表中）
 * @param id 会话唯一标识
 * @param name 会话名称（联系人名或群名）
 * @param avatar 头像 URL
 * @param lastMessage 最后一条消息内容
 * @param lastMessageTime 最后消息时间（如 "09:35"）
 * @param unreadCount 未读消息数量
 * @param isPinned 是否置顶
 * @param isMuted 是否静音
 * @param isGroup 是否为群聊
 * @param lastMessageSender 最后消息发送者名称（群聊时显示）
 */
data class Chat(
    val id: String,
    val name: String,
    val avatar: String? = null,
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isGroup: Boolean = false,
    val lastMessageSender: String = ""
)
