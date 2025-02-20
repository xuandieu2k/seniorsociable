package vn.xdeuhug.seniorsociable.model.entity.modelMedia

import com.google.firebase.firestore.Exclude
import com.google.gson.annotations.SerializedName
import com.hjq.http.annotation.HttpRename
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 16 / 10 / 2023
 */
class MultiMedia {
    var id = ""

    var timeCreated = Date().time

    var url = ""

    var name = ""

    var type = 0

    var size = 0L

    var width = 0

    var height = 0

    var isKeep = 0

    var realPath = ""

    @get:Exclude
    var isUpdated = false // Kiểm tra đã được cập nhật hay chưa

    @get:Exclude
    var isDeleted = false // Kiểm tra đã được cập nhật hay chưa

    constructor()

    constructor(
        id: String,
        timeCreated: Long,
        url: String,
        name: String,
        type: Int,
        size: Long,
        width: Int,
        height: Int,
        isKeep: Int,
        realPath: String
    ) {
        this.id = id
        this.timeCreated = timeCreated
        this.url = url
        this.name = name
        this.type = type
        this.size = size
        this.width = width
        this.height = height
        this.isKeep = isKeep
        this.realPath = realPath
    }


    companion object {
        fun MultiMedia.toMap(): Map<String, Any> {
            val map = HashMap<String, Any>()

            map["id"] = id
            map["timeCreated"] = timeCreated
            map["url"] = url
            map["name"] = name
            map["type"] = type
            map["size"] = size
            map["width"] = width
            map["height"] = height
            map["isKeep"] = isKeep
            map["realPath"] = realPath

            return map
        }
    }



}