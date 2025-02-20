package vn.xdeuhug.seniorsociable.model.entity.modelPost

import com.google.firebase.firestore.PropertyName
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 10 / 2023
 */
class Interact {
    @PropertyName("id")
    var id = "" // id của User

    var type = PostConstants.INTERACT_LIKE

    @PropertyName("time_created")
    var timeCreated = Date()

    constructor(id: String, userInteract: User, type: Int, timeCreated: Date) {
        this.id = id
//        this.userInteract = userInteract
        this.type = type
        this.timeCreated = timeCreated
    }

    constructor(id: String, type: Int, timeCreated: Date) {
        this.id = id
        this.type = type
        this.timeCreated = timeCreated
    }

    constructor()

    companion object{
        fun Interact.toMap(): Map<String, Any> {
            val map = HashMap<String, Any>()

            map["id"] = id
            map["type"] = type
            map["timeCreated"] = timeCreated

            return map
        }
    }
}