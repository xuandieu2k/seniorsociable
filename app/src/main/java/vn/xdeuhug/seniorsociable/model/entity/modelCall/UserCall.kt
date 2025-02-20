package vn.xdeuhug.seniorsociable.model.entity.modelCall

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 13 / 12 / 2023
 */
class UserCall {

    var id = ""
    var photo = ""
    var name = ""

    constructor(id: String, photo: String, name: String) {
        this.id = id
        this.photo = photo
        this.name = name
    }

    constructor()


    companion object {
        fun UserCall.toMap(): Map<String, Any> {
            val map = HashMap<String, Any>()

            map["id"] = id
            map["photo"] = photo
            map["name"] = name

            return map
        }
    }
}