package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

class Thumb {
    @SerializedName("url")
    var url  = ""

    @SerializedName("name")
    var name  = ""

    @SerializedName("size")
    var size  = ""

    @SerializedName("height")
    var height  = 0

    @SerializedName("width")
    var width  = 0
}