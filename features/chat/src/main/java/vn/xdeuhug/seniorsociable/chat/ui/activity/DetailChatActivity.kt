package vn.xdeuhug.seniorsociable.chat.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.paginate.Paginate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.startActivity
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.chat.databinding.ActivityDetailChatBinding
import vn.xdeuhug.seniorsociable.chat.ui.adapter.MessageAdapter
import vn.xdeuhug.seniorsociable.chat.constants.ChatConstants
import vn.xdeuhug.seniorsociable.chat.database.ChatManagerFSDB
import vn.xdeuhug.seniorsociable.chat.entity.Chat
import vn.xdeuhug.seniorsociable.chat.entity.Message
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.CallConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.database.CallRoomManagerFSDB
import vn.xdeuhug.seniorsociable.database.FireCloudManager
import vn.xdeuhug.seniorsociable.database.PostManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelCall.CallRoom
import vn.xdeuhug.seniorsociable.model.entity.modelCall.UserCall
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.other.CustomLoadingListItemCreator
import vn.xdeuhug.seniorsociable.ui.dialog.PreviewMediaDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoPickerUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import vn.xdeuhug.seniorsociable.widget.audiorecorder.uikit.VoiceSenderDialog
import vn.xdeuhug.seniorsociable.widget.audiorecorder.worker.AudioRecordListener
import vn.xdeuhug.widget.layout.NestedLinearLayout
import java.util.Date
import java.util.UUID

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 20 / 11 / 2023
 */
class DetailChatActivity : AppActivity(), AudioRecordListener, MessageAdapter.OnListenerCLick {
    private var localMedia = ArrayList<LocalMedia>()
    private lateinit var binding: ActivityDetailChatBinding
    private var userChat = User()
    private lateinit var launcher: ActivityResultLauncher<Intent>

    // Data chat
    private lateinit var messageAdapter: MessageAdapter
    private var listMessage = ArrayList<Message>()

    // Boolean
    private var isCreated = false

    //
    private var chatCurrent: Chat? = null
    private var idChatCurrent = ""

    /* paging*/
    private var limit = AppConstants.PAGE_SIZE_15
    private var currentPage = 1
    private var lastVisible: DocumentSnapshot? = null
    private var loading = false
    private var lastPage = false
    /* end */

