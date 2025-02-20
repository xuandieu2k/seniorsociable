package vn.xdeuhug.seniorsociable.news.model

import com.google.firebase.firestore.PropertyName
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 10 / 2023
 */
class News {
    @PropertyName("id")
    var id = ""

    var userPost = User() // User post story

    @PropertyName("time_created")
    var timeCreated = Date()
}