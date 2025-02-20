package vn.xdeuhug.seniorsociable.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.UserCache

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 09 / 10 / 2023
 */
object MultimediaUtils {

    const val TOKEN = "f5d26d0f-911f-4821-ba82-0c61c18023eb"
    const val ROOT_URL = "https://firebasestorage.googleapis.com/v0/b/seniorsociable.appspot.com/o/"
    const val ALT = "?alt=media&"
    /**
     * @param imagePath phải đi cùng hàm các getRootURL trong UploadFireStorageUtils cấu trúc như sau
     * @return getURL${imagePath}
     */
    fun getURLMedia(imagePath: String, callback: (String) -> Unit) {
        // Khởi tạo Firebase Storage
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.reference

        // Tạo một đối tượng StorageReference đến tệp hình ảnh
        val imageRef = storageReference.child(imagePath)

        // Lấy URL của hình ảnh và gọi callback khi hoàn thành
        imageRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val imageUrl = downloadUri.toString()
                callback(imageUrl) // Gọi callback với URL của hình ảnh
            } else {
                // Xử lý lỗi nếu cần
                callback("") // Trả về chuỗi rỗng trong trường hợp lỗi
            }
        }
    }
//    fun getURLMedia(imagePath: String, callback: (String) -> Unit) {
//        val imagePathNew = imagePath.replace("/","%2F")
//        val str = "$ROOT_URL$imagePathNew$ALT$TOKEN"
//        AppUtils.logJsonFromObject(str)
//        callback(str)
//    }

    fun getURLVideo(videoPath: String, callback: (String) -> Unit) {
        // Khởi tạo Firebase Storage
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.reference

        // Tạo một đối tượng StorageReference đến tệp hình ảnh
        val videoRef = storageReference.child(videoPath)

        // Lấy URL của hình ảnh và gọi callback khi hoàn thành
        videoRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val imageUrl = downloadUri.toString()
                callback(imageUrl) // Gọi callback với URL của hình ảnh
            } else {
                // Xử lý lỗi nếu cần
                callback("") // Trả về chuỗi rỗng trong trường hợp lỗi
            }
        }
    }
}