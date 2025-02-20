package vn.xdeuhug.seniorsociable.chat.entity

import vn.xdeuhug.seniorsociable.chat.constants.ChatConstants
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 16 / 11 / 2023
 */
class Message {
    var id = ""
    var idReply = ""
    var idUserSend = ""
    var timeSend = Date()
    var status = ChatConstants.STATUS_ON
    var isUsersRead = ArrayList<String>()
    var message = ""
    var address = Address()
    var multiMedia = ArrayList<MultiMedia>()
    var interacts = ArrayList<Interact>()
    var messageType = ChatConstants.TYPE_MESSAGE // Loại tin nhắn văn bản hình ảnh video âm thanh,...
    var urlMessageParent = ""  // Cấu trúc gồm có ${getRootURLChatById(idChat)}${UploadFireStorageConstants.BODY_UPLOAD_COMMENT}/$idComment/

    constructor(
        id: String,
        idReply: String,
        idUserSend: String,
        timeSend: Date,
        status: Int,
        message: String,
        multiMedia: ArrayList<MultiMedia>,
        interacts: ArrayList<Interact>
    ) {
        this.id = id
        this.idReply = idReply
        this.idUserSend = idUserSend
        this.timeSend = timeSend
        this.status = status
        this.message = message
        this.multiMedia = multiMedia
        this.interacts = interacts
    }





    constructor()

    constructor(
        id: String,
        idReply: String,
        idUserSend: String,
        timeSend: Date,
        status: Int,
        isUsersRead: ArrayList<String>,
        message: String,
        multiMedia: ArrayList<MultiMedia>,
        interacts: ArrayList<Interact>
    ) {
        this.id = id
        this.idReply = idReply
        this.idUserSend = idUserSend
        this.timeSend = timeSend
        this.status = status
        this.isUsersRead = isUsersRead
        this.message = message
        this.multiMedia = multiMedia
        this.interacts = interacts
    }



    constructor(
        id: String,
        idReply: String,
        idUserSend: String,
        timeSend: Date,
        status: Int,
        isUsersRead: ArrayList<String>,
        message: String,
        address: Address,
        multiMedia: ArrayList<MultiMedia>,
        interacts: ArrayList<Interact>,
        messageType: Int,
        urlMessageParent: String
    ) {
        this.id = id
        this.idReply = idReply
        this.idUserSend = idUserSend
        this.timeSend = timeSend
        this.status = status
        this.isUsersRead = isUsersRead
        this.message = message
        this.address = address
        this.multiMedia = multiMedia
        this.interacts = interacts
        this.messageType = messageType
        this.urlMessageParent = urlMessageParent
    }

    constructor(
        id: String,
        idReply: String,
        idUserSend: String,
        timeSend: Date,
        status: Int,
        isUsersRead: ArrayList<String>,
        message: String,
        multiMedia: ArrayList<MultiMedia>,
        interacts: ArrayList<Interact>,
        messageType: Int,
        urlMessageParent: String
    ) {
        this.id = id
        this.idReply = idReply
        this.idUserSend = idUserSend
        this.timeSend = timeSend
        this.status = status
        this.isUsersRead = isUsersRead
        this.message = message
        this.multiMedia = multiMedia
        this.interacts = interacts
        this.messageType = messageType
        this.urlMessageParent = urlMessageParent
    }


}