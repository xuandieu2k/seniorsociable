package vn.xdeuhug.seniorsociable.post.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.TextAppearanceSpan
import android.text.util.Linkify
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import cn.jzvd.JZDataSource
import com.devs.readmoreoption.ReadMoreOption
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.database.CommentManagerFSDB
import vn.xdeuhug.seniorsociable.database.FireCloudManager
import vn.xdeuhug.seniorsociable.database.InteractManagerFSDB
import vn.xdeuhug.seniorsociable.database.NotificationManagerFSDB
import vn.xdeuhug.seniorsociable.database.PostManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.eventbus.ReloadDataNotImageEventBus
import vn.xdeuhug.seniorsociable.post.databinding.ActivityPostDetailBinding
import vn.xdeuhug.seniorsociable.ui.activity.BrowserActivity
import vn.xdeuhug.seniorsociable.ui.activity.DetailInteractActivity
import vn.xdeuhug.seniorsociable.ui.adapter.CommentAdapter
import vn.xdeuhug.seniorsociable.ui.adapter.MediaAdapter
import vn.xdeuhug.seniorsociable.ui.dialog.PreviewMediaDialog
import vn.xdeuhug.seniorsociable.ui.dialog.TagUserInPostDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.AppUtils.toArrayList
import vn.xdeuhug.seniorsociable.utils.PhotoPickerUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.PostUtils
import vn.xdeuhug.seniorsociable.utils.ReactObjectUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import vn.xdeuhug.seniorsociable.widget.reactbutton.ReactButton
import vn.xdeuhug.seniorsociable.widget.reactbutton.Reaction
import java.util.Date
import java.util.UUID

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 11 / 12 / 2023
 */
