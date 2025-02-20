package vn.xdeuhug.seniorsociable.constants

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 13 / 12 / 2023
 */
object CallConstants {
    const val IS_BUSY = true
    const val IS_NOT_BUSY = false
    const val IS_DEFAULT = -1 // Mặc định
    const val IS_REFUSE = 1 // Từ chối
    const val IS_ACCEPT = 2 // Chấp nhận
    const val IS_NOT_ALLOW = 3 // Không chấp nhận
    const val IS_RINGING = 4 // Đang đổ chuông
    const val IS_CALLING = 5 // Đang gọi

    const val TYPE_VIDEO_CALL = 1000 // Gọi video
    const val TYPE_VOICE_CALL = 2000 // Gọi voice

    const val ID_CALlER = "ID_CALlER"
    const val ID_RECEIVER = "ID_RECEIVER"
}