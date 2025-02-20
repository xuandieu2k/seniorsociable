package vn.xdeuhug.seniorsociable.model.entity.modelPexels


import com.google.gson.annotations.SerializedName

class VideoFile {
    @SerializedName("file_type")
    var fileType: String = ""
    var fps: Double = 0.0
    var height: Int = 0
    var id: Int = 0
    var link: String = ""
    var quality: String = ""
    var width: Int = 0
}