package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.CallConstants
import vn.xdeuhug.seniorsociable.model.entity.modelCall.CallRoom
import vn.xdeuhug.seniorsociable.model.entity.modelCall.CallRoom.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User.Companion.toMap

//import vn.xdeuhug.seniorsociable.service.CallRoomService

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 13 / 12 / 2023
 */
class CallRoomManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface CallRoomCallback {
            fun onCallRoomFound(request: CallRoom?)
            fun onFailure(exception: Exception)
        }

        interface ListenerDataChange {
            fun onAdded(callRoom: CallRoom)
            fun onUpdated(callRoom: CallRoom)
            fun onRemoved(callRoom: CallRoom)
        }

        interface ListenUpdate {
            fun updateStatusRoom(value: Boolean)
        }

        interface ListenerDataCallChange {
            fun onDataCallChange(callRoom: CallRoom)
        }

        interface ListenerCalling {
            fun onBusy(isBusy: Boolean)
            fun onAcceptStatusRoom()
            fun onRefuseStatusRoom()
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun updateCall(callRoom: CallRoom, callback: FireStoreCallback<Unit>) {
            val callMap = callRoom.toMap()
            db.collection("CallRoom").document(callRoom.id).update(callMap).addOnSuccessListener {
                callback.onSuccess(Unit)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        suspend fun updateCall(
            callRoom: CallRoom
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val callMap = callRoom.toMap()
                    db.collection("CallRoom").document(callRoom.id).update(callMap).await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }


        suspend fun getAllCallRoomByUserIdAsync(userId: String): ArrayList<CallRoom> {
            return withContext(Dispatchers.IO) {
                try {
                    val callRoomCollection =
                        db.collection("CallRoom").document(userId).collection("UsersCallRoom")
                    val querySnapshot = callRoomCollection.get().await()
                    val callRoomList = ArrayList<CallRoom>()

                    for (document in querySnapshot.documents) {
                        val request = document.toObject(CallRoom::class.java)
                        if (request != null) {
                            callRoomList.add(request)
                        }
                    }

                    callRoomList
                } catch (exception: Exception) {
                    logExceptionUseTimber(exception)
                    ArrayList()
                }
            }
        }

        fun listenBusy(userId: String, listener: ListenerCalling) {
            val callRoomCollection = db.collection("CallRoom")
            val documentReference = callRoomCollection.document(userId)
            // Tạo một lắng nghe để theo dõi thay đổi trên tài liệu cụ thể
            documentReference.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Xử lý lỗi nếu có
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    // Lấy giá trị của trường "busy"
                    val busy = snapshot.getBoolean("busy")
                    listener.onBusy(busy!!)
                } else {
                    // Document không tồn tại hoặc có sự cố khác
                }
            }

        }

        fun listenStatusRoom(userId: String, listener: ListenerCalling) {
            val callRoomCollection = db.collection("CallRoom")
            val documentReference = callRoomCollection.document(userId)
            // Tạo một lắng nghe để theo dõi thay đổi trên tài liệu cụ thể
            documentReference.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Xử lý lỗi nếu có
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    // Lấy giá trị của trường "busy"
                    val data = snapshot.toObject(CallRoom::class.java)
                    if (data!!.statusRoom == CallConstants.IS_ACCEPT) {
                        listener.onAcceptStatusRoom()
                    }
                    if (data.statusRoom == CallConstants.IS_REFUSE) {
                        listener.onRefuseStatusRoom()
                    }
                } else {
                    // Document không tồn tại hoặc có sự cố khác
                }
            }

        }

        fun updateStatusRoom(userId: String, value: Int,listener:ListenUpdate ) {
            val callRoomCollection = db.collection("CallRoom")
            val documentReference = callRoomCollection.document(userId)

            val updates = hashMapOf<String, Any>(
                "statusRoom" to value
            )
            documentReference.update(updates).addOnSuccessListener {
                listener.updateStatusRoom(true)
            }.addOnFailureListener { e ->
                // Xử lý khi có lỗi xảy ra trong quá trình cập nhật
                Timber.tag("Firestore error").e("Error updating document" + e.message)
                listener.updateStatusRoom(false)
            }

        }

        fun listenToCallRoomChanges(userId: String, listener: ListenerDataCallChange) {
            val callRoomCollection = db.collection("CallRoom")
            val documentReference = callRoomCollection.document(userId)

            documentReference.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, exception ->
                if (exception != null) {
                    exception.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // Kiểm tra metadata để đảm bảo dữ liệu từ server
                    if (snapshot.metadata.isFromCache) {
                        // Dữ liệu từ local cache, không xử lý
                        return@addSnapshotListener
                    }

                    val data = snapshot.toObject(CallRoom::class.java)
                    if (data != null && data.busy == CallConstants.IS_BUSY && data.statusRoom != CallConstants.IS_DEFAULT) {
                        listener.onDataCallChange(data)
                    } else {
                        // Xử lý trường hợp khác nếu cần
                    }
                }
            }


        }

        suspend fun addCallRoom(userId: String, CallRoom: CallRoom) {
            withContext(Dispatchers.IO) {
                try {
                    val callRoomDocument = db.collection("CallRoom").document(userId)

                    // Thêm thông báo vào Firestore
                    callRoomDocument.set(CallRoom).await()
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                }
            }
        }

        suspend fun updateCallRoom(userId: String, updatedCallRoom: CallRoom) {
            withContext(Dispatchers.IO) {
                try {
                    val callRoomDocument = db.collection("CallRoom").document(userId)
                    // Cập nhật thông báo trong Firestore
                    callRoomDocument.set(updatedCallRoom, SetOptions.merge()).await()
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                }
            }
        }

        suspend fun deleteCallRoom(userId: String) {
            withContext(Dispatchers.IO) {
                try {
                    val callRoomDocument = db.collection("CallRoom").document(userId)

                    // Xóa thông báo khỏi Firestore
                    callRoomDocument.delete().await()
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