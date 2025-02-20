package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelFriend.RequestAddFriend
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.service.NotificationServiceNew

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 29 / 11 / 2023
 */
class NotificationManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface NotificationCallback {
            fun onNotificationFound(request: Notification?)
            fun onFailure(exception: Exception)
        }

        interface ListenerDataChange {
            fun onAdded(notification: Notification)
            fun onUpdated(notification: Notification)
            fun onRemoved(notification: Notification)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun getListNotificationByIdUser(
            userId: String, callback: FireStoreCallback<ArrayList<Notification>>
        ) {
            val notificationCollection =
                db.collection("Notification").document(userId).collection("UsersNotification")
            notificationCollection.get().addOnSuccessListener {
                val notificationList = ArrayList<Notification>()

                for (document in it.documents) {
                    val notification = document.toObject(Notification::class.java)
                    if (notification != null) {
                        notificationList.add(notification)
                    }
                }
                callback.onSuccess(notificationList)
            }.addOnFailureListener {
                logExceptionUseTimber(it)
                callback.onFailure(it)
            }
        }

//        suspend fun updateFirstSendNotification(notificationId: String, userId: String) {
//            return withContext(Dispatchers.IO) {
//                try {
//                    val notificationCollection = db.collection("Notification").document(userId)
//                        .collection("UsersNotification")
//                        .document(notificationId)
//                    notificationCollection.update("firstShow", false).await()
//
//                } catch (exception: Exception) {
//                    logExceptionUseTimber(exception)
//                }
//            }
//        }

        suspend fun updateFirstSendNotification(notificationId: String, userId: String) {
            return withContext(Dispatchers.IO) {
                try {
                    val documentRef = db.collection("Notification")
                        .document(userId)
                        .collection("UsersNotification")
                        .document(notificationId)

                    // Kiểm tra xem tài liệu có tồn tại không trước khi cập nhật
                    val documentSnapshot = documentRef.get().await()
                    if (documentSnapshot.exists()) {
                        // Thực hiện cập nhật
                        documentRef.update("firstShow", false).await()
                    } else {
                        // Xử lý khi tài liệu không tồn tại
                        // ...
                        Timber.d("Không có $documentRef")
                    }
                } catch (exception: Exception) {
                    logExceptionUseTimber(exception)
                }
            }
        }


        suspend fun getAllNotificationByUserIdAsync(userId: String): ArrayList<Notification> {
            return withContext(Dispatchers.IO) {
                try {
                    val notificationCollection = db.collection("Notification").document(userId)
                        .collection("UsersNotification")
                    val querySnapshot = notificationCollection.get().await()
                    val notificationList = ArrayList<Notification>()

                    for (document in querySnapshot.documents) {
                        val request = document.toObject(Notification::class.java)
                        if (request != null) {
                            notificationList.add(request)
                        }
                    }

                    notificationList
                } catch (exception: Exception) {
                    logExceptionUseTimber(exception)
                    ArrayList()
                }
            }
        }

        fun registerListenerNodeChangeById(
            userIdListener: String, callback: (ArrayList<Notification>) -> Unit
        ) {
            val requestCollection = db.collection("Notification").document(userIdListener)
                .collection("UsersNotification").orderBy("timeCreated")

            requestCollection.addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null) {
                    val list = ArrayList<Notification>()
                    for (document in querySnapshot.documents) {
                        val request = document.toObject(Notification::class.java)
                        request?.let {
                            if (request.read == AppConstants.IS_UNREAD) {
                                list.add(request)
                            }
                        }
                    }
                    callback(list)
                } else {
                    // Xử lý lỗi hoặc trường hợp khác
                    logExceptionUseTimber(Exception("Query snapshot is null"))
                }
            }
        }

        fun listenToNotificationChanges(
            userId: String, listener: ListenerDataChange
        ) {
            val notificationCollection =
                db.collection("Notification").document(userId).collection("UsersNotification")
                    .orderBy("timeCreated", Query.Direction.DESCENDING)
                    .addSnapshotListener { querySnapshot, exception ->
                        if (exception != null) {
                            logExceptionUseTimber(exception)
                            return@addSnapshotListener
                        }

                        querySnapshot?.documentChanges?.forEach { documentChange ->
                            when (documentChange.type) {
                                DocumentChange.Type.ADDED -> {
                                    val addedMessage =
                                        documentChange.document.toObject(Notification::class.java)
                                    listener.onAdded(addedMessage)
                                    // ... Do something with addedMessage
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    val modifiedMessage =
                                        documentChange.document.toObject(Notification::class.java)
                                    listener.onUpdated(modifiedMessage)
                                    // ... Do something with modifiedMessage
                                }

                                DocumentChange.Type.REMOVED -> {
                                    val removedMessage =
                                        documentChange.document.toObject(Notification::class.java)
                                    listener.onRemoved(removedMessage)
                                    // ... Do something with removedMessage
                                }
                            }
                        }
                    }
        }

        fun listenToNotificationDataChange(
            userId: String, listener: ListenerDataChange
        ) {
            if (userId != "") {
                val notificationCollection =
                    db.collection("Notification").document(userId).collection("UsersNotification")

                // Lắng nghe sự thay đổi của dữ liệu
                notificationCollection.addSnapshotListener { querySnapshot, exception ->
                    if (exception != null) {
                        logExceptionUseTimber(exception)
                        return@addSnapshotListener
                    }

                    // Xử lý các sự kiện thay đổi
                    for (change in querySnapshot!!.documentChanges) {
                        val document = change.document.toObject(Notification::class.java)
                        if (document != null) {
                            when (change.type) {
                                DocumentChange.Type.ADDED -> {
                                    listener.onAdded(document)
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    // Có thể xử lý sự kiện thay đổi nếu cần thiết
                                    listener.onUpdated(document)
                                }

                                DocumentChange.Type.REMOVED -> {
                                    // Xử lý khi có thông báo bị xóa (nếu cần)
                                    listener.onRemoved(document)
                                }
                            }
                        }
                    }
                }
            }
        }

        suspend fun addNotification(userId: String, notification: Notification) {
            withContext(Dispatchers.IO) {
                try {
                    val notificationCollection = db.collection("Notification").document(userId)
                        .collection("UsersNotification")

                    // Thêm thông báo vào Firestore
                    notificationCollection.document(notification.id).set(notification).await()
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                }
            }
        }

        suspend fun updateNotification(
            userId: String, notificationId: String, updatedNotification: Notification
        ) {
            withContext(Dispatchers.IO) {
                try {
                    val notificationDocument = db.collection("Notification").document(userId)
                        .collection("UsersNotification").document(notificationId)

                    // Cập nhật thông báo trong Firestore
                    notificationDocument.set(updatedNotification, SetOptions.merge()).await()
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                }
            }
        }

        fun updateReadNotification(
            userId: String, notificationId: String, callback: FireStoreCallback<Boolean>
        ) {
            val notificationDocument =
                db.collection("Notification").document(userId).collection("UsersNotification")
                    .document(notificationId)
            val map = HashMap<String, Any>()
            map["read"] = AppConstants.IS_READ
            // Cập nhật thông báo trong Firestore
            notificationDocument.update(map).addOnSuccessListener {
                callback.onSuccess(true)
            }.addOnFailureListener {
                logExceptionUseTimber(it)
                callback.onFailure(it)
            }
        }

        suspend fun deleteNotification(userId: String, notificationId: String) {
            withContext(Dispatchers.IO) {
                try {
                    val notificationDocument = db.collection("Notification").document(userId)
                        .collection("UsersNotification").document(notificationId)

                    // Xóa thông báo khỏi Firestore
                    notificationDocument.delete().await()
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                }
            }
        }


        private fun logExceptionUseTimber(exception: Exception) {
            Timber.tag("Exception FireStore").d(exception)
        }
    }
}