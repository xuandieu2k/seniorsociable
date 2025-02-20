package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 02 / 11 / 2023
 */
class CommentManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface CommentCallback {
            fun onCommentFound(post: Post?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun updateCommentInPost(
            postId: String, commentToAdd: Comment, callback: FireStoreCallback<Boolean>
        ) {
            val postCollection = db.collection("Post").document(postId)

            postCollection.update("comments", FieldValue.arrayUnion(commentToAdd))
                .addOnSuccessListener {
                    callback.onSuccess(true)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun deleteCommentInPost(
            postId: String, commentToRemove: Interact, callback: FireStoreCallback<Boolean>
        ) {
            val postCollection = db.collection("Post").document(postId)

            postCollection.update("comments", FieldValue.arrayRemove(commentToRemove))
                .addOnSuccessListener {
                    callback.onSuccess(true)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        suspend fun updateCommentInPostAsync(postId: String, commentToAdd: Interact): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postCollection = db.collection("Post").document(postId)
                    postCollection.update("comments", FieldValue.arrayUnion(commentToAdd)).await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun deleteCommentInPostAsync(postId: String, commentToRemove: Interact): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postCollection = db.collection("Post").document(postId)
                    postCollection.update("comments", FieldValue.arrayRemove(commentToRemove))
                        .await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }


        fun addCommentToPost(
            postId: String, commentToAdd: Comment, callback: FireStoreCallback<Boolean>
        ) {
            val postCommentsCollection =
                db.collection("Post").document(postId).collection("Comments")

            postCommentsCollection.document(commentToAdd.id).set(commentToAdd)
                .addOnCompleteListener {
                    callback.onSuccess(true)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }


        suspend fun addCommentToPost(postId: String, commentToAdd: Comment): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postCommentsCollection =
                        db.collection("Post").document(postId).collection("Comments")

                    postCommentsCollection.document(commentToAdd.id).set(commentToAdd).await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun deleteCommentInPost(postId: String, commentId: String): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postCommentsCollection =
                        db.collection("Post").document(postId).collection("Comments")

                    val commentDocument = postCommentsCollection.document(commentId)
                    commentDocument.delete().await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        // #### Interact Trong COMMENT POST #####

        suspend fun addInteractToCommentInPost(
            postId: String, commentId: String, interactToAdd: Interact
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Comments")
                            .document(commentId).collection("Interacts")
                    postInteractsCollection.document(interactToAdd.id).set(interactToAdd).await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun updateInteractToCommentInPost(
            postId: String, commentId: String, interactToUpdate: Interact
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Comments")
                            .document(commentId).collection("Interacts")
                    postInteractsCollection.document(interactToUpdate.id)
                        .update(interactToUpdate.toMap())
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun deleteInteractToCommentInPost(
            postId: String,
            commentId: String,
            interactId: String
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Comments")
                            .document(commentId).collection("Interacts")
                    val interactDocument = postInteractsCollection.document(interactId)
                    interactDocument.delete().await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        // #### Interact Trong CHILD COMMENT Của COMMENT PẢENT POST #####

        suspend fun addInteractToChildCommentInPost(
            postId: String, commentId: String, commentChildId: String, interactToAdd: Interact
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Comments")
                            .document(commentId).collection("Comments")
                            .document(commentChildId).collection("Interacts")
                    postInteractsCollection.document(interactToAdd.id).set(interactToAdd).await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun updateInteractToChildCommentInPost(
            postId: String, commentId: String, commentChildId: String, interactToUpdate: Interact
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Comments")
                            .document(commentId).collection("Comments")
                            .document(commentChildId).collection("Interacts")
                    postInteractsCollection.document(interactToUpdate.id)
                        .update(interactToUpdate.toMap())
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun deleteInteractToChildCommentInPost(
            postId: String,
            commentId: String,
            commentChildId: String,
            interactId: String
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Comments")
                            .document(commentId).collection("Comments")
                            .document(commentChildId).collection("Interacts")
                    val interactDocument = postInteractsCollection.document(interactId)
                    interactDocument.delete().await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        // #### Comment Trong COMMENT POST #####

        fun addCommentToCommentInPost(
            postId: String,
            commentParentId: String,
            commentToAdd: Comment,
            callback: FireStoreCallback<Boolean>
        ) {
            val postInteractsCollection =
                db.collection("Post").document(postId).collection("Comments")
                    .document(commentParentId).collection("Comments")
            postInteractsCollection.document(commentToAdd.id).set(commentToAdd)
                .addOnCompleteListener {
                    callback.onSuccess(true)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        suspend fun addCommentToCommentInPost(
            postId: String, commentParentId: String, commentToAdd: Comment
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Comments")
                            .document(commentParentId).collection("Comments")
                    postInteractsCollection.document(commentToAdd.id).set(commentToAdd).await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun updateCommentToCommentInPost(
            postId: String, commentParentId: String, commentUpdate: Comment
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Comments")
                            .document(commentParentId).collection("Interacts")
                    postInteractsCollection.document(commentUpdate.id).update(commentUpdate.toMap())
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun deleteCommentToCommentInPost(
            postId: String,
            commentParentId: String,
            commentChildId: String
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val postInteractsCollection =
                        db.collection("Post").document(postId).collection("Comments")
                            .document(commentParentId).collection("Comments")
                    val interactDocument = postInteractsCollection.document(commentChildId)
                    interactDocument.delete().await()
                    true
                } catch (e: Exception) {
                    logExceptionUseTimber(e)
                    false
                }
            }
        }

        suspend fun getAllCommentByIdPost(
            idPost: String,
            limit: Long,
            lastVisible: DocumentSnapshot?
        ): Triple<ArrayList<Comment>, DocumentSnapshot?, Boolean> = withContext(Dispatchers.IO) {
            return@withContext try {
                val commentCollection = db.collection("Post")
                    .document(idPost).collection("Comments")
                    .orderBy("timeCreated", Query.Direction.DESCENDING)
                    .limit(limit)

                // Thêm điều kiện startAfter nếu lastVisible không null
                if (lastVisible != null) {
                    commentCollection.startAfter(lastVisible)
                }

                val querySnapshot = commentCollection.get().await()

                val commentList = ArrayList<Comment>()

                for (document in querySnapshot.documents) {
                    val comment = document.toObject(Comment::class.java)
                    if (comment != null) {
                        // Lấy Interacts Con
                        val interactsSnapshot =
                            document.reference.collection("Interacts").get().await()
                        val interacts = ArrayList(interactsSnapshot.documents.mapNotNull {
                            it.toObject(Interact::class.java)
                        })

                        // Gán Interacts và Comments vào Post
                        comment.interacts = interacts

                        // Lấy Comments Con
                        val commentsSnapshot =
                            document.reference.collection("Comments").get().await()
//                        val comments = ArrayList(commentsSnapshot.documents.mapNotNull {
//                            it.toObject(Comment::class.java)
//                        })

                        // // Lấy Interacts Con trong comment con
                        for (documentChildComment in commentsSnapshot.documents) {
                            val commentChild = documentChildComment.toObject(Comment::class.java)
                            if(commentChild != null)
                            {
                                // Lấy Interacts Con
                                val interactsChildSnapshot =
                                    documentChildComment.reference.collection("Interacts").get().await()
                                val interactsChild = ArrayList(interactsChildSnapshot.documents.mapNotNull {
                                    it.toObject(Interact::class.java)
                                })
                                commentChild.interacts = interactsChild
                                comment.comments.add(commentChild)
                            }
                        }

                        commentList.add(comment)
                    }
                }

                val newLastVisible =
                    if (!querySnapshot.isEmpty) querySnapshot.documents[querySnapshot.size() - 1] else null
                var isLastPage = newLastVisible == null || commentList.size < limit
//                if(postList.size == limit.toInt())
//                {
//                    val postCollectionNew = db.collection("Post")
//                        .whereEqualTo("idUserPost", idUser)
//                        .orderBy("timeCreated", Query.Direction.DESCENDING)
//                        .startAfter(newLastVisible)
//                        .limit(limit)
//                    val querySnapshotCheck = postCollectionNew.get().await()
//                    Timber.tag("Log xem").i("${querySnapshotCheck.documents.isEmpty()} ${querySnapshotCheck.documents}")
//                    isLastPage = querySnapshotCheck.documents.isEmpty()
//                }
                Triple(commentList, newLastVisible, isLastPage)
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                Triple(ArrayList(), null, false)
            }
        }


        private fun logExceptionUseTimber(exception: Exception) {
            Timber.tag("Exception FireStore").d(exception)
        }
    }
}