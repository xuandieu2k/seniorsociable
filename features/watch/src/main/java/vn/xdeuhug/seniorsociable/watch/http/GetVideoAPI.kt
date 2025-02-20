package vn.xdeuhug.seniorsociable.watch.http

import com.hjq.http.annotation.HttpRename
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.http.api.BaseApi
import vn.xdeuhug.seniorsociable.router.ApiVideoRouters

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 11 / 2023
 */
class GetVideoAPI : BaseApi() {

    @HttpRename("page")
    var page = 1

    /**
     * @param limit giới hạn record mỗi trang
     */
    @HttpRename("per_page")
    var limit = 20

    override fun getApi(): String {
        return ApiVideoRouters.API_GET_VIDEO_POPULAR()
    }

    companion object {
        fun params(): BaseApi {
            val data = GetVideoAPI()
            data.method = AppConstants.HTTP_METHOD_GET
            data.authorization = AppConstants.KEY_PEXEL
            return data
        }

        fun params(page: Int, limit: Int): BaseApi {
            val data = GetVideoAPI()
            data.method = AppConstants.HTTP_METHOD_GET
            data.authorization = AppConstants.KEY_PEXEL
            data.page = page
            data.limit = limit
            return data
        }

        fun params(page: Int, limit: Int, category: String): BaseApi {
            val data = GetVideoAPI()
            data.method = AppConstants.HTTP_METHOD_GET
            data.authorization = AppConstants.KEY_PEXEL
            data.page = page
            data.limit = limit
            return data
        }

    }
}