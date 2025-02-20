package vn.xdeuhug.seniorsociable.model.entity.modelPexels


import com.google.gson.annotations.SerializedName

class Video {
    @SerializedName("avg_color")
    var avgColor: Any = Any()
    var duration: Int = 0

    @SerializedName("full_res")
    var fullRes: Any = Any()
    var height: Int = 0
    var id: Int = 0
    var image: String = ""
    var tags = ArrayList<Any>()
    var url: String = ""
    var user: User = User()

    @SerializedName("video_files")
    var videoFiles = ArrayList<VideoFile>()

    @SerializedName("video_pictures")
    var videoPictures = ArrayList<VideoPicture>()
    var width: Int = 0
}