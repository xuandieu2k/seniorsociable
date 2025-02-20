package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

/**
 * @Author: Bùi Hửu Thắng
 * @Date: 13/4/23
 */
class Setting {
    @SerializedName("service_restaurant_level_id")
    var serviceRestaurantLevelId = 0

    @SerializedName("service_restaurant_level_type")
    var serviceRestaurantLevelType = 0

    @SerializedName("lat")
    var lat = ""

    @SerializedName("lng")
    var lng = ""

    @SerializedName("min_distance_checkin")
    var minDistanceCheckIn = 0

    @SerializedName("branch_type_option")
    var branchTypeOption = 0

    @SerializedName("branch_type_name")
    var branchTypeName = ""

    @SerializedName("branch_type")
    var branchType = 0

    @SerializedName("is_enable_booking")
    var isEnableBooking = 0
}