class PostDetailActivity : AppActivity(), CommentAdapter.OnListenerCLick,
    MediaAdapter.OnListenerCLick {
    private lateinit var binding: ActivityPostDetailBinding
    private var postID = ""

    //
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private var localMedia = ArrayList<LocalMedia>()
    private var multiMedia = MultiMedia()

    private var postCurrent = Post()

    private var textEntered = false

    private var listComment = ArrayList<Comment>()
    /* paging*/
    private var limit = AppConstants.PAGE_SIZE_10
    private var currentPage = 1
    private var lastVisible: DocumentSnapshot? = null
    private var loading = false
    private var lastPage = false
    //
    /* end */
    private var userIdToNameMap = hashMapOf<String, String>()
    private var nameToIdMap = hashMapOf<String, String>()

    private var isCommentParent =
        true // True insert vào comment cha và ngược lại là rep comment - comment con
    private var idParentComment =
        "" // True insert vào comment cha và ngược lại là rep comment - comment con
    private var positionReplyComment = -1 // Vị trí comment được reply
    override fun getLayoutView(): View {
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setDataForView()
    }

    private fun setUpComment() {
        setViewLoading(true)
        startShimmer()
        setClickButtonRemove()
        setDataOnTitle()
        setUpEventReact()
        setDataComment()
        setClickShowChooseImage()
        setClickSendComment()
        setStateButtonSend(false)
        setEditTextCommentChange()
        showDetailReact()
        setClickCancel()
        initMapUser()
    }

    private fun startShimmer() {
        binding.llShimmer.root.startShimmer()
        binding.llShimmer.root.show()
    }

    private fun finishShimmer() {
        binding.llShimmer.root.stopShimmer()
        binding.llShimmer.root.hide()
    }

    private fun setViewLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.llShimmer.root.show()
            binding.rlData.hide()
        } else {
            binding.llShimmer.root.hide()
            binding.rlData.show()
        }
    }

    private fun setDataForView() {
        if(intent.getStringExtra(AppConstants.POST_ID) != null){
            postID = intent.getStringExtra(AppConstants.POST_ID)!!
            binding.llPostNotExist.rlNotPost.hide()
            PostManagerFSDB.getPostById(postID, object : PostManagerFSDB.Companion.PostCallback {
                override fun onPostFound(post: Post?) {
                    if(post != null)
                    {
                        postCurrent = post
                        setUpPost()
                        setUpComment()
                        postDelayed({
                            finishShimmer()
                        setViewLoading(false)
                        },1000)
                    }else{
                        binding.llPostNotExist.rlNotPost.show()
                        binding.llShimmer.root.hide()
                        binding.rlData.hide()
                    }
                }

                override fun onFailure(exception: Exception) {
                    toast(R.string.please_try_later)
                    postDelayed({
                        finishShimmer()
                        setViewLoading(false)
                    },1000)
                }

            })
        }else{
            binding.llPostNotExist.rlNotPost.show()
            binding.llShimmer.root.hide()
            binding.rlData.hide()
        }
    }

    private fun setUpPost() {
        val item = postCurrent
        val listUser = ListUserCache.getList()
        val list = listUser.filter { it.id in item.idUserTags }.toArrayList() // List gán thẻ
        val userPost = listUser.firstOrNull { it.id == item.idUserPost }
        PhotoShowUtils.loadAvatarImage(
            UploadFireStorageUtils.getRootURLAvatarById(userPost!!.id),
            userPost.avatar,
            binding.imvAuthor
        )
        AppUtils.singleTextView(
            postCurrent.typePost,
            getContext(),
            binding.tvAuthor,
            userPost,
            list,
            item.address,
            object :
                AppUtils.OnListenerClickTagUser {
                override fun clickNameUserInPost(user: User) {
                    if (user.id == UserCache.getUser().id) {
                        val intent = Intent(this@PostDetailActivity, Class.forName(ModuleClassConstants.PERSONAL_ACTIVITY))
                        intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                        startActivity(intent)
                    } else {
                        val intent =
                            Intent(this@PostDetailActivity, Class.forName(ModuleClassConstants.PERSONAL_MEMBER_ACTIVITY))
                        intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                        startActivity(intent)
                    }
                }

                override fun clickListUser(listUserTag: ArrayList<User>) {
                    TagUserInPostDialog.Builder(getContext(), listUserTag)
                        .onActionDone(object : TagUserInPostDialog.Builder.OnActionDone {
                            override fun onActionDone(isConfirm: Boolean) {
                                //
                            }

                            override fun onClickUser(user: User) {
                                if (user.id == UserCache.getUser().id) {
                                    val intent =
                                        Intent(this@PostDetailActivity, Class.forName(ModuleClassConstants.PERSONAL_ACTIVITY))
                                    intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                                    startActivity(intent)
                                } else {
                                    val intent = Intent(
                                        this@PostDetailActivity,
                                        Class.forName(ModuleClassConstants.PERSONAL_MEMBER_ACTIVITY)
                                    )
                                    intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                                    startActivity(intent)
                                }
                            }

                        }).create().show()
                }

                override fun clickAddress(address: Address) {
                    val latitude = address.latitude
                    val longitude = address.longitude
                    val locationName = address.address

                    // Tạo Uri với thông tin vị trí
                    val gmmIntentUri: Uri =
                        Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($locationName)")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (mapIntent.resolveActivity(getContext().packageManager) != null) {
                        startActivity(mapIntent)
                    } else {
                        BrowserActivity.start(
                            getContext(),
                            "https://www.google.com/maps?q=$latitude,$longitude",
                            ""
                        )
                    }
                }

            })
        val mediaAdapter = MediaAdapter(getContext(), userPost, item.typePost, item.id)
        mediaAdapter.onListenerCLick = this@PostDetailActivity
        AppUtils.setMedia(binding.rvMedia, item.multiMedia, mediaAdapter)
        PostUtils.setViewTypePost(item.type, binding.imvTypePost)
        if (item.multiMedia.isNotEmpty()) {
            binding.rvMedia.show()
        } else {
            binding.rvMedia.hide()
        }
        if (item.idsUserShare.isNotEmpty() && item.comments.isNotEmpty()) {
            binding.tvBigDot.show()
        } else {
            binding.tvBigDot.hide()
        }
        binding.tvTimePost.text = TimeUtils.timeAgoChat(getContext(), item.timeCreated)
        // OR using options to customize
        val readMoreOption: ReadMoreOption =
            ReadMoreOption.Builder(getContext()).textLength(300) // OR
                .moreLabel(getString(R.string.see_more)!!)
                .lessLabel(getString(R.string.hide_less)!!)
                .moreLabelColor(getColor(R.color.blue_700))
                .lessLabelColor(getColor(R.color.red_app_senior_sociable))
                .labelUnderLine(false).expandAnimation(true).build()

        readMoreOption.addReadMoreTo(binding.tvContent, item.content)
        setTypePost(item)
        //
