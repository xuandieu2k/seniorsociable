package vn.xdeuhug.seniorsociable.model.entity.modelCall

import vn.xdeuhug.seniorsociable.constants.CallConstants
import vn.xdeuhug.seniorsociable.model.entity.modelCall.UserCall.Companion.toMap

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 13 / 12 / 2023
 */
class CallRoom {
    companion object {
        fun CallRoom.toMap(): Map<String, Any?> {
            val map = HashMap<String, Any?>()

            map["id"] = id
            map["busy"] = busy
            map["typeCall"] = typeCall
            map["statusRoom"] = statusRoom
            map["userCall"] = userCall

            return map
        }
    }

    var id = ""
    var busy = CallConstants.IS_NOT_BUSY
    var typeCall = CallConstants.TYPE_VOICE_CALL
    var statusRoom = CallConstants.IS_DEFAULT
    var userCall: UserCall? = null


    constructor(
        id: String,
        busy: Boolean,
        typeCall: Int,
        statusRoom: Int,
        userCall: UserCall?
    ) {
        this.id = id
        this.busy = busy
        this.typeCall = typeCall
        this.statusRoom = statusRoom
        this.userCall = userCall
    }

    constructor()
}