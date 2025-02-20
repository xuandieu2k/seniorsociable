package vn.xdeuhug.seniorsociable.watch.http

import com.hjq.http.annotation.HttpRename
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.http.api.BaseApi
import vn.xdeuhug.seniorsociable.router.ApiVideoRouters

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 06 / 11 / 2023
 */
/**
 * @param limit giới hạn record mỗi trang
 */
class GetVideoWithKeyword : BaseApi() {

    @HttpRename("page")
    var page = 1

    @HttpRename("per_page")
    var limit = 20


    @HttpRename("query")
    var query = ""


    override fun getApi(): String {
        return ApiVideoRouters.API_GET_VIDEO_WITH_KEYSEARCH()
    }

    companion object {
        fun params(): BaseApi {
            val data = GetVideoWithKeyword()
            data.method = AppConstants.HTTP_METHOD_GET
            data.authorization = AppConstants.KEY_PEXEL
            return data
        }

        fun params(page: Int, limit: Int, query: String): BaseApi {
            val data = GetVideoWithKeyword()
            data.method = AppConstants.HTTP_METHOD_GET
            data.authorization = AppConstants.KEY_PEXEL
            data.page = page
            data.limit = limit
            data.query = query
            return data
        }

    }
}