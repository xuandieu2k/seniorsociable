package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import vn.xdeuhug.seniorsociable.constants.NewsConstants
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.News
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.News.Companion.toMap

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 26 / 12 / 2023
 */
class NewsManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface NewsCallback {
            fun onEventFound(news: News?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun getAllNews(callback: FireStoreCallback<ArrayList<News>>) {
            val newsCollection = db.collection("News")
            newsCollection.get().addOnSuccessListener { querySnapshot ->
                val newsList = ArrayList<News>()

                for (document in querySnapshot.documents) {
                    val news = document.toObject(News::class.java)
                    if (news != null) {
                        newsList.add(news)
                    }
                }
                if (newsList.isNotEmpty()) {
                    callback.onSuccess(newsList)
                }
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun getNewsTop(callback: FireStoreCallback<ArrayList<News>>) {
            val newsCollection =
                db.collection("News").whereArrayContains("category", NewsConstants.NEWS_TOP)
            newsCollection.get().addOnSuccessListener { querySnapshot ->
                val newsList = ArrayList<News>()

                for (document in querySnapshot.documents) {
                    val news = document.toObject(News::class.java)
                    if (news != null) {
                        newsList.add(news)
                    }
                }
                if (newsList.isNotEmpty()) {
                    callback.onSuccess(newsList)
                }
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }


        fun addNews(news: News, callback: FireStoreCallback<Boolean>) {
            val newsCollection = db.collection("News")
            val mapNews = news.toMap()
            newsCollection.document(news.articleId).set(mapNews).addOnSuccessListener { _ ->
                // Dữ liệu album đã được thêm vào với ID ngẫu nhiên
                callback.onSuccess(true)
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