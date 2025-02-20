package vn.xdeuhug.seniorsociable.router

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 03 / 11 / 2023
 */
object ApiNewsRouters {
    const val VERSION = "1"
    const val Host = "https://newsdata.io/"

    fun API_GET_LIST_NEWS(): String {
        return "api/${VERSION}/news"
    }
}