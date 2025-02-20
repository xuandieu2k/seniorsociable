package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User.Companion.toMap
import vn.xdeuhug.seniorsociable.utils.AppUtils
import java.util.Date

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 04/09/2023
 */
class UserManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface UserCallback {
            fun onUserFound(user: User?)
            fun onFailure(exception: Exception)
        }

        interface ListenerDataChange {
            fun onAdded(user: User)
            fun onUpdated(user: User)
            fun onRemoved(user: User)
        }

        interface ListenerBlockChange {
            fun onBlock(isBlock: Boolean, user: User)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        //
        /**
         * @param lastVisible tài liệu cuối cùng của lần truy vấn
         * @param Triple(Boolean) true nếu đã đến cuối cùng
         */
        suspend fun getAllUserWithStatus(
            typeActive: Int,
            pageSize: Long,
            lastVisible: DocumentSnapshot? = null
        ): Triple<ArrayList<User>, DocumentSnapshot?, Boolean> = withContext(Dispatchers.IO) {
            return@withContext try {
//                db.clearPersistence()
                val userCollection = db.collection("User")
                    .whereEqualTo("roleAccount", AppConstants.ROLE_NORMAL)
                var query: Query
                query = if (typeActive != AppConstants.FILTER_ALL) {
                    userCollection
                        .whereEqualTo("typeActive", typeActive)
                        .orderBy("timeCreated", Query.Direction.DESCENDING)
                        .limit(pageSize)
                } else {
                    userCollection
                        .orderBy("timeCreated", Query.Direction.DESCENDING)
                        .limit(pageSize)
                }

                if (lastVisible != null) {
                    query = query.startAfter(lastVisible)
                }

                val querySnapshot = query.get().await()

                val userList = ArrayList<User>()
                userList.clear()

                for (document in querySnapshot.documents) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {

                        userList.add(user)
                    }
                }

                val newLastVisible =
                    if (!querySnapshot.isEmpty) querySnapshot.documents[querySnapshot.size() - 1] else null
                val isLastPage = newLastVisible == null || userList.size < pageSize
                Timber.tag("Log list user filter")
                    .i(GsonBuilder().setPrettyPrinting().create().toJson(userList))
                Triple(userList, newLastVisible, isLastPage)
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                Triple(ArrayList(), null, false)
            }
        }

        fun getAllUser(callback: FireStoreCallback<ArrayList<User>>) {
            val userCollection = db.collection("User")
            userCollection.get().addOnSuccessListener { querySnapshot ->
                val userList = ArrayList<User>()

                for (document in querySnapshot.documents) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }

                callback.onSuccess(userList)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun getAllUserChange(callback: FireStoreCallback<ArrayList<User>>) {
            val userCollection = db.collection("User")

            // Lắng nghe sự thay đổi trong bảng User
            userCollection.addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                    return@addSnapshotListener
                }

                val userList = ArrayList<User>()

                for (document in querySnapshot!!.documents) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }

                callback.onSuccess(userList)
            }
        }


        fun getUserById(userId: String, callback: UserCallback) {
            db.collection("User").whereEqualTo("id", userId).get()
                .addOnSuccessListener { result: QuerySnapshot ->
                    if (!result.isEmpty) {
                        val documentSnapshot = result.documents[0]
                        val userFound = documentSnapshot.toObject(User::class.java)
                        callback.onUserFound(userFound)
                    } else {
                        // Không tìm thấy người dùng, trả về null
                        callback.onUserFound(null)
                    }
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        suspend fun getListUserAsync(): ArrayList<User> {
            return withContext(Dispatchers.IO) {
                try {
                    val result = db.collection("User").get().await()

                    val users = ArrayList<User>()

                    for (document in result.documents) {
                        val user = document.toObject(User::class.java)
                        user?.let {
                            users.add(it)
                        }
                    }

                    return@withContext users
                } catch (exception: Exception) {
                    logExceptionUseTimber(exception)
                    throw exception
                }
            }
        }

        suspend fun listenerTypeActiveChange(
            listener: ListenerBlockChange
        ) {
            withContext(Dispatchers.IO) {
                try {
                    if (FirebaseAuth.getInstance().currentUser != null && UserCache.getUser().id != "") {
                        db.clearPersistence()
                        val userCollection =
                            db.collection("User").document(UserCache.getUser().id)
                        userCollection.addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                listener.onFailure(e)
                                return@addSnapshotListener
                            }
                            if (snapshot != null && snapshot.exists()) {
                                val user = snapshot.toObject(User::class.java)
                                if (user!!.typeActive != AppConstants.ACTIVATING) {
                                    listener.onBlock(true, user)
                                }
                            } else {
                                listener.onFailure(NullPointerException("Snapshot is null"))
                            }
                        }
                    }
                } catch (ex: Exception) {
                    listener.onFailure(ex)
                }
            }
        }

        suspend fun listenToUsersChanges(
            listener: ListenerDataChange
        ) {
            withContext(Dispatchers.IO) {
                try {
                    if (FirebaseAuth.getInstance().currentUser != null && UserCache.getUser().id != "") {
                        val userCollection =
                            db.collection("User").orderBy("timeCreated", Query.Direction.DESCENDING)

                        // Lắng nghe sự thay đổi của dữ liệu
                        userCollection.addSnapshotListener { querySnapshot, exception ->
                            if (exception != null) {
                                logExceptionUseTimber(exception)
                                return@addSnapshotListener
                            }

                            // Xử lý các sự kiện thay đổi
                            for (change in querySnapshot!!.documentChanges) {
                                val document = change.document.toObject(User::class.java)
                                if (document != null) {
                                    when (change.type) {
                                        DocumentChange.Type.ADDED -> listener.onAdded(document)
                                        DocumentChange.Type.MODIFIED -> listener.onUpdated(document)
                                        DocumentChange.Type.REMOVED -> listener.onRemoved(document)
                                    }
                                }
                            }
                        }
                    }
                } catch (exception: Exception) {
                    logExceptionUseTimber(exception)
                }
            }
        }

        suspend fun getUsersByUserIdsAsync(userIds: ArrayList<String>): ArrayList<User> {
            return withContext(Dispatchers.IO) {
                try {
                    if (userIds.isNotEmpty()) {
                        val result = db.collection("User").whereIn("id", userIds).get().await()

                        val users = ArrayList<User>()

                        for (document in result.documents) {
                            val user = document.toObject(User::class.java)
                            user?.let {
                                users.add(it)
                            }
                        }
                        return@withContext users
                    } else {
                        return@withContext ArrayList()
                    }
                } catch (exception: Exception) {
                    logExceptionUseTimber(exception)
                    throw exception
                }
            }
        }

        suspend fun getUserByIdAsync(userId: String): User? {
            return withContext(Dispatchers.IO) {
                try {
                    val result = db.collection("User").whereEqualTo("id", userId).get().await()

                    if (result.isEmpty) {
                        // Không tìm thấy người dùng, trả về null
                        return@withContext null
                    } else {
                        val documentSnapshot = result.documents[0]
                        return@withContext documentSnapshot.toObject(User::class.java)
                    }
                } catch (exception: Exception) {
                    logExceptionUseTimber(exception)
                    throw exception
                }
            }
        }

        fun getUserByPhone(phone: String, callback: UserCallback) {
            db.collection("User").whereEqualTo("phone", phone)
                .whereEqualTo("typeAccount", AppConstants.TYPE_PHONE).get()
                .addOnSuccessListener { result: QuerySnapshot ->
                    if (!result.isEmpty) {
                        val documentSnapshot = result.documents[0]
                        val userFound = documentSnapshot.toObject(User::class.java)
                        callback.onUserFound(userFound)
                    } else {
                        // Không tìm thấy người dùng, trả về null
                        callback.onUserFound(null)
                    }
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun addUser(user: User, callback: FireStoreCallback<Boolean>) {
            db.collection("User").document(user.id).set(user).addOnSuccessListener {
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        @SuppressLint("UncheckCast")
        fun updateUser(user: User, callback: FireStoreCallback<Unit>) {
            val userMap = user.toMap() // Sử dụng hàm toMap để chuyển đổi User thành Map

            db.collection("User").document(user.id).update(userMap).addOnSuccessListener {
                callback.onSuccess(Unit)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        @SuppressLint("UncheckCast")
        fun updateBlockUser(
            idUser: String,
            startDate: Date?,
            endDate: Date?,
            typeBlock: Int,
            reason: String,
            callback: FireStoreCallback<Boolean>
        ) {
            val map = HashMap<String, Any?>()
            map["typeActive"] = typeBlock
            map["reasonBlock"] = reason
            when (typeBlock) {
                AppConstants.BLOCKING -> { // Có thời hạn
                    map["timeEndBlock"] = endDate!!
                    map["timeStartBlock"] = startDate!!
                }

                AppConstants.BLOCKED_UN_LIMITED -> { // Có thời hạn
                    map["timeEndBlock"] = null
                    map["timeStartBlock"] = null
                }
            }

            db.collection("User").document(idUser).update(map).addOnSuccessListener {
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        @SuppressLint("UncheckCast")
        fun updateOpenUser(
            idUser: String,
            callback: FireStoreCallback<Boolean>
        ) {
            val map = HashMap<String, Any?>()
            map["typeActive"] = AppConstants.ACTIVATING
            map["reasonBlock"] = ""
            map["timeEndBlock"] = null
            map["timeStartBlock"] = null

            db.collection("User").document(idUser).update(map).addOnSuccessListener {
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        @SuppressLint("UncheckCast")
        fun updateUserOnline(
            idUser: String, online: Boolean, lastTimeOnline: Date, callback: FireStoreCallback<Unit>
        ) {
            if (FirebaseAuth.getInstance().currentUser != null && UserCache.getUser().id != "") {
                val map = HashMap<String, Any>()
                map["online"] = online
                map["lastTimeOnline"] = lastTimeOnline

                db.collection("User").document(idUser).update(map).addOnSuccessListener {
                    callback.onSuccess(Unit)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
            }
        }

        suspend fun updateUserOnlineAsyns(
            idUser: String, online: Boolean, lastTimeOnline: Date
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val map = HashMap<String, Any>()
                    map["online"] = online
                    map["lastTimeOnline"] = lastTimeOnline

                    db.collection("User").document(idUser).update(map).await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        @SuppressLint("UncheckCast")
        fun updatePasswordUser(user: User, callback: FireStoreCallback<Unit>) {
            db.collection("User").document(user.id).update("password", user.password)
                .addOnSuccessListener {
                    callback.onSuccess(Unit)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun deleteUser(userId: String, callback: FireStoreCallback<Unit>) {
            db.collection("User").document(userId).delete().addOnSuccessListener {
                callback.onSuccess(Unit)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

//        fun searchUsersByName(keyword: String, callback: FireStoreCallback<ArrayList<User>>) {
//            db.collection("User").get().addOnSuccessListener { querySnapshot ->
//                    val users = ArrayList<User>()
//                    for (document in querySnapshot.documents) {
//                        val user = document.toObject(User::class.java)
//                        user?.let {item ->
//                            val nameNormalize = item.nameNormalize
//                            val keywordNormalized = AppUtils.removeVietnameseFromStringNice(keyword)
//
//                            // Log để kiểm tra dữ liệu và từ khóa tìm kiếm
//                            Timber.tag("SearchLog").i("NameNormalize: $nameNormalize, Keyword: $keywordNormalized")
//
//                            val words = nameNormalize.split("\\s+".toRegex())
//                            Timber.tag("Log Data").i(GsonBuilder().setPrettyPrinting().create().toJson(words))
//
//                            if (words.any { it.contains(keywordNormalized) }) {
//                                AppUtils.logJsonFromObject(item)
//                                users.add(item)
//                                Timber.tag("SearchLog Result has data").i("NameNormalize: $nameNormalize, Keyword: $keywordNormalized")
//                            }else{
//                                Timber.tag("SearchLog Result NO data").i("")
//                            }
//                        }
//
//                    }
//
//                    callback.onSuccess(users)
//                }.addOnFailureListener { exception ->
//                    logExceptionUseTimber(exception)
//                    callback.onFailure(exception)
//                }
//        }

        fun searchUsersByName(keyword: String, callback: FireStoreCallback<ArrayList<User>>) {
            try {
                val users = ArrayList<User>()

                ListUserCache.getList().forEach { user ->
                    val nameNormalize = user.nameNormalize
                    val keywordNormalized = AppUtils.removeVietnameseFromStringNice(keyword)
                    val words = nameNormalize.split("\\s+".toRegex())
                    if (words.any { it.contains(keywordNormalized) }) {
                        AppUtils.logJsonFromObject(user)
                        users.add(user)
                    } else {
                        Timber.tag("SearchLog Result NO data").i("")
                    }
                }

                callback.onSuccess(users)
            } catch (ex: Exception) {
                logExceptionUseTimber(ex)
                callback.onFailure(ex)
            }
        }


        fun updateDescriptionUser(user: User, callback: FireStoreCallback<Unit>) {
            db.collection("User").document(user.id).update("description", user.description)
                .addOnSuccessListener {
                    callback.onSuccess(Unit)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun updateHobbyUser(user: User, callback: FireStoreCallback<Unit>) {
            db.collection("User").document(user.id).update("hobbies", user.hobbies)
                .addOnSuccessListener {
                    callback.onSuccess(Unit)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun updateAvatarUser(user: User, callback: FireStoreCallback<Unit>) {
            db.collection("User").document(user.id).update("avatar", user.avatar)
                .addOnSuccessListener {
                    callback.onSuccess(Unit)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun updateAddressUser(user: User, callback: FireStoreCallback<Unit>) {
            db.collection("User").document(user.id).update("address", user.address)
                .addOnSuccessListener {
                    callback.onSuccess(Unit)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun updateBackgroundUser(user: User, callback: FireStoreCallback<Unit>) {
            db.collection("User").document(user.id).update("background", user.background)
                .addOnSuccessListener {
                    callback.onSuccess(Unit)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun updateUserInfo(user: User, callback: FireStoreCallback<Unit>) {
            val updateData = hashMapOf(
                "name" to user.name,
                "nameNormalize" to user.nameNormalize,
                "address" to user.address,
                "birthday" to user.birthday,
                "workAt" to user.workAt,
                "maritalStatus" to user.maritalStatus
            )

            db.collection("User").document(user.id).update(updateData).addOnSuccessListener {
                callback.onSuccess(Unit)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }


        private fun logExceptionUseTimber(exception: Exception) {
            Timber.tag("Exception FireStore").d(exception)
        }
    }
}