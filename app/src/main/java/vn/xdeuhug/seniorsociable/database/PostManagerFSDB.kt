package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post.Companion.toMap
import vn.xdeuhug.seniorsociable.model.entity.modelReport.Report
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.StringUtils
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 20 / 10 / 2023
 */
class PostManagerFSDB {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface PostCallback {
            fun onPostFound(post: Post?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        suspend fun getAllPost(
            pageSize: Long,
            lastVisible: DocumentSnapshot? = null,
            callback: FireStoreCallback<Triple<ArrayList<Post>, DocumentSnapshot?, Boolean>>
        ) {
            try {
                val postCollection = db.collection("Post")

                var query = postCollection.orderBy("timeCreated", Query.Direction.DESCENDING)
                    .limit(pageSize)

                if (lastVisible != null) {
                    query = query.startAfter(lastVisible)
                }

                val querySnapshot = query.get().await()

                val postList = ArrayList<Post>()
                postList.clear()

                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        // Lấy Interacts
                        val interactsSnapshot =
                            document.reference.collection("Interacts").get().await()
                        val interacts = ArrayList(interactsSnapshot.documents.mapNotNull {
                            it.toObject(Interact::class.java)
                        })

                        // Lấy Comments
                        val commentsSnapshot =
                            document.reference.collection("Comments").get().await()
                        val comments = ArrayList(commentsSnapshot.documents.mapNotNull {
                            it.toObject(Comment::class.java)
                        })

                        // Gán Interacts và Comments vào Post
                        post.interacts = interacts
                        post.comments = comments

                        postList.add(post)
                    }
                }

                val newLastVisible =
                    if (!querySnapshot.isEmpty) querySnapshot.documents[querySnapshot.size() - 1] else null
                val isLastPage = newLastVisible == null || postList.size < pageSize
                callback.onSuccess(Triple(postList, newLastVisible, isLastPage))
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun getAllPost(callback: FireStoreCallback<ArrayList<Post>>) {
            val postCollection = db.collection("Post")
            postCollection.get().addOnSuccessListener { querySnapshot ->
                val postList = ArrayList<Post>()

                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        postList.add(post)
                    }
                }

                callback.onSuccess(postList)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun updateMultiMedia(
            idPost: String, listMedia: ArrayList<MultiMedia>, callback: FireStoreCallback<Unit>
        ) {
            db.collection("Post").document(idPost).update("multiMedia", listMedia)
                .addOnSuccessListener {
                    callback.onSuccess(Unit)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        suspend fun getPostCount(): Int = withContext(Dispatchers.IO) {
            try {
                val postCollection = db.collection("Post")
                val querySnapshot = postCollection.get().await()
                return@withContext querySnapshot.size()
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                return@withContext 0
            }
        }


        suspend fun getFirstPost(): DocumentSnapshot? {
            val postCollection = db.collection("Post")

            return try {
                val querySnapshot =
                    postCollection.orderBy("timeCreated", Query.Direction.ASCENDING).limit(1).get()
                        .await()

                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    document
                } else {
                    null
                }
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                null
            }
        }


        /**
         * @param lastVisible tài liệu cuối cùng của lần truy vấn
         * @param Triple(Boolean) true nếu đã đến cuối cùng
         */
        suspend fun getAllPost(
            pageSize: Long, lastVisible: DocumentSnapshot? = null
        ): Triple<ArrayList<Post>, DocumentSnapshot?, Boolean> = withContext(Dispatchers.IO) {
            return@withContext try {
                val postCollection = db.collection("Post")
                    .whereEqualTo("typePost",PostConstants.POST_DEFAULT)
                    .whereEqualTo("status", PostConstants.IS_PASSED)
                var query = postCollection.orderBy("timeCreated", Query.Direction.DESCENDING)
                    .limit(pageSize)

                if (lastVisible != null) {
                    query = query.startAfter(lastVisible)
                }

                val querySnapshot = query.get().await()

                val postList = ArrayList<Post>()
                postList.clear()

                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        // Lấy Interacts
                        val interactsSnapshot =
                            document.reference.collection("Interacts").get().await()
                        val interacts = ArrayList(interactsSnapshot.documents.mapNotNull {
                            it.toObject(Interact::class.java)
                        })

                        // Lấy Comments
                        val commentsSnapshot =
                            document.reference.collection("Comments").get().await()
                        val comments = ArrayList(commentsSnapshot.documents.mapNotNull {
                            it.toObject(Comment::class.java)
                        })

                        // Gán Interacts và Comments vào Post
                        post.interacts = interacts
                        post.comments = comments

                        postList.add(post)
                    }
                }

                val newLastVisible =
                    if (!querySnapshot.isEmpty) querySnapshot.documents[querySnapshot.size() - 1] else null
                val isLastPage = newLastVisible == null || postList.size < pageSize

                Triple(postList, newLastVisible, isLastPage)
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                Triple(ArrayList(), null, false)
            }
        }

