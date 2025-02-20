package vn.xdeuhug.seniorsociable.router

import vn.xdeuhug.seniorsociable.cache.UserCache

@Suppress("FunctionName")
object ApiRouters {
    private const val VERSION = "v5"

    fun API_FEEDBACK(): String {
        return "api/$VERSION/employees/feedback"
    }

    fun API_CHECK_OTP_NEW(): String {
        return "api/$VERSION/suppliers/verify-code"
    }

    fun API_UPDATE_PROFILE_STAFF(): String {
        return "api/$VERSION/employees/${UserCache.getUser().id}"
    }

    fun API_GET_RESTAURANTS(): String {
        return "api/$VERSION/restaurants"
    }

    fun API_GET_TABLE_PRICE(id: Long): String {
        return "api/$VERSION/materials/${id}/supplier-materials-restaurant-using"
    }

    fun API_UPDATE_PRICE(id: Long): String {
        return "api/$VERSION/materials/${id}/supplier-material-price"
    }

    fun API_GET_DETAILS_ORDER_RESTAURANTS(id: Long): String {
        return "api/$VERSION/restaurants/supplier-restaurant-detail/${id}"
    }

    fun API_GET_BRAND(): String {
        return "api/$VERSION/restaurant-brand"
    }

    fun API_GET_BRANCHES(): String {
        return "api/$VERSION/branches"
    }

    fun REPORT_GROUP_BY_RESTAURANT(): String{
        return "api/${VERSION}/supplier-orders/group-by-restaurant"
    }

    fun REPORT_WARE_HOUSE_SESSIONS(): String{
        return "api/${VERSION}/supplier-warehouse-sessions"
    }

}