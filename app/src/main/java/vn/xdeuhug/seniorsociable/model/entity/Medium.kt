package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

class Medium {
    @SerializedName("url")
    var url: String = ""

    @SerializedName("name")
    var name: String = ""

    @SerializedName("size")
    var size: String = ""

    @SerializedName("height")
    var height: Int = 0

    @SerializedName("width")
    var width: Int = 0
}