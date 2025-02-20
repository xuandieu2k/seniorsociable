package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 28 / 10 / 2023
 */
class InteractManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface InteractCallback {
            fun onInteractFound(post: Post?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun updateInteractInPost(
            postId: String, interactToAdd: Interact, callback: FireStoreCallback<Boolean>
        ) {
            val postCollection = db.collection("Post").document(postId)

            postCollection.update("interacts", FieldValue.arrayUnion(interactToAdd))
                .addOnSuccessListener {
                    callback.onSuccess(true)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun deleteInteractInPost(
            postId: String, interactToRemove: Interact, callback: FireStoreCallback<Boolean>
        ) {
            val postCollection = db.collection("Post").document(postId)

            postCollection.update("interacts", FieldValue.arrayRemove(interactToRemove))
                .addOnSuccessListener {
                    callback.onSuccess(true)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }


        suspend fun updateInteractInPostAsync(postId: String, interactToAdd: Interact): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postCollection = db.collection("Post").document(postId)

                    postCollection.update("interacts", FieldValue.arrayUnion(interactToAdd)).await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun deleteInteractInPostAsync(postId: String, interactToRemove: Interact): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postCollection = db.collection("Post").document(postId)
                    postCollection.update("interacts", FieldValue.arrayRemove(interactToRemove))
                        .await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        // #### Trong POST #####

        suspend fun addInteractToPost(postId: String, interactToAdd: Interact): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Interacts")
                    postInteractsCollection.document(interactToAdd.id).set(interactToAdd).await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun updateInteractToPost(
            postId: String, interactId: String, interactToUpdate: Interact
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Interacts")
                    postInteractsCollection.document(interactId).set(interactToUpdate)
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun deleteInteractInPost(postId: String, interactId: String): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Interacts")

                    val interactDocument = postInteractsCollection.document(interactId)
                    interactDocument.delete().await()
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