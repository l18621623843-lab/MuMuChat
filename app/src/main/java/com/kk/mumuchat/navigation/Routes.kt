package com.kk.mumuchat.navigation

/**
 * 应用导航路由定义
 * 集中管理所有页面的路由路径，避免硬编码字符串
 */
object Routes {
    /** 聊天列表页（主页） */
    const val CHAT_LIST = "chat_list"

    /** 通讯录/联系人页 */
    const val CONTACTS = "contacts"

    /** 发现/设置页 */
    const val DISCOVER = "discover"

    /** 个人资料页 */
    const val PROFILE = "profile"

    /** 聊天详情页，需要传入 chatId 参数 */
    const val CHAT_DETAIL = "chat_detail/{chatId}"

    /** 生成聊天详情页的完整路由 */
    fun chatDetail(chatId: String) = "chat_detail/$chatId"

    /** Tab页顺序索引，用于判断滑动方向 */
    val tabOrder = listOf(CHAT_LIST, CONTACTS, DISCOVER, PROFILE)

    fun tabIndex(route: String): Int = tabOrder.indexOf(route)
}
