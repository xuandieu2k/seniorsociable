package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.model.entity.modelFriend.Friend
import vn.xdeuhug.seniorsociable.model.entity.modelFriend.RequestAddFriend
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.toArrayList
import java.util.UUID

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 20 / 10 / 2023
 */
class FriendManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface FriendCallback {
            fun onFriendFound(user: User?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun getFriendByUserId(userId: String, callback: FriendCallback) {
            val userCollection =
                db.collection("Friend").document(UserCache.getUser().id).collection("UserFriends")
            userCollection.whereEqualTo("id", userId).get()
                .addOnSuccessListener { result: QuerySnapshot ->
                    if (!result.isEmpty) {
                        val documentSnapshot = result.documents[0]
                        val userFound = documentSnapshot.toObject(User::class.java)
                        callback.onFriendFound(userFound)
                    } else {
                        // Không tìm thấy , trả về null
                        callback.onFriendFound(null)
                    }
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        /**
         * @param userId là id cần kiểm tra trong danh sách bạn bè
         */
        suspend fun getFriendByUserIdAsync(userId: String): User? {
            return try {
                val userCollection = db.collection("Friend").document(UserCache.getUser().id)
                    .collection("UserFriends")
                val result = userCollection.whereEqualTo("id", userId).get().await()

                if (!result.isEmpty) {
                    val documentSnapshot = result.documents[0]
                    val friend = documentSnapshot?.toObject(Friend::class.java)
                    val user = friend?.id?.let { UserManagerFSDB.getUserByIdAsync(it) }
                    AppUtils.logJsonFromObject(user!!)
                    user
                } else {
                    null
                }
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                null
            }
        }

        fun registerListenerChangeDataFriend(
            userIdListener: String, userIdNeedToCheck: String, callback: (Boolean) -> Unit
        ) {
            val userCollection = db.collection("Friend").document(userIdListener)
                .collection("UserFriends")

            userCollection.addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null) {
                    for (document in querySnapshot.documents) {
                        val request = document.toObject(Friend::class.java)
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

        fun getAllFriends(callback: FireStoreCallback<ArrayList<User>>) {
            val userCollection =
                db.collection("Friend").document(UserCache.getUser().id).collection("UserFriends")
            userCollection.get().addOnSuccessListener { querySnapshot ->
                runBlocking {
                    val userList: ArrayList<User>
                    val listIds = ArrayList<String>()
                    for (document in querySnapshot.documents) {
                        val user = document.toObject(Friend::class.java)
                        if (user != null) {
                            listIds.add(user.id)
                        }
                    }
                    userList = UserManagerFSDB.getUsersByUserIdsAsync(listIds)
                    callback.onSuccess(userList)
                }
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun getAllFriendsByIdUser(idUser: String, callback: FireStoreCallback<ArrayList<User>>) {
            val userCollection =
                db.collection("Friend").document(idUser).collection("UserFriends")
            userCollection.get().addOnSuccessListener { querySnapshot ->
                runBlocking {
                    val userList: ArrayList<User>
                    val listIds = ArrayList<String>()
                    for (document in querySnapshot.documents) {
                        val user = document.toObject(Friend::class.java)
                        if (user != null) {
                            listIds.add(user.id)
                        }
                    }
                    userList = ListUserCache.getList().filter { it.id in listIds }.toArrayList()
                    callback.onSuccess(userList)
                }
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun addFriend(user: User, callback: FireStoreCallback<Boolean>) {
            val userCollection =
                db.collection("Friend").document(UserCache.getUser().id).collection("UserFriends")
            userCollection.document(user.id).set(user).addOnSuccessListener { _ ->
                // Dữ liệu album đã được thêm
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        suspend fun addFriendAsync(friend: Friend, idFriendIsAdded: String): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val userCollection = db.collection("Friend").document(idFriendIsAdded)
                        .collection("UserFriends")
                    userCollection.document(friend.id).set(friend).await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun deleteFriendAsync(idFriend: String): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val userCollection = db.collection("Friend").document(UserCache.getUser().id)
                        .collection("UserFriends")
                    userCollection.document(idFriend).delete().await()
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