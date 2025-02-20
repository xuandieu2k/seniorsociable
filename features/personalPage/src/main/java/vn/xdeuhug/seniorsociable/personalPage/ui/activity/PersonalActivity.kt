package vn.xdeuhug.seniorsociable.personalPage.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import cn.jzvd.JZDataSource
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
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.startActivity
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.database.FireCloudManager
import vn.xdeuhug.seniorsociable.database.FriendManagerFSDB
import vn.xdeuhug.seniorsociable.database.InteractManagerFSDB
import vn.xdeuhug.seniorsociable.database.PostManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.eventbus.ReloadDataNotImageEventBus
import vn.xdeuhug.seniorsociable.personalPage.constants.PersonalPageConstants
import vn.xdeuhug.seniorsociable.personalPage.databinding.ActivityPersonalBinding
import vn.xdeuhug.seniorsociable.personalPage.ui.adapter.FriendsAdapter
import vn.xdeuhug.seniorsociable.ui.adapter.PostAdapter
import vn.xdeuhug.seniorsociable.ui.dialog.ChooseOptionBottomDialog
import vn.xdeuhug.seniorsociable.ui.dialog.ConfirmDialog
import vn.xdeuhug.seniorsociable.ui.dialog.PreviewMediaDialog
import vn.xdeuhug.seniorsociable.ui.dialog.ReportPostDialog
import vn.xdeuhug.seniorsociable.ui.fragment.CommentFragment
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PhotoPickerUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.ReactObjectUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import vn.xdeuhug.seniorsociable.widget.reactbutton.Reaction
import java.util.Date
import java.util.UUID

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 07 / 11 / 2023
 */
class PersonalActivity : AppActivity(), PostAdapter.OnListenerCLick {
    private lateinit var binding: ActivityPersonalBinding

    // init adapter
    private var listFriends = ArrayList<User>()
    private lateinit var friendsAdapter: FriendsAdapter

    /* Data and Adapter for Post */
    private lateinit var postAdapter: PostAdapter
    private var listPost = ArrayList<Post>()

    //
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var launcherImage: ActivityResultLauncher<Intent> // Chọn hình để tạo mới bài viết
    private var listMedia = ArrayList<MultiMedia>()
    private var localMedia = ArrayList<LocalMedia>()
    private var listMediaImage = ArrayList<MultiMedia>()
    private var localMediaImage = ArrayList<LocalMedia>()

    //
    private lateinit var launcherPhoto: ActivityResultLauncher<Intent> // Cập nhật hình đại diện và nền
    var typeNextView = PersonalPageConstants.TYPE_AVATAR // Type chuyển màn
    //
    private lateinit var launcherStory: ActivityResultLauncher<Intent> // Tạo story//
    private var listMediaStory = ArrayList<MultiMedia>()
    private var localMediaStory = ArrayList<LocalMedia>()
    /* paging*/
    private var limit = AppConstants.PAGE_SIZE
    private var currentPage = 1
    private var lastVisible: DocumentSnapshot? = null
    private var loading = false
    private var lastPage = false
    /* end */

    //
    override fun getLayoutView(): View {
        binding = ActivityPersonalBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLauncher()
    }

