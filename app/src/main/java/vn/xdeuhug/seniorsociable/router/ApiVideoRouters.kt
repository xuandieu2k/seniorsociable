package vn.xdeuhug.seniorsociable.router

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 11 / 2023
 */
object ApiVideoRouters {
    const val Host = "https://api.pexels.com"

    fun API_GET_VIDEO_POPULAR(): String {
        return "/videos/popular"
    }

    fun API_GET_VIDEO_WITH_KEYSEARCH(): String {
        return "/videos/search"
    }

}