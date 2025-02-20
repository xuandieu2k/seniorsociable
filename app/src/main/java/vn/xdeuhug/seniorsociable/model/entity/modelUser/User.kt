package vn.xdeuhug.seniorsociable.model.entity.modelUser


import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.Album.Companion.toMap
import java.io.Serializable
import java.util.Date

class User : Serializable {
    @PropertyName("address")
    var address = Address()

    @PropertyName("background")
    var background = ""

    @PropertyName("avatar")
    var avatar = ""

    @PropertyName("birthday")
    var birthday = ""

    @PropertyName("name")
    var name = ""

    var nameNormalize = ""

    @PropertyName("gender")
    var gender = AppConstants.NOT_UPDATE

    @PropertyName("id")
    var id = ""

    @PropertyName("password")
    var password = ""

    @PropertyName("phone")
    var phone = ""

    @PropertyName("description")
    var description = ""

    @PropertyName("time_created")
    var timeCreated = Date()

    var timeLastUpdate = Date() // Cập nhật thông tin lần cuối

    @PropertyName("email")
    var email = ""

    @get:Exclude
    var isUsedBiometric = false

    @PropertyName("is_updated_info")
    var isUpdatedInfo = false

    @PropertyName("type_account")
    var typeAccount = 0

    @get:Exclude
    var isChecked = false

    var roleAccount = AppConstants.ROLE_NORMAL
    // Block
    var typeActive = AppConstants.ACTIVATING
    var timeEndBlock: Date? = null
    var timeStartBlock: Date? = null
    var reasonBlock = ""

    // More
    var workAt = Address()
    var maritalStatus = AppConstants.MARITAL_STATUS_NOT_UPDATE
    var hobbies = ArrayList<Int>() // Lưu id của sở thích

    //
    var online = false
    var lastTimeOnline = Date()

    constructor()
    constructor(
        avatar: String, name: String, id: String, phone: String, email: String, typeAccount: Int
    ) {
        this.avatar = avatar
        this.name = name
        this.id = id
        this.phone = phone
        this.email = email
        this.typeAccount = typeAccount
    }

    constructor(
        avatar: String,
        birthday: String,
        name: String,
        gender: Int,
        id: String,
        phone: String,
        timeCreated: Date,
        email: String,
        isUpdatedInfo: Boolean,
        typeAccount: Int
    ) {
        this.avatar = avatar
        this.birthday = birthday
        this.name = name
        this.gender = gender
        this.id = id
        this.phone = phone
        this.timeCreated = timeCreated
        this.email = email
        this.isUpdatedInfo = isUpdatedInfo
        this.typeAccount = typeAccount
    }

    constructor(
        avatar: String,
        name: String,
        id: String,
        phone: String,
        timeCreated: Date,
        email: String,
        isUpdatedInfo: Boolean,
        typeAccount: Int
    ) {
        this.avatar = avatar
        this.name = name
        this.id = id
        this.phone = phone
        this.timeCreated = timeCreated
        this.email = email
        this.isUpdatedInfo = isUpdatedInfo
        this.typeAccount = typeAccount
    }

    constructor(
        avatar: String,
        birthday: String,
        name: String,
        gender: Int,
        id: String,
        password: String,
        phone: String,
        timeCreated: Date,
        email: String,
        isUpdatedInfo: Boolean,
        typeAccount: Int
    ) {
        this.avatar = avatar
        this.birthday = birthday
        this.name = name
        this.gender = gender
        this.id = id
        this.password = password
        this.phone = phone
        this.timeCreated = timeCreated
        this.email = email
        this.isUpdatedInfo = isUpdatedInfo
        this.typeAccount = typeAccount
    }

    companion object {
        fun User.toMap(): Map<String, Any> {
            val map = HashMap<String, Any>()
            map["address"] = address
            map["background"] = background
            map["avatar"] = avatar
            map["birthday"] = birthday
            map["name"] = name
            map["nameNormalize"] = nameNormalize
            map["gender"] = gender
            map["id"] = id
            map["password"] = password
            map["phone"] = phone
            map["timeCreated"] = timeCreated
            map["timeLastUpdate"] = timeLastUpdate
            map["email"] = email
            map["isUpdatedInfo"] = isUpdatedInfo
            map["typeAccount"] = typeAccount
            map["description"] = description
            if(timeEndBlock != null)
            {
                map["timeEndBlock"] = timeEndBlock!!
            }
            if(timeStartBlock != null)
            {
                map["timeStartBlock"] = timeStartBlock!!
            }
            map["typeActive"] = typeActive
            map["reasonBlock"] = reasonBlock
            map["roleAccount"] = roleAccount
            map["workAt"] = workAt
            map["maritalStatus"] = maritalStatus
            map["hobbies"] = hobbies
            map["online"] = online
            map["lastTimeOnline"] = lastTimeOnline
            return map
        }
    }


}