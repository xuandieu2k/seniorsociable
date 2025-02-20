package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import vn.xdeuhug.seniorsociable.model.entity.modelMovieShort.MovieShort
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 12 / 2023
 */
class MovieShortManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface MovieShortCallback {
            fun onMovieShortFound(movieShort: MovieShort?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun getMovieShortById(movieShortId: Int, callback: MovieShortCallback) {
            db.collection("MovieShort").whereEqualTo("id", movieShortId).get()
                .addOnSuccessListener { result: QuerySnapshot ->
                    if (!result.isEmpty) {
                        val documentSnapshot = result.documents[0]
                        val movieShortFound = documentSnapshot.toObject(MovieShort::class.java)
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
                            val comments = ArrayList(commentsSnapshot.documents.mapNotNull {
                                it.toObject(Comment::class.java)
                            })
                            movieShortFound!!.comments.clear()
                            movieShortFound.comments.addAll(comments)
                            movieShortFound.interacts.clear()
                            movieShortFound.interacts.addAll(interacts)
                            callback.onMovieShortFound(movieShortFound)
                        }
                    } else {
                        // Không tìm thấy người dùng, trả về null
                        callback.onMovieShortFound(null)
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