        //
        /**
         * @param lastVisible tài liệu cuối cùng của lần truy vấn
         * @param Triple(Boolean) true nếu đã đến cuối cùng
         */
        suspend fun getAllPostWithStatus(
            status: Int,
            keyword: String,
            fromDate: Date,
            toDate: Date,
            pageSize: Long,
            lastVisible: DocumentSnapshot? = null
        ): Triple<ArrayList<Post>, DocumentSnapshot?, Boolean> = withContext(Dispatchers.IO) {
            val fromDateNew = DateUtils.atStartOfDay(fromDate)
            val toDateNew = DateUtils.atEndOfDay(toDate)
            return@withContext try {
                val postCollection = db.collection("Post")
                var query: Query
                query = if (status != PostConstants.FILTER_ALL) {
                    postCollection
                        .whereEqualTo("status", status)
                        .whereGreaterThanOrEqualTo("timeCreated", fromDateNew)
                        .whereLessThanOrEqualTo("timeCreated", toDateNew)
                        .orderBy("timeCreated", Query.Direction.DESCENDING)
                        .limit(pageSize)
                } else {
                    postCollection.whereGreaterThanOrEqualTo("timeCreated", fromDateNew)
                        .whereLessThanOrEqualTo("timeCreated", toDateNew)
                        .orderBy("timeCreated", Query.Direction.DESCENDING)
                        .limit(pageSize)
                }

                if (keyword.isNotEmpty()) {
                    query = query.whereArrayContainsAny("contentsNormalize", StringUtils.splitStringIntoWords(keyword))
                }

                if (lastVisible != null) {
                    query = query.startAfter(lastVisible)
                }

                val querySnapshot = query.get().await()

                val postList = ArrayList<Post>()
                postList.clear()

                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        postList.add(post)
                    }
                }

                val newLastVisible =
                    if (!querySnapshot.isEmpty) querySnapshot.documents[querySnapshot.size() - 1] else null
                val isLastPage = newLastVisible == null || postList.size < pageSize

