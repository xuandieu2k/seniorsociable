package vn.xdeuhug.seniorsociable.utility.model


import com.google.gson.annotations.SerializedName
import java.util.Date

class UpdateVersion {
    @SerializedName("app_version")
    var appVersion = ""

    @SerializedName("created_at")
    var createdAt = Date()

    @SerializedName("download_link")
    var downloadLink = ""
    var id = 0L

    @SerializedName("is_require_update")
    var isRequireUpdate = 0
    var message = ""

    @SerializedName("os_name")
    var osName = ""

    @SerializedName("updated_at")
    var updatedAt = Date()

    @SerializedName("version_application_name")
    var versionApplicationName = ""
}