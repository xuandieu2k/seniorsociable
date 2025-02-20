package vn.xdeuhug.seniorsociable.model.entity


import com.google.gson.annotations.SerializedName

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 14/08/2023
 */

class RestaurantNew {
    @SerializedName("address")
    var address = ""

    @SerializedName("brand_name")
    var brandName = ""

    @SerializedName("email")
    var email = ""

    @SerializedName("id")
    var id = 0L

    @SerializedName("info")
    var info = ""

    @SerializedName("is_done_setup")
    var isDoneSetup = 0

    @SerializedName("is_public")
    var isPublic = 0

    @SerializedName("name")
    var name = ""

    @SerializedName("phone")
    var phone = ""

    @SerializedName("restaurant_balance")
    var restaurantBalance = 0

    @SerializedName("restaurant_name")
    var restaurantName = ""

    @SerializedName("server_ip_address")
    var serverIpAddress = ""

    @SerializedName("status")
    var status = 0

    @SerializedName("tax_number")
    var taxNumber = ""

    @SerializedName("total_supplier_order")
    var totalSupplierOrder = 0

    @SerializedName("total_supplier_order_request")
    var totalSupplierOrderRequest = 0
}