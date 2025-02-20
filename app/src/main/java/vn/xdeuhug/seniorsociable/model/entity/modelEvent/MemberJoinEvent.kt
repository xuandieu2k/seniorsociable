package vn.xdeuhug.seniorsociable.model.entity.modelEvent

import vn.xdeuhug.seniorsociable.constants.EventConstant
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 12 / 2023
 */
class MemberJoinEvent {
    var id = ""
    var timeJoin = Date()
    var timeUpdate = Date()
    var reason = "" // Lý do hủy tham gia
    var status = EventConstant.STATUS_DEFAULT

    constructor()
    constructor(id: String, timeJoin: Date, timeUpdate: Date, reason: String, status: Int) {
        this.id = id
        this.timeJoin = timeJoin
        this.timeUpdate = timeUpdate
        this.reason = reason
        this.status = status
    }
}