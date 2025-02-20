package vn.xdeuhug.seniorsociable.model.entity.modelFriend

import java.io.Serializable
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 10 / 11 / 2023
 */
class RequestAddFriend : Serializable {
    var id = "" // idUser send
    var timeCreated = Date()
    var status = 0

    constructor(id: String, timeCreated: Date, status: Int) {
        this.id = id
        this.timeCreated = timeCreated
        this.status = status
    }

    constructor()
}