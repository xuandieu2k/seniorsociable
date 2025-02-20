package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 22/07/2023
 */
class RestaurantsResponse {
    @SerializedName("limit")
    val limit = 0

    @SerializedName("total_record")
    val totalRecord = 0

    @SerializedName("list")
    val list = ArrayList<Restaurants>()
}