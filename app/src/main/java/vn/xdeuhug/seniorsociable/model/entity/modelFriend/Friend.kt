package vn.xdeuhug.seniorsociable.model.entity.modelFriend

import java.io.Serializable
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 28 / 11 / 2023
 */
class Friend: Serializable {
    var id = "" // idUser
    var timeCreated = Date()


    constructor(id: String, timeCreated: Date) {
        this.id = id
        this.timeCreated = timeCreated
    }

    constructor()
}