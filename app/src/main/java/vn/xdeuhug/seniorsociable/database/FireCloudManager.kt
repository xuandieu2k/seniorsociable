package vn.xdeuhug.seniorsociable.database

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.firebase.storage.StorageReference
import com.luck.picture.lib.utils.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import java.io.File

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 20 / 10 / 2023
 */
class FireCloudManager {
    companion object {
        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(result: T)
        }

        suspend fun uploadFilesToFirebaseStorage(
            files: ArrayList<MultiMedia>,
            storageRef: StorageReference,
            callback: FireStoreCallback<ArrayList<MultiMedia>> // Thay đổi kiểu callback
        ) = coroutineScope {
            val deferredUploads = files.map { media ->
                async(Dispatchers.IO) {
                    val fileUri = Uri.fromFile(File(media.url))
                    val fileRef = storageRef.child(media.id)
                    try {
                        val uploadTask = fileRef.putFile(fileUri)
                        uploadTask.await()
                        val downloadUrl = fileRef.downloadUrl.await()
                        media.url = downloadUrl.toString()
                        media.isUpdated = true
                    } catch (e: Exception) {
                        logExceptionUseTimber(e)
                    }
                }
            }

            deferredUploads.awaitAll()

            val allFilesUploaded = files.all { it.isUpdated }

            if (allFilesUploaded) {
                callback.onSuccess(files)
            } else {
                callback.onFailure(files)
            }
        }

        suspend fun deleteFilesFromFirebaseStorage(
            files: ArrayList<MultiMedia>,
            storageRef: StorageReference,
            callback: FireStoreCallback<ArrayList<MultiMedia>>
        ) = coroutineScope {
            val deferredDeletions = files.map { media ->
                async(Dispatchers.IO) {
                    try {
                        val fileRef = storageRef.child(media.id)
                        fileRef.delete().await()
                        media.isDeleted = true // Đặt lại trạng thái nếu cần
                    } catch (e: Exception) {
                        logExceptionUseTimber(e)
                    }
                }
            }

            deferredDeletions.awaitAll()

            val allFilesDeleted = files.all { it.isUpdated }

            if (allFilesDeleted) {
                callback.onSuccess(files)
            } else {
                callback.onFailure(files)
            }
        }


        suspend fun uploadAudioFile(
            audioFile: MultiMedia,
            storageRef: StorageReference,
            callback: FireStoreCallback<MultiMedia>
        ) = coroutineScope {
            val fileUri = Uri.fromFile(File(audioFile.url))
            val fileRef = storageRef.child(audioFile.id)

            try {
                fileRef.putFile(fileUri).await()
                audioFile.isUpdated = true
                val downloadUrl = fileRef.downloadUrl.await() //
                audioFile.url = downloadUrl.toString()
//                audioFile.url = audioFile.id //
                callback.onSuccess(audioFile)
            } catch (e: Exception) {
                logExceptionUseTimber(e)
                callback.onFailure(audioFile)
            }
        }

        fun uploadFileToFirebaseStorageNotAsync(
            uriFile: Uri,
            files: MultiMedia,
            storageRef: StorageReference,
            callback: FireStoreCallback<MultiMedia>
        ) {
            val fileRef = storageRef.child(files.id)
            fileRef.putFile(uriFile).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener {
                    files.url = it.toString() // Cập nhật trường URL của media
                    callback.onSuccess(files)
                }.addOnFailureListener {
                    logExceptionUseTimber(it)
                    callback.onFailure(files)
                }
            }.addOnFailureListener {
                logExceptionUseTimber(it)
                callback.onFailure(files)
            }

        }


        fun uploadFilesToFirebaseStorageNotAsync(
            files: ArrayList<MultiMedia>,
            folderName: String,
            storageRef: StorageReference,
            callback: FireStoreCallback<ArrayList<MultiMedia>>
        ) {
            var successCount = 0
            var failureCount = 0

            for (media in files) {
                val fileUri = Uri.fromFile(File(media.url))
                val fileRef = storageRef.child("$folderName/${media.id}")
                media.url = media.id

                try {
                    fileRef.putFile(fileUri).addOnSuccessListener {
                        media.isUpdated = true
                        successCount++
                        if (successCount + failureCount == files.size) {
                            if (successCount == files.size) {
                                callback.onSuccess(files)
                            } else {
                                callback.onFailure(files)
                            }
                        }
                    }.addOnFailureListener {
                        logExceptionUseTimber(it)
                        failureCount++
                        if (successCount + failureCount == files.size) {
                            callback.onFailure(files)
                        }
                    }
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    failureCount++
                    if (successCount + failureCount == files.size) {
                        callback.onFailure(files)
                    }
                }
            }
        }


        private fun logExceptionUseTimber(exception: Exception) {
            Timber.tag("Exception Storage").d(exception)
        }
    }
}