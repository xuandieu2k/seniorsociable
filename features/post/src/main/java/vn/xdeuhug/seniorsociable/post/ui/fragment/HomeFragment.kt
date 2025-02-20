package vn.xdeuhug.seniorsociable.post.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.JZDataSource
import cn.jzvd.JzvdStd
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.support.v4.startActivity
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.CallConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.database.CallRoomManagerFSDB
import vn.xdeuhug.seniorsociable.database.FireCloudManager
import vn.xdeuhug.seniorsociable.database.InteractManagerFSDB
import vn.xdeuhug.seniorsociable.database.NotificationManagerFSDB
import vn.xdeuhug.seniorsociable.database.PostManagerFSDB
import vn.xdeuhug.seniorsociable.database.RequestAddFriendManagerFSDB
import vn.xdeuhug.seniorsociable.database.StoryManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelCall.CallRoom
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Story
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.eventbus.AddedPostEventBus
import vn.xdeuhug.seniorsociable.model.eventbus.NotificationEventBus
import vn.xdeuhug.seniorsociable.model.eventbus.PreviewMediaEventBus
import vn.xdeuhug.seniorsociable.model.eventbus.ReloadDataNotImageEventBus
import vn.xdeuhug.seniorsociable.post.databinding.FragmentHomeBinding
import vn.xdeuhug.seniorsociable.post.ui.activity.CreatePostActivity
import vn.xdeuhug.seniorsociable.post.ui.activity.CreateStoryActivity
import vn.xdeuhug.seniorsociable.post.ui.adapter.StoryAdapter
import vn.xdeuhug.seniorsociable.storyview.StoryView
import vn.xdeuhug.seniorsociable.storyview.callback.StoryClickListeners
import vn.xdeuhug.seniorsociable.storyview.model.MyStory
import vn.xdeuhug.seniorsociable.ui.activity.BrowserActivity
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.ui.activity.NotificationActivity
import vn.xdeuhug.seniorsociable.ui.adapter.MediaAdapter
import vn.xdeuhug.seniorsociable.ui.adapter.PostAdapter
import vn.xdeuhug.seniorsociable.ui.dialog.ChooseOptionBottomDialog
import vn.xdeuhug.seniorsociable.ui.dialog.ConfirmDialog
import vn.xdeuhug.seniorsociable.ui.dialog.PreviewMediaDialog
import vn.xdeuhug.seniorsociable.ui.dialog.ReportPostDialog
import vn.xdeuhug.seniorsociable.ui.dialog.TagUserInPostDialog
import vn.xdeuhug.seniorsociable.ui.fragment.CommentFragment
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.addScrollListener
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
 * @Date: 03 / 10 / 2023
 */
