package vn.xdeuhug.seniorsociable.model.eventbus

import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 17 / 12 / 2023
 */
class AddedPostEventBus(var isAdded: Boolean,var post: Post) {}