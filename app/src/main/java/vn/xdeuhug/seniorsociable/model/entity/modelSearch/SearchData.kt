package vn.xdeuhug.seniorsociable.model.entity.modelSearch

import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 26 / 12 / 2023
 */
class SearchData {
    var keySearch = ""
    var user: User? = null
    var type = AppConstants.TYPE_KEY_SEARCH
}