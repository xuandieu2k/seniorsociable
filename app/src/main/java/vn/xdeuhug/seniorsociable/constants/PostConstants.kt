package vn.xdeuhug.seniorsociable.constants

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 14 / 10 / 2023
 */
object PostConstants {
    // Trạng thái bài viết
    const val TYPE_PUBLIC = 1 // Công khai
    const val TYPE_PRIVATE = 2 // Cá nhân - Chỉ mình tôi
    const val TYPE_FRIEND = 3 // Bạn bè


    // Loại bài viết
    const val POST_DEFAULT = 1 // Bài viết thông thường được tạo từ chính người tạo
    const val POST_SHARE = 2 // Bài viết được chia sẻ từ bài viết khác
    const val POST_UPDATE_INFORMATION = 3 // Bài viết được tạo thông qua việc cập nhật trang cá nhân
    const val POST_UPDATE_AVATAR = 4 // Bài viết được tạo thông qua việc cập nhật trang cá nhân
    const val POST_UPDATE_BACKGROUND = 5 // Bài viết được tạo thông qua việc cập nhật trang cá nhân
    const val POST_EVENT = 6 // Bài viết từ sự kiện

    // Tương tác
    const val INTERACT_TAB_DEFAULT = 0
    const val INTERACT_LIKE = 1
    const val INTERACT_LOVE = 2
    const val INTERACT_SMILE = 3 // Ngượng ngùng
    const val INTERACT_WOW = 4 //
    const val INTERACT_SAD = 5
    const val INTERACT_ANGRY = 6

    const val OBJECT_DETAIL_INTERACT = "OBJECT_DETAIL_INTERACT"

    /**
     * Type chia sẻ
     */

    const val SHARE_TYPE_POST = 1
    const val SHARE_TYPE_STORY = 2

    /*
    * Trang thái bài viết ---- STATUS ----
     */
    const val IS_PASSED = 1 // Đã duyệt
    const val IS_NOT_PASSED = 2 // Không duyệt
    const val IS_PENDING_PASSED = 3 // Chờ duyệt
    const val IS_LOCKED = 4 // Đã khóa

    /*
    * Trang thái kiểm duyệt
    */
    const val STATUS_PASSED = 1 // Đã duyệt
    const val STATUS_PENDING_PASSED = 2 // Chờ duyệt
    const val STATUS_NOT_PASSED = 3 // Không duyệt
    const val STATUS_LOCKED = 4 // khóa bài
    const val STATUS_RE_OPENED = 5 // Mở lại

    /*
    * Lọc Trang thái bài viết
    */
    const val FILTER_ALL = 0
    const val FILTER_IS_PASSED = 1
    const val FILTER_IS_NOT_PASSED = 2
    const val FILTER_IS_PENDING_PASSED = 3
    const val FILTER_IS_LOCKED = 4

}