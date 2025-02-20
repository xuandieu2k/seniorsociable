package vn.xdeuhug.seniorsociable.personalPage.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import cn.jzvd.JZDataSource
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.anko.support.v4.startActivity
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
import vn.xdeuhug.seniorsociable.database.RequestAddFriendManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelFriend.Friend
import vn.xdeuhug.seniorsociable.model.entity.modelFriend.RequestAddFriend
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.personalPage.databinding.ActivityPersonalMemberBinding
import vn.xdeuhug.seniorsociable.personalPage.ui.adapter.FriendsAdapter
import vn.xdeuhug.seniorsociable.ui.adapter.PostAdapter
import vn.xdeuhug.seniorsociable.ui.dialog.ChooseOptionBottomDialog
import vn.xdeuhug.seniorsociable.ui.dialog.ConfirmDialog
import vn.xdeuhug.seniorsociable.ui.dialog.PreviewMediaDialog
import vn.xdeuhug.seniorsociable.ui.dialog.ReportPostDialog
import vn.xdeuhug.seniorsociable.ui.dialog.WarningDialog
import vn.xdeuhug.seniorsociable.ui.fragment.CommentFragment
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.ReactObjectUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import vn.xdeuhug.seniorsociable.widget.reactbutton.Reaction
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 10 / 11 / 2023
 */
class PersonalMemberActivity : AppActivity(), PostAdapter.OnListenerCLick {
    private lateinit var binding: ActivityPersonalMemberBinding

    // init adapter
    private var listFriends = ArrayList<User>()
    private lateinit var friendsAdapter: FriendsAdapter

    /* Data and Adapter for Post */
    private lateinit var postAdapter: PostAdapter
    private var listPost = ArrayList<Post>()
    private var user = User()


    private var isRequestFromYou = false
    private var isRequestFromEnemy = false

    /* paging*/
    private var limit = AppConstants.PAGE_SIZE
    private var currentPage = 1
    private var lastVisible: DocumentSnapshot? = null
    private var loading = false
    private var lastPage = false
    /* end */

    //
    private var typeFriend = AppConstants.TYPE_IS_NOT_REQUESTED_ADD_FRIENDS
    override fun getLayoutView(): View {
        binding = ActivityPersonalMemberBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        AppUtils.setFontTypeFaceTitleBar(this, binding.tbCreatePost)
        startShimmer()
        setClickButton()
        initProfile()
        initFriendsList()
        setDataPost()
    }

    private fun setClickButton() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val userJob =
                    async { FriendManagerFSDB.getFriendByUserIdAsync(this@PersonalMemberActivity.user.id) }
                val requestUserCurrentJob = async {
                    RequestAddFriendManagerFSDB.getAllRequestAddFriendByUserIdAsync(UserCache.getUser().id)
                }
                val requestEnemyJob =
                    async { RequestAddFriendManagerFSDB.getAllRequestAddFriendByUserIdAsync(this@PersonalMemberActivity.user.id) }
                val user = userJob.await()
                val requestUserCurrentList = requestUserCurrentJob.await()
                val requestEnemyList = requestEnemyJob.await()
                if (user != null) // Đã là bạn bè
                {
                    typeFriend = AppConstants.TYPE_IS_FRIENDS
                    Timber.tag("Log TYPE FRIEND").i("Bạn bè")
                } else {
                    if (requestEnemyList.any { it.id == UserCache.getUser().id }) {
                        typeFriend = AppConstants.TYPE_IS_REQUESTED_ADD_FRIENDS
                        Timber.tag("Log TYPE FRIEND").i("Đã gửi lời mời")

                    } else {
                        typeFriend = AppConstants.TYPE_IS_NOT_REQUESTED_ADD_FRIENDS
                        Timber.tag("Log TYPE FRIEND").i("Chưa gửi lời mời")
                        if (requestUserCurrentList.any { it.id == this@PersonalMemberActivity.user.id }) {
                            typeFriend = AppConstants.TYPE_ENEMY_IS_REQUESTED_ADD_FRIENDS
                        }
                        Timber.tag("Log TYPE FRIEND").i("Đối phương đã gửi lời mời")

                    }
                }
                setViewForButtonAddFriend()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

//        registerListener()

