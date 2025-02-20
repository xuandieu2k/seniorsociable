package vn.xdeuhug.seniorsociable.post.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.dimen
import org.jetbrains.anko.topPadding
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.database.FireCloudManager
import vn.xdeuhug.seniorsociable.database.PostManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.Album
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.eventbus.AddedPostEventBus
import vn.xdeuhug.seniorsociable.post.databinding.ActivityCreatePostBinding
import vn.xdeuhug.seniorsociable.ui.adapter.MultiMediaAdapter
import vn.xdeuhug.seniorsociable.ui.dialog.AlbumDialog
import vn.xdeuhug.seniorsociable.ui.dialog.ChooseOptionBottomDialog
import vn.xdeuhug.seniorsociable.ui.dialog.ObjectPostDialog
import vn.xdeuhug.seniorsociable.ui.dialog.PlaceDialog
import vn.xdeuhug.seniorsociable.ui.dialog.PreviewMediaDialog
import vn.xdeuhug.seniorsociable.ui.dialog.TagUserDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.toArrayList
import vn.xdeuhug.seniorsociable.utils.PhotoPickerUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import java.util.UUID.*

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 11 / 10 / 2023
 */
class CreatePostActivity : AppActivity(), OnMapReadyCallback, MultiMediaAdapter.OnListenerCLick {
    private lateinit var binding: ActivityCreatePostBinding
    private var listUser = ArrayList<User>()
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var localMedia = ArrayList<LocalMedia>()
    private var listMedia = ArrayList<MultiMedia>()
    private var address: Address? = null
    private lateinit var googleMap: GoogleMap
    private var statusPost = 0
    private var albumCurrent: Album? = null

    //
    private lateinit var multiMediaAdapter: MultiMediaAdapter
    override fun getLayoutView(): View {
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        AppUtils.setFontTypeFaceTitleBar(this, binding.tbCreatePost)
        setStatusPost()
        setAlbum()
        setLayoutRightTitleBar()
        setUpData()
        setClickTabChooseButton()
        setUpMediaAdapter()
        setChooseMedia()
        validText()
        setClickButtonPost()
    }




    private fun createPost(postId: String): Post {
        val post = Post()
        post.id = postId
        post.idUserPost = UserCache.getUser().id
        post.type = statusPost
        post.typePost = PostConstants.POST_DEFAULT
        post.album = albumCurrent
        post.status = PostConstants.IS_PASSED
        post.multiMedia = listMedia
        post.content = binding.edtDoYouThinking.text.toString().trim()
        post.contentNormalize =
            AppUtils.removeVietnameseFromStringNice(post.content)
        post.address = address
        post.idUserTags = listUser.map { it.id }.toArrayList()
        return post
    }

