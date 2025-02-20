package vn.xdeuhug.seniorsociable.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.TextAppearanceSpan
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.database.CommentManagerFSDB
import vn.xdeuhug.seniorsociable.database.FireCloudManager
import vn.xdeuhug.seniorsociable.database.InteractManagerFSDB
import vn.xdeuhug.seniorsociable.database.NotificationManagerFSDB
import vn.xdeuhug.seniorsociable.database.PostManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.FragmentCommentBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.eventbus.ReloadDataNotImageEventBus
import vn.xdeuhug.seniorsociable.ui.activity.DetailInteractActivity
import vn.xdeuhug.seniorsociable.ui.adapter.CommentAdapter
import vn.xdeuhug.seniorsociable.ui.dialog.PreviewMediaDialog
import vn.xdeuhug.seniorsociable.ui.dialog.WaitDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.clickWithDebounce
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PhotoPickerUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.PostUtils
import vn.xdeuhug.seniorsociable.utils.ReactObjectUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import vn.xdeuhug.seniorsociable.widget.reactbutton.ReactButton
import vn.xdeuhug.seniorsociable.widget.reactbutton.Reaction
import java.util.Date
import java.util.UUID

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 31 / 10 / 2023
 */
class CommentFragment(private var postCurrent: Post, var positionPost: Int) :
    BottomSheetDialogFragment(), CommentAdapter.OnListenerCLick {
    private lateinit var binding: FragmentCommentBinding

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private var localMedia = ArrayList<LocalMedia>()
    private var multiMedia = MultiMedia()

    private var textEntered = false

    private var listComment = ArrayList<Comment>()

    /* paging*/
    private var limit = AppConstants.PAGE_SIZE_10
    private var currentPage = 1
    private var lastVisible: DocumentSnapshot? = null
    private var loading = false
    private var lastPage = false

    /* end */
    private var userIdToNameMap = hashMapOf<String, String>()
    private var nameToIdMap = hashMapOf<String, String>()

    private var isCommentParent =
        true // True insert vào comment cha và ngược lại là rep comment - comment con
    private var idParentComment =
        "" // True insert vào comment cha và ngược lại là rep comment - comment con
    private var positionReplyComment = -1 // Vị trí comment được reply


    @Suppress("DEPRECATION")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentBinding.inflate(inflater, container, false)
//        handleCancelDialog()
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    localMedia.clear()
                    localMedia = PictureSelector.obtainSelectorList(data)
                    val mediaId = UUID.randomUUID().toString()
                    multiMedia.id = mediaId
                    multiMedia.name = "Media $mediaId"
                    multiMedia.url = localMedia[0].realPath
                    multiMedia.realPath = localMedia[0].realPath
                    multiMedia.size = localMedia[0].size
                    multiMedia.height = localMedia[0].height
                    multiMedia.width = localMedia[0].width
                    if (PictureMimeType.isHasVideo(localMedia[0].mimeType)) {
                        multiMedia.type = AppConstants.UPLOAD_VIDEO
                        binding.imvPlay.show()
                    }
                    if (PictureMimeType.isHasImage(localMedia[0].mimeType)) {
                        multiMedia.type = AppConstants.UPLOAD_IMAGE
                        binding.imvPlay.hide()
                    }
                    binding.rlMedia.show()
                    PhotoShowUtils.loadPhotoRound(multiMedia.realPath, binding.imvImage)
                    setStateButtonSend(true)
                } else {
                    // Error and not choose
                }
            }
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleCancelDialog() {
        binding.edtComment.doOnTextChanged { text, start, before, count ->
            textEntered = count > 0
        }
        binding.root.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                // Lấy chiều cao hiện tại của dialog
                val currentHeight = binding.root.height
                // Lấy chiều cao màn hình
                val screenHeight = Resources.getSystem().displayMetrics.heightPixels
                // Nếu vuốt xuống (motionEvent.getY() là vị trí chạm trên màn hình)
                if (motionEvent.y >= currentHeight && motionEvent.y <= screenHeight) {
                    // Kiểm tra xem có văn bản được nhập hay không
                    if (textEntered) {
                        // Không ẩn BottomSheetDialog
                        isCancelable = false
                    } else {
                        // Ẩn BottomSheetDialog khi không có văn bản được nhập
                        isCancelable = true
                        val bottomSheetDialog = dialog!!
                        val parentLayout =
                            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                        parentLayout?.let { it ->
                            val behaviour = BottomSheetBehavior.from(it)
                            behaviour.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                        dismiss()
                    }
                }
            }
            false
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        setClickButtonShare()
        initMapUser()
    }

    private fun setClickButtonShare() {
        binding.imvShare.clickWithDebounce {
            val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://seniorsociable.page.link/post?id=${postCurrent.id}")) // Đường dẫn sâu đến bài viết
                .setDomainUriPrefix("https://seniorsociable.page.link") // Domain của bạn
                .setAndroidParameters(
                    DynamicLink.AndroidParameters.Builder().build()
                ) // Đặt thông tin chỉ cho Android
                .buildDynamicLink()
            val dynamicLinkUri = dynamicLink.uri.toString()
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, dynamicLinkUri)
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, getString(R.string.share_to)))
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
                    requireContext(), DetailInteractActivity::class.java
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
                            ds.color = requireContext().getColor(R.color.gray_900)
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
                            .post(ReloadDataNotImageEventBus(true, positionPost, postCurrent))
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
                requireActivity(), launcher, localMedia
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
                            listMyInteract[0].type, requireContext()
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
                        mostFrequentType1, requireContext()
                    )
                )
                binding.imvSecondReact.setImageDrawable(
                    ReactObjectUtils.getDrawableWithType(
                        mostFrequentType2, requireContext()
                    )
                )


            } else { // size nhỏ hơn = 1
                binding.imvFirstReact.show()
                binding.imvSecondReact.hide()
                if (listMyInteract.isNotEmpty()) {
                    binding.imvFirstReact.setImageDrawable(
                        ReactObjectUtils.getDrawableWithType(
                            listMyInteract[0].type, requireContext()
                        )
                    )
                    binding.tvReactDetails.text = getString(R.string.you)
                } else {
                    binding.tvReactDetails.text =
                        AppUtils.formatFacebookLikes(postCurrent.interacts.size)
                    binding.imvFirstReact.setImageDrawable(
                        ReactObjectUtils.getDrawableWithType(
                            postCurrent.interacts[0].type, requireContext()
                        )
                    )
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataComment() {
        val listUser = ListUserCache.getList()
        commentAdapter = CommentAdapter(requireContext(), listUser)
        commentAdapter.onListenerCLick = this@CommentFragment
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

    private fun setViewNoData(isHaveData: Boolean) {
        if (isHaveData) {
            binding.rvComment.show()
            binding.RlNotData.hide()
        } else {
            binding.rvComment.hide()
            binding.RlNotData.show()
        }
    }

    private fun startShimmer() {
        binding.sflLoadData.show()
        binding.sflLoadData.startShimmer()
        binding.rvComment.hide()
        binding.RlNotData.hide()

    }

    private fun finishShimmer() {
        binding.sflLoadData.stopShimmer()
        binding.sflLoadData.hide()
        binding.rvComment.show()
        binding.RlNotData.hide()

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

    private var dialog: BaseDialog? = null

    private fun showDialog(message: String) {
        dialog =
            WaitDialog.Builder(requireContext()).setCancelable(false).setMessage(message).create()
        dialog!!.show()
    }

    private fun hideDialog() {
        dialog!!.hide()
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
            EventBus.getDefault().post(ReloadDataNotImageEventBus(true, positionPost, postCurrent))
            setDataOnTitle()
        }
    }

    override fun onClick(position: Int, comment: Comment) {
        PreviewMediaDialog.Builder(
            requireContext(),
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
        AppUtils.startDetailUserPage(user, activity)
    }

    override fun onClickDetailReact(position: Int, comment: Comment) {
        val bundle = Bundle()
        val intent = Intent(
            requireContext(), DetailInteractActivity::class.java
        )
        bundle.putString(
            PostConstants.OBJECT_DETAIL_INTERACT, Gson().toJson(comment.interacts)
        )
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onClickInChild(position: Int, comment: Comment) {
        PreviewMediaDialog.Builder(
            requireContext(),
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
            requireContext(), DetailInteractActivity::class.java
        )
        bundle.putString(
            PostConstants.OBJECT_DETAIL_INTERACT, Gson().toJson(comment.interacts)
        )
        intent.putExtras(bundle)
        startActivity(intent)
    }


    @SuppressLint("SetTextI18n")
    private fun setViewReply(userComment: User) {
        AppUtils.setTextTitleReply(requireContext(), binding.tvRepling, userComment)
        if (userComment.id == UserCache.getUser().id) {
            binding.edtComment.setText("")
        } else {
            binding.edtComment.setText("@[${userComment.id}]")
        }
    }


    // COMMENT PARENT
    private fun updateInteractInComment(
        interactNew: Interact, interactOld: Interact, comment: Comment, position: Int
    ) {
        comment.interacts.remove(interactOld)
        comment.interacts.add(interactNew)
        commentAdapter.notifyItemChanged(position)
    }

    private fun removeInteractInComment(
        interactCurrent: Interact, comment: Comment, position: Int
    ) {
        comment.interacts.remove(interactCurrent)
        commentAdapter.notifyItemChanged(position)
    }

    private fun addInteractInComment(
        interactCurrent: Interact, comment: Comment, position: Int
    ) {
        comment.interacts.add(interactCurrent)
        commentAdapter.notifyItemChanged(position)
    }

    // COMMENT CHILD
    private fun updateInteractInChildComment(
        interactNew: Interact, interactOld: Interact, comment: Comment, positionParentComment: Int
    ) {
        comment.interacts.remove(interactOld)
        comment.interacts.add(interactNew)
        commentAdapter.notifyItemChanged(positionParentComment)
    }

    private fun removeInteractInChildComment(
        interactCurrent: Interact, comment: Comment, positionParentComment: Int
    ) {
        comment.interacts.remove(interactCurrent)
        commentAdapter.notifyItemChanged(positionParentComment)
    }

    private fun addInteractInChildComment(
        interactCurrent: Interact, comment: Comment, positionParentComment: Int
    ) {
        comment.interacts.add(interactCurrent)
        commentAdapter.notifyItemChanged(positionParentComment)
    }

}