//        setDataLayoutInteract(item)
    }

    override fun initData() {
        //
    }

    private fun setTypePost(item: Post) {
        when (item.type) {
            PostConstants.TYPE_PRIVATE -> {
                binding.imvTypePost.setImageResource(R.drawable.ic_private)
            }

            PostConstants.TYPE_FRIEND -> {
                binding.imvTypePost.setImageResource(R.drawable.ic_friend)
            }

            PostConstants.TYPE_PUBLIC -> {
                binding.imvTypePost.setImageResource(R.drawable.ic_public)
            }
        }
    }

    // ### ViewComment
    private fun initMapUser() {
        ListUserCache.getList().forEach {
            userIdToNameMap[it.id] = it.name
            nameToIdMap[it.name] = it.id

        }
    }

    private fun setClickCancel() {
        binding.tvCancelReply.clickWithDebounce {
            idParentComment = ""
            isCommentParent = true
            binding.llReply.hide()
            binding.edtComment.setText("")
        }
    }

    private fun showDetailReact() {
        binding.llInteract.clickWithDebounce {
            if (postCurrent.comments.isNotEmpty() || postCurrent.interacts.isNotEmpty()) {
                val bundle = Bundle()
                val intent = Intent(
                    getContext(), DetailInteractActivity::class.java
                )
                bundle.putString(
                    PostConstants.OBJECT_DETAIL_INTERACT, Gson().toJson(postCurrent.interacts)
                )
                intent.putExtras(bundle)
                startActivity(intent)
            }
        }
    }

    private fun updateEditTextWithNames(text: Editable?) {
        text?.let {
            val spannableStringBuilder = SpannableStringBuilder(text)
            val pattern = Regex("@\\[(\\w+)]")
            val matches = pattern.findAll(text)

            for (match in matches) {
                val userId = match.groupValues[1]
                val userName = userIdToNameMap[userId]

                if (userName != null) {
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            //
                            //
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false // Loại bỏ gạch chân
                            ds.isFakeBoldText = true
                            ds.color = getContext().getColor(R.color.gray_900)
                        }
                    }

                    val textAppearanceSpan = TextAppearanceSpan(null, 0, 0, null, null)
                    spannableStringBuilder.setSpan(
                        textAppearanceSpan,
                        match.range.first,
                        match.range.last + 1,
                        0
                    )
                    spannableStringBuilder.setSpan(
                        clickableSpan,
                        match.range.first,
                        match.range.last + 1,
                        0
                    )
                    spannableStringBuilder.replace(
                        match.range.first,
                        match.range.last + 1,
                        userName
                    )
                    spannableStringBuilder.append(" ")
                }
            }

            if (spannableStringBuilder.toString() != binding.edtComment.text.toString()) {
                binding.edtComment.text = spannableStringBuilder
            }
            binding.edtComment.movementMethod = LinkMovementMethod.getInstance()
            binding.edtComment.requestFocus()
            binding.edtComment.setSelection(binding.edtComment.text!!.length)
        }
    }

    private fun setEditTextCommentChange() {
        binding.edtComment.linksClickable = true
        binding.edtComment.autoLinkMask = Linkify.WEB_URLS
        //If the edit text contains previous text with potential links
        Linkify.addLinks(binding.edtComment, Linkify.WEB_URLS)
        binding.edtComment.doOnTextChanged { text, start, before, count ->
            updateEditTextWithNames(binding.edtComment.text)
            setStateButtonSend(count > 0 || localMedia.isNotEmpty())
        }

    }

    private fun setStateButtonSend(isEnable: Boolean) {
        binding.imvSend.isEnabled = isEnable
    }

    private fun createComment():Comment
    {
        val comment = Comment()
        comment.id = UUID.randomUUID().toString()
        comment.idUserComment = UserCache.getUser().id
        comment.timeCreated = Date()
        comment.timeUpdate = Date()
        comment.content = binding.edtComment.text.toString().trim()
        if (localMedia.isNotEmpty()) {
            comment.multiMedia.add(multiMedia)
        }
        return comment
    }

    private fun createReplyComment():Comment
    {
        val comment = Comment()
        comment.id = UUID.randomUUID().toString()
        comment.idParent = idParentComment // Comment cha
        comment.idUserComment = UserCache.getUser().id
        comment.timeCreated = Date()
        comment.timeUpdate = Date()
        comment.content = AppUtils.mapClickableSpansToIds(
            binding.edtComment.text!!,
            AppUtils.findClickableSpans(binding.edtComment.text!!),
            nameToIdMap
        )
        if (localMedia.isNotEmpty()) {
            comment.multiMedia.add(multiMedia)
        }
        return comment
    }

    private fun setClickSendComment() {
        binding.imvSend.clickWithDebounce {
            if (isCommentParent) {
                showDialog("")
                val comment = createComment()
                postCurrent.comments.add(comment)
                if (localMedia.isNotEmpty()) {
                    val storageReference = FirebaseStorage.getInstance().reference
                    val storageRef: StorageReference =
                        storageReference.child(UploadFireStorageUtils.getRootURLCommentById(comment.id))
                    // Upload hình
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            FireCloudManager.uploadFilesToFirebaseStorage(comment.multiMedia,
                                storageRef,
                                object :
                                    FireCloudManager.Companion.FireStoreCallback<ArrayList<MultiMedia>> {
                                    override fun onSuccess(result: ArrayList<MultiMedia>) {
                                        // Cập nhật post
                                        addComment(comment)
                                    }

                                    override fun onFailure(result: ArrayList<MultiMedia>) {
                                        toast(R.string.please_try_later)
                                        hideDialog()
                                    }

                                })
                        } catch (e: Exception) {
                            toast(R.string.please_try_later)
                            hideDialog()
                        }
                    }
                } else {
                    addComment(comment)
                }
            } else { // Rep comment
                showDialog("")
                val comment = createReplyComment()
                listComment[positionReplyComment].comments.add(comment)
                if (localMedia.isNotEmpty()) {
                    val storageReference = FirebaseStorage.getInstance().reference
                    val storageRef: StorageReference =
                        storageReference.child(UploadFireStorageUtils.getRootURLCommentById(comment.id))
                    // Upload hình
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            FireCloudManager.uploadFilesToFirebaseStorage(comment.multiMedia,
                                storageRef,
                                object :
                                    FireCloudManager.Companion.FireStoreCallback<ArrayList<MultiMedia>> {
                                    override fun onSuccess(result: ArrayList<MultiMedia>) {
                                        // Cập nhật post
                                        addChildComment(comment)
                                    }

                                    override fun onFailure(result: ArrayList<MultiMedia>) {
                                        toast(R.string.please_try_later)
                                        hideDialog()
                                    }

                                })
                        } catch (e: Exception) {
                            toast(R.string.please_try_later)
                            hideDialog()
                        }
                    }
                } else {
                    addChildComment(comment)
                }
            }
        }
    }

    fun addChildComment(comment: Comment) {
        CommentManagerFSDB.addCommentToCommentInPost(postCurrent.id,
            comment.idParent,
            comment,
            object : CommentManagerFSDB.Companion.FireStoreCallback<Boolean> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onSuccess(result: Boolean) {
                    if (result) {
                        commentAdapter.notifyItemChanged(positionReplyComment)
                        hideDialog()
                        resetPositionCommentReply()
                        CoroutineScope(Dispatchers.Main).launch {
                            addNotificationInChildComment(comment)
                        }
                        // Thành công
                    } else {
                        toast(R.string.please_try_later)
                        hideDialog()
                        resetPositionCommentReply()
                    }
                }

                override fun onFailure(exception: Exception) {
                    Timber.tag("Error Update").i(exception)
                    toast(R.string.please_try_later)
                    hideDialog()
                    resetPositionCommentReply()
                }

            })
    }

    fun resetPositionCommentReply() {
        positionReplyComment = -1
        binding.llReply.hide()
        resetTextComment()
        resetImage()
    }

    fun addComment(comment: Comment) {
        CommentManagerFSDB.addCommentToPost(postCurrent.id,
            comment,
            object : CommentManagerFSDB.Companion.FireStoreCallback<Boolean> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onSuccess(result: Boolean) {
                    if (result) {
                        getPostById(false)
                        CoroutineScope(Dispatchers.Main).launch {
                            addNotification(comment)
                        }
                        // Thành công
                    } else {
                        toast(R.string.please_try_later)
                        hideDialog()
                    }
                }

                override fun onFailure(exception: Exception) {
                    Timber.tag("Error Update").i(exception)
                    toast(R.string.please_try_later)
                    hideDialog()
                }

            })
    }

    private suspend fun addNotification(comment: Comment) {
        if (postCurrent.idUserPost != UserCache.getUser().id) {
            val notification = Notification(
                UUID.randomUUID().toString(),
                UserCache.getUser().id,
                postCurrent.id,
                comment.id,
                Date(),
                AppConstants.IS_UNREAD,
                AppConstants.NOTIFICATION_FROM_POST_COMMENT,
                -1,
                "đã bình luận bài viết của bạn",
                "",
                true
            )
            NotificationManagerFSDB.addNotification(postCurrent.idUserPost, notification)
        }
    }

    private suspend fun addNotificationInChildComment(comment: Comment) {
        if (postCurrent.idUserPost != UserCache.getUser().id) {
            val notification = Notification(
                UUID.randomUUID().toString(),
                UserCache.getUser().id,
                postCurrent.id,
                comment.idParent,
                Date(),
                AppConstants.IS_UNREAD,
                AppConstants.NOTIFICATION_FROM_POST_COMMENT,
                -1,
                "đã trả lời bình luận của bạn",
                "",
                true
            )
            NotificationManagerFSDB.addNotification(postCurrent.idUserPost, notification)
        }
    }


    private fun getPostById(isChangeReact: Boolean) {
        PostManagerFSDB.getPostById(postCurrent.id,
            object : PostManagerFSDB.Companion.PostCallback {
                @SuppressLint("NotifyDataSetChanged")
                override fun onPostFound(post: Post?) {
                    if (post != null) {
                        postCurrent = post
                        commentAdapter.setData(postCurrent.comments)
                        commentAdapter.notifyDataSetChanged()
                        if (isChangeReact) {
                            setDataOnTitle()
                        } else {
                            resetTextComment()
                            resetImage()
                        }
                        if (postCurrent.comments.isNotEmpty()) {
                            binding.rvComment.show()
                            binding.RlNotData.hide()
                        } else {
                            binding.rvComment.hide()
                            binding.RlNotData.show()
                        }
                        EventBus.getDefault()
                            .post(ReloadDataNotImageEventBus(true, -1, postCurrent))
                        hideDialog()
                    } else {
                        toast(R.string.please_try_later)
                        hideDialog()
                    }
                }

                override fun onFailure(exception: Exception) {
                    Timber.tag("Get Post Exception").i(exception)
                }

            })
    }

    private fun resetTextComment() {
        binding.edtComment.setText("")
    }

    private fun setClickButtonRemove() {
        binding.imvClose.clickWithDebounce {
            resetImage()
        }
    }

    private fun resetImage() {
        binding.rlMedia.hide()
        localMedia.clear()
        PhotoShowUtils.loadPhotoRound(multiMedia.realPath, binding.imvImage)
        setStateButtonSend(localMedia.isNotEmpty())
    }

    private fun setClickShowChooseImage() {
        binding.imvChoosePicture.setOnClickListener {
            PhotoPickerUtils.showImagePickerUploadComment(
                this, launcher, localMedia
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDataOnTitle() {
        binding.btnReact.setDefaultReactionNotListener(ReactObjectUtils.defaultReact)
        binding.btnReact.text = ""
        if (postCurrent.interacts.isEmpty()) {
            binding.imvFirstReact.hide()
            binding.imvSecondReact.hide()
            binding.tvReactDetails.text = getString(R.string.become_first_person_interact_post)
        } else {
            val listMyInteract = postCurrent.interacts.filter { it.id == UserCache.getUser().id }
            if (listMyInteract.isNotEmpty()) {
                binding.btnReact.setCurrentReactionNotListener(
                    ReactObjectUtils.getReactionsWithType(
                        listMyInteract[0].type
                    )
                )
                binding.btnReact.text = ""
            } else {
                binding.btnReact.setCurrentReactionNotListener(ReactObjectUtils.defaultReact)
                binding.btnReact.text = ""
            }
            // Nếu size lớn hơn 1
            if (postCurrent.interacts.size > 1) {
                binding.imvFirstReact.show()
                binding.imvSecondReact.show()
                if (listMyInteract.isNotEmpty()) {
                    binding.imvFirstReact.setImageDrawable(
                        ReactObjectUtils.getDrawableWithType(
                            listMyInteract[0].type, getContext()
                        )
                    )
                    binding.tvReactDetails.text =
                        "${getString(R.string.you)} ${getString(R.string.and)} ${
                            AppUtils.formatFacebookLikes(postCurrent.interacts.size - 1)
                        } ${getString(R.string.other_person)}"
                } else {
                    binding.tvReactDetails.text =
                        AppUtils.formatFacebookLikes(postCurrent.interacts.size)
                }
                val mostFrequentInteractions =
                    PostUtils.findMostFrequentInteractions(postCurrent.interacts)
                val mostFrequentType1 = mostFrequentInteractions.first
                val mostFrequentType2 = mostFrequentInteractions.second
                binding.imvFirstReact.setImageDrawable(
                    ReactObjectUtils.getDrawableWithType(
                        mostFrequentType1, getContext()
                    )
                )
                binding.imvSecondReact.setImageDrawable(
                    ReactObjectUtils.getDrawableWithType(
                        mostFrequentType2, getContext()
                    )
                )


            } else { // size nhỏ hơn = 1
                binding.imvFirstReact.show()
                binding.imvSecondReact.hide()
                if (listMyInteract.isNotEmpty()) {
                    binding.imvFirstReact.setImageDrawable(
                        ReactObjectUtils.getDrawableWithType(
                            listMyInteract[0].type, getContext()
                        )
                    )
                    binding.tvReactDetails.text = getString(R.string.you)
                } else {
                    binding.tvReactDetails.text =
                        AppUtils.formatFacebookLikes(postCurrent.interacts.size)
                    binding.imvFirstReact.setImageDrawable(
                        ReactObjectUtils.getDrawableWithType(
                            postCurrent.interacts[0].type, getContext()
                        )
                    )
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataComment() {
        val listUser = ListUserCache.getList()
        commentAdapter = CommentAdapter(getContext(), listUser)
        commentAdapter.onListenerCLick = this@PostDetailActivity
        // Create recycleView

        commentAdapter.setData(listComment)
        AppUtils.initRecyclerViewVertical(binding.rvComment, commentAdapter)
        getDataComment()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataComment() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val listTriple = async {
                    CommentManagerFSDB.getAllCommentByIdPost(
                        postCurrent.id,
                        limit,
                        lastVisible
                    )
                }.await()
                if (currentPage == 1) {
                    finishShimmer()
                    listComment.clear()
                }
                // UI update code here
                listComment.addAll(listTriple.first)
                commentAdapter.notifyDataSetChanged()
                lastVisible = listTriple.second
                lastPage = listTriple.third
                loading = false
                setViewNoData(listComment.isNotEmpty())
            } catch (e: Exception) {
                // Xử lý ngoại lệ khi gặp lỗi
                e.printStackTrace()
                toast(R.string.please_try_later)
                loading = false
                finishShimmer()
                setViewNoData(listComment.isNotEmpty())
            }
        }
    }

    private fun setUpEventReact() {
        binding.btnReact.text = ""
        binding.btnReact.setReactions(*ReactObjectUtils.reactions)
        binding.btnReact.setEnableReactionTooltip(true)

        binding.btnReact.setOnReactionChangeListener { reaction ->
            binding.btnReact.text = ""
            changeReact(reaction, binding.btnReact.defaultReaction)
            Timber.tag("").d("onReactionChange: %s", reaction.reactText)
        }

        binding.btnReact.setOnReactionDialogStateListener(object :
            ReactButton.OnReactionDialogStateListener {
            override fun onDialogOpened() {
                binding.btnReact.text = ""
                Timber.tag("").d("onDialogOpened")
            }

            override fun onDialogDismiss() {
                binding.btnReact.text = ""
                Timber.tag("").d("onDialogDismiss")
            }
        })
    }

    private fun setViewNoData(isHaveData: Boolean) {
        if (isHaveData) {
            binding.rvComment.show()
            binding.RlNotData.hide()
        } else {
            binding.rvComment.hide()
            binding.RlNotData.show()
        }
    }

    fun changeReact(reaction: Reaction, defaultReaction: Reaction) {
        val userCurrent = UserCache.getUser()

        // Sử dụng coroutine để thực hiện các tác vụ bất đồng bộ
        lifecycleScope.launch(Dispatchers.IO) {
            // Nếu icon hiện tại khác với icon mặc định
            if (reaction != defaultReaction) {
                val typeInteract = ReactObjectUtils.getTypeByReact(reaction)
                val interact = Interact(userCurrent.id, typeInteract, Date())
                val listInteract = postCurrent.interacts.filter { it.id == userCurrent.id }

                if (listInteract.isNotEmpty()) {
                    val oldInteract = listInteract.first()
                    oldInteract.type = typeInteract
                    oldInteract.timeCreated = interact.timeCreated
                    updateInteract(oldInteract, listInteract.first())

                    val updateResult = InteractManagerFSDB.updateInteractToPost(
                        postCurrent.id, oldInteract.id, oldInteract
                    )
                    if (!updateResult) {
                        withContext(Dispatchers.Main) {
                            toast(R.string.please_try_later)
                        }
                    }
                } else {
                    // Thêm mới interact và cập nhật trong Firestore
                    addInteract(interact)

                    // Chuyển về luồng chính để cập nhật giao diện người dùng
                    withContext(Dispatchers.Main) {
                        val addResult =
                            InteractManagerFSDB.addInteractToPost(postCurrent.id, interact)
                        if (!addResult) {
                            toast(R.string.please_try_later)
                        }
                    }
                }
            } else {
                // Nếu icon hiện tại là icon mặc định thực hiện xóa icon ra khỏi list icon
                val interactCurrent = postCurrent.interacts.filter { it.id == userCurrent.id }
                removeInteract(interactCurrent.first())

                // Chuyển về luồng chính để cập nhật giao diện người dùng
                withContext(Dispatchers.Main) {
                    // Xóa interact và cập nhật trong Firestore
                    val deleteResult = InteractManagerFSDB.deleteInteractInPost(
                        postCurrent.id, interactCurrent.first().id
                    )
                    if (!deleteResult) {
                        toast(R.string.please_try_later)
                    }
                }
            }
        }
    }


    private suspend fun updateInteract(interactNew: Interact, interactOld: Interact) {
        postCurrent.interacts.remove(interactOld)
        postCurrent.interacts.add(interactNew)
        setDataOnTitleOnUiThread()
    }

    private suspend fun removeInteract(interactCurrent: Interact) {
        postCurrent.interacts.remove(interactCurrent)
        setDataOnTitleOnUiThread()
    }

    private suspend fun addInteract(interactCurrent: Interact) {
        postCurrent.interacts.add(interactCurrent)
        setDataOnTitleOnUiThread()
    }

    private suspend fun setDataOnTitleOnUiThread() {
        withContext(Dispatchers.Main) {
            EventBus.getDefault().post(ReloadDataNotImageEventBus(true, -1, postCurrent))
            setDataOnTitle()
        }
    }

    override fun onClick(position: Int, comment: Comment) {
        PreviewMediaDialog.Builder(
            getContext(),
            comment.multiMedia[0],
            arrayListOf(comment.multiMedia[0]),
            UploadFireStorageConstants.TYPE_COMMENT,
            "",
            null
        ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
            override fun onActionDone(isConfirm: Boolean) {
                //
            }
        }).create().show()
    }

    override fun onReply(position: Int, comment: Comment) {
        idParentComment = comment.id
        isCommentParent = false // Comment con
        positionReplyComment = position
        val userComment = ListUserCache.getList().first { it.id == comment.idUserComment }
        binding.llReply.show()
        setViewReply(userComment)
    }

    override fun onReact(
        position: Int, comment: Comment, reactionCurrent: Reaction, defaultReaction: Reaction
    ) {
        //
        val userCurrent = UserCache.getUser()

        // Sử dụng coroutine để thực hiện các tác vụ bất đồng bộ
        lifecycleScope.launch(Dispatchers.IO) {
            // Nếu icon hiện tại khác với icon mặc định
            if (reactionCurrent != defaultReaction) {
                val typeInteract = ReactObjectUtils.getTypeByReact(reactionCurrent)
                val interact = Interact(userCurrent.id, typeInteract, Date())
                val listInteract = comment.interacts.filter { it.id == userCurrent.id }

                if (listInteract.isNotEmpty()) {
                    val oldInteract = listInteract.first()
                    oldInteract.type = typeInteract
                    oldInteract.timeCreated = interact.timeCreated
                    updateInteractInComment(oldInteract, listInteract.first(), comment, position)

                    val updateResult = CommentManagerFSDB.updateInteractToCommentInPost(
                        postCurrent.id, comment.id, oldInteract
                    )
                    if (!updateResult) {
                        withContext(Dispatchers.Main) {
                            toast(R.string.please_try_later)
                        }
                    }
                } else {
                    // Thêm mới interact và cập nhật trong Firestore
                    addInteractInComment(interact, comment, position)

                    // Chuyển về luồng chính để cập nhật giao diện người dùng
                    withContext(Dispatchers.Main) {
                        val addResult = CommentManagerFSDB.addInteractToCommentInPost(
                            postCurrent.id, comment.id, interact
                        )
                        if (!addResult) {
                            toast(R.string.please_try_later)
                        }
                    }
                }
            } else {
                // Nếu icon hiện tại là icon mặc định thực hiện xóa icon ra khỏi list icon
                val interactCurrent = comment.interacts.filter { it.id == userCurrent.id }
                removeInteractInComment(interactCurrent.first(), comment, position)

                // Chuyển về luồng chính để cập nhật giao diện người dùng
                withContext(Dispatchers.Main) {
                    // Xóa interact và cập nhật trong Firestore
                    val deleteResult = CommentManagerFSDB.deleteInteractToCommentInPost(
                        postCurrent.id, comment.id, interactCurrent.first().id
                    )
                    if (!deleteResult) {
                        toast(R.string.please_try_later)
                    }
                }
            }
        }
    }

    override fun clickNameUserInComment(user: User) {
        AppUtils.startDetailUserPage(user, this)
    }

    override fun onClickDetailReact(position: Int, comment: Comment) {
        val bundle = Bundle()
        val intent = Intent(
            getContext(), DetailInteractActivity::class.java
        )
        bundle.putString(
            PostConstants.OBJECT_DETAIL_INTERACT, Gson().toJson(comment.interacts)
        )
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onClickInChild(position: Int, comment: Comment) {
        PreviewMediaDialog.Builder(
            getContext(),
            comment.multiMedia[0],
            arrayListOf(comment.multiMedia[0]),
            UploadFireStorageConstants.TYPE_COMMENT,
            "",
            null
        ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
            override fun onActionDone(isConfirm: Boolean) {
                //
            }
        }).create().show()
    }

    override fun onReplyInChild(positionParent: Int, position: Int, comment: Comment) {
        idParentComment = comment.idParent
        isCommentParent = false // Comment con
        positionReplyComment = positionParent
        val userComment = ListUserCache.getList().first { it.id == comment.idUserComment }
        binding.llReply.show()
        setViewReply(userComment)
    }

    override fun onReactInChild(
        position: Int,
        comment: Comment,
        reactionCurrent: Reaction,
        defaultReaction: Reaction
    ) {
        //
        val userCurrent = UserCache.getUser()

        // Sử dụng coroutine để thực hiện các tác vụ bất đồng bộ
        lifecycleScope.launch(Dispatchers.IO) {
            // Nếu icon hiện tại khác với icon mặc định
            if (reactionCurrent != defaultReaction) {
                val typeInteract = ReactObjectUtils.getTypeByReact(reactionCurrent)
                val interact = Interact(userCurrent.id, typeInteract, Date())
                val listInteract = comment.interacts.filter { it.id == userCurrent.id }

                if (listInteract.isNotEmpty()) {
                    val oldInteract = listInteract.first()
                    oldInteract.type = typeInteract
                    oldInteract.timeCreated = interact.timeCreated
                    updateInteractInChildComment(
                        oldInteract,
                        listInteract.first(),
                        comment,
                        position
                    )

                    val updateResult = CommentManagerFSDB.updateInteractToChildCommentInPost(
                        postCurrent.id, comment.idParent, comment.id, oldInteract
                    )
                    if (!updateResult) {
                        withContext(Dispatchers.Main) {
                            toast(R.string.please_try_later)
                        }
                    }
                } else {
                    // Thêm mới interact và cập nhật trong Firestore
                    addInteractInChildComment(interact, comment, position)

                    // Chuyển về luồng chính để cập nhật giao diện người dùng
                    withContext(Dispatchers.Main) {
                        val addResult = CommentManagerFSDB.addInteractToChildCommentInPost(
                            postCurrent.id, comment.idParent, comment.id, interact
                        )
                        if (!addResult) {
                            toast(R.string.please_try_later)
                        }
                    }
                }
            } else {
                // Nếu icon hiện tại là icon mặc định thực hiện xóa icon ra khỏi list icon
                val interactCurrent = comment.interacts.filter { it.id == userCurrent.id }
                removeInteractInChildComment(interactCurrent.first(), comment, position)

                // Chuyển về luồng chính để cập nhật giao diện người dùng
                withContext(Dispatchers.Main) {
                    // Xóa interact và cập nhật trong Firestore
                    val deleteResult = CommentManagerFSDB.deleteInteractToChildCommentInPost(
                        postCurrent.id, comment.idParent, comment.id, interactCurrent.first().id
                    )
                    if (!deleteResult) {
                        toast(R.string.please_try_later)
                    }
                }
            }
        }
    }

    override fun onClickDetailReactInChild(position: Int, comment: Comment) {
        val bundle = Bundle()
        val intent = Intent(
            getContext(), DetailInteractActivity::class.java
        )
        bundle.putString(
            PostConstants.OBJECT_DETAIL_INTERACT, Gson().toJson(comment.interacts)
        )
        intent.putExtras(bundle)
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun setViewReply(userComment: User) {
        AppUtils.setTextTitleReply(getContext(), binding.tvRepling, userComment)
        if (userComment.id == UserCache.getUser().id) {
            binding.edtComment.setText("")
        } else {
            binding.edtComment.setText("@[${userComment.id}]")
        }
    }


    // COMMENT PARENT
    private suspend fun updateInteractInComment(
        interactNew: Interact, interactOld: Interact, comment: Comment, position: Int
    ) {
        comment.interacts.remove(interactOld)
        comment.interacts.add(interactNew)
        commentAdapter.notifyItemChanged(position)
    }

    private suspend fun removeInteractInComment(
        interactCurrent: Interact, comment: Comment, position: Int
    ) {
        comment.interacts.remove(interactCurrent)
        commentAdapter.notifyItemChanged(position)
    }

    private suspend fun addInteractInComment(
        interactCurrent: Interact, comment: Comment, position: Int
    ) {
        comment.interacts.add(interactCurrent)
        commentAdapter.notifyItemChanged(position)
    }

    // COMMENT CHILD
    private suspend fun updateInteractInChildComment(
        interactNew: Interact, interactOld: Interact, comment: Comment, positionParentComment: Int
    ) {
        comment.interacts.remove(interactOld)
        comment.interacts.add(interactNew)
        commentAdapter.notifyItemChanged(positionParentComment)
    }

    private suspend fun removeInteractInChildComment(
        interactCurrent: Interact, comment: Comment, positionParentComment: Int
    ) {
        comment.interacts.remove(interactCurrent)
        commentAdapter.notifyItemChanged(positionParentComment)
    }

    private suspend fun addInteractInChildComment(
        interactCurrent: Interact, comment: Comment, positionParentComment: Int
    ) {
        comment.interacts.add(interactCurrent)
        commentAdapter.notifyItemChanged(positionParentComment)
    }

    override fun onClick(
        jzDataSource: JZDataSource?,
        multiMedia: MultiMedia,
        lisMultiMedia: ArrayList<MultiMedia>,
        idPost: String
    ) {
        if (jzDataSource == null) {
            PreviewMediaDialog.Builder(
                getContext(),
                multiMedia,
                lisMultiMedia,
                UploadFireStorageConstants.TYPE_POST,
                UploadFireStorageUtils.getRootURLPostById(idPost),
                null
            ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }
            }).create().show()
        } else {
            val dialog = PreviewMediaDialog.Builder(
                getContext(),
                multiMedia,
                lisMultiMedia,
                UploadFireStorageConstants.TYPE_POST,
                UploadFireStorageUtils.getRootURLPostById(idPost),
                jzDataSource
            ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }
            })
            dialog.create().show()
        }
    }
}