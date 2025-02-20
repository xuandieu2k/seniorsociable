package vn.xdeuhug.seniorsociable.model.entity.modelPexels


import com.google.gson.annotations.SerializedName

class ResponsePexel {
    @SerializedName("next_page")
    var nextPage: String = ""
    var page: Int = 0

    @SerializedName("per_page")
    var perPage: Int = 0

    @SerializedName("total_results")
    var totalResults: Int = 0
    var url: String = ""
    var videos = ArrayList<Video>()
}