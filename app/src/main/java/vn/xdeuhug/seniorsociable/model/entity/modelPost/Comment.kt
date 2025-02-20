package vn.xdeuhug.seniorsociable.model.entity.modelPost

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User.Companion.toMap
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 10 / 2023
 */
class Comment {
    @PropertyName("id")
    var id = ""

    var idParent = "" // Nếu khác rỗng thì đây là comment reply

    var idUserComment = "" // User post story

    var interacts = ArrayList<Interact>()

    @get:Exclude
    var comments = ArrayList<Comment>()

    var timeCreated = Date()

    var timeUpdate = Date()

    var content = ""

    var multiMedia = ArrayList<MultiMedia>()

    @get:Exclude
    var isReloadDataNotPic = false

    constructor(
        id: String,
        idUserComment: String,
        timeCreated: Date,
        timeUpdate: Date,
        content: String,
        multiMedia: ArrayList<MultiMedia>
    ) {
        this.id = id
        this.idUserComment = idUserComment
        this.timeCreated = timeCreated
        this.timeUpdate = timeUpdate
        this.content = content
        this.multiMedia = multiMedia
    }

    constructor()

    companion object {
        fun Comment.toMap(): Map<String, Any> {
            val map = HashMap<String, Any>()
            map["id"] = id
            map["idUserComment"] = idUserComment
            map["interacts"] =
                interacts.map { it.toMap() } // Chuyển mỗi Interact trong ArrayList thành Map
            map["timeCreated"] = timeCreated
            map["timeUpdate"] = timeUpdate
            map["content"] = content
            map["multiMedia"] =
                multiMedia.map { it.toMap() } // Chuyển mỗi MultiMedia trong ArrayList thành Map

            return map
        }
    }

}