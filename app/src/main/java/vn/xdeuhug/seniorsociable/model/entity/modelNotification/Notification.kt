package vn.xdeuhug.seniorsociable.model.entity.modelNotification

import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 29 / 11 / 2023
 */
class Notification {
    var id = ""
    var idUserSend = ""
    // ## Nếu kà comment bài viết thì idParentTypeOfNotification là id post idTypeOfNotification id comment
    var idParentTypeOfNotification = "" // id cha theo loại thông báo
    var idTypeOfNotification = "" // id theo loại thông báo
    var timeCreated = Date()
    var read = AppConstants.IS_UNREAD
    var type = AppConstants.NOTIFICATION_FROM_POST // Loại thông báo
    var typeInteract = PostConstants.INTERACT_LIKE
    var title = ""
    var content = ""
    var firstShow = false


    constructor(
        id: String,
        idUserSend: String,
        idTypeOfNotification: String,
        timeCreated: Date,
        read: Int,
        type: Int,
        title: String,
        content: String
    ) {
        this.id = id
        this.idUserSend = idUserSend
        this.idTypeOfNotification = idTypeOfNotification
        this.timeCreated = timeCreated
        this.read = read
        this.type = type
        this.title = title
        this.content = content
    }



    constructor()

    constructor(
        id: String,
        idUserSend: String,
        idParentTypeOfNotification: String,
        idTypeOfNotification: String,
        timeCreated: Date,
        read: Int,
        type: Int,
        typeInteract: Int,
        title: String,
        content: String,
        firstShow:Boolean
    ) {
        this.id = id
        this.idUserSend = idUserSend
        this.idParentTypeOfNotification = idParentTypeOfNotification
        this.idTypeOfNotification = idTypeOfNotification
        this.timeCreated = timeCreated
        this.read = read
        this.type = type
        this.typeInteract = typeInteract
        this.title = title
        this.content = content
        this.firstShow = firstShow
    }
}