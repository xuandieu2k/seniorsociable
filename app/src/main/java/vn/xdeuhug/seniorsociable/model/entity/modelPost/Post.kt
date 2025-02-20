package vn.xdeuhug.seniorsociable.model.entity.modelPost

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.Album
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.Album.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelReport.Report
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User.Companion.toMap
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 10 / 2023
 */
class Post {
    @PropertyName("id")
    var id = ""

    var idShareFrom = ""

    var idUserPost = "" // User post story

    @PropertyName("time_created")
    var timeCreated = Date()

    var type = PostConstants.TYPE_PUBLIC // Chế độ của bài viết

    var typePost = PostConstants.POST_DEFAULT

    var comments = ArrayList<Comment>()

    var interacts = ArrayList<Interact>()

    var idUserTags = ArrayList<String>()

    var content = ""

    var contentNormalize = ""

    var contentsNormalize = ArrayList<String>()

    var multiMedia = ArrayList<MultiMedia>()

    var album: Album? = null
    var status = PostConstants.IS_PASSED // Mặc định mở

    @get:Exclude
    var isReloadDataNotPic = false

    var idsUserShare = ArrayList<String>()

    var address: Address? = null


    // Report
    @get:Exclude
    var reports = ArrayList<Report>()

    companion object {

        fun Post.toMap(): Map<String, Any> {
            val map = HashMap<String, Any>()

            map["id"] = id
            map["idUserPost"] = idUserPost // Chuyển User thành Map
            map["timeCreated"] = timeCreated
            map["type"] = type
            map["status"] = status
//            map["comments"] =
//                comments.map { it.toMap() } // Chuyển mỗi Comment trong ArrayList thành Map
//            map["interacts"] =
//                interacts.map { it.toMap() } // Chuyển mỗi Interact trong ArrayList thành Map
            map["userTags"] = idUserTags // Chuyển mỗi User trong ArrayList thành Map
            map["content"] = content
            map["contentNormalize"] = contentNormalize
            map["contentsNormalize"] = contentsNormalize
            address?.let { map["address"] = it.toMap() }
            map["multiMedia"] =
                multiMedia.map { it.toMap() } // Chuyển mỗi MultiMedia trong ArrayList thành Map

            // Nếu album không null, chuyển album thành Map
            album?.let { map["album"] = it.toMap() }
            map["idsUserShare"] = idsUserShare
            return map
        }
    }
}