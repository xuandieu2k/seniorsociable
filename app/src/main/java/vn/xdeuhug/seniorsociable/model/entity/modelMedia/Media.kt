package vn.xdeuhug.seniorsociable.model.entity.modelMedia

import com.google.gson.annotations.SerializedName
import com.hjq.http.annotation.HttpRename

class Media {
    @SerializedName("url")
    var url = ""

    @SerializedName("name")
    var name = ""

    @SerializedName("type")
    var type = 0

    @SerializedName("size")
    var size = 0L

    @SerializedName("width")
    var width = 0

    @SerializedName("height")
    var height = 0

    @HttpRename("is_keep")
    var isKeep = 0

    @SerializedName("realPath")
    var realPath = ""
}