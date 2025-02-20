package vn.xdeuhug.seniorsociable.model.entity.modelUser

import com.google.firebase.firestore.Exclude
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.Album
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post.Companion.toMap


/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 31/08/2023
 */
class Address {
    var address = ""
    var addressNormalize = ""
    var fullAddress = ""
    var fullAddressNormalize = ""
    var longitude = 0.00
    var latitude = 0.00

    @get:Exclude
    var isSelected = false
    constructor()

    constructor(
        address: String,
        addressNormalize: String,
        fullAddress: String,
        fullAddressNormalize: String,
        longitude: Double,
        latitude: Double
    ) {
        this.address = address
        this.addressNormalize = addressNormalize
        this.fullAddress = fullAddress
        this.fullAddressNormalize = fullAddressNormalize
        this.longitude = longitude
        this.latitude = latitude
    }

    companion object{
        fun Address.toMap(): Map<String, Any> {
            val map = HashMap<String, Any>()

            map["address"] = address
            map["addressNormalize"] = addressNormalize
            map["fullAddress"] = fullAddress
            map["fullAddressNormalize"] = fullAddressNormalize
            map["longitude"] = longitude
            map["latitude"] = latitude
            return map
        }
    }
}