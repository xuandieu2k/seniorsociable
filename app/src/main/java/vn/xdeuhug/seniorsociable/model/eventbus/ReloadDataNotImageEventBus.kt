package vn.xdeuhug.seniorsociable.model.eventbus

import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 01 / 11 / 2023
 */
class ReloadDataNotImageEventBus(var isReload: Boolean, var position: Int,var post: Post) {}