                Triple(postList, newLastVisible, isLastPage)
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                Triple(ArrayList(), null, false)
            }
        }

        //
        //
        /**
         * @param lastVisible tài liệu cuối cùng của lần truy vấn
         * @param Triple(Boolean) true nếu đã đến cuối cùng
         */
        suspend fun getAllPostRPWithStatus(
            status: Int,
            keyword: String,
            fromDate: Date,
            toDate: Date,
            pageSize: Long,
            lastVisible: DocumentSnapshot? = null
        ): Triple<ArrayList<Post>, DocumentSnapshot?, Boolean> = withContext(Dispatchers.IO) {
            val fromDateNew = DateUtils.atStartOfDay(fromDate)
            val toDateNew = DateUtils.atEndOfDay(toDate)
            return@withContext try {
                val postCollection = db.collection("Post")
                var query: Query
                query = if (status != PostConstants.FILTER_ALL) {
                    postCollection
                        .whereEqualTo("status", status)
                        .whereGreaterThanOrEqualTo("timeCreated", fromDateNew)
                        .whereLessThanOrEqualTo("timeCreated", toDateNew)
                        .orderBy("timeCreated", Query.Direction.DESCENDING)
                        .limit(pageSize)
                } else {
                    postCollection.whereGreaterThanOrEqualTo("timeCreated", fromDateNew)
                        .whereLessThanOrEqualTo("timeCreated", toDateNew)
                        .orderBy("timeCreated", Query.Direction.DESCENDING)
                        .limit(pageSize)
                }

                if (keyword.isNotEmpty()) {
                    query = query.whereArrayContainsAny("contentsNormalize", StringUtils.splitStringIntoWords(keyword))
                }

                if (lastVisible != null) {
                    query = query.startAfter(lastVisible)
                }

                val querySnapshot = query.get().await()

                val postList = ArrayList<Post>()
                postList.clear()

                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        val reports = async { ReportManagerFSDB.getAllReportByIdPosts(post.id) }.await()
                        post.reports.addAll(reports)
                        postList.add(post)
                    }
                }
                AppUtils.logJsonFromObject(postList)

                val newLastVisible =
                    if (!querySnapshot.isEmpty) querySnapshot.documents[querySnapshot.size() - 1] else null
                val isLastPage = newLastVisible == null || postList.size < pageSize

                Triple(postList, newLastVisible, isLastPage)
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                Triple(ArrayList(), null, false)
            }
        }

        suspend fun getInteracts(postRef: DocumentReference): List<Interact> {
            return try {
                val interactsSnapshot = postRef.collection("Interacts").get().await()
                interactsSnapshot.documents.mapNotNull { it.toObject(Interact::class.java) }
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                emptyList()
            }
        }

        suspend fun getComments(postRef: DocumentReference): List<Comment> {
            return try {
                val commentsSnapshot = postRef.collection("Comments").get().await()
                commentsSnapshot.documents.mapNotNull { it.toObject(Comment::class.java) }
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                emptyList()
            }
        }

        suspend fun getAllPostByIdUser(
            idUser: String,
            limit: Long,
            lastVisible: DocumentSnapshot?
        ): Triple<ArrayList<Post>, DocumentSnapshot?, Boolean> = withContext(Dispatchers.IO) {
            return@withContext try {
                val postCollection = db.collection("Post")
                    .whereEqualTo("idUserPost", idUser)
                    .whereEqualTo("status", PostConstants.IS_PASSED)
                    .orderBy("timeCreated", Query.Direction.DESCENDING)
                    .limit(limit)

                // Thêm điều kiện startAfter nếu lastVisible không null
                if (lastVisible != null) {
                    postCollection.startAfter(lastVisible)
                }

                val querySnapshot = postCollection.get().await()

                val postList = ArrayList<Post>()

                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        // Lấy Interacts
                        val interactsSnapshot =
                            document.reference.collection("Interacts").get().await()
                        val interacts = ArrayList(interactsSnapshot.documents.mapNotNull {
                            it.toObject(Interact::class.java)
                        })

                        // Lấy Comments
                        val commentsSnapshot =
                            document.reference.collection("Comments").get().await()
                        val comments = ArrayList(commentsSnapshot.documents.mapNotNull {
                            it.toObject(Comment::class.java)
                        })

                        // Gán Interacts và Comments vào Post
                        post.interacts = interacts
                        post.comments = comments

                        postList.add(post)
                    }
                }

                val newLastVisible =
                    if (!querySnapshot.isEmpty) querySnapshot.documents[querySnapshot.size() - 1] else null
                var isLastPage = newLastVisible == null || postList.size < limit
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
                Triple(postList, newLastVisible, isLastPage)
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                Triple(ArrayList(), null, false)
            }
        }


        fun getAllPostByIdUser(idUser: String, callback: FireStoreCallback<ArrayList<Post>>) {
            val postCollection = db.collection("Post").whereEqualTo("idUserPost", idUser)
                .orderBy("timeCreated", Query.Direction.DESCENDING)
            postCollection.get().addOnSuccessListener { querySnapshot ->
                val postList = ArrayList<Post>()

                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        runBlocking {
                            // Lấy Interacts
                            val interactsSnapshot =
                                document.reference.collection("Interacts").get().await()
                            val interacts = ArrayList(interactsSnapshot.documents.mapNotNull {
                                it.toObject(Interact::class.java)
                            })

                            val commentsSnapshot =
                                document.reference.collection("Comments").get().await()
                            val comments = ArrayList(commentsSnapshot.documents.mapNotNull {
                                it.toObject(Comment::class.java)
                            })
                            post.comments.clear()
                            post.comments.addAll(comments)
                            post.interacts.clear()
                            post.interacts.addAll(interacts)
                            postList.add(post)

                        }
                    }
                }
                callback.onSuccess(postList)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun getAllPostByIdUser(
            idUser: String,
            limit: Long,
            lastVisible: DocumentSnapshot?,
            callback: FireStoreCallback<Triple<ArrayList<Post>, DocumentSnapshot?, Boolean>>
        ) {
            val postCollection = db.collection("Post")
                .whereEqualTo("idUserPost", idUser)
                .orderBy("timeCreated", Query.Direction.DESCENDING)
                .limit(limit)

            // Thêm điều kiện startAfter nếu lastVisible không null
            if (lastVisible != null) {
                postCollection.startAfter(lastVisible)
            }

            postCollection.get().addOnSuccessListener { querySnapshot ->
                val postList = ArrayList<Post>()

                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        runBlocking {
                            // Lấy Interacts
                            val interactsSnapshot =
                                document.reference.collection("Interacts").get().await()
                            val interacts = ArrayList(interactsSnapshot.documents.mapNotNull {
                                it.toObject(Interact::class.java)
                            })

                            val commentsSnapshot =
                                document.reference.collection("Comments").get().await()
                            val comments = ArrayList(commentsSnapshot.documents.mapNotNull {
                                it.toObject(Comment::class.java)
                            })
                            post.comments.clear()
                            post.comments.addAll(comments)
                            post.interacts.clear()
                            post.interacts.addAll(interacts)
                            postList.add(post)
                        }
                    }
                }

                // Trả về cả lastVisible mới và trạng thái lastPage
                val newLastVisible = querySnapshot.documents.lastOrNull()
                val lastPage = querySnapshot.documents.size < limit.toInt()
                callback.onSuccess(Triple(postList, newLastVisible, lastPage))
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }


        fun addPost(post: Post, callback: FireStoreCallback<Boolean>) {
            val postCollection = db.collection("Post")
            postCollection.document(post.id).set(post).addOnSuccessListener { _ ->
                // Dữ liệu album đã được thêm vào với ID ngẫu nhiên
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }


        @SuppressLint("UncheckCast")
        fun updateStatusPost(
            idPost: String, status: Int, callback: FireStoreCallback<Boolean>
        ) {
            val map = HashMap<String, Any>()
            map["status"] = status
            db.collection("Post").document(idPost).update(map).addOnSuccessListener {
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        @SuppressLint("UncheckCast")
        fun updateContentNormalizePost(
            callback: FireStoreCallback<Boolean>
        ) {
            val postCollection = db.collection("Post")
            postCollection.get().addOnSuccessListener {
                for (document in it.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        val map = HashMap<String, Any>()
                        map["contentsNormalize"] =
                            StringUtils.splitStringIntoWords(AppUtils.removeVietnameseFromStringNice(post.content))
                        db.collection("Post").document(post.id).update(map).addOnSuccessListener {
                            callback.onSuccess(true)
                        }.addOnFailureListener { exception ->
                            logExceptionUseTimber(exception)
                            callback.onFailure(exception)
                        }
                    }
                }
            }
        }

        fun updatePost(postId: String, updatedPost: Post, callback: FireStoreCallback<Boolean>) {
            val postCollection = db.collection("Post")
            postCollection.document(postId).set(updatedPost).addOnSuccessListener {
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun updatePost(updatedPost: Post, callback: FireStoreCallback<Unit>) {
            val updatedPostMap =
                updatedPost.toMap() // Sử dụng hàm toMap để chuyển đổi Post thành Map
            db.collection("Post").document(updatedPost.id).update(updatedPostMap)
                .addOnSuccessListener {
                    callback.onSuccess(Unit)
                }.addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun deletePost(postId: String, callback: FireStoreCallback<Boolean>) {
            val postCollection = db.collection("Post")
            postCollection.document(postId).delete().addOnSuccessListener {
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun getPostById(postId: String, callback: PostCallback) {
            db.collection("Post").whereEqualTo("id", postId).get()
                .addOnSuccessListener { result: QuerySnapshot ->
                    if (!result.isEmpty) {
                        val documentSnapshot = result.documents[0]
                        val postFound = documentSnapshot.toObject(Post::class.java)
                        val commentList = ArrayList<Comment>()
                        // Lấy Comments
                        runBlocking {
                            // Lấy Interacts
                            val interactsSnapshot =
                                documentSnapshot.reference.collection("Interacts").get().await()
                            val interacts = ArrayList(interactsSnapshot.documents.mapNotNull {
                                it.toObject(Interact::class.java)
                            })

                            val commentsSnapshot =
                                documentSnapshot.reference.collection("Comments").get().await()
                            for (document in commentsSnapshot.documents) {
                                val comment = document.toObject(Comment::class.java)
                                if (comment != null) {
                                    // Lấy Interacts Con
                                    val interactsChildSnapshot =
                                        document.reference.collection("Interacts").get().await()
                                    val interactsChild =
                                        ArrayList(interactsChildSnapshot.documents.mapNotNull {
                                            it.toObject(Interact::class.java)
                                        })

                                    // Gán Interacts và Comments vào Post
                                    comment.interacts = interactsChild

                                    // Lấy Comments Con
                                    val commentsChildSnapshot =
                                        document.reference.collection("Comments").get().await()

                                    // // Lấy Interacts Con trong comment con
                                    for (documentChildComment in commentsChildSnapshot.documents) {
                                        val commentChild =
                                            documentChildComment.toObject(Comment::class.java)
                                        if (commentChild != null) {
                                            // Lấy Interacts Con
                                            val interactsChildInCommentSnapshot =
                                                documentChildComment.reference.collection("Interacts")
                                                    .get().await()
                                            val interactsChildInComment =
                                                ArrayList(interactsChildInCommentSnapshot.documents.mapNotNull {
                                                    it.toObject(Interact::class.java)
                                                })
                                            commentChild.interacts = interactsChildInComment
                                            comment.comments.add(commentChild)
                                        }
                                    }

                                    commentList.add(comment)
                                }
                            }




                            postFound!!.comments.clear()
                            postFound.comments.addAll(commentList)
                            postFound.interacts.clear()
                            postFound.interacts.addAll(interacts)
                            callback.onPostFound(postFound)
                        }
                    } else {
                        // Không tìm thấy người dùng, trả về null
                        callback.onPostFound(null)
                    }
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