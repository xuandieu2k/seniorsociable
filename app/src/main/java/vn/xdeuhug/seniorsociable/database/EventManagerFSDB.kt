package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import vn.xdeuhug.seniorsociable.model.entity.modelEvent.Event
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2023
 */
class EventManagerFSDB {
    companion object{
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface EventCallback {
            fun onEventFound(event: Event?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun getAllEvent(callback: FireStoreCallback<ArrayList<Event>>) {
            val eventCollection = db.collection("Event")
            eventCollection.get().addOnSuccessListener { querySnapshot ->
                val eventList = ArrayList<Event>()

                for (document in querySnapshot.documents) {
                    val event = document.toObject(Event::class.java)
                    if (event != null) {
                        eventList.add(event)
                    }
                }

                callback.onSuccess(eventList)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }


        fun addEvent(event: Event, callback: FireStoreCallback<Boolean>) {
            val postCollection = db.collection("Event")
            postCollection.document(event.id).set(event).addOnSuccessListener { _ ->
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