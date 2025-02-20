package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.Album
import java.util.UUID

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 10 / 2023
 */
class AlbumManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface AlbumCallback {
            fun onAlbumFound(album: Album?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun getAlbumOfUserCurrentById(albumId: String, callback: AlbumCallback) {
            val albumCollection = db.collection("Album").document(UserCache.getUser().id).collection("UserAlbums")
            albumCollection
                .whereEqualTo("id", albumId)
                .get()
                .addOnSuccessListener { result: QuerySnapshot ->
                    if (!result.isEmpty) {
                        val documentSnapshot = result.documents[0]
                        val albumFound = documentSnapshot.toObject(Album::class.java)
                        callback.onAlbumFound(albumFound)
                    } else {
                        // Không tìm thấy , trả về null
                        callback.onAlbumFound(null)
                    }
                }
                .addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun getAllAlbums(callback: FireStoreCallback<ArrayList<Album>>) {
            val albumCollection = db.collection("Album").document(UserCache.getUser().id).collection("UserAlbums")
            albumCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    val albumList = ArrayList<Album>()

                    for (document in querySnapshot.documents) {
                        val album = document.toObject(Album::class.java)
                        if (album != null) {
                            albumList.add(album)
                        }
                    }

                    callback.onSuccess(albumList)
                }
                .addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun addAlbum(album: Album, callback: FireStoreCallback<Boolean>) {
            album.id = UUID.randomUUID().toString()
            val albumCollection = db.collection("Album").document(UserCache.getUser().id).collection("UserAlbums")
            albumCollection.document(album.id)
                .set(album)
                .addOnSuccessListener { _ ->
                    // Dữ liệu album đã được thêm
                    callback.onSuccess(true)
                }
                .addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun updateAlbum(albumId: String, updatedAlbum: Album, callback: FireStoreCallback<Boolean>) {
            val albumCollection = db.collection("Album").document(UserCache.getUser().id).collection("UserAlbums")
            albumCollection.document(albumId)
                .set(updatedAlbum)
                .addOnSuccessListener {
                    callback.onSuccess(true)
                }
                .addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        fun deleteAlbum(albumId: String, callback: FireStoreCallback<Boolean>) {
            val albumCollection = db.collection("Album/${UserCache.getUser().id}")
            albumCollection.document(albumId)
                .delete()
                .addOnSuccessListener {
                    callback.onSuccess(true)
                }
                .addOnFailureListener { exception ->
                    logExceptionUseTimber(exception)
                    callback.onFailure(exception)
                }
        }

        private fun logExceptionUseTimber(exception:Exception)
        {
            Timber.tag("Exception FireStore").d(exception)
        }
    }
}