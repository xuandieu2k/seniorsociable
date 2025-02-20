package vn.xdeuhug.seniorsociable.model.entity.modelMovieShort

import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 12 / 2023
 */
class MovieShort {
    var id = 0
    var timeCreated = Date()

    var interacts = ArrayList<Interact>()
    var comments = ArrayList<Comment>()
    var idsUserShare = ArrayList<String>()


    constructor(
        id: Int,
        timeCreated: Date,
        interacts: ArrayList<Interact>,
        comments: ArrayList<Comment>,
        idsUserShare: ArrayList<String>
    ) {
        this.id = id
        this.timeCreated = timeCreated
        this.interacts = interacts
        this.comments = comments
        this.idsUserShare = idsUserShare
    }

    constructor()
}