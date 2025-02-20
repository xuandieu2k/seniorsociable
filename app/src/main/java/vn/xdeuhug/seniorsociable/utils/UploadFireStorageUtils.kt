package vn.xdeuhug.seniorsociable.utils

import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 22 / 10 / 2023
 */
object UploadFireStorageUtils {
    fun getUploadRootURLAvatar(): String {
        return "${UploadFireStorageConstants.HEAD_UPLOAD}${UploadFireStorageConstants.BODY_UPLOAD_AVATAR}"
    }

    fun getUploadRootURLPost(): String {
        return "${UploadFireStorageConstants.HEAD_UPLOAD}${UploadFireStorageConstants.BODY_UPLOAD_POST}"
    }


    fun getRootURLAvatarById(idUser: String): String {
        return "${UploadFireStorageConstants.HEAD_UPLOAD}${UploadFireStorageConstants.BODY_UPLOAD_AVATAR}/$idUser/"
    }

    fun getRootURLBackgroundById(idUser: String): String {
        return "${UploadFireStorageConstants.HEAD_UPLOAD}${UploadFireStorageConstants.BODY_UPLOAD_BACKGROUND}/$idUser/"
    }

    fun getRootURLEventById(idEvent: String): String {
        return "${UploadFireStorageConstants.HEAD_UPLOAD}${UploadFireStorageConstants.BODY_UPLOAD_EVENT}/$idEvent/"
    }

    fun getRootURLPostById(idPost: String): String {
        return "${UploadFireStorageConstants.HEAD_UPLOAD}${UploadFireStorageConstants.BODY_UPLOAD_POST}/$idPost/"
    }

    fun getRootURLStoryById(idStory: String): String {
        return "${UploadFireStorageConstants.HEAD_UPLOAD}${UploadFireStorageConstants.BODY_UPLOAD_STORY}/$idStory/"
    }

    /**
     * @param idComment - id của comment và hàm trả về url gốc của nơi lưu hình ảnh đã bao
     */
    fun getRootURLCommentById(idComment: String): String {
        return "${UploadFireStorageConstants.HEAD_UPLOAD}${UploadFireStorageConstants.BODY_UPLOAD_COMMENT}/$idComment/"
    }

    fun getRootURLMessageById(idChat: String,idMessage: String): String {
        return "${getRootURLChatById(idChat)}${UploadFireStorageConstants.BODY_UPLOAD_MESSAGE}/$idMessage/"
    }

    fun getRootURLChatById(idChat: String): String {
        return "${UploadFireStorageConstants.HEAD_UPLOAD}${UploadFireStorageConstants.BODY_UPLOAD_CHAT}/$idChat/"
    }

    fun getRootURLEventsById(idPost: String): String {
        return "${UploadFireStorageConstants.HEAD_UPLOAD}${UploadFireStorageConstants.BODY_UPLOAD_POST}/$idPost/"
    }
}