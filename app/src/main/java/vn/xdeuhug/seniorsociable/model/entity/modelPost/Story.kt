package vn.xdeuhug.seniorsociable.model.entity.modelPost

import com.google.firebase.firestore.PropertyName
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 10 / 2023
 */
class Story {
    @PropertyName("id")
    var id = ""

    var idUserCreate = "" // User post story

    @PropertyName("time_created")
    var timeCreated = Date()

    var multiMedia = ArrayList<MultiMedia>()

    constructor(
        id: String,
        idUserCreate: String,
        timeCreated: Date,
        multiMedia: ArrayList<MultiMedia>
    ) {
        this.id = id
        this.idUserCreate = idUserCreate
        this.timeCreated = timeCreated
        this.multiMedia = multiMedia
    }
    constructor()
}