class HomeFragment : AppFragment<HomeActivity>(), MediaAdapter.OnListenerCLick,
    PostAdapter.OnListenerCLick, StoryAdapter.OnClickListener, AppUtils.OnListenerClickTagUser {
    private lateinit var binding: FragmentHomeBinding

    /* Data and Adapter for Story */
    private lateinit var storyAdapter: StoryAdapter
    private var listStory = ArrayList<Story>()

    /* Data and Adapter for Post */
    private lateinit var postAdapter: PostAdapter
    private var listPost = ArrayList<Post>()

    /* paging*/
    private var limit = AppConstants.PAGE_SIZE
    private var currentPage = 1
    private var lastVisible: DocumentSnapshot? = null
    private var loading = false
    private var lastPage = false

    /* end */
    private var listMedia = ArrayList<MultiMedia>()
    private var localMedia = ArrayList<LocalMedia>()

    private lateinit var launcher: ActivityResultLauncher<Intent>

    //
    private var listMediaStory = ArrayList<MultiMedia>()
    private var localMediaStory = ArrayList<LocalMedia>()

    private lateinit var launcherStory: ActivityResultLauncher<Intent>


    override fun getLayoutView(): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initData() {
        startShimmer()
        startShimmerStory()
        setFirstData()
        setDataStory()
        setDataPost()
        setClickButton()
        getDataPost()
        customPaginate()
        addOnScrollRecycleView()
    }

    override fun onResume() {
        super.onResume()
        getDataStory()
    }

    private fun addOnScrollRecycleView() {
        binding.nsvData.viewTreeObserver.addOnScrollChangedListener {
            // Find the view located in the middle of the NestedScrollView
            val centerOfNestedScrollView = binding.nsvData.scrollY + binding.nsvData.height / 2

            for (i in 0 until  binding.rvPost.childCount) {
                val child =  binding.rvPost.getChildAt(i)
                val childTop = child.top +  binding.rvPost.top
                val childBottom = child.bottom +  binding.rvPost.top

                // Check if the view is within the visible area of the NestedScrollView
                if (centerOfNestedScrollView in childTop..childBottom) {
                    // This is the central view, probably the current view
                    val currentPosition =  binding.rvPost.getChildAdapterPosition(child)
                    break
                }
            }
        }

    }


    private fun addCallRoom() {
        CoroutineScope(Dispatchers.Main).launch {
            ListUserCache.getList().forEach {
                val callRoom = CallRoom(
                    it.id,
                    CallConstants.IS_NOT_BUSY,
                    CallConstants.TYPE_VOICE_CALL,
                    CallConstants.IS_DEFAULT,
                    null
                )
                CallRoomManagerFSDB.addCallRoom(it.id, callRoom)
            }
        }

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
                        startActivity<CreatePostActivity>(
                            AppConstants.MULTIMEDIA_LIST_OBJECT to Gson().toJson(
                                listMedia
                            )
                        )
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
                        startActivity<CreateStoryActivity>(
                            AppConstants.MULTIMEDIA_OBJECT to Gson().toJson(
                                listMediaStory[0]
                            )
                        )
                    } catch (ex: Exception) {
                        // Lỗi
                    }
                } else {
                    //
                }
            }
    }

    private fun setClickButton() {
        binding.tvCreatePost.clickWithDebounce {
            startActivity<CreatePostActivity>()
        }


        binding.imvChoosePicture.setOnClickListener {
            PhotoPickerUtils.showImagePickerUploadPost(requireActivity(), launcher, localMedia)
        }

        binding.imvAvatar.clickWithDebounce {
            try {
                startActivity(
                    Intent(
                        context, Class.forName(ModuleClassConstants.PERSONAL_ACTIVITY)
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }

        AppUtils.setChildListener(binding.ttBar.rlAddFriend) {
            try {
                startActivity(
                    Intent(
                        context, Class.forName(ModuleClassConstants.FRIENDS_ACTIVITY)
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }
        AppUtils.setChildListener(binding.ttBar.rlNotification) {
            try {
                startActivity(
                    Intent(
                        context, NotificationActivity::class.java
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }
        binding.ttBar.imvSearch.clickWithDebounce {
            try {
                startActivity(
                    Intent(
                        context, Class.forName(ModuleClassConstants.SEARCH_ACTIVITY)
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }
        registerDataChange()
    }

    private var listNotificationUnread = ArrayList<Notification>()

    private fun registerDataChange() {
        val id = UserCache.getUser().id
        NotificationManagerFSDB.getListNotificationByIdUser(
            id,
            object : NotificationManagerFSDB.Companion.FireStoreCallback<ArrayList<Notification>> {
                override fun onSuccess(result: ArrayList<Notification>) {
                    listNotificationUnread.clear()
                    listNotificationUnread.addAll(result.filter { it.read == AppConstants.IS_UNREAD })
                    setViewNumberNotification()
                }

                override fun onFailure(exception: Exception) {
                    setViewNumberNotification()
                }

            })
        NotificationManagerFSDB.registerListenerNodeChangeById(id) {


        }
        RequestAddFriendManagerFSDB.registerListenerNodeChangeById(id) {
            if (it.isNotEmpty()) {
                binding.ttBar.tvNumberOfAddFriend.show()
                if (it.size > 99) {
                    binding.ttBar.tvNumberOfAddFriend.text = "99+"
                } else {
                    binding.ttBar.tvNumberOfAddFriend.text = it.size.toString()
                }
            } else {
                binding.ttBar.tvNumberOfAddFriend.hide()
            }

        }

    }

    private fun setViewNumberNotification() {
        if (listNotificationUnread.isNotEmpty()) {
            binding.ttBar.tvNumberOfNotification.show()
            if (listNotificationUnread.size > 99) {
                binding.ttBar.tvNumberOfNotification.text = "99+"
            } else {
                binding.ttBar.tvNumberOfNotification.text =
                    listNotificationUnread.size.toString()
            }
        } else {
            binding.ttBar.tvNumberOfNotification.hide()
        }
    }

    /*
    * Init Data For Post
    * */
    @SuppressLint("NotifyDataSetChanged")
    private fun setDataPost() {
        postAdapter = PostAdapter(requireContext())
        postAdapter.onListenerCLick = this
        postAdapter.onListenerClickTagUser = this
        postAdapter.setData(listPost)
        // Create recycleView
        binding.rvPost.show()
        AppUtils.initRecyclerViewVertical(binding.rvPost, postAdapter)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataPost() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val listTriple = async { PostManagerFSDB.getAllPost(limit, lastVisible) }.await()
                if (currentPage == 1) {
                    handleSuccess(1000)
                    listPost.clear()
                    binding.splReset.finishRefresh()
                }
                // UI update code here
                listPost.addAll(listTriple.first)
                postAdapter.notifyDataSetChanged()
                lastVisible = listTriple.second
                lastPage = listTriple.third
                loading = false
                binding.loadMore.hide()
            } catch (e: Exception) {
                // Xử lý ngoại lệ khi gặp lỗi
                e.printStackTrace()
                toast(R.string.please_try_later)
                loading = false
                binding.loadMore.hide()
                binding.splReset.finishRefresh()
            }
        }
    }

    private fun customPaginate() {
        binding.splReset.setOnRefreshListener {
            postDelayed({
                currentPage = 1
                lastPage = false
                lastVisible = null
                getDataPost()
            }, 1000)
        }
        binding.nsvData.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            var recyclerViewBottom = binding.rvPost.bottom
            if (binding.loadMore.visibility == View.VISIBLE) {
                recyclerViewBottom = binding.rvPost.bottom + binding.loadMore.height
            }
            val nestedScrollViewHeight = binding.nsvData.height

            if (binding.rvPost.visibility == View.VISIBLE && binding.sflLoadData.visibility == View.GONE) {
                if (scrollY + nestedScrollViewHeight >= recyclerViewBottom) {
                    if (!loading && !lastPage) {
                        binding.loadMore.show()
                        binding.nsvData.post {
                            binding.nsvData.smoothScrollTo(
                                0,
                                binding.rvPost.bottom + binding.loadMore.height
                            )
                        }
                        loading = true
                        currentPage += 1
                        getDataPost()
                    } else {
                        if (lastPage) {
//                            toast(getString(R.string.end_page))
                        }
                    }
                }
            }
        })
    }


    /*
    * Init Data For Story
    * */
    @SuppressLint("NotifyDataSetChanged")
    private fun setDataStory() {
        storyAdapter = StoryAdapter(requireContext())
        storyAdapter.onClickListener = this
        // Create recycleView
        AppUtils.initRecyclerViewHorizontal(binding.rvStory, storyAdapter)
        storyAdapter.setData(listStory)
        storyAdapter.notifyDataSetChanged()
        getDataStory()
    }

    private fun getDataStory() {
        StoryManagerFSDB.getAllStory(object :
            StoryManagerFSDB.Companion.FireStoreCallback<ArrayList<Story>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(result: ArrayList<Story>) {
                handleSuccessStory(0)
                listStory.clear()
                val item = Story("00000", UserCache.getUser().id, Date(), ArrayList())
                listStory.add(0, item)
                listStory.addAll(result)
                storyAdapter.notifyDataSetChanged()
            }

            override fun onFailure(exception: Exception) {
                exception.printStackTrace()
            }

        })
    }

    private fun setFirstData() {
        PhotoShowUtils.loadAvatarImage(
            UploadFireStorageUtils.getRootURLAvatarById(UserCache.getUser().id),
            UserCache.getUser().avatar,
            binding.imvAvatar
        )
    }

    override fun onClick(
        jzDataSource: JZDataSource?,
        multiMedia: MultiMedia,
        lisMultiMedia: ArrayList<MultiMedia>,
        idPost: String
    ) {
        if (jzDataSource == null) {
            PreviewMediaDialog.Builder(
                requireContext(),
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
                requireContext(),
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
        bottomSheetDialog.show(childFragmentManager, "Dialog")
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

    private fun reFreshView() {
        binding.root.invalidate()
    }

    override fun onSeeMore(post: Post, position: Int) {
        if (post.idUserPost != UserCache.getUser().id) {
            val dialog = ChooseOptionBottomDialog.Builder(requireContext(), AppConstants.TWO_OPTION)
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
                    val rpDialog = ReportPostDialog.Builder(context!!, post.id)
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
        } else {
            val dialog = ChooseOptionBottomDialog.Builder(requireContext(), AppConstants.ONE_OPTION)
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
                    val dialog = ConfirmDialog.Builder(
                        context!!,
                        getString(R.string.notification),
                        getString(R.string.confirm_remove_post)
                    )
                        .onActionDone(object : ConfirmDialog.Builder.OnActionDone {
                            override fun onActionDone(isConfirm: Boolean) {
                                if (isConfirm) {
                                    dialog.dismiss()
                                    showDialog("")
                                    val storageReference = FirebaseStorage.getInstance().reference
                                    val storageRef: StorageReference =
                                        storageReference.child(
                                            UploadFireStorageUtils.getRootURLPostById(
                                                post.id
                                            )
                                        )
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            if (post.multiMedia.isNotEmpty() && post.typePost !in arrayListOf(
                                                    PostConstants.POST_UPDATE_BACKGROUND,
                                                    PostConstants.POST_UPDATE_AVATAR
                                                )
                                            ) {
                                                FireCloudManager.Companion.deleteFilesFromFirebaseStorage(
                                                    post.multiMedia,
                                                    storageRef,
                                                    object :
                                                        FireCloudManager.Companion.FireStoreCallback<ArrayList<MultiMedia>> {
                                                        override fun onSuccess(result: ArrayList<MultiMedia>) {
                                                            PostManagerFSDB.deletePost(post.id,
                                                                object :
                                                                    PostManagerFSDB.Companion.FireStoreCallback<Boolean> {
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
                                            } else {
                                                PostManagerFSDB.deletePost(post.id, object :
                                                    PostManagerFSDB.Companion.FireStoreCallback<Boolean> {
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
        if (user.id == UserCache.getUser().id) {
            activity?.let {
                val intent = Intent(it, Class.forName(ModuleClassConstants.PERSONAL_ACTIVITY))
                intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                it.startActivity(intent)
            }
        } else {
            activity?.let {
                val intent =
                    Intent(it, Class.forName(ModuleClassConstants.PERSONAL_MEMBER_ACTIVITY))
                intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                it.startActivity(intent)
            }
        }
    }

    private fun reloadPostEveryOneMinutes() {
        // Khởi tạo Handler
        val handler = Handler(Looper.getMainLooper())
        // Hàm mà bạn muốn thực hiện sau mỗi khoảng thời gian
        val runnable = object : Runnable {
            override fun run() {
                // Thực hiện công việc ở đây
                Timber.tag("Call API get data post every one minutes").i("is calling ...")
                // Lập lịch để gọi hàm này lại sau một khoảng thời gian cố định (ví dụ: sau một phút)
                handler.postDelayed(this, 60000) // 60000 milliseconds = 1 phút
            }
        }

        // Bắt đầu bằng cách gọi hàm runnable lần đầu
        handler.post(runnable)
    }

    private fun getPostById(postUpdate: Post, position: Int) {
        PostManagerFSDB.getPostById(postUpdate.id, object : PostManagerFSDB.Companion.PostCallback {
            override fun onPostFound(post: Post?) {
                if (post != null) {
                    post.isReloadDataNotPic = true
                    listPost[position] = post
                    postAdapter.notifyItemChanged(position)
                } else {
                    toast(R.string.please_try_later)
                }
            }

            override fun onFailure(exception: Exception) {
                Timber.tag("Get Post Exception").i(exception)
            }

        })
    }


    private fun startShimmer() {
        binding.sflLoadData.startShimmer()
        binding.rvPost.hide()
        binding.sflLoadData.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccess(timer: Long) {
        postDelayed({
            binding.rvPost.show()
            binding.sflLoadData.stopShimmer()
            binding.sflLoadData.hide()
        }, timer)

    }

    private fun startShimmerStory() {
        binding.sflLoadStory.startShimmer()
        binding.rvStory.hide()
        binding.sflLoadStory.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccessStory(timer: Long) {
        postDelayed({
            binding.rvStory.show()
            // hide and stop simmer
            binding.sflLoadStory.stopShimmer()
            binding.sflLoadStory.hide()
        }, timer)

    }


    override fun onCreateStory() {
        PhotoPickerUtils.showImagePickerUploadPosterInEvent(
            getAttachActivity()!!, launcherStory, ArrayList()
        )
    }

    override fun onClick(position: Int) {
        showStories(position)
    }

    fun showStories(position: Int) {
        val user = ListUserCache.getList().first { it.id == listStory[position].idUserCreate }
        val myStories = ArrayList<MyStory>()
        val story1 = MyStory()
//        story1.date = listStory[position].timeCreated
        story1.url = listStory[position].multiMedia.first().url
        myStories.add(story1)

        StoryView.Builder(childFragmentManager).setStoriesList(myStories).setStoryDuration(5000)
            .setTitleText(user.name) // TÊN USER
            .setSubtitleText(
                TimeUtils.timeAgoChat(
                    requireContext(), listStory[position].timeCreated
                )
            ) // Thơừi gian đăng
            .setTitleLogoUrl(user.avatar).setStoryClickListeners(object : StoryClickListeners {
                override fun onDescriptionClickListener(position: Int) {
                    // code
                }

                override fun onTitleIconClickListener(position: Int) {
                    // code
                }
            }).setOnStoryChangedCallback { position ->
                // code
            }.setStartingIndex(0).setRtl(false).build().show()
    }

    override fun clickNameUserInPost(user: User) {
        if (user.id == UserCache.getUser().id) {
            activity?.let {
                val intent = Intent(it, Class.forName(ModuleClassConstants.PERSONAL_ACTIVITY))
                intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                it.startActivity(intent)
            }
        } else {
            activity?.let {
                val intent =
                    Intent(it, Class.forName(ModuleClassConstants.PERSONAL_MEMBER_ACTIVITY))
                intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                it.startActivity(intent)
            }
        }
    }

    override fun clickListUser(listUserTag: ArrayList<User>) {
        TagUserInPostDialog.Builder(requireContext(), listUserTag)
            .onActionDone(object : TagUserInPostDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }

                override fun onClickUser(user: User) {
                    if (user.id == UserCache.getUser().id) {
                        activity?.let {
                            val intent =
                                Intent(it, Class.forName(ModuleClassConstants.PERSONAL_ACTIVITY))
                            intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                            it.startActivity(intent)
                        }
                    } else {
                        activity?.let {
                            val intent = Intent(
                                it,
                                Class.forName(ModuleClassConstants.PERSONAL_MEMBER_ACTIVITY)
                            )
                            intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                            it.startActivity(intent)
                        }
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
        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            BrowserActivity.start(
                getAttachActivity()!!,
                "https://www.google.com/maps?q=$latitude,$longitude",
                ""
            )
        }
    }


    // ##  Subscribe Event Bus


    @Subscribe
    fun onNotificationChange(event: NotificationEventBus) {
        if (event.onAdded) {
            val list = listNotificationUnread.filter { it.id == event.notification.id }
            if (list.isEmpty()) {
                listNotificationUnread.add(event.notification)
                setViewNumberNotification()
            }
        } else {
            val list = listNotificationUnread.filter { it.id == event.notification.id }
            if (list.isNotEmpty()) {
                listNotificationUnread.remove(list.first())
            }
            setViewNumberNotification()
        }
    }

    @Subscribe
    fun onAddedNewPost(eventBus: AddedPostEventBus) {
        if (eventBus.isAdded) {
            listPost.add(0, eventBus.post)
            postAdapter.notifyItemInserted(0)
        }
    }

    @Subscribe
    fun onReloadPostFromCommentFragment(eventBus: ReloadDataNotImageEventBus) {
        if (eventBus.isReload) {
            eventBus.post.isReloadDataNotPic = true
            listPost[eventBus.position] = eventBus.post
            if (eventBus.position == -1) {
                val index = listPost.indexOf(listPost.first { it.id == eventBus.post.id })
                listPost[index] = eventBus.post
                postAdapter.notifyItemChanged(index)
            } else {
                postAdapter.notifyItemChanged(eventBus.position)
            }
        }
    }

}