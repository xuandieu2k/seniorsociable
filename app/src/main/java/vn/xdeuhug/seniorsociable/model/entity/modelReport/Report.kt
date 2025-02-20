package vn.xdeuhug.seniorsociable.model.entity.modelReport

import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 12 / 2023
 */
class Report {
    var id = ""
    var idUser = ""
    var reason: Reason? = null
    var timeReport = Date()
    var idPostReport = ""
    var contentReport = ""

    constructor(
        id: String,
        idUser: String,
        reason: Reason?,
        timeReport: Date,
        idPostReport: String,
        contentReport: String
    ) {
        this.id = id
        this.idUser = idUser
        this.reason = reason
        this.timeReport = timeReport
        this.idPostReport = idPostReport
        this.contentReport = contentReport
    }

    constructor()


}