    private fun setLauncher() {
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    localMedia.clear()
                    localMedia = PictureSelector.obtainSelectorList(data)
                    listMedia.clear()
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
                        listMedia.add(media)
                    }
                    try {
                        val intent = Intent(
                            getContext(), Class.forName(ModuleClassConstants.CREATE_POST_ACTIVITY)
                        )
                        intent.putExtra(AppConstants.MULTIMEDIA_LIST_OBJECT, Gson().toJson(listMedia))
                        startActivity(intent)
                    } catch (e: ClassNotFoundException) {
                        //code
                    }
                } else {
                    //
                }
            }

        launcherImage =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    localMediaImage.clear()
                    localMediaImage = PictureSelector.obtainSelectorList(data)
                    listMediaImage.clear()
                    localMediaImage.forEach { localMedia ->
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
                        listMediaImage.add(media)
                    }
                    try {
                        val intent = Intent(
                            getContext(), Class.forName(ModuleClassConstants.CREATE_POST_ACTIVITY)
                        )
                        intent.putExtra(AppConstants.MULTIMEDIA_LIST_OBJECT, Gson().toJson(listMediaImage))
                        startActivity(intent)
                    } catch (e: ClassNotFoundException) {
                        //code
                    }
                } else {
                    //
                }
            }

        launcherPhoto =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    localMedia = PictureSelector.obtainSelectorList(data) as ArrayList<LocalMedia>
                    try {
                        when (typeNextView) {
                            PersonalPageConstants.TYPE_AVATAR -> {
                                startActivity<UpdateAvatarActivity>(
                                    PersonalPageConstants.LOCAL_MEDIA to Gson().toJson(
                                        localMedia[0]
                                    )
                                )
                            }

                            PersonalPageConstants.TYPE_BACKGROUND -> {
                                startActivity<UpdateBackgroundActivity>(
                                    PersonalPageConstants.LOCAL_MEDIA to Gson().toJson(
                                        localMedia[0]
                                    )
                                )
                            }
                        }
                    } catch (ex: Exception) {
                        // Lỗi
                    }
                } else {
                    //
                }
            }

        launcherStory =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    localMediaStory.clear()
                    localMediaStory = PictureSelector.obtainSelectorList(data)
                    listMediaStory.clear()
                    localMediaStory.forEach { localMedia ->
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
                        listMediaStory.add(media)
                    }
                    try {
                        val intent = Intent(this, Class.forName(ModuleClassConstants.CREATE_STORY_ACTIVITY))
                        intent.putExtra(AppConstants.MULTIMEDIA_OBJECT,Gson().toJson(listMediaStory[0]))
                        startActivity(intent)
                    } catch (ex: Exception) {
                        // Lỗi
                    }
                } else {
                    //
                }
            }
    }

    override fun initView() {
        AppUtils.setFontTypeFaceTitleBar(this, binding.tbCreatePost)
        startShimmer()
        initProfile()
        initFriendsList()
        setDataPost()
        setClickAvatar()
        setClickButton()
        setClickCreatePost()
    }

    private fun setClickCreatePost() {
        binding.imvChoosePicture.setOnClickListener {
            PhotoPickerUtils.showImagePickerUploadPost(this, launcher, localMedia)
        }
        binding.tvCreatePost.clickWithDebounce {
            try {
                val intent = Intent(
                    getContext(), Class.forName(ModuleClassConstants.CREATE_POST_ACTIVITY)
                )
                startActivity(intent)
            } catch (e: ClassNotFoundException) {
                //code
            }
        }
    }

    private fun setClickButton() {

        AppUtils.setChildListener(binding.llEdit) {
            try {
                startActivity<EditProfileActivity>()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        AppUtils.setChildListener(binding.llAddStory) {
            PhotoPickerUtils.showImagePickerChooseAvatarNotGif(
                this, launcherStory
            )
        }
    }

    private fun setClickAvatar() {
        AppUtils.setChildListener(binding.RlAvatar) {
            val dialog = ChooseOptionBottomDialog.Builder(getContext(), AppConstants.TWO_OPTION)
            dialog.setDataForView(
                getString(R.string.see_avatar),
                getString(R.string.change_avatar),
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
                    val multiMedia = MultiMedia()
                    multiMedia.id = UserCache.getUser().id
                    multiMedia.url = UserCache.getUser().avatar
                    PreviewMediaDialog.Builder(
                        getContext(),
                        multiMedia,
                        arrayListOf(multiMedia),
                        UploadFireStorageConstants.TYPE_AVATAR,
                        "",
                        null
                    ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
                        override fun onActionDone(isConfirm: Boolean) {
                            //
                        }
                    }).create().show()
                }

                override fun onClickButton2() {
                    typeNextView = PersonalPageConstants.TYPE_AVATAR
                    PhotoPickerUtils.showImagePickerChooseAvatarNotGif(getActivity()!!, launcherPhoto)
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

        AppUtils.setChildListener(binding.RlBackground) {
            val dialog = ChooseOptionBottomDialog.Builder(getContext(), AppConstants.THREE_OPTION)
            dialog.setDataForView(
                getString(R.string.see_background),
                getString(R.string.change_background),
                getString(R.string.choose_image_in_senior_sociable),
                "",
                "",
                R.drawable.ic_user_circle,
                R.drawable.ic_gallery_circle,
                R.drawable.ic_senior_sociable_circle,
                0,
                0
            )
            dialog.onActionDone(object : ChooseOptionBottomDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }

                override fun onClickButton1() {
                    val multiMedia = MultiMedia()
                    multiMedia.id = UserCache.getUser().id
                    multiMedia.url = UserCache.getUser().background
                    PreviewMediaDialog.Builder(
                        getContext(),
                        multiMedia,
                        arrayListOf(multiMedia),
                        UploadFireStorageConstants.TYPE_AVATAR,
                        "",
                        null
                    ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
                        override fun onActionDone(isConfirm: Boolean) {
                            //
                        }
                    }).create().show()
                }

                override fun onClickButton2() {
                    typeNextView = PersonalPageConstants.TYPE_BACKGROUND
                    PhotoPickerUtils.showImagePickerChooseAvatarNotGif(getActivity()!!, launcherPhoto)
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

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun initFriendsList() {
        friendsAdapter = FriendsAdapter(this)
        friendsAdapter.setData(listFriends)
        friendsAdapter.notifyDataSetChanged()
        AppUtils.initRecyclerViewGrid(binding.rvFriends, friendsAdapter)
        getFriend()
        setViewNoFriend()
    }

    private fun setViewNoFriend(){
        if(listFriends.isNotEmpty())
        {
            binding.rvFriends.show()
            binding.llNoFriend.hide()
        }else{
            binding.rvFriends.hide()
            binding.llNoFriend.show()
        }
    }

    private fun getFriend()
    {

        FriendManagerFSDB.getAllFriendsByIdUser(UserCache.getUser().id,object :
            FriendManagerFSDB.Companion.FireStoreCallback<ArrayList<User>>{
            override fun onSuccess(result: ArrayList<User>) {
                listFriends.clear()
                if(result.isNotEmpty())
                {
                    if(result.size >= 6)
                    {
                        listFriends.addAll(result.subList(0,6))
                    }else{
                        listFriends.addAll(result)
                    }
                    binding.tvNumberOfFriend.text = "${AppUtils.formatFacebookLikes(listFriends.size)} ${getString(R.string.friend)}"
                }
                setViewNoFriend()
            }

            override fun onFailure(exception: Exception) {
                exception.printStackTrace()
                setViewNoFriend()
            }

        })
    }

    private fun initProfile() {
        val user = UserCache.getUser()
        binding.tvUsername.text = user.name
        PhotoShowUtils.loadAvatarImage(
            UploadFireStorageUtils.getRootURLAvatarById(user.id), user.avatar, binding.imvAvatar
        )

        PhotoShowUtils.loadAvatarImage(
            UploadFireStorageUtils.getRootURLAvatarById(user.id), user.avatar, binding.imvAvatar2
        )

        PhotoShowUtils.loadBackgroundImageCenterCrop(
            UploadFireStorageUtils.getRootURLBackgroundById(user.id),
            user.background,
            binding.imvBackground
        )
        if (user.birthday.isNotEmpty()) {
            binding.tvBirthday.text = "${getString(R.string.birthday)} ${user.birthday}"
        } else {
            binding.tvBirthday.text = getString(R.string.not_update)
        }
        if (user.address.address.isNotEmpty()) {
            binding.tvFromTo.text = "${getString(R.string.from_to)} ${user.address.address}"
        } else {
            binding.tvFromTo.text = getString(R.string.not_update)
        }
        binding.tvJoinIn.text = TimeUtils.getJoinIn(user.timeCreated, this)
    }


    /*
    * Init Data For Post
    * */
    @SuppressLint("NotifyDataSetChanged")
    private fun setDataPost() {
        postAdapter = PostAdapter(this)
        postAdapter.onListenerCLick = this
        postAdapter.setData(listPost)
        // Create recycleView
        AppUtils.initRecyclerViewVertical(binding.rvPost, postAdapter)
        getDataPost()
        customPaginate()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataPost() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val listTriple = async { PostManagerFSDB.getAllPostByIdUser(UserCache.getUser().id,limit, lastVisible) }.await()
                if (currentPage == 1) {
                    finishShimmer()
                    listPost.clear()
                }
                // UI update code here
                listPost.addAll(listTriple.first)
                postAdapter.notifyDataSetChanged()
                lastVisible = listTriple.second
                lastPage = listTriple.third
                loading = false
                binding.loadMore.hide()
                setViewNoData(listPost.isNotEmpty())
            } catch (e: Exception) {
                // Xử lý ngoại lệ khi gặp lỗi
                Timber.tag("Log Error$localClassName").e(e)
                toast(R.string.please_try_later)
                loading = false
                binding.loadMore.hide()
                finishShimmer()
                setViewNoData(listPost.isNotEmpty())
            }
        }
    }

    private fun customPaginate() {
        binding.nsvParent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            var recyclerViewBottom = binding.rvPost.bottom
            if(binding.loadMore.visibility == View.VISIBLE)
            {
                recyclerViewBottom = binding.rvPost.bottom + binding.loadMore.height
            }
            val nestedScrollViewHeight = binding.nsvParent.height

            if(binding.rvPost.visibility == View.VISIBLE)
            {
                // Kiểm tra xem cuộn đến cuối cùng của NestedScrollView hay không
                if (scrollY + nestedScrollViewHeight >= recyclerViewBottom) {
                    if (!loading && !lastPage) {
                        binding.loadMore.show()
                        binding.nsvParent.post {
                            binding.nsvParent.smoothScrollTo(0, binding.rvPost.bottom + binding.loadMore.height)
                        }
                        loading = true
                        currentPage += 1
                        getDataPost()
                    }else{
                        if(lastPage)
                        {
//                            toast(getString(R.string.end_page))
                        }
                    }
                }
            }
        })
    }

    override fun initData() {
        //
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

    @Suppress("DEPRECATION")
    override fun onShowDialogComment(post: Post, position: Int) {
        val bottomSheetDialog = CommentFragment(post, position)
        bottomSheetDialog.show(supportFragmentManager, "")
    }

    /**
     * @param position - Vị trí post có thay đổi
     * @param reaction - Icon hiện tại
     * @param defaultReaction - Icon mặc định là like màu xám
     */
    override fun onClickReact(
        position: Int, postUpdate: Post, reaction: Reaction, defaultReaction: Reaction
    ) {
        val userCurrent = UserCache.getUser()

        // Sử dụng coroutine để thực hiện các tác vụ bất đồng bộ
        lifecycleScope.launch(Dispatchers.IO) {
            // Nếu icon hiện tại khác với icon mặc định
            if (reaction != defaultReaction) {
                val typeInteract = ReactObjectUtils.getTypeByReact(reaction)
                val interact = Interact(userCurrent.id, typeInteract, Date())
                val listInteract = postUpdate.interacts.filter { it.id == userCurrent.id }

                if (listInteract.isNotEmpty()) { // List icon đã tồn tại icon hiện tại thì cập nhật icon đã có thành icon hiện tại
                    val oldInteract = listInteract.first()
                    oldInteract.type = typeInteract
                    oldInteract.timeCreated = interact.timeCreated
                    updateInteract(oldInteract, listInteract.first(), position)

                    val updateResult = InteractManagerFSDB.updateInteractToPost(
                        postUpdate.id, oldInteract.id, oldInteract
                    )
                    if (!updateResult) {
                        withContext(Dispatchers.Main) {
                            toast(R.string.please_try_later)
                        }
                    }
                } else { // List icon chưa có icon hiện tại - tiến hành thêm mới icon
                    // Thêm mới interact và cập nhật trong Firestore
                    addInteract(interact, position)

                    try {
                        val addResult =
                            InteractManagerFSDB.addInteractToPost(postUpdate.id, interact)
                        if (!addResult) {
                            // Xử lý khi thêm mới thất bại
                            withContext(Dispatchers.Main) {
                                toast(R.string.please_try_later)
                            }
                        }
                    } catch (e: Exception) {
                        // Xử lý exception khi thêm mới interact
                        Timber.tag("Update interact Exception").i(e)
                    }
                }
            } else {
                // Nếu icon hiện tại là icon mặc định thực hiện xóa icon ra khỏi list icon
                val interactCurrent = postUpdate.interacts.filter { it.id == userCurrent.id }
                removeInteract(interactCurrent.first(), position)

                try {
                    // Xóa interact và cập nhật trong Firestore
                    val deleteResult = InteractManagerFSDB.deleteInteractInPost(
                        postUpdate.id, interactCurrent.first().id
                    )
                    if (!deleteResult) {
                        // Xử lý khi xóa thất bại
                        withContext(Dispatchers.Main) {
                            toast(R.string.please_try_later)
                        }
                    }
                } catch (e: Exception) {
                    // Xử lý exception khi xóa interact
                    Timber.tag("Delete interact Exception").i(e)
                }
            }
        }
    }

    private fun reFreshView()
    {
        binding.root.invalidate()
    }

    override fun onSeeMore(post: Post, position: Int) {
        if(post.idUserPost != UserCache.getUser().id)
        {
            val dialog = ChooseOptionBottomDialog.Builder(getContext(), AppConstants.TWO_OPTION)
            dialog.setDataForView(
                getString(R.string.hide_less),
                getString(R.string.report),
                "",
                "",
                "",
                R.drawable.ic_post_hide_eyes,
                R.drawable.ic_post_report,
                0,
                0,
                0
            )
            dialog.onActionDone(object : ChooseOptionBottomDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }

                override fun onClickButton1() {
                    dialog.dismiss()
                    listPost.removeAt(position)
                    postAdapter.notifyDataSetChanged()
                    reFreshView()
                }

                override fun onClickButton2() {
                    dialog.dismiss()
                    val rpDialog = ReportPostDialog.Builder(getContext(), post.id)
                    rpDialog.onActionDone(object : ReportPostDialog.Builder.OnActionDone {
                        override fun onActionDone(isConfirm: Boolean) {
                            if (isConfirm) {
                                toast(getString(R.string.thank_report))
                            } else {
                                toast(getString(R.string.please_try_later))
                            }
                            rpDialog.dismiss()
                            dialog.dismiss()
                        }

                    }).create().show()
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
        }else{
            val dialog = ChooseOptionBottomDialog.Builder(getContext(), AppConstants.ONE_OPTION)
            dialog.setDataForView(
                getString(R.string.remove_post),
                "",
                "",
                "",
                "",
                R.drawable.ic_post_remove,
                0,
                0,
                0,
                0
            )
            dialog.onActionDone(object : ChooseOptionBottomDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }

                override fun onClickButton1() {
                    val dialog = ConfirmDialog.Builder(getContext(),getString(R.string.notification),getString(R.string.confirm_remove_post))
                        .onActionDone(object : ConfirmDialog.Builder.OnActionDone{
                            override fun onActionDone(isConfirm: Boolean) {
                                if(isConfirm)
                                {
                                    dialog.dismiss()
                                    showDialog("")
                                    val storageReference = FirebaseStorage.getInstance().reference
                                    val storageRef: StorageReference =
                                        storageReference.child(UploadFireStorageUtils.getRootURLPostById(post.id))
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            if(post.multiMedia.isNotEmpty() && post.typePost !in arrayListOf(
                                                    PostConstants.POST_UPDATE_BACKGROUND,
                                                    PostConstants.POST_UPDATE_AVATAR))
                                            {
                                                FireCloudManager.Companion.deleteFilesFromFirebaseStorage(post.multiMedia, storageRef, object :
                                                    FireCloudManager.Companion.FireStoreCallback<ArrayList<MultiMedia>> {
                                                    override fun onSuccess(result: ArrayList<MultiMedia>) {
                                                        PostManagerFSDB.deletePost(post.id,object :
                                                            PostManagerFSDB.Companion.FireStoreCallback<Boolean>{
                                                            override fun onSuccess(result: Boolean) {
                                                                dialog.dismiss()
                                                                listPost.removeAt(position)
                                                                postAdapter.notifyDataSetChanged()
                                                                reFreshView()
                                                                toast(R.string.post_is_removed)
                                                                hideDialog()
                                                            }

                                                            override fun onFailure(exception: Exception) {
                                                                toast(R.string.remove_post_lailure)
                                                                hideDialog()
                                                            }

                                                        })
                                                    }

                                                    override fun onFailure(result: ArrayList<MultiMedia>) {
                                                        toast(R.string.please_try_later)
                                                        hideDialog()
                                                    }
                                                })
                                            }else{
                                                PostManagerFSDB.deletePost(post.id,object :
                                                    PostManagerFSDB.Companion.FireStoreCallback<Boolean>{
                                                    override fun onSuccess(result: Boolean) {
                                                        dialog.dismiss()
                                                        listPost.removeAt(position)
                                                        postAdapter.notifyDataSetChanged()
                                                        reFreshView()
                                                        toast(R.string.post_is_removed)
                                                        hideDialog()
                                                    }

                                                    override fun onFailure(exception: Exception) {
                                                        toast(R.string.remove_post_lailure)
                                                        hideDialog()
                                                    }

                                                })
                                            }
                                        } catch (e: Exception) {
                                            toast(R.string.please_try_later)
                                            hideDialog()
                                        }
                                    }
                                }
                            }

                        })
                    dialog.create().show()
                }

                override fun onClickButton2() {
                    //
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

    private fun updateInteract(interactNew: Interact, interactOld: Interact, position: Int) {
        listPost[position].interacts.remove(interactOld)
        listPost[position].interacts.add(interactNew)
        postAdapter.notifyItemChanged(position)
    }

    private fun removeInteract(interactCurrent: Interact, position: Int) {
        listPost[position].interacts.remove(interactCurrent)
        postAdapter.notifyItemChanged(position)
    }

    private fun addInteract(interactCurrent: Interact, position: Int) {
        listPost[position].interacts.add(interactCurrent)
        postAdapter.notifyItemChanged(position)
    }

    override fun onClickShare(position: Int, post: Post) {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://seniorsociable.page.link/post?id=${post.id}")) // Đường dẫn sâu đến bài viết
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

    override fun onClickTitleTag(position: Int, post: Post) {
        //
    }

    override fun onClickAvatar(user: User) {
        //
    }

    //    private fun startShimmer() {
//        binding.sflLoadData.startShimmer()
//        binding.rvPost.hide()
//        binding.sflLoadData.show()
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    private fun handleSuccess(timer: Long) {
//        postDelayed({
//            binding.rvPost.show()
//            // hide and stop simmer
//            binding.sflLoadData.stopShimmer()
//            binding.sflLoadData.hide()
//        }, timer)
//
//    }
    private fun setViewNoData(isHaveData: Boolean) {
        binding.tvNoData.text = "${getString(R.string.you)} ${getString(R.string.not_have_a_post)}"
        if (isHaveData) {
            binding.rvPost.show()
            binding.rlNoData.hide()
        } else {
            binding.rvPost.hide()
            binding.rlNoData.show()
        }
    }

    private fun startShimmer()
    {
        binding.sflLoadData.show()
        binding.sflLoadData.startShimmer()
        binding.rvPost.hide()
        binding.rlNoData.hide()

    }

    private fun finishShimmer()
    {
        binding.sflLoadData.stopShimmer()
        binding.sflLoadData.hide()
        binding.rvPost.show()
        binding.rlNoData.hide()

    }


    // ##  Subscribe Event Bus
    @Subscribe
    fun onReloadPostFromCommentFragment(eventBus: ReloadDataNotImageEventBus) {
        if (eventBus.isReload) {
            eventBus.post.isReloadDataNotPic = true
            listPost[eventBus.position] = eventBus.post
            postAdapter.notifyItemChanged(eventBus.position)
        }
    }
}