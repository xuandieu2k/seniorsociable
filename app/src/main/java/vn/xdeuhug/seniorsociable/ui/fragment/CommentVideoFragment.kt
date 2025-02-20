package vn.xdeuhug.seniorsociable.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.database.FireCloudManager
import vn.xdeuhug.seniorsociable.database.InteractManagerFSDB
import vn.xdeuhug.seniorsociable.database.MovieShortManagerFSDB
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.FragmentCommentBinding
import vn.xdeuhug.seniorsociable.databinding.FragmentCommentVideoBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelMovieShort.MovieShort
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
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
 * @Date: 19 / 12 / 2023
 */
class CommentVideoFragment(private var movieShortCurrent: MovieShort, var positionMovie: Int) :
    BottomSheetDialogFragment(), CommentAdapter.OnListenerCLick {
    private lateinit var binding: FragmentCommentVideoBinding

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private var localMedia = ArrayList<LocalMedia>()
    private var multiMedia = MultiMedia()

    private var textEntered = false


    @Suppress("DEPRECATION")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { item ->
                val behaviour = BottomSheetBehavior.from(item)
                setupFullHeight(item)
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
        binding = FragmentCommentVideoBinding.inflate(inflater, container, false)
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
        setClickButtonRemove()
        setDataComment()
        setClickShowChooseImage()
        setClickSendComment()
        setStateButtonSend(false)
        setEditTextCommentChange()
    }

    private fun setEditTextCommentChange() {
        binding.edtComment.linksClickable = true
        binding.edtComment.autoLinkMask = Linkify.WEB_URLS
        //If the edit text contains previous text with potential links
        Linkify.addLinks(binding.edtComment, Linkify.WEB_URLS)
        binding.edtComment.doOnTextChanged { _, _, _, count ->
            setStateButtonSend(count > 0 || localMedia.isNotEmpty())
        }
    }

    private fun setStateButtonSend(isEnable: Boolean) {
        binding.imvSend.isEnabled = isEnable
    }

    private fun setClickSendComment() {
        binding.imvSend.clickWithDebounce {
            showDialog("")
            val comment = Comment()
            comment.id = UUID.randomUUID().toString()
            comment.idUserComment = UserCache.getUser().id
            comment.timeCreated = Date()
            comment.timeUpdate = Date()
            comment.content = binding.edtComment.text.toString()
            if (localMedia.isNotEmpty()) {
                comment.multiMedia.add(multiMedia)
            }
            movieShortCurrent.comments.add(comment)
            if (localMedia.isNotEmpty()) {
                val storageReference = FirebaseStorage.getInstance().reference
                val storageRef: StorageReference =
                    storageReference.child(UploadFireStorageUtils.getRootURLCommentById(comment.id))
                runBlocking {
                    FireCloudManager.uploadFilesToFirebaseStorage(comment.multiMedia,
                        storageRef,
                        object :
                            FireCloudManager.Companion.FireStoreCallback<ArrayList<MultiMedia>> {
                            override fun onSuccess(result: ArrayList<MultiMedia>) {
                                // Cập nhật post
//                                addComment(comment)
                            }

                            override fun onFailure(result: ArrayList<MultiMedia>) {
                                toast(R.string.please_try_later)
                                hideDialog()
                            }

                        })
                }
            } else {
//                addComment(comment)
            }
        }
    }

//    fun addComment(comment: Comment) {
//        CommentManagerFSDB.addCommentToPost(movieShortCurrent.id,
//            comment,
//            object : CommentManagerFSDB.Companion.FireStoreCallback<Boolean> {
//                @SuppressLint("NotifyDataSetChanged")
//                override fun onSuccess(result: Boolean) {
//                    if (result) {
//                        getPostById(false)
//                        // Thành công
//                    } else {
//                        toast(R.string.please_try_later)
//                        hideDialog()
//                    }
//                }
//
//                override fun onFailure(exception: Exception) {
//                    Timber.tag("Error Update").i(exception)
//                    hideDialog()
//                }
//
//            })
//    }

    private fun getPostById(isChangeReact: Boolean) {
        MovieShortManagerFSDB.getMovieShortById(
            movieShortCurrent.id,
            object : MovieShortManagerFSDB.Companion.MovieShortCallback{
                @SuppressLint("NotifyDataSetChanged")
                override fun onMovieShortFound(movieShort: MovieShort?) {
                    if (movieShort != null) {
                        movieShortCurrent = movieShort
                        commentAdapter.setData(movieShortCurrent.comments)
                        commentAdapter.notifyDataSetChanged()
                        if (!isChangeReact) {
                            resetTextComment()
                            resetImage()
                        }
                        if (movieShortCurrent.comments.isNotEmpty()) {
                            binding.rvComment.show()
                            binding.RlNotData.hide()
                        } else {
                            binding.rvComment.hide()
                            binding.RlNotData.show()
                        }
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

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataComment() {
        lifecycleScope.launch {
            val listUserDeferred = async { UserManagerFSDB.getListUserAsync() }
            val listUser = listUserDeferred.await()
            commentAdapter = CommentAdapter(requireContext(), listUser)
            commentAdapter.onListenerCLick = this@CommentVideoFragment
            // Create recycleView
            AppUtils.initRecyclerViewVertical(binding.rvComment, commentAdapter)
            commentAdapter.setData(movieShortCurrent.comments)
            commentAdapter.notifyDataSetChanged()
            if (movieShortCurrent.comments.isNotEmpty()) {
                binding.rvComment.show()
                binding.RlNotData.hide()
            } else {
                binding.rvComment.hide()
                binding.RlNotData.show()
            }
        }
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
                val listInteract = movieShortCurrent.interacts.filter { it.id == userCurrent.id }

                if (listInteract.isNotEmpty()) {
                    val oldInteract = listInteract.first()

                    val updateResult = InteractManagerFSDB.updateInteractToPost(
                        movieShortCurrent.id.toString(), oldInteract.id, oldInteract
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
                            InteractManagerFSDB.addInteractToPost(movieShortCurrent.id.toString(), interact)
                        if (!addResult) {
                            toast(R.string.please_try_later)
                        }
                    }
                }
            } else {
                // Nếu icon hiện tại là icon mặc định thực hiện xóa icon ra khỏi list icon
                val interactCurrent = movieShortCurrent.interacts.filter { it.id == userCurrent.id }
                removeInteract(interactCurrent.first())

                // Chuyển về luồng chính để cập nhật giao diện người dùng
                withContext(Dispatchers.Main) {
                    // Xóa interact và cập nhật trong Firestore
                    val deleteResult = InteractManagerFSDB.deleteInteractInPost(
                        movieShortCurrent.id.toString(), interactCurrent.first().id
                    )
                    if (!deleteResult) {
                        toast(R.string.please_try_later)
                    }
                }
            }
        }
    }


    private suspend fun updateInteract(interactNew: Interact, interactOld: Interact) {
        movieShortCurrent.interacts.remove(interactOld)
        movieShortCurrent.interacts.add(interactNew)
        setDataOnTitleOnUiThread()
    }

    private suspend fun removeInteract(interactCurrent: Interact) {
        movieShortCurrent.interacts.remove(interactCurrent)
        setDataOnTitleOnUiThread()
    }

    private suspend fun addInteract(interactCurrent: Interact) {
        movieShortCurrent.interacts.add(interactCurrent)
        setDataOnTitleOnUiThread()
    }

    private suspend fun setDataOnTitleOnUiThread() {
//        withContext(Dispatchers.Main) {
//            EventBus.getDefault().post(ReloadDataNotImageEventBus(true, positionPost, movieShortCurrent))
//            setDataOnTitle()
//        }
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
        //
    }

    override fun onReact(
        position: Int,
        comment: Comment,
        reactionCurrent: Reaction,
        defaultReaction: Reaction
    ) {
        //
    }

    override fun clickNameUserInComment(user: User) {
        //
    }

    override fun onClickDetailReact(position: Int, comment: Comment) {
        //
    }

    override fun onClickInChild(position: Int, comment: Comment) {
        //
    }

    override fun onReplyInChild(positionParent: Int, position: Int, comment: Comment) {
        //
    }

    override fun onReactInChild(
        position: Int,
        comment: Comment,
        reactionCurrent: Reaction,
        defaultReaction: Reaction
    ) {
        //
    }

    override fun onClickDetailReactInChild(position: Int, comment: Comment) {
        //
    }
}