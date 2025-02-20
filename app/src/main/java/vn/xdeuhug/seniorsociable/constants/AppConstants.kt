package vn.xdeuhug.seniorsociable.constants

/**
 * @Author: Bùi Hửu Thắng
 * @Date: 03/10/2022
 */
object
AppConstants {
    const val SECOND_INSTALL = "SECOND_INSTALL"
    const val CACHE_USER = "CACHE_USER"
    const val CACHE_WORK_AT = "CACHE_WORK_AT_LIST"
    const val CACHE_USER_LIST = "CACHE_USER_LIST"
    const val CACHE_SEARCH_DATA_LIST = "CACHE_SEARCH_DATA_LIST"
    const val CACHE_DISEASES_LIST = "CACHE_DISEASES_LIST"
    const val CACHE_SETTING = "CACHES_SETTING"
    const val CACHE_BRANCH = "CACHES_BRANCH"
    const val CACHE_HTTP_LOG = "CACHE_HTTP_LOG"
    const val CACHE_RESTAURANT = "CACHES_RESTAURANT"
    const val CACHE_CONFIG = "CACHE_CONFIG"
    const val TERMS_OF_USE = "https://xuandieu2709.github.io/seniorsociable.github.io/term_of_use.html"
    const val PRIVACY_POLICY = "https://xuandieu2709.github.io/seniorsociable.github.io/privacy_policy.html"

    const val UPLOAD_IMAGE = 0 // type up image
    const val UPLOAD_VIDEO = 1 // type up video
    const val UPLOAD_AUDIO = 2 // type up audio
    const val UPLOAD_FILE = 3 // type up file

    const val MEDIA_ID = "medias[0][media_id]"
    const val TYPE = "medias[0][type]"
    const val FILE = "medias[0][file]"

    const val PUSH_TOKEN = "PUSH_TOKEN"
    const val USER_ID = "USER_ID"
    const val MEDIA_DATA = "MEDIA_DATA"
    const val MEDIA_DATA_TYPE = "MEDIA_DATA_TYPE"
    const val MEDIA_POSITION = "MEDIA_POSITION"
    const val DATA_MEDIA_TYPE = "DATA_MEDIA_TYPE"
    const val TYPE_FILE_CHAT = "TYPE_FILE_CHAT"

    //Dialog Input Type
    const val INTEGER_INPUT_TABLE = 1
    const val DECIMAL_INPUT_TABLE = 3
    const val PERCENT_INPUT_TABLE = 4 //
    const val INTEGER_INPUT_TABLE_MIN_HUNDRED_ZERO = 5
    const val SHIFT_INPUT = -1
    const val MAX_TEXT_LENGTH = 255

    // Tab
    const val TAB_EVENT = 10
    const val TAB_NEWS = 20
    const val TAB_MOVIE_SHORT = 30



    const val FOLDER_APP = "techres/supplier"//folder lưu file của App
    const val EXPIRED = 1
    const val LOCATION = 2
    const val PROGRESS = "progress"
    const val DOWNLOAD_CACHE = "DOWNLOAD_CACHE"
    const val FILE_DOWNLOAD = "FILE_DOWNLOAD"
    const val TYPE_DOWNLOAD = "TYPE_DOWNLOAD"

    const val KEY_DATA = "KEY_DATA"

    const val TYPE_WARNING = 1
    const val TYPE_NOTE = 2
    const val TYPE_RESET_PASSWORD = 3
    const val TYPE_NOTIFICATION = 4
    const val TYPE_EMPLOYEE_CREATED = 5
    const val TYPE_THREE_OPTION = 6

    const val BUTTON_CONFIRM = 4
    const val BUTTON_CANCEL = 5
    const val BUTTON_EXIT = 6

    const val OBJECT_ID = "OBJECT_ID"
    const val NOTIFICATION_TYPE = "NOTIFICATION_TYPE"
    const val JSON_ADDITION_DATA = "JSON_ADDITION_DATA"

    const val PAGE_SIZE = 5L
    const val PAGE_SIZE_10 = 10L
    const val PAGE_SIZE_15 = 15L

    const val TYPE_GOOGLE = 1000
    const val TYPE_FACEBOOK = 2000
    const val TYPE_PHONE = 3000
    const val CHAR_PLUS = "+"
    const val DATE_FORMAT = "dd/MM/yyyy"

    // Gender
    const val NOT_UPDATE = -1
    const val MAN = 1
    const val WOMAN = 0

    const val HTTP_METHOD_POST = 1
    const val HTTP_METHOD_GET = 0

    const val KEY_PEXEL = "vueCURBr9F2QljdlpP1zOl4FsjKOpmBmV6J3OhWmj3vp4ZGI6rGGRMJ7"

    const val PERSONAL_USER = "PERSONAL_USER"

    // Type group chat
    const val TYPE_CHAT_TWO_PERSON = 1
    const val TYPE_CHAT_GROUP = 2


    //
    const val TYPE_IS_NOT_REQUESTED_ADD_FRIENDS = 0 // bạn CHưa gửi lời mời
    const val TYPE_IS_REQUESTED_ADD_FRIENDS = 1 // Bản thân đã gửi lời mời
    const val TYPE_ENEMY_IS_REQUESTED_ADD_FRIENDS = 2 // Đối phương Đã gửi lời mời
    const val TYPE_IS_FRIENDS = 3 // Là bạn bè

    //
    const val IS_REFUSE = 0
    const val IS_AGREE = 1
    const val IS_PENDING = 2

    //
    const val ONE_OPTION = 1
    const val TWO_OPTION = 2
    const val THREE_OPTION = 3
    const val FOUR_OPTION = 4
    const val FIVE_OPTION = 5

    // HANDLE NOTIFICATION
    const val OBJECT_CHAT = "OBJECT_CHAT"
    const val IS_READ = 1
    const val IS_UNREAD = 2
    const val SEND_NOTIFICATION = true
    const val BLOCK_SEND_NOTIFICATION = false
    const val NOTIFICATION_FROM_POST_COMMENT = 0
    const val NOTIFICATION_FROM_POST = 1
    const val NOTIFICATION_FROM_POST_INTERACT = 2
    const val NOTIFICATION_FROM_MOVIE_SHORT_COMMENT = 3
    const val NOTIFICATION_FROM_MOVIE_SHORT_INTERACT = 4
    const val NOTIFICATION_FROM_REQUEST_ADD_FRIEND = 5
    const val NOTIFICATION_FROM_EVENT = 6
    const val NOTIFICATION_FROM_EVENT_POST = 7
    const val POST_ID = "POST_ID"
    const val NOTIFICATION_ID = "NOTIFICATION_ID"
    const val NOTIFICATION_ID_2 = "NOTIFICATION_ID_2"
    const val COMMENT_POST_ID = "COMMENT_POST_ID"
    const val INTERACT_POST_ID = "INTERACT_POST_ID"
    const val COMMENT_MOVIE_SHORT_ID = "COMMENT_MOVIE_SHORT_ID"
    const val INTERACT_MOVIE_SHORT_ID = "INTERACT_MOVIE_SHORT_ID"

    const val MULTIMEDIA_LIST_OBJECT = "MULTIMEDIA_LIST_OBJECT"
    const val MULTIMEDIA_OBJECT = "MULTIMEDIA_OBJECT"

    const val MARITAL_STATUS_NOT_UPDATE = -1
    const val MARITAL_STATUS_SINGLE = 0
    const val MARITAL_STATUS_MARRIED = 1

    //
    const val OBJECT_NOTIFICATION = "OBJECT_NOTIFICATION"
    const val OBJECT_DISEASES = "OBJECT_DISEASES"
    const val OBJECT_POST = "OBJECT_POST"
    const val LIST_NEWS = "LIST_NEWS"

    // TYPE
    const val TYPE_KEY_SEARCH = 1
    const val TYPE_USER = 2

    // ROLE
    const val ROLE_NORMAL = 10 // Default
    const val ROLE_ADMIN = 20 // ADMIn có thêm duyệt bài khóa tài khoản
    const val ROLE_ADMIN_1 = 30 // ADMIn được admin cấp quyền thêm duyệt bài khóa tài khoản

    // STATUS ACTIVE
    const val ACTIVATING = 1 // Default Đang hoạt động
    const val BLOCKING = 2 // Đang bị khóa Có thời hạn
    const val BLOCKED_UN_LIMITED = 3 // Vĩnh viễn

    // FILTER
    const val FILTER_ALL = 0
    const val FILTER_ACTIVATING = 1
    const val FILTER_BLOCKING = 2
    const val FILTER_BLOCKING_UN_LIMITED = 3


    // TYPE LOCk
    const val LOCK_ONE_WEAK = 0
    const val LOCK_ONE_MONTH = 1
    const val LOCK_ONE_YEAR = 2
    const val LOCK_FOREVER = 3
    const val LOCK_LIMITED = 4


    // REPORT
    const val REPORT_TODAY = 1
    const val REPORT_YESTERDAY = 9
    const val REPORT_THIS_WEEK = 2
    const val REPORT_THIS_MONTH = 3
    const val REPORT_LAST_MONTH = 10
    const val REPORT_THIS_YEAR = 5
    const val REPORT_LAST_YEAR = 11
    const val REPORT_THREE_YEARS = 6

//    const val REPORT_TODAY = 0
//    const val REPORT_YESTERDAY = 1
//    const val REPORT_THIS_WEEK = 2
//    const val REPORT_THIS_MONTH = 3
//    const val REPORT_LAST_MONTH = 4
//    const val REPORT_THIS_YEAR = 5
//    const val REPORT_THREE_YEARS = 6
}