        AppUtils.setChildListener(binding.llAddFriend) {
            setAddFriend()
        }

        AppUtils.setChildListener(binding.llChat) {
            try {
                val intent = Intent(
                    this@PersonalMemberActivity,
                    Class.forName(ModuleClassConstants.DETAIL_CHAT_ACTIVITY)
                )
                intent.putExtra(
                    AppConstants.OBJECT_CHAT, Gson().toJson(
                        user
                    )
                )
                startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun registerListener() {
        RequestAddFriendManagerFSDB.registerListenerChangeDataRequest(
            UserCache.getUser().id, user.id
        ) { isHasIdCheck ->
            if (isHasIdCheck) {
                typeFriend = AppConstants.TYPE_ENEMY_IS_REQUESTED_ADD_FRIENDS
                setViewForButtonAddFriend()
            }
        }

//        RequestAddFriendManagerFSDB.registerListenerChangeDataRequest(
//            user.id, UserCache.getUser().id
//        ) { isHasIdCheck ->
//            if (isHasIdCheck) {
//                typeFriend = AppConstants.TYPE_IS_REQUESTED_ADD_FRIENDS
//                setViewForButtonAddFriend()
//            }
//        }

        FriendManagerFSDB.registerListenerChangeDataFriend(
            UserCache.getUser().id, user.id
        ) { isHasIdCheck ->
            typeFriend = if (isHasIdCheck) {
                AppConstants.TYPE_IS_FRIENDS
            } else {
                AppConstants.TYPE_IS_NOT_REQUESTED_ADD_FRIENDS
            }
            runBlocking {
                val isEnemyIdInYourRequest = async {
                    RequestAddFriendManagerFSDB.checkIdIsSendingRequestAddFriend(
                        UserCache.getUser().id,
                        this@PersonalMemberActivity.user.id
                    )
                }.await()
                val isYourIdInEnemyRequest =
                    async {
                        RequestAddFriendManagerFSDB.checkIdIsSendingRequestAddFriend(
                            this@PersonalMemberActivity.user.id,
                            UserCache.getUser().id
                        )
                    }.await()
                if (isEnemyIdInYourRequest) {
                    typeFriend = AppConstants.TYPE_ENEMY_IS_REQUESTED_ADD_FRIENDS
                }
                if (isYourIdInEnemyRequest) {
                    typeFriend = AppConstants.TYPE_IS_REQUESTED_ADD_FRIENDS
                }
                if (!isHasIdCheck && isEnemyIdInYourRequest && isYourIdInEnemyRequest) {
                    typeFriend = AppConstants.TYPE_IS_NOT_REQUESTED_ADD_FRIENDS
                }
            }
            setViewForButtonAddFriend()
        }
    }

    private fun setAddFriend() {
        when (typeFriend) {
            AppConstants.TYPE_IS_FRIENDS -> {
                showBottomDialogOption()
            }

            AppConstants.TYPE_IS_REQUESTED_ADD_FRIENDS -> {
                showBottomDialogOption()
            }

            AppConstants.TYPE_IS_NOT_REQUESTED_ADD_FRIENDS -> {
                val request =
                    RequestAddFriend(UserCache.getUser().id, Date(), AppConstants.IS_PENDING)
                RequestAddFriendManagerFSDB.addRequestAddFriend(user.id,
                    request,
                    object : RequestAddFriendManagerFSDB.Companion.FireStoreCallback<Boolean> {
                        override fun onSuccess(result: Boolean) {
                            if (result) {
                                toast(getString(R.string.is_send_request_add_friend))
                                typeFriend = AppConstants.TYPE_IS_REQUESTED_ADD_FRIENDS
                                setViewForButtonAddFriend()
                            } else {
                                toast(getString(R.string.please_try_later))
                            }
                        }

                        override fun onFailure(exception: Exception) {
                            toast(getString(R.string.please_try_later))
                        }

                    })
            }

            AppConstants.TYPE_ENEMY_IS_REQUESTED_ADD_FRIENDS -> {
                showBottomDialogOption()
            }
        }
    }

    private fun showBottomDialogOption() {
        when (typeFriend) {
            AppConstants.TYPE_IS_FRIENDS -> {
                val dialog = ChooseOptionBottomDialog.Builder(getContext(), AppConstants.ONE_OPTION)
                dialog.setDataForView(
                    getString(R.string.unfriend),
                    "",
                    "",
                    "",
                    "",
                    R.drawable.ic_user_remove,
                    0,
                    0,
                    0,
                    0
                )
                dialog.onActionDone(object : ChooseOptionBottomDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean) {
                        //
                    }

                    override fun onClickButton1() { // Hủy kết bạn
                        WarningDialog.Builder(
                            getContext(),
                            getString(R.string.confirm_unfriend),
                            "${user.name} ${getString(R.string.content_warning_unfriend)}",
                            AppConstants.TYPE_WARNING
                        ).onActionDone(object : WarningDialog.Builder.OnActionDone {
                            override fun onActionDone(isConfirm: Int) {
                                if(isConfirm == AppConstants.BUTTON_CONFIRM){
                                    showDialog(getString(R.string.is_processing))
                                    runBlocking {
                                        val removeFriend =
                                            FriendManagerFSDB.deleteFriendAsync(UserCache.getUser().id)
                                        val removeFriendEnemy =
                                            FriendManagerFSDB.deleteFriendAsync(user.id)
                                        if (removeFriend && removeFriendEnemy) {
                                            hideDialog()
                                            toast(getString(R.string.is_canceled_add_friend))
                                            typeFriend =
                                                AppConstants.TYPE_IS_NOT_REQUESTED_ADD_FRIENDS
                                            setViewForButtonAddFriend()
                                        } else {
                                            toast(R.string.please_try_later)
                                            hideDialog()
                                        }
                                        dialog.dismiss()
                                    }
                                }
                            }

                        }).create().show()
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

            AppConstants.TYPE_IS_REQUESTED_ADD_FRIENDS -> { // Đã gửi yêu cầu kết bạn
                val dialog = ChooseOptionBottomDialog.Builder(getContext(), AppConstants.ONE_OPTION)
                dialog.setDataForView(
                    getString(R.string.cancel_request),
                    "",
                    "",
                    "",
                    "",
                    R.drawable.ic_user_remove,
                    0,
                    0,
                    0,
                    0
                )
                dialog.onActionDone(object : ChooseOptionBottomDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean) {
                        //
                    }

                    override fun onClickButton1() { // Hủy yêu cầu kết bạn
                        showDialog(getString(R.string.is_processing))
                        runBlocking {
                            val deleteRequest =
                                RequestAddFriendManagerFSDB.deleteRequestAddFriendFromYouAsync(user.id)
                            if (deleteRequest) {
                                hideDialog()
                                toast(getString(R.string.is_canceled_request_add_friend))
                                typeFriend = AppConstants.TYPE_IS_NOT_REQUESTED_ADD_FRIENDS
                                setViewForButtonAddFriend()
                            } else {
                                toast(R.string.please_try_later)
                                hideDialog()
                            }
                            dialog.dismiss()
                        }
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

            AppConstants.TYPE_ENEMY_IS_REQUESTED_ADD_FRIENDS -> { // Đối phương đã gửi yêu cầu kết bạn
                val dialog = ChooseOptionBottomDialog.Builder(getContext(), AppConstants.TWO_OPTION)
                dialog.setDataForView(
                    getString(R.string.confirm),
                    getString(R.string.delete_request),
                    "",
                    "",
                    "",
                    R.drawable.ic_user_confirm,
                    R.drawable.ic_user_remove,
                    0,
                    0,
                    0
                )
                dialog.onActionDone(object : ChooseOptionBottomDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean) {
                        //
                    }

                    override fun onClickButton1() { // Xác nhận yêu cầu kết bạn từ yêu cầu của đối phương
                        showDialog(getString(R.string.is_processing))
                        runBlocking {
                            val friend = Friend(user.id, Date())
                            val deleteRequest =
                                RequestAddFriendManagerFSDB.deleteRequestAddFriendAsync(user.id)
                            val friendEnemy = Friend(UserCache.getUser().id, Date())
                            val addResult =
                                FriendManagerFSDB.addFriendAsync(friend, UserCache.getUser().id)
                            val addResultEnemy =
                                FriendManagerFSDB.addFriendAsync(friendEnemy, user.id)
                            if (addResult && addResultEnemy && deleteRequest) {
                                hideDialog()
                                toast("${user.name} ${getString(R.string.and_you_is_become_friend)}")
                                typeFriend = AppConstants.TYPE_IS_FRIENDS
                                setViewForButtonAddFriend()
                            } else {
                                toast(R.string.please_try_later)
                                hideDialog()
                            }
                            dialog.dismiss()
                        }
                    }

                    override fun onClickButton2() { // Gỡ yêu cầu kết bạn từ đối phương
                        runBlocking {
                            val deleteRequest =
                                RequestAddFriendManagerFSDB.deleteRequestAddFriendAsync(user.id)
                            if (deleteRequest) {
                                hideDialog()
                                toast("${getString(R.string.you_is_denied_add_friend)}")
                                typeFriend = AppConstants.TYPE_IS_NOT_REQUESTED_ADD_FRIENDS
                                setViewForButtonAddFriend()
                            } else {
                                toast(R.string.please_try_later)
                                hideDialog()
                            }
                            dialog.dismiss()
                        }
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
    }

    private fun setViewForButtonAddFriend() {
        Timber.tag("Log data:").i("$typeFriend")
        when (typeFriend) {
            AppConstants.TYPE_IS_FRIENDS -> {
                binding.imvAddFriend.setImageResource(R.drawable.ic_user_confirm)
                binding.tvAddFriend.text = getString(R.string.friend)
            }

            AppConstants.TYPE_IS_REQUESTED_ADD_FRIENDS -> {
                binding.imvAddFriend.setImageResource(R.drawable.ic_user_waitting)
                binding.tvAddFriend.text = getString(R.string.pending_confirm)
            }

            AppConstants.TYPE_IS_NOT_REQUESTED_ADD_FRIENDS -> {
                binding.imvAddFriend.setImageResource(R.drawable.ic_user_plus)
                binding.tvAddFriend.text = getString(R.string.add_friends)
            }

            AppConstants.TYPE_ENEMY_IS_REQUESTED_ADD_FRIENDS -> {
                binding.imvAddFriend.setImageResource(R.drawable.ic_user_confirm)
                binding.tvAddFriend.text = getString(R.string.reply)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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

        FriendManagerFSDB.getAllFriendsByIdUser(user.id,object :
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

    @Suppress("DEPRECATION")
    private fun initProfile() {
        user = Gson().fromJson(intent.getStringExtra(AppConstants.PERSONAL_USER), User::class.java)
        binding.tvUsername.text = user.name

        PhotoShowUtils.loadAvatarImage(
            UploadFireStorageUtils.getRootURLAvatarById(user.id), user.avatar, binding.imvAvatar
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
        postAdapter.setData(listPost)
        postAdapter.onListenerCLick = this
        // Create recycleView
        AppUtils.initRecyclerViewVertical(binding.rvPost, postAdapter)
        getDataPost()
        customPaginate()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataPost() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val listTriple = async { PostManagerFSDB.getAllPostByIdUser(user.id,limit, lastVisible) }.await()
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
                finishShimmer()
                // Xử lý ngoại lệ khi gặp lỗi
                Timber.tag("Log Error$localClassName").e(e)
                toast(R.string.please_try_later)
                loading = false
                binding.loadMore.hide()
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
                    // Đã cuộn đến cuối cùng, thực hiện các xử lý tải dữ liệu mới ở đây
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
            val dialog = ChooseOptionBottomDialog.Builder(getContext(), AppConstants.ONE_OPTION)
            dialog.setDataForView(
                getString(R.string.report),
                "",
                "",
                "",
                "",
                R.drawable.ic_post_report,
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

    private fun setViewNoData(isHaveData: Boolean) {
        binding.tvNoData.text = "${user.name} ${getString(R.string.not_have_a_post)}"
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
}