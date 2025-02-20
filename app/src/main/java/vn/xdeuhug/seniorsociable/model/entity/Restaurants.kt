package vn.xdeuhug.seniorsociable.model.entity


import com.google.gson.annotations.SerializedName

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 20/07/2023
 */
class Restaurants {
    @SerializedName("address")
    var address = ""

    @SerializedName("contact_name")
    var contactName = ""

    @SerializedName("contactor_avatar")
    var contactorAvatar = ""

    @SerializedName("email")
    var email = ""

    @SerializedName("id")
    var id = 0L

    @SerializedName("info")
    var info = ""

    @SerializedName("is_done_setup")
    var isDoneSetup = 0

    @SerializedName("is_main_contactor")
    var isMainContactor = 0

    @SerializedName("is_public")
    var isPublic = 0

    @SerializedName("logo")
    var logo = ""

    @SerializedName("name")
    var name = ""

    @SerializedName("number_branches")
    var numberBranches = 0

    @SerializedName("phone")
    var phone = ""

    @SerializedName("restaurant_balance")
    var restaurantBalance = 0.0

    @SerializedName("restaurant_name")
    var restaurantName = ""

    @SerializedName("status")
    var status = 0

    @SerializedName("tax_number")
    var taxNumber = ""

    @SerializedName("total_done")
    var totalDone = 0

    @SerializedName("total_order")
    var totalOrder = 0

    @SerializedName("total_return")
    var totalReturn = 0

    @SerializedName("total_delivering")
    var totalDelivering = 0

    @SerializedName("total_waiting")
    var totalWaiting = 0
}