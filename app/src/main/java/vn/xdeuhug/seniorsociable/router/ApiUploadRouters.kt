package vn.xdeuhug.seniorsociable.router

import vn.xdeuhug.seniorsociable.BuildConfig
import vn.xdeuhug.seniorsociable.cache.ConfigCache

@Suppress("FunctionName")
object ApiUploadRouters {
    private const val VERSION = "v2"

    var UPLOAD_URL_PATH = "${ConfigCache.getConfig().apiUpload}/api/$VERSION/media/upload"

    fun API_GENERATE_FILE(): String {
        return "api/$VERSION/media/generate"
    }

    fun API_UPLOAD_FILE(): String {
        return "${BuildConfig.HOST_URL}api/$VERSION/media/upload"
    }

}