package vn.xdeuhug.seniorsociable.model.entity.modelNewsData


import com.google.gson.annotations.SerializedName
import com.hjq.http.annotation.HttpRename
import java.io.Serializable

class News: Serializable {
    @SerializedName("article_id")
    @HttpRename("article_id")
    var articleId: String = ""
    var category = ArrayList<String>()
    var content: String = ""
    var country = ArrayList<String>()
    var creator = ArrayList<String>()
    var description: String = ""

    @SerializedName("image_url")
    @HttpRename("image_url")
    var imageUrl: String = ""
    var keywords: String = ""
    var language: String = ""
    var link: String = ""
    var pubDate: String = ""

    @SerializedName("source_id")
    @HttpRename("source_id")
    var sourceId: String = ""

    @SerializedName("source_priority")
    @HttpRename("source_priority")
    var sourcePriority: Int = 0
    var title: String = ""

    @SerializedName("video_url")
    @HttpRename("video_url")
    var videoUrl: String = ""

    constructor()


    constructor(
        articleId: String,
        category: ArrayList<String>,
        content: String,
        country: ArrayList<String>,
        creator: ArrayList<String>,
        description: String,
        imageUrl: String,
        keywords: String,
        language: String,
        link: String,
        pubDate: String,
        sourceId: String,
        sourcePriority: Int,
        title: String,
        videoUrl: String
    ) {
        this.articleId = articleId
        this.category = category
        this.content = content
        this.country = country
        this.creator = creator
        this.description = description
        this.imageUrl = imageUrl
        this.keywords = keywords
        this.language = language
        this.link = link
        this.pubDate = pubDate
        this.sourceId = sourceId
        this.sourcePriority = sourcePriority
        this.title = title
        this.videoUrl = videoUrl
    }


    companion object {
        fun News.toMap(): Map<String, Any> {
            val map = HashMap<String, Any>()
            map["articleId"] = articleId
            map["category"] = category
            map["content"] = content
            map["country"] = country
            map["creator"] = ""
            map["description"] = description
            map["imageUrl"] = imageUrl
            map["keywords"] = keywords
            map["language"] = language
            map["link"] = link
            map["pubDate"] = pubDate
            map["sourceId"] = sourceId
            map["sourcePriority"] = sourcePriority
            map["title"] = title
            map["videoUrl"] = videoUrl
            return map
        }
    }
}