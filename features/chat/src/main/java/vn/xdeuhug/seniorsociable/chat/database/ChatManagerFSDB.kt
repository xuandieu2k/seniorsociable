package vn.xdeuhug.seniorsociable.chat.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.chat.entity.Chat
import vn.xdeuhug.seniorsociable.chat.entity.Message
import vn.xdeuhug.seniorsociable.constants.AppConstants
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 22 / 11 / 2023
 */
class ChatManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface ChatCallback {
            fun onChatFound(message: Message?)
            fun onFailure(exception: Exception)
        }

        interface CallBackCRUD {
            fun onAdded(message: Message?, lastVisible: DocumentSnapshot?)
            fun onUpdated(message: Message?, lastVisible: DocumentSnapshot?)
            fun onRemoved(message: Message?, lastVisible: DocumentSnapshot?)
            fun onEnd(isEnd: Boolean)
        }

        interface CallBackCRUDChat {
            fun onAdded(message: Message?, idChat: String)
            fun onUpdated(message: Message?, idChat: String)
            fun onRemoved(message: Message?, idChat: String)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        suspend fun getChatById(idChat: String): Pair<Chat?, Boolean> {
            return try {
                val chatCollection = db.collection("Chat").whereEqualTo("id", idChat)
                val chatSnapshot = chatCollection.get().await()
                if(chatSnapshot.documents.isEmpty())
                {
                    Pair(null, false)
                }else{
                    val chat = chatSnapshot.documents.first().toObject(Chat::class.java)
                    Pair(chat, true)
                }
            } catch (ex: Exception) {
                logExceptionUseTimber(ex)
                Pair(null, false)
            }
        }

        suspend fun getMessageById(
            idChat: String, pageSize: Long, lastVisible: DocumentSnapshot? = null
        ): Triple<ArrayList<Message>, DocumentSnapshot?, Boolean> {
            return try {
                val messagesCollection =
                    db.collection("Chat").document(idChat).collection("Messages")

                var query = messagesCollection.limit(pageSize)

                if (lastVisible != null) {
                    query = query.startAfter(lastVisible)
                }

                val querySnapshot = query.get().await()

                val messageList = ArrayList<Message>()

                for (document in querySnapshot.documents) {
                    val message = document.toObject(Message::class.java)
                    if (message != null) {
                        messageList.add(message)
                    }
                }

                val newLastVisible =
                    if (!querySnapshot.isEmpty) querySnapshot.documents[querySnapshot.size() - 1] else null
                val isLastPage = newLastVisible == null || messageList.size < pageSize
                Triple(messageList, newLastVisible, isLastPage)
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                Triple(ArrayList(), null, false)
            }
        }

        fun addMessage(idChat: String, message: Message, callback: FireStoreCallback<Boolean>) {
            val chatCollection = db.collection("Chat").document(idChat).collection("Messages")
            chatCollection.document(message.id).set(message).addOnSuccessListener { _ ->
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun getListChatBuIdChat(
            listChat: ArrayList<String>, callback: FireStoreCallback<ArrayList<Pair<Chat, Message>>>
        ) {
            val chatCollection = db.collection("Chat")
            val query = chatCollection.whereIn(FieldPath.documentId(), listChat)
            val listChatNew = ArrayList<Chat>()
            val listPair = ArrayList<Pair<Chat, Message>>()
            query.get().addOnSuccessListener {
                for (item in it.documents) {
                    val chat = item.toObject(Chat::class.java)
                    listChatNew.add(chat!!)
                }
                Timber.tag("fChat: ").e("${listChatNew.size}")
                var count = 0
                for (chat in listChatNew) {
                    val messagesCollection =
                        db.collection("Chat").document(chat.id).collection("Messages")
                    val query =
                        messagesCollection.orderBy("timeSend", Query.Direction.DESCENDING).limit(1)
                    query.get().addOnSuccessListener { item ->
                        val lastMessage = item.documents[0].toObject(Message::class.java)
                        listPair.add(Pair(chat, lastMessage!!))
                        count++
                        if (count == listChatNew.size) {
                            Timber.tag("Size Pair: ").e("${listPair.size}")
                            callback.onSuccess(listPair)
                        }
                    }.addOnFailureListener { ex ->
                        Timber.tag("Loi roi").e(ex)
                    }
                }
            }.addOnFailureListener {
                logExceptionUseTimber(it)
                callback.onFailure(it)
            }
        }

        fun getListChatByIdChat(
            listChat: ArrayList<String>,callback: FireStoreCallback<ArrayList<Pair<Chat, Message>>>) {
            db.clearPersistence()
            val chatCollection = db.collection("Chat")
//            val query = chatCollection.whereIn("members", arrayListOf(UserCache.getUser().id))
            val query = chatCollection.whereIn(FieldPath.documentId(), listChat)
            val listChatNew = ArrayList<Chat>()
            val listPair = mutableMapOf<String, Pair<Chat, Message>>()

            query.get().addOnSuccessListener {
                for (item in it.documents) {
                    val chat = item.toObject(Chat::class.java)
                    chat?.let { listChatNew.add(it) }
                }
                Timber.tag("fChat: ").e("${listChatNew.size}")

                for (chat in listChatNew) {
                    val messagesCollection = db.collection("Chat").document(chat.id).collection("Messages")
                    val query = messagesCollection.orderBy("timeSend", Query.Direction.DESCENDING).limit(1)

                    query.addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            Timber.tag("Error").e(e)
                            return@addSnapshotListener
                        }

                        snapshots?.let {
                            if (!it.isEmpty) {
                                val lastMessage = it.documents[0].toObject(Message::class.java)
                                lastMessage?.let { message ->
                                    listPair[chat.id] = Pair(chat, message)
                                    callback.onSuccess(ArrayList(listPair.values))
                                }
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                logExceptionUseTimber(it)
                callback.onFailure(it)
            }
        }



        fun listenForChatAndMessagesChanges(
            listChat: ArrayList<String>,
            callback: FireStoreCallback<ArrayList<Pair<Chat, Message>>>
        ) {
            val listPair = ArrayList<Pair<Chat, Message>>()

            // Lắng nghe sự thay đổi trong bảng "Chat"
            val chatListenerRegistration = db.collection("Chat")
                .whereIn(FieldPath.documentId(), listChat)
                .addSnapshotListener { chatSnapshot, chatException ->
                    if (chatException != null) {
                        logExceptionUseTimber(chatException)
                        callback.onFailure(chatException)
                        return@addSnapshotListener
                    }

                    val listChatNew = ArrayList<Chat>()
                    chatSnapshot?.documents?.forEach { chatDocument ->
                        val chat = chatDocument.toObject(Chat::class.java)
                        chat?.let { listChatNew.add(it) }
                    }

                    Timber.tag("fChat: ").e("${listChatNew.size}")

                    var count = 0
                    if (listChatNew.isEmpty()) {
                        callback.onSuccess(listPair)
                    }

                    // Xử lý sự thay đổi trong bảng "Messages" cho từng node "Chat"
                    for (chat in listChatNew) {
                        val messagesCollection =
                            db.collection("Chat").document(chat.id).collection("Messages")
                        val query =
                            messagesCollection.orderBy("timeSend", Query.Direction.DESCENDING)
                                .limit(1)
                        query.addSnapshotListener { messageSnapshot, messageException ->
                            if (messageException != null) {
                                Timber.tag("MessagesChanges").e("Listen failed: $messageException")
                                callback.onFailure(messageException)
                                return@addSnapshotListener
                            }

                            val lastMessage = messageSnapshot?.documents?.getOrNull(0)
                                ?.toObject(Message::class.java)
                            lastMessage?.let { listPair.add(Pair(chat, it)) }

                            count++
                            if (count == listChatNew.size) {
                                Timber.tag("Size Pair: ").e("${listPair.size}")
                                callback.onSuccess(listPair)
                            }
                        }
                    }
                }
        }


        fun addChat(chat: Chat, callback: FireStoreCallback<Boolean>) {
            val chatCollection = db.collection("Chat")
            chatCollection.document(chat.id).set(chat).addOnSuccessListener { _ ->
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        suspend fun addChat(chat: Chat): Boolean {
            return try {
                val chatCollection = db.collection("Chat")
                chatCollection.document(chat.id).set(chat).await()
                true
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                false
            }
        }

//        fun listenerDataChange(idChatList: List<String>, callBackCRUD: CallBackCRUD) {
//            val chatCollection = db.collection("Chat")
//
//            // Tạo một truy vấn với điều kiện là ID của bản ghi nằm trong danh sách idChatList
//            val query = chatCollection.whereIn(FieldPath.documentId(), idChatList)
//
//            query.addSnapshotListener { querySnapshot, exception ->
//                if (exception != null) {
//                    logExceptionUseTimber(exception)
//                    return@addSnapshotListener
//                }
//
//                querySnapshot?.documentChanges?.forEach { documentChange ->
//                    when (documentChange.type) {
//                        DocumentChange.Type.ADDED -> {
//                            val addedMessage = documentChange.document.toObject(Message::class.java)
//                            callBackCRUD.onAdded(addedMessage)
//                            // ... Do something with addedMessage
//                        }
//
//                        DocumentChange.Type.MODIFIED -> {
//                            val modifiedMessage =
//                                documentChange.document.toObject(Message::class.java)
//                            callBackCRUD.onUpdated(modifiedMessage)
//                            // ... Do something with modifiedMessage
//                        }
//
//                        DocumentChange.Type.REMOVED -> {
//                            val removedMessage =
//                                documentChange.document.toObject(Message::class.java)
//                            callBackCRUD.onRemoved(removedMessage)
//                            // ... Do something with removedMessage
//                        }
//                    }
//                }
//            }
//        }

        fun getChatsContainingIds(ids: List<String>, callback: (List<Chat>) -> Unit) {
            val chatCollection = db.collection("Chat")

            // Tạo một truy vấn với điều kiện là ID của chat nằm trong danh sách ids
            val query = chatCollection.whereIn(FieldPath.documentId(), ids)

            query.get().addOnSuccessListener { querySnapshot ->
                val chatList = mutableListOf<Chat>()

                for (document in querySnapshot.documents) {
                    val chat = document.toObject(Chat::class.java)
                    if (chat != null) {
                        chatList.add(chat)
                    }
                }

                callback(chatList)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback(emptyList()) // Trả về danh sách rỗng trong trường hợp lỗi
            }
        }


        fun listenerDataChatChange(idChatList: List<String>, callBackCRUD: CallBackCRUDChat) {
            val messageCollections = mutableListOf<CollectionReference>()

            // Tạo một danh sách các CollectionReference tương ứng với Messages trong mỗi Chat
            idChatList.forEach { idChat ->
                val chatCollection = db.collection("Chat").document(idChat).collection("Messages")
                messageCollections.add(chatCollection)
            }

            // Lắng nghe sự thay đổi trên từng CollectionReference
            messageCollections.forEach { messageCollection ->
                messageCollection.orderBy("timeSend", Query.Direction.ASCENDING)
                    .addSnapshotListener { querySnapshot, exception ->
                        if (exception != null) {
                            logExceptionUseTimber(exception)
                            return@addSnapshotListener
                        }

                        querySnapshot?.documentChanges?.forEach { documentChange ->
                            when (documentChange.type) {
                                DocumentChange.Type.ADDED -> {
                                    val addedMessage =
                                        documentChange.document.toObject(Message::class.java)
                                    callBackCRUD.onAdded(
                                        addedMessage, messageCollection.parent!!.id
                                    )
                                    // ... Do something with addedMessage
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    val modifiedMessage =
                                        documentChange.document.toObject(Message::class.java)
                                    callBackCRUD.onUpdated(
                                        modifiedMessage, messageCollection.parent!!.id
                                    )
                                    // ... Do something with modifiedMessage
                                }

                                DocumentChange.Type.REMOVED -> {
                                    val removedMessage =
                                        documentChange.document.toObject(Message::class.java)
                                    callBackCRUD.onRemoved(
                                        removedMessage, messageCollection.parent!!.id
                                    )
                                    // ... Do something with removedMessage
                                }
                            }
                        }
                    }
            }
        }

        /**
         * @param lastVisible tài liệu cuối cùng của lần truy vấn
         * @param Triple(Boolean) true nếu đã đến cuối cùng
         */
        suspend fun getPagingMessages(
            idChat: String, pageSize: Long, lastVisible: DocumentSnapshot? = null
        ): Triple<ArrayList<Message>, DocumentSnapshot?, Boolean> = withContext(Dispatchers.IO) {
            return@withContext try {
                db.clearPersistence()
                val messageCollection =
                    db.collection("Chat").document(idChat).collection("Messages")

                var query = messageCollection.orderBy("timeSend", Query.Direction.DESCENDING)
                    .limit(pageSize)

                if (lastVisible != null) {
                    query = query.startAfter(lastVisible)
                }

                val querySnapshot = query.get().await()

                val messageList = ArrayList<Message>()
                messageList.clear()

                for (document in querySnapshot.documents) {
                    val message = document.toObject(Message::class.java)
                    if (message != null) {
                        messageList.add(message)
                    }
                }

                val newLastVisible =
                    if (!querySnapshot.isEmpty) querySnapshot.documents[querySnapshot.size() - 1] else null
                val isLastPage = newLastVisible == null || messageList.size < pageSize

                Triple(messageList, newLastVisible, isLastPage)
            } catch (exception: Exception) {
                logExceptionUseTimber(exception)
                Triple(ArrayList(), null, false)
            }
        }

        fun listenerForNewMessages(idChat: String, lastTimeSend: Date, callBackCRUD: CallBackCRUD) {
            val messageCollection = db.collection("Chat").document(idChat).collection("Messages")
            messageCollection
                .orderBy("timeSend", Query.Direction.DESCENDING)
                .whereGreaterThan("timeSend", lastTimeSend)
                .addSnapshotListener { querySnapshot, exception ->
                    if (exception != null) {
                        callBackCRUD.onEnd(true)
                        logExceptionUseTimber(exception)
                        return@addSnapshotListener
                    }

                    querySnapshot?.documentChanges?.forEach { documentChange ->
                        if (documentChange.type == DocumentChange.Type.ADDED) {
                            val addedMessage = documentChange.document.toObject(Message::class.java)
                            callBackCRUD.onAdded(addedMessage, documentChange.document)
                            // ... Xử lý tin nhắn mới được thêm
                        }
                    }
                }
        }



        fun listenerDataChatDetailChange(idChat: String, callBackCRUD: CallBackCRUD) {

            val messageCollection = db.collection("Chat").document(idChat).collection("Messages")
            messageCollection.limit(AppConstants.PAGE_SIZE_10)
                .orderBy("timeSend", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot, exception ->
                    if (exception != null) {
                        callBackCRUD.onEnd(true)
                        logExceptionUseTimber(exception)
                        return@addSnapshotListener
                    }

                    querySnapshot?.documentChanges?.forEach { documentChange ->
                        when (documentChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val addedMessage =
                                    documentChange.document.toObject(Message::class.java)
                                callBackCRUD.onAdded(addedMessage, documentChange.document)
                                // ... Do something with addedMessage
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val modifiedMessage =
                                    documentChange.document.toObject(Message::class.java)
                                callBackCRUD.onUpdated(modifiedMessage, documentChange.document)
                                // ... Do something with modifiedMessage
                            }

                            DocumentChange.Type.REMOVED -> {
                                val removedMessage =
                                    documentChange.document.toObject(Message::class.java)
                                callBackCRUD.onRemoved(removedMessage, documentChange.document)
                                // ... Do something with removedMessage
                            }
                        }
                    }
                }
        }


        // Hàm mở rộng cho CollectionReference để thêm khả năng lắng nghe sự kiện
        private fun Query.addMessagesListener(
            onAdded: (DocumentChange) -> Unit,
            onModified: (DocumentChange) -> Unit,
            onRemoved: (DocumentChange) -> Unit,
            onError: (FirebaseFirestoreException) -> Unit
        ) {
            val registration = addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    onError(exception)
                    return@addSnapshotListener
                }

                querySnapshot?.documentChanges?.forEach { documentChange ->
                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> onAdded(documentChange)
                        DocumentChange.Type.MODIFIED -> onModified(documentChange)
                        DocumentChange.Type.REMOVED -> onRemoved(documentChange)
                    }
                }
            }

            // Nếu bạn muốn có khả năng hủy lắng nghe, bạn có thể lưu giữ đối tượng registration và gọi registration.remove() khi không cần nữa
        }


        private fun logExceptionUseTimber(exception: Exception) {
            Timber.tag("Exception FireStore").e(exception)
        }
    }
}