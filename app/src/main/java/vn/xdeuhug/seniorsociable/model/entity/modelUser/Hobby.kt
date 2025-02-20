package vn.xdeuhug.seniorsociable.model.entity.modelUser

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import com.google.firebase.firestore.Exclude
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.utils.AppUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 30 / 11 / 2023
 */
class Hobby {
    var id = 0
    var name = ""
    var imageResource = 0

    @get:Exclude
    var isChecked = false

    constructor(id: Int, name: String, imageResource: Int, isChecked: Boolean) {
        this.id = id
        this.name = name
        this.imageResource = imageResource
        this.isChecked = isChecked
    }

    constructor(id: Int, name: String, imageResource: Int) {
        this.id = id
        this.name = name
        this.imageResource = imageResource
    }

    constructor()

}