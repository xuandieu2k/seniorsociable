package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 04/09/2023
 */
class FireStoreManager {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface UserCallback {
            fun onUserFound(user: User?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun getUserById(userId: String, callback: UserCallback) {
            db.collection("User")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener { result: QuerySnapshot ->
                    if (!result.isEmpty) {
                        val documentSnapshot = result.documents[0]
                        val userFound = documentSnapshot.toObject(User::class.java)
                        callback.onUserFound(userFound)
                    } else {
                        // Không tìm thấy người dùng, trả về null
                        callback.onUserFound(null)
                    }
                }
                .addOnFailureListener { exception ->
                    Timber.tag("Exception FireStore").d(exception)
                    callback.onFailure(exception)
                }
        }

        suspend fun getUserById(userId: String): User? = try {
            val result = db.collection("User").whereEqualTo("id", userId).get().await()
            if (!result.isEmpty) {
                val documentSnapshot = result.documents[0]
                documentSnapshot.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.tag("Exception FireStore").d(e)
            null
        }

        fun addUser(user: User, callback: FireStoreCallback<Boolean>) {
            val userMap = hashMapOf(
                user to User::class.java
            )
            db.collection("User")
                .document(user.id)
                .set(userMap)
                .addOnSuccessListener {
                    callback.onSuccess(true)
                }
                .addOnFailureListener { exception ->
                    Timber.tag("Exception Firestore").d(exception)
                    callback.onFailure(exception)
                }
        }
    }
}