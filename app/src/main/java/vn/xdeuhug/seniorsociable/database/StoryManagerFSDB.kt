package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Story

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 21 / 12 / 2023
 */
class StoryManagerFSDB {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface StoryCallback {
            fun onStoryFound(story: Story?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }
        fun getAllStory(callback: FireStoreCallback<ArrayList<Story>>) {
            val storyCollection = db.collection("Story").orderBy("timeCreated",Query.Direction.DESCENDING)
            storyCollection.get().addOnSuccessListener { querySnapshot ->
                val storyList = ArrayList<Story>()

                for (document in querySnapshot.documents) {
                    val story = document.toObject(Story::class.java)
                    if (story != null) {
                        storyList.add(story)
                    }
                }
                callback.onSuccess(storyList)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun addStory(story: Story, callback: FireStoreCallback<Boolean>) {
            val storyCollection = db.collection("Story")
            storyCollection.document(story.id).set(story).addOnSuccessListener { _ ->
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