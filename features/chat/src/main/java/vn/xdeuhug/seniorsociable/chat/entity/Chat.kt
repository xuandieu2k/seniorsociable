package vn.xdeuhug.seniorsociable.chat.entity

import vn.xdeuhug.seniorsociable.chat.constants.ChatConstants
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 13 / 11 / 2023
 */
class Chat {
    var id = ""
    var timeCreated = Date()
    var nickNameFirstMember = "" // Là nickName người tạo cuộc trò chuyện
    var nickNameSecondMember = ""
    var backgroundTopic = ChatConstants.BG_DEFAULT // nền chủ đề đoạn chat
    var avatarGroup = ""
    var isNotification = true // Kiểm tra nhận thông báo
    var members = ArrayList<String>()
    var nameChat = ""
    var idUserBlock = ArrayList<String>() // Một trong hai người block



    constructor(
        id: String,
        timeCreated: Date,
        nickNameFirstMember: String,
        nickNameSecondMember: String,
        backgroundTopic: Int,
        isNotification: Boolean,
        members: ArrayList<String>,
        nameChat: String,
        idUserBlock: ArrayList<String>
    ) {
        this.id = id
        this.timeCreated = timeCreated
        this.nickNameFirstMember = nickNameFirstMember
        this.nickNameSecondMember = nickNameSecondMember
        this.backgroundTopic = backgroundTopic
        this.isNotification = isNotification
        this.members = members
        this.nameChat = nameChat
        this.idUserBlock = idUserBlock
    }

    constructor()
}