    // check hidden Linear action more(Voice and media)
    private var isShowingLinearMoreAction = false
    override fun getLayoutView(): View {
        binding = ActivityDetailChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
//        setViews()
        startShimmer()
        userChat = Gson().fromJson(
            intent.getStringExtra(AppConstants.OBJECT_CHAT), User::class.java
        )
        checkCreatedIsChat()
        initDataRecycleView()
        setLauncherImage()
        setDataForView()
        setClickView()
        setClickButton()
        listener()
        sendChat()
        getDataChatPaging()
        customPaginate()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataChatPaging() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val listTriple = async {
                    ChatManagerFSDB.getPagingMessages(
                        idChatCurrent,
                        limit,
                        lastVisible
                    )
                }.await()
                if (currentPage == 1) {
                    listMessage.clear()
                    finishShimmer()
                }
                // UI update code here
//                var posOld = 0
//                if(listMessage.isNotEmpty())
//                {
//                    posOld = listMessage.size - 1
//                }
                listMessage.addAll(listTriple.first)
//                messageAdapter.notifyItemRangeInserted(posOld, listMessage.size)
                messageAdapter.notifyDataSetChanged()
                lastVisible = listTriple.second
                lastPage = listTriple.third
                loading = false
                if (currentPage == 1) {
                    ChatManagerFSDB.listenerForNewMessages(
                        idChatCurrent,
                        listMessage.first().timeSend,
                        object :
                            ChatManagerFSDB.Companion.CallBackCRUD {
                            override fun onAdded(
                                message: Message?,
                                lastVisible: DocumentSnapshot?
                            ) {
                                listMessage.add(0, message!!)
//                                messageAdapter.notifyItemInserted(0)
                                messageAdapter.notifyDataSetChanged()
                                binding.nsvData.post {
                                    binding.nsvData.smoothScrollTo(
                                        0,
                                        getHeightRecyclerViewBottom()
                                    )
                                }
                                showViewNoData()
                            }

                            override fun onUpdated(
                                message: Message?,
                                lastVisible: DocumentSnapshot?
                            ) {
                                //
                            }

                            override fun onRemoved(
                                message: Message?,
                                lastVisible: DocumentSnapshot?
                            ) {
                                //
                            }

                            override fun onEnd(isEnd: Boolean) {
                                //
                            }

                        })

                    binding.nsvData.post {
                        binding.nsvData.smoothScrollTo(
                            0,
                            getHeightRecyclerViewBottom()
                        )
                    }
                }
                showViewNoData()
                loading = false
                binding.loadMore.hide()
            } catch (e: Exception) {
                // Xử lý ngoại lệ khi gặp lỗi
                e.printStackTrace()
                toast(R.string.please_try_later)
                loading = false
                binding.loadMore.hide()
            }
        }
    }

    private fun getHeightRecyclerViewBottom(): Int {
        var recyclerViewBottom = binding.rvMessage.bottom
        if (binding.loadMore.visibility == View.VISIBLE) {
            recyclerViewBottom = binding.rvMessage.bottom + binding.loadMore.height
        }
        return recyclerViewBottom
    }

    private fun customPaginate() {
        binding.nsvData.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            if (binding.rvMessage.visibility == View.VISIBLE && binding.sflLoadData.visibility == View.GONE) {
                // Kiểm tra xem cuộn đến cuối cùng của NestedScrollView hay không
                if (scrollY == 0) {
                    if (!loading && !lastPage) {
                        binding.loadMore.show()
                        binding.nsvData.post {
                            binding.nsvData.smoothScrollTo(
                                0,
                                0
                            )
                        }
                        loading = true
                        currentPage += 1
                        getDataChatPaging()
                    } else {
                        if (lastPage) {
                            binding.loadMore.hide()
//                            toast(getString(R.string.end_page))
                        }
                    }
                }
            }
        })
    }

    private fun paginate() {
        val callback: Paginate.Callbacks = object : Paginate.Callbacks {
            override fun onLoadMore() {
                postDelayed({
                    if (!loading && !lastPage && lastVisible != null) {
                        currentPage += 1
                        getDataChatPaging()
                    }
                }, 1000)
            }

            override fun isLoading(): Boolean {
                return loading
            }

            override fun hasLoadedAllItems(): Boolean {
                return lastPage
            }

        }

        Paginate.with(binding.rvMessage, callback).setLoadingTriggerThreshold(0)
            .addLoadingListItem(true).setLoadingListItemCreator(CustomLoadingListItemCreator())
            .build()
    }

    private fun startShimmer() {
        binding.sflLoadData.show()
        binding.sflLoadData.startShimmer()
        binding.rvMessage.hide()
        binding.rlBackgroundNotFound.hide()
    }

    private fun finishShimmer() {
        binding.sflLoadData.stopShimmer()
        binding.sflLoadData.hide()
    }

    private fun showViewNoData() {
        if (listMessage.isNotEmpty()) {
            binding.rlBackgroundNotFound.hide()
            binding.rvMessage.show()
        } else {
            binding.rlBackgroundNotFound.show()
            binding.rvMessage.hide()
        }
    }

    private fun listener() {
        binding.edtMessage.doOnTextChanged { _, _, _, _ ->
            setViewActionMore(binding.edtMessage.text!!.trim().isNotEmpty())
            binding.imvSend.isEnabled = binding.edtMessage.text!!.trim().isNotEmpty()
        }

//        binding.edtMessage.addTextChangedListener(object : TextWatcher {
//            private var isFormatting = false
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                setViewActionMore(binding.edtMessage.text!!.trim().isNotEmpty())
//                binding.imvSend.isEnabled = binding.edtMessage.text!!.trim().isNotEmpty()
//                if (!isFormatting) {
//                    if (binding.edtMessage.text.toString().isNotEmpty()) {
//                        isFormatting = true
//                        binding.edtMessage.removeTextChangedListener(this)
//                        setSingleLine()
//                        binding.edtMessage.addTextChangedListener(this)
//                        isFormatting = false
//                    } else {
//                        binding.edtMessage.setSelection(0)
//                    }
//                }
//                binding.edtMessage.setSelection(binding.edtMessage.text!!.length)
//            }
//
//            override fun afterTextChanged(arg0: Editable?) {
//                //
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                //
//            }
//        })

        binding.edtMessage.setOnClickListener {
            setSingleLine()
            setViewActionMore(binding.edtMessage.text!!.trim().isNotEmpty())
        }

        binding.imvExpand.setOnClickListener() {
            setSingleLine()
            setViewActionMore(false)
        }
    }

    /**
     * @param isHidden True - Ẩn view linear hidden
     */
    private fun setViewActionMore(isHidden: Boolean) {
        if (isHidden) {
            binding.llActionMore.hide()
            binding.imvExpand.show()
            isShowingLinearMoreAction = false
        } else {
            binding.llActionMore.show()
            binding.imvExpand.hide()
            isShowingLinearMoreAction = true
        }
    }

    private fun setSingleLine() {
        binding.edtMessage.singleLine = !isShowingLinearMoreAction
        binding.edtMessage.setSelection(binding.edtMessage.text!!.trim().length)
    }

    private fun checkCreatedIsChat() {
        idChatCurrent = AppUtils.combineAndHashIds(
            UserCache.getUser().id, userChat.id
        )
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val chatData = async {
                    ChatManagerFSDB.getChatById(idChatCurrent)
                }.await()
                isCreated = chatData.second
                chatCurrent = chatData.first
            } catch (e: Exception) {
                // Xử lý ngoại lệ khi gặp lỗi
                e.printStackTrace()
                toast(R.string.please_try_later)
            }
        }
    }

    private fun initDataRecycleView() {
        messageAdapter = MessageAdapter(this, idChatCurrent)
        messageAdapter.setData(listMessage)
        messageAdapter.onListenerCLick = this
        AppUtils.initRecyclerViewReverse(binding.rvMessage, messageAdapter)
//        getDataMessages()
    }

