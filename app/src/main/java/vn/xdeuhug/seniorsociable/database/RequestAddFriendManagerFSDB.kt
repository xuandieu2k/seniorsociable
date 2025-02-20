package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.model.entity.modelFriend.RequestAddFriend

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 10 / 11 / 2023
 */
class RequestAddFriendManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface RequestAddFriendCallback {
            fun onRequestAddFriendFound(request: RequestAddFriend?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun getRequestAddFriendById(requestId: String, callback: RequestAddFriendCallback) {
            val requestCollection =
                db.collection("RequestAddFriend").document(UserCache.getUser().id)
                    .collection("UsersRequestAddFriend")
            requestCollection.whereEqualTo("id", requestId).get()
                .addOnSuccessListener { result: QuerySnapshot ->
                    if (!result.isEmpty) {
                        val documentSnapshot = result.documents[0]
                        val requestFound = documentSnapshot.toObject(RequestAddFriend::class.java)
                        callback.onRequestAddFriendFound(requestFound)
                    } else {
                        // Không tìm thấy , trả về null
                        callback.onRequestAddFriendFound(null)
                    }
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun getAllRequestAddFriendByUserId(
            userId: String, callback: FireStoreCallback<ArrayList<RequestAddFriend>>
        ) {
            val requestCollection = db.collection("RequestAddFriend").document(userId)
                .collection("UsersRequestAddFriend")
            requestCollection.get().addOnSuccessListener { querySnapshot ->
                val requestList = ArrayList<RequestAddFriend>()

                for (document in querySnapshot.documents) {
                    val request = document.toObject(RequestAddFriend::class.java)
                    if (request != null) {
                        requestList.add(request)
                    }
                }

                callback.onSuccess(requestList)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        suspend fun getAllRequestAddFriendByUserIdAsync(userId: String): ArrayList<RequestAddFriend> {
            return withContext(Dispatchers.IO) {
                try {
                    val requestCollection = db.collection("RequestAddFriend").document(userId)
                        .collection("UsersRequestAddFriend")
                    val querySnapshot = requestCollection.get().await()
                    val requestList = ArrayList<RequestAddFriend>()

                    for (document in querySnapshot.documents) {
                        val request = document.toObject(RequestAddFriend::class.java)
                        if (request != null) {
                            requestList.add(request)
                        }
                    }

                    requestList
                } catch (exception: Exception) {
                    logExceptionUseTimber(exception)
                    ArrayList()
                }
            }
        }

        suspend fun checkIdIsSendingRequestAddFriend(idNodeRoot: String, idCheck: String): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val requestCollection = db.collection("RequestAddFriend").document(idNodeRoot)
                        .collection("UsersRequestAddFriend").document(idCheck)
                    val querySnapshot = requestCollection.get().await()
                    val request = querySnapshot.toObject(RequestAddFriend::class.java)
                    request != null
                } catch (exception: Exception) {
                    logExceptionUseTimber(exception)
                    false
                }
            }
        }

        fun registerListenerChangeDataRequest(
            userIdListener: String, userIdNeedToCheck: String, callback: (Boolean) -> Unit
        ) {
            val requestCollection = db.collection("RequestAddFriend").document(userIdListener)
                .collection("UsersRequestAddFriend")

            requestCollection.addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null) {
                    for (document in querySnapshot.documents) {
                        val request = document.toObject(RequestAddFriend::class.java)
                        if (request?.id == userIdNeedToCheck) {
                            callback(true)
                            return@addSnapshotListener
                        }
                    }
                    callback(false)
                } else {
                    // Xử lý lỗi hoặc trường hợp khác
                    logExceptionUseTimber(Exception("Query snapshot is null"))
                }
            }
        }

        fun registerListenerNodeChangeById(
            userIdListener: String, callback: (ArrayList<RequestAddFriend>) -> Unit
        ) {
            val requestCollection = db.collection("RequestAddFriend").document(userIdListener)
                .collection("UsersRequestAddFriend").orderBy("timeCreated")

            requestCollection.addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null) {
                    val list = ArrayList<RequestAddFriend>()
                    for (document in querySnapshot.documents) {
                        val request = document.toObject(RequestAddFriend::class.java)
                        request?.let {
                            list.add(request)
                        }
                    }
                    callback(list)
                } else {
                    // Xử lý lỗi hoặc trường hợp khác
                    logExceptionUseTimber(Exception("Query snapshot is null"))
                }
            }
        }


        /**
         * @param lastVisible tài liệu cuối cùng của lần truy vấn
         */
        suspend fun getAllRequestAddFriendByUserIdAsync(
            userId: String, pageSize: Long, lastVisible: DocumentSnapshot? = null
        ): Pair<ArrayList<RequestAddFriend>, DocumentSnapshot?> {
            return try {
                val requestCollection = db.collection("RequestAddFriend").document(userId)
                    .collection("UsersRequestAddFriend")

                var query = requestCollection.orderBy("timeCreated", Query.Direction.DESCENDING)
                    .limit(pageSize)

                if (lastVisible != null) {
                    query = query.startAfter(lastVisible)
                }

                val querySnapshot = query.get().await()

                val requestList = ArrayList<RequestAddFriend>()

                for (document in querySnapshot.documents) {
                    val request = document.toObject(RequestAddFriend::class.java)
                    if (request != null) {
                        requestList.add(request)
                    }
                }

                val newLastVisible =
                    if (!querySnapshot.isEmpty) querySnapshot.documents[querySnapshot.size() - 1] else null

                Pair(requestList, newLastVisible)
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                Pair(ArrayList(), null)
            }
        }


        /**
         * @param idUserIsAdded Là id của user được người dùng hiện tại gửi yêu cầu
         * @param RequestAddFriend là object có chứa id là id của người dùng hiện tại(người dùng gửi yêu cầu)
         */
        fun addRequestAddFriend(
            idUserIsAdded: String, request: RequestAddFriend, callback: FireStoreCallback<Boolean>
        ) {
            val requestCollection = db.collection("RequestAddFriend").document(idUserIsAdded)
                .collection("UsersRequestAddFriend")
            requestCollection.document(request.id).set(request).addOnSuccessListener { _ ->
                // Dữ liệu album đã được thêm
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun updateRequestAddFriend(
            requestId: String,
            updatedRequest: RequestAddFriend,
            callback: FireStoreCallback<Boolean>
        ) {
            val requestCollection =
                db.collection("RequestAddFriend").document(UserCache.getUser().id)
                    .collection("UsersRequestAddFriend")
            requestCollection.document(requestId).set(updatedRequest).addOnSuccessListener {
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        suspend fun deleteRequestAddFriendAsync(idRequest: String): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val requestCollection =
                        db.collection("RequestAddFriend").document(UserCache.getUser().id)
                            .collection("UsersRequestAddFriend")
                    requestCollection.document(idRequest).delete().await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }

        }

        suspend fun deleteRequestAddFriendFromYouAsync(idRequest: String): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val requestCollection = db.collection("RequestAddFriend").document(idRequest)
                        .collection("UsersRequestAddFriend")
                    requestCollection.document(UserCache.getUser().id).delete().await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }

        }


        private fun logExceptionUseTimber(exception: Exception) {
            Timber.tag("Exception FireStore").d(exception)
        }
    }
}