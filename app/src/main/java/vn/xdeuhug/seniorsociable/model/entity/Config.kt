package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

/**
 * @Author: 阮仲伦 Nguyễn Trọng Luân
 * @Date: 12/17/22
 */
class Config {
    @SerializedName("type")
    var type = ""

    @SerializedName("api_key")
    var apiKey = ""

    //Đường dẫn api chat aloline
    @SerializedName("realtime_domain")
    var realtimeDomain = ""

    //Đường dẫn api upload
    @SerializedName("api_upload")
    var apiUpload = ""

    // Đường dẫn api upload
    @SerializedName("api_upload_short")
    var apiUploadShort = ""

    var sessionToken = ""
}