//    private fun getDataMessages() {
//        CoroutineScope(Dispatchers.Main).launch {
//            try {
//                val messageData = async {
//                    ChatManagerFSDB.getMessageById(idChatCurrent, 5, null)
//                }.await()
//                listMessage.addAll(messageData.first)
//                AppUtils.logJsonFromObject(listMessage)
//                messageAdapter.notifyDataSetChanged()
//            } catch (e: Exception) {
//                // Xử lý ngoại lệ khi gặp lỗi
//                e.printStackTrace()
//                toast(R.string.please_try_later)
//            }
//        }
//    }

    private fun setLauncherImage() {
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    localMedia.clear()
                    localMedia = PictureSelector.obtainSelectorList(data)
                    val list = ArrayList<MultiMedia>()
                    localMedia.forEach { localMedia ->
                        val media = MultiMedia()
                        val mediaId = UUID.randomUUID().toString()
                        media.id = mediaId
                        media.name = "Media $mediaId"
                        media.url = localMedia.realPath
                        media.realPath = localMedia.realPath
                        media.size = localMedia.size
                        media.height = localMedia.height
                        media.width = localMedia.width
                        if (PictureMimeType.isHasVideo(localMedia.mimeType)) {
                            media.type = AppConstants.UPLOAD_VIDEO
                        }
                        if (PictureMimeType.isHasImage(localMedia.mimeType)) {
                            media.type = AppConstants.UPLOAD_IMAGE
                        }
                        list.add(media)
                    }
                    sendMedia(list)
                } else {
                    // Error and not choose
                }
            }
    }

    private fun sendMedia(data: ArrayList<MultiMedia>) {
        val idMessage = UUID.randomUUID().toString()
        val storageReference = FirebaseStorage.getInstance().reference
        val storageRef: StorageReference = storageReference.child(
            UploadFireStorageUtils.getRootURLMessageById(
                idChatCurrent, idMessage
            )
        )
        runBlocking {
            FireCloudManager.uploadFilesToFirebaseStorage(data,
                storageRef,
                object : FireCloudManager.Companion.FireStoreCallback<ArrayList<MultiMedia>> {
                    override fun onSuccess(result: ArrayList<MultiMedia>) {
                        val message = Message(
                            idMessage,
                            "",
                            UserCache.getUser().id,
                            Date(),
                            ChatConstants.STATUS_ON,
                            arrayListOf(UserCache.getUser().id),
                            "",
                            data,
                            ArrayList(),
                            ChatConstants.TYPE_MEDIA,
                            ""
                        )
                        if (!isCreated) {
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    addChat()

                                } catch (e: Exception) {
                                    // Xử lý ngoại lệ khi gặp lỗi
                                    e.printStackTrace()
                                    toast(R.string.please_try_later)
                                }
                            }
                        }
                        ChatManagerFSDB.addMessage(idChatCurrent,
                            message,
                            object : ChatManagerFSDB.Companion.FireStoreCallback<Boolean> {

                                override fun onSuccess(result: Boolean) {
                                    if (result) {
                                        //
                                    } else {
                                        toast(R.string.please_try_later)
                                    }
                                }

                                override fun onFailure(exception: Exception) {
                                    toast(R.string.please_try_later)
                                }
                            })

                    }

                    override fun onFailure(result: ArrayList<MultiMedia>) {
                        toast(R.string.please_try_later)
                    }

                })
        }
    }

    private fun setClickButton() {
        binding.imvSend.isEnabled = false
        binding.imvChoosePicture.clickWithDebounce {
            PhotoPickerUtils.showImagePickerChat(this, launcher)
        }
        binding.imvVoice.clickWithDebounce {
            val dialog = VoiceSenderDialog(this)
            dialog.setBeepEnabled(true)
            dialog.show(supportFragmentManager, "VOICE")
        }
        binding.btnVideoCall.clickWithDebounce {
            try {
                startActivity<VideoCallActivity>(
                    CallConstants.ID_CALlER to UserCache.getUser().id,
                    CallConstants.ID_RECEIVER to userChat.id
                )
            } catch (ex: Exception) {
                //
            }
        }
        binding.btnVoiceCall.clickWithDebounce {
            val userCurrent = UserCache.getUser() // Đang đăng nhập
            runBlocking {
                val userCaller = UserCall(
                    userChat.id, userChat.avatar, userChat.name
                )
                val callRoomCaller = CallRoom(
                    userCurrent.id,
                    CallConstants.IS_BUSY,
                    CallConstants.TYPE_VOICE_CALL,
                    CallConstants.IS_CALLING,
                    userCaller
                )
                val userReceiver = UserCall(
                    userCurrent.id, userCurrent.avatar, userCurrent.name
                )
                val callRoomReceiver = CallRoom(
                    userChat.id,
                    CallConstants.IS_BUSY,
                    CallConstants.TYPE_VOICE_CALL,
                    CallConstants.IS_RINGING,
                    userReceiver
                )
                CallRoomManagerFSDB.updateCall(callRoomCaller)
                CallRoomManagerFSDB.updateCall(callRoomReceiver)
            }
//            try {
//                startActivity<CallActivity>(
//                    CallConstants.ID_CALlER to UserCache.getUser().id,
//                    CallConstants.ID_RECEIVER to userChat.id
//                )
//            } catch (ex: Exception) {
//                //
//            }
        }
    }

    private fun setClickView() {
        binding.btnBack.clickWithDebounce {
            finish()
        }
    }


    private fun setDataForView() {
        PhotoShowUtils.loadAvatarImage(
            UploadFireStorageUtils.getRootURLAvatarById(userChat.id),
            userChat.avatar,
            binding.imvAvatar
        )
        AppUtils.setForeground(userChat, binding.imvAvatar, getContext())
        binding.tvUsername.text = userChat.name
        if (userChat.online) {
            binding.tvOnline.text = getString(R.string.online)
        } else {
            binding.tvOnline.text = TimeUtils.timeAgoChat(getContext(), userChat.lastTimeOnline)
        }
    }

    private fun sendChat() {
        binding.imvSend.clickWithDebounce {
            if (!isCreated) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        addChat()

                    } catch (e: Exception) {
                        // Xử lý ngoại lệ khi gặp lỗi
                        e.printStackTrace()
                        toast(R.string.please_try_later)
                    }
                }
            }
            binding.imvSend.isEnabled = false
            val message = Message(
                UUID.randomUUID().toString(),
                "",
                UserCache.getUser().id,
                Date(),
                ChatConstants.STATUS_ON,
                arrayListOf(UserCache.getUser().id),
                binding.edtMessage.text!!.trim().toString(),
                ArrayList(),
                ArrayList(),
                ChatConstants.TYPE_MESSAGE,
                ""
            )
            ChatManagerFSDB.addMessage(idChatCurrent,
                message,
                object : ChatManagerFSDB.Companion.FireStoreCallback<Boolean> {

                    override fun onSuccess(result: Boolean) {
                        if (result) {
                            resetDataChat()
                        } else {
                            toast(R.string.please_try_later)
                            binding.imvSend.isEnabled = true
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        toast(R.string.please_try_later)
                        binding.imvSend.isEnabled = true
                    }
                })
        }
    }

    private fun resetDataChat() {
        binding.edtMessage.setText("")
        setViewActionMore(false)
        binding.imvSend.isEnabled = false
    }

    override fun initData() {
        //
    }

    override fun onAudioReady(audioUri: String?) {
        sendAudio(audioUri!!)
    }

    private fun sendAudio(audioUri: String) {
        val idMessage = UUID.randomUUID().toString()
        val media = MultiMedia()
        media.id = UUID.randomUUID().toString()
        media.type = AppConstants.UPLOAD_AUDIO
        media.realPath = audioUri
        media.url = audioUri
        val message = Message(
            idMessage,
            "",
            UserCache.getUser().id,
            Date(),
            ChatConstants.STATUS_ON,
            arrayListOf(UserCache.getUser().id),
            "",
            arrayListOf(media),
            ArrayList(),
            ChatConstants.TYPE_AUDIO,
            ""
        )
        val storageReference = FirebaseStorage.getInstance().reference
        val storageRef: StorageReference = storageReference.child(
            UploadFireStorageUtils.getRootURLMessageById(
                idChatCurrent, idMessage
            )
        )
        runBlocking {
            FireCloudManager.uploadAudioFile(media,
                storageRef,
                object : FireCloudManager.Companion.FireStoreCallback<MultiMedia> {
                    override fun onSuccess(result: MultiMedia) {
                        if (!isCreated) {
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    addChat()

                                } catch (e: Exception) {
                                    // Xử lý ngoại lệ khi gặp lỗi
                                    e.printStackTrace()
                                    toast(R.string.please_try_later)
                                }
                            }
                        }
                        ChatManagerFSDB.addMessage(idChatCurrent,
                            message,
                            object : ChatManagerFSDB.Companion.FireStoreCallback<Boolean> {

                                override fun onSuccess(result: Boolean) {
                                    if (result) {
                                        //
                                    } else {
                                        toast(R.string.please_try_later)
                                    }
                                }

                                override fun onFailure(exception: Exception) {
                                    toast(R.string.please_try_later)
                                }
                            })

                    }

                    override fun onFailure(result: MultiMedia) {
                        toast(R.string.please_try_later)
                    }

                })
        }
    }

    private suspend fun addChat() {
        val chat = Chat(
            idChatCurrent,
            Date(),
            "",
            "",
            ChatConstants.BG_DEFAULT,
            true,
            arrayListOf(UserCache.getUser().id, userChat.id),
            "",
            ArrayList()
        )
        ChatManagerFSDB.addChat(chat)
    }

    override fun onRecordFailed(errorMessage: String?) {
        toast(getString(R.string.please_try_later))
        AppUtils.logJsonFromObject(errorMessage!!)
    }

    override fun onReadyForRecord() {
        //
    }

    override fun onClickLongItemChat(position: Int) {
        //
    }

    override fun onClickImage(
        multiMedia: MultiMedia,
        lisMultiMedia: ArrayList<MultiMedia>,
        idChat: String
    ) {
        PreviewMediaDialog.Builder(
            this,
            multiMedia,
            lisMultiMedia,
            UploadFireStorageConstants.TYPE_CHAT,
            UploadFireStorageUtils.getRootURLPostById(idChat),
            null
        ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
            override fun onActionDone(isConfirm: Boolean) {
                //
            }
        }).create().show()
    }
}