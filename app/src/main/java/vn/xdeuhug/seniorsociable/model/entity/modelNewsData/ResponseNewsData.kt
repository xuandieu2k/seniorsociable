package vn.xdeuhug.seniorsociable.model.entity.modelNewsData

import com.google.gson.annotations.SerializedName


class ResponseNewsData {
    var nextPage = 0L

    @SerializedName("results")
    var newses = ArrayList<News>()
    var status: String = ""
    var totalResults: Int = 0
}