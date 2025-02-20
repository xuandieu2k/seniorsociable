package vn.xdeuhug.seniorsociable.model.entity.modelUser

import vn.xdeuhug.seniorsociable.constants.PostConstants
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 30 / 10 / 2023
 */
class Share {
    var id = ""
    var timeCreated = Date()
    var idUserShare = ""
    var type = PostConstants.SHARE_TYPE_POST
}