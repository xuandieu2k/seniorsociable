package vn.xdeuhug.seniorsociable.model.entity.modelEvent

import vn.xdeuhug.seniorsociable.constants.EventConstant
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 12 / 2023
 */
class Event {
    var id = ""
    var idUserCreate = ""
    var timeCreate = Date()
    var timeStart = Date()
    var timeEnd = Date()
    var membersJoin = ArrayList<MemberJoinEvent>()
    var poster = "" // HÌnh nền cho sự kiện
    var content = ""
    var status = EventConstant.NORMAL
    var address = Address()
    var topic = TopicEvent()
    var posts = ArrayList<Post>()

    constructor()
    constructor(
        id: String,
        idUserCreate: String,
        timeCreate: Date,
        timeStart: Date,
        timeEnd: Date,
        membersJoin: ArrayList<MemberJoinEvent>,
        poster: String,
        content: String,
        status: Int,
        address: Address,
        topic: TopicEvent
    ) {
        this.id = id
        this.idUserCreate = idUserCreate
        this.timeCreate = timeCreate
        this.timeStart = timeStart
        this.timeEnd = timeEnd
        this.membersJoin = membersJoin
        this.poster = poster
        this.content = content
        this.status = status
        this.address = address
        this.topic = topic
    }

}