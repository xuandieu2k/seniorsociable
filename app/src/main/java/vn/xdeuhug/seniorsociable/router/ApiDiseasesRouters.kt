package vn.xdeuhug.seniorsociable.router

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 12 / 2023
 */
object ApiDiseasesRouters {
    const val VERSION = "v1"
    const val Host = "https://diseases-api.onrender.com/api/"

    fun API_GET_LIST_DISEASES(): String {
        return "${VERSION}/diseases"
    }
}