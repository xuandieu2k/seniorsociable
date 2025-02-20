package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 27/07/2023
 */
class TicketData {
    @SerializedName("limit")
    val limit = 0

    @SerializedName("total_record")
    var totalRecord = 0

    @SerializedName("list")
    val list = ArrayList<Ticket>()
}