    private fun postToFireStore(post: Post) {
        PostManagerFSDB.addPost(post, object : PostManagerFSDB.Companion.FireStoreCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                if (result) {
                    toast(R.string.your_post_is_posted)
                } else {
                    toast(R.string.post_failure)
                }
                hideDialog()
                finish()
            }

            override fun onFailure(exception: Exception) {
                toast(R.string.please_try_later)
                hideDialog()
            }
        })
    }

    private fun setClickButtonPost() {
        binding.tbCreatePost.rightView.clickWithDebounce {
            if (binding.tbCreatePost.rightView.isSelected) {
                showDialog(getString(R.string.is_uploading_post))

                val postId = randomUUID().toString()
                val storageReference = FirebaseStorage.getInstance().reference
                val storageRef: StorageReference =
                    storageReference.child(UploadFireStorageUtils.getRootURLPostById(postId))

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        FireCloudManager.Companion.uploadFilesToFirebaseStorage(listMedia, storageRef, object :
                            FireCloudManager.Companion.FireStoreCallback<ArrayList<MultiMedia>> {
                            override fun onSuccess(result: ArrayList<MultiMedia>) {
                                val post = createPost(postId)
                                postToFireStore(post)
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
            }
        }
    }

    private fun validText() {
        binding.edtDoYouThinking.filters = arrayOf(
            InputFilter.LengthFilter(1501)
        )
        binding.edtDoYouThinking.doOnTextChanged { _, _, _, _ ->
            if (binding.edtDoYouThinking.length() > 1500) {
                binding.edtDoYouThinking.setText(
                    binding.edtDoYouThinking.text.toString().substring(0, 1500)
                )
                binding.edtDoYouThinking.setSelection(binding.edtDoYouThinking.text!!.length)
                toast(getString(R.string.res_max_1500_characters))
            }
            validButtonPost(binding.edtDoYouThinking.text.toString())
        }
    }

    private fun validButtonPost(textPost: String) {
        binding.tbCreatePost.rightView.isSelected =
            (textPost.isNotEmpty() && textPost.length <= 1500) || listMedia.isNotEmpty()
    }

    private fun setAlbum() {
        val drawableEnd = ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_down_bold)
        if (albumCurrent != null) {
            val drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_galleyry_new)
            binding.btnAlbum.setCompoundDrawablesWithIntrinsicBounds(
                drawable, null, drawableEnd, null
            )
            binding.btnAlbum.text = albumCurrent?.name
        } else {
            val drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_plus)
            binding.btnAlbum.setCompoundDrawablesWithIntrinsicBounds(
                drawable, null, drawableEnd, null
            )
            binding.btnAlbum.text = getString(R.string.album)
        }
    }

    private fun setUpMediaAdapter() {
        multiMediaAdapter = MultiMediaAdapter(getContext())
        multiMediaAdapter.onListenerCLick = this
        AppUtils.initRecyclerViewVerticalWithStaggeredGridLayoutManager(
            binding.rvMedia, multiMediaAdapter, 2
        )

        val bundleIntent = intent.extras
        if (bundleIntent != null && bundleIntent.containsKey(AppConstants.MULTIMEDIA_LIST_OBJECT)) {
            val listType = object : TypeToken<ArrayList<MultiMedia>>() {}.type
            listMedia.clear()
            listMedia.addAll(
                Gson().fromJson(
                    bundleIntent.getString(AppConstants.MULTIMEDIA_LIST_OBJECT), listType
                )
            )
            setDataRecycleView()
        }

    }

    private fun setChooseMedia() {
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    localMedia.clear()
                    localMedia = PictureSelector.obtainSelectorList(data)
                    listMedia.clear()
                    localMedia.forEach { localMedia ->
                        val media = MultiMedia()
                        val mediaId = randomUUID().toString()
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
                        listMedia.add(media)
                    }
                    setDataRecycleView()
                } else {
                    // Error and not choose
//                    validChooseImage()
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataRecycleView() {
        multiMediaAdapter.setData(listMedia)
        multiMediaAdapter.notifyDataSetChanged()
        if (listMedia.isNotEmpty()) {
            binding.edtDoYouThinking.setHint(R.string.ask_for_pictures)
        } else {
            binding.edtDoYouThinking.setHint(R.string.do_you_thinking_about)
        }
        validButtonPost(binding.edtDoYouThinking.text.toString())
    }

    private fun setStatusPost() {
        statusPost = PostConstants.TYPE_PUBLIC
        setViewStatusPost(statusPost)
    }

    private fun setClickTabChooseButton() {
        binding.tvFullName.clickWithDebounce {
            if (listUser.isNotEmpty()) {
                val dialog = TagUserDialog.Builder(getContext(), listUser)
                    .onActionDone(object : TagUserDialog.Builder.OnActionDone {
                        override fun onActionDone(isConfirm: Boolean, listData: ArrayList<User>) {
                            if (isConfirm) {
                                listUser.clear()
                                listUser.addAll(listData)
                                setDataTag()
                            }
                        }

                    })
                dialog.create().show()
            }
        }
        binding.btnUserTag.clickWithDebounce {
            TagUserDialog.Builder(getContext(), listUser)
                .onActionDone(object : TagUserDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean, listData: ArrayList<User>) {
                        if (isConfirm) {
                            listUser.clear()
                            listUser.addAll(listData)
                            setDataTag()
                        }
                    }

                }).create().show()
        }

        binding.btnChooseLocation.clickWithDebounce {
            PlaceDialog.Builder(getContext(), getString(R.string.choose_location), address)
                .onActionDone(object : PlaceDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean, address: Address?) {
                        if (isConfirm) {
                            this@CreatePostActivity.address = address
                            setDataTag()
                        }
                    }

                }).create().show()


        }
        binding.btnChoosePicture.setOnClickListener {
            PhotoPickerUtils.showImagePickerUploadPost(this, launcher, localMedia)
        }
        //
        binding.btnStatusPost.clickWithDebounce {
            ObjectPostDialog.Builder(getContext(), statusPost)
                .onActionDone(object : ObjectPostDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean, statusPost: Int) {
                        this@CreatePostActivity.statusPost = reSetStatusPost(statusPost)
                    }

                }).create().show()
        }
        //
        binding.btnAlbum.clickWithDebounce {
            AlbumDialog.Builder(getContext(), albumCurrent)
                .onActionDone(object : AlbumDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean, album: Album?) {
                        if (isConfirm) {
                            albumCurrent = album
                            setAlbum()
                        }
                    }

                }).create().show()
        }

        binding.btnChooseMore.clickWithDebounce {
            val dialog = ChooseOptionBottomDialog.Builder(getContext(), AppConstants.TWO_OPTION)
            dialog.setDataForView(
                getString(R.string.cancel_post),
                getString(R.string.save_cache),
                "",
                "",
                "",
                R.drawable.ic_user_circle,
                R.drawable.ic_gallery_circle,
                0,
                0,
                0
            )
            dialog.onActionDone(object : ChooseOptionBottomDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }

                override fun onClickButton1() { // Xem ảnh đại diện
                    finish()
                }

                override fun onClickButton2() {
                    toast("Lưu cache và thoát")
                }

                override fun onClickButton3() {
                    //
                }

                override fun onClickButton4() {
                    //
                }

                override fun onClickButton5() {
                    //
                }

            })
            dialog.create().show()
        }
    }

    private fun setDataTag() {
        if (address == null) {
            if(listUser.isNotEmpty())
            {
                when (listUser.size) {
                    1 -> {

                        binding.tvFullName.text = AppUtils.fromHtml(
                            "<b>${UserCache.getUser().name}</b> - ${getString(R.string.with_of)} " + " <b>${listUser[0].name}</b>"
                        )
                    }

                    2 -> {
                        binding.tvFullName.text = AppUtils.fromHtml(
                            "<b>${UserCache.getUser().name}</b> - ${getString(R.string.with_of)}" + " <b>${listUser[0].name}</b> ${
                                getString(
                                    R.string.and
                                )
                            } <b>${listUser[1].name}</b>"
                        )
                    }

                    else -> {
                        binding.tvFullName.text = AppUtils.fromHtml(
                            "<b>${UserCache.getUser().name}</b> - ${getString(R.string.with_of)}" + " <b>${listUser[0].name}</b> ${
                                getString(
                                    R.string.and
                                )
                            } <b>${listUser.size - 1} ${
                                getString(
                                    R.string.other_people
                                )
                            }</b>"
                        )
                    }

                }
            }else{
                binding.tvFullName.text = AppUtils.fromHtml(
                    "<b>${UserCache.getUser().name}</b>"
                )
            }
        } else {
            if(listUser.isNotEmpty())
            {
                when (listUser.size) {
                    1 -> {

                        binding.tvFullName.text = AppUtils.fromHtml(
                            "<b>${UserCache.getUser().name}</b> - ${getString(R.string.with_of)} " + " <b>${listUser[0].name}</b>" + " - ${
                                getString(
                                    R.string.at
                                )
                            } <b> ${address!!.address}</b>"
                        )
                    }

                    2 -> {
                        binding.tvFullName.text = AppUtils.fromHtml(
                            "<b>${UserCache.getUser().name}</b> - ${getString(R.string.with_of)}" + " <b>${listUser[0].name}</b> ${
                                getString(
                                    R.string.and
                                )
                            } <b>${listUser[1].name}</b>" + " - ${getString(R.string.at)} <b> ${address!!.address}</b>"
                        )
                    }

                    else -> {
                        binding.tvFullName.text = AppUtils.fromHtml(
                            "<b>${UserCache.getUser().name}</b> - ${getString(R.string.with_of)}" + " <b>${listUser[0].name}</b> ${
                                getString(
                                    R.string.and
                                )
                            } <b>${listUser.size - 1} ${
                                getString(
                                    R.string.other_people
                                )
                            }</b>" +" - ${getString(R.string.at)} <b> ${address!!.address}</b>"
                        )
                    }

                }
            }else{
                binding.tvFullName.text = AppUtils.fromHtml(
                    "<b>${UserCache.getUser().name}</b> - ${getString(R.string.at)} <b> ${address!!.address}</b>"
                )
            }
        }
    }

    private fun reSetStatusPost(statusPost: Int): Int {
        setViewStatusPost(statusPost)
        return statusPost
    }

    private fun setViewStatusPost(statusPost: Int) {
        val drawableEnd = ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_down_bold)
        when (statusPost) {
            PostConstants.TYPE_PUBLIC -> {
                val drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_public)
                binding.btnStatusPost.setCompoundDrawablesWithIntrinsicBounds(
                    drawable, null, drawableEnd, null
                )
                binding.btnStatusPost.text = getString(R.string.status_public)
            }

            PostConstants.TYPE_PRIVATE -> {
                val drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_private)
                binding.btnStatusPost.setCompoundDrawablesWithIntrinsicBounds(
                    drawable, null, drawableEnd, null
                )
                binding.btnStatusPost.text = getString(R.string.status_private)
            }

            PostConstants.TYPE_FRIEND -> {
                val drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_friend)
                binding.btnStatusPost.setCompoundDrawablesWithIntrinsicBounds(
                    drawable, null, drawableEnd, null
                )
                binding.btnStatusPost.text = getString(R.string.status_friend)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setLayoutRightTitleBar() {
        binding.tbCreatePost.rightView.isSelected = false
        val layout = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, // Width
            ViewGroup.LayoutParams.WRAP_CONTENT, // Height
            Gravity.CENTER_VERTICAL or Gravity.END
        )
        binding.tbCreatePost.rightView.layoutParams = layout
        binding.tbCreatePost.rightView.topPadding = getContext().dimen(R.dimen.dp_4)
        binding.tbCreatePost.rightView.bottomPadding = getContext().dimen(R.dimen.dp_4)
        binding.tbCreatePost.rightView.allCaps = true
        binding.tbCreatePost.rightView.background =
            getDrawable(R.drawable.button_right_titlebar_selected_dp_8)
    }

    private fun setUpData() {
        val userCurrent = UserCache.getUser()
        PhotoShowUtils.loadAvatarImage(
            UploadFireStorageUtils.getRootURLAvatarById(userCurrent.id),
            userCurrent.avatar,
            binding.imvAvatar
        )
        binding.tvFullName.text = AppUtils.fromHtml("<b>${userCurrent.name}</b>")
    }

    override fun initData() {
        //
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(position: Int) {
        loop@ for ((index, item) in localMedia.withIndex()) {
            if (item.realPath == listMedia[position].realPath) {
                localMedia.removeAt(index)
                break@loop
            }
        }
        listMedia.removeAt(position)
        multiMediaAdapter.notifyDataSetChanged()
        if (listMedia.isNotEmpty()) {
            binding.edtDoYouThinking.setHint(R.string.ask_for_pictures)
        } else {
            binding.edtDoYouThinking.setHint(R.string.do_you_thinking_about)
        }
        validButtonPost(binding.edtDoYouThinking.text.toString())
    }

    override fun onShowPreview(position: Int) {
        PreviewMediaDialog.Builder(
            getContext(), listMedia[position], listMedia, UploadFireStorageConstants.TYPE_POST, "", null
        ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
            override fun onActionDone(isConfirm: Boolean) {
                //
            }
        }).create().show()
    }
}