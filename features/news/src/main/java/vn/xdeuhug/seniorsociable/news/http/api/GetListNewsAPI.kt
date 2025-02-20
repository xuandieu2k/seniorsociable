package vn.xdeuhug.seniorsociable.news.http.api

import com.hjq.http.annotation.HttpRename
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.http.api.BaseApi
import vn.xdeuhug.seniorsociable.router.ApiNewsRouters

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 03 / 11 / 2023
 */
class GetListNewsAPI : BaseApi() {


    @HttpRename("country")
    var country = "vi"

    @HttpRename("category")
    var category = "top"

    @HttpRename("apikey")
    var apikey = "pub_306097c75eb9654ca735dc6fc7a5cf8862c0c"


    override fun getApi(): String {
        return ApiNewsRouters.API_GET_LIST_NEWS()
    }

    companion object {
        fun params(country: String, category: String): BaseApi {
            val data = GetListNewsAPI()
            data.method = AppConstants.HTTP_METHOD_GET
            data.country = country
            data.category = category
            return data
        }
    }
}