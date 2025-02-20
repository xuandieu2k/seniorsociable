package vn.xdeuhug.seniorsociable.model.entity.modelMedia

import com.google.firebase.firestore.Exclude
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post.Companion.toMap
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 16 / 10 / 2023
 */
class Album {
    var id = ""
    var name = ""
    var timeCreated = Date()
    var posts: ArrayList<Post>? = null

    @get:Exclude
    var isChecked = false

    companion object{
        fun Album.toMap(): Map<String, Any> {
            val map = HashMap<String, Any>()

            map["id"] = id
            map["name"] = name
            map["timeCreated"] = timeCreated
//            map["posts"] = posts.map { it.toMap() } // Chuyển mỗi Post trong ArrayList thành Map
            posts?.let {
                map["posts"] = it.map { post -> post.toMap() }
            }

            return map
        }
    }
}