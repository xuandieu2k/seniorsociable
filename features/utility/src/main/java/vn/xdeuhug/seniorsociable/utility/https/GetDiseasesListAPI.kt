package vn.xdeuhug.seniorsociable.utility.https

import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.http.api.BaseApi
import vn.xdeuhug.seniorsociable.router.ApiDiseasesRouters
import vn.xdeuhug.seniorsociable.router.ApiNewsRouters

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 12 / 2023
 */
class GetDiseasesListAPI : BaseApi() {
    override fun getApi(): String {
        return ApiDiseasesRouters.API_GET_LIST_DISEASES()
    }

    companion object {
        fun params(): BaseApi {
            val data = GetDiseasesListAPI()
            return data
        }
    }

}