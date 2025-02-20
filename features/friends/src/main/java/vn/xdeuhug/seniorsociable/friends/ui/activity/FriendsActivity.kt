package vn.xdeuhug.seniorsociable.friends.ui.activity

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.database.FriendManagerFSDB
import vn.xdeuhug.seniorsociable.database.InteractManagerFSDB
import vn.xdeuhug.seniorsociable.database.RequestAddFriendManagerFSDB
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.friends.databinding.ActivityFriendsBinding
import vn.xdeuhug.seniorsociable.friends.ui.adapter.RequestAddFriendAdapter
import vn.xdeuhug.seniorsociable.model.entity.modelFriend.Friend
import vn.xdeuhug.seniorsociable.model.entity.modelFriend.RequestAddFriend
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.AppUtils.toArrayList
import java.util.Date


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 09 / 11 / 2023
 */
class FriendsActivity : AppActivity(), RequestAddFriendAdapter.OnListenerCLick {
    private lateinit var binding: ActivityFriendsBinding

    /* *
    *  init data
    * */
    private lateinit var requestAddFriendAdapter: RequestAddFriendAdapter
    private var listUser = ArrayList<User>()
    private var listRequestAddFriend = ArrayList<RequestAddFriend>()
    private var listPair = ArrayList<Pair<User,RequestAddFriend>>()
    override fun getLayoutView(): View {
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        AppUtils.setFontTypeFaceTitleBar(this, binding.tbAddFriends)
        startShimmer()
        initRequestAddFriend()
        clickSearch()
    }

    @SuppressLint("CommitTransaction")
    private fun clickSearch() {
        binding.tbAddFriends.rightView.clickWithDebounce {
            startActivity<SearchFriendActivity>()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRequestAddFriend() {
        requestAddFriendAdapter = RequestAddFriendAdapter(this)
        requestAddFriendAdapter.setData(listPair)
        requestAddFriendAdapter.onListenerCLick = this
        AppUtils.initRecyclerViewVertical(binding.rvRequestAddFriends, requestAddFriendAdapter)
        fetchFriendRequests()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fetchFriendRequests() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val requestUserCurrentJob = async {
                    RequestAddFriendManagerFSDB.getAllRequestAddFriendByUserIdAsync(UserCache.getUser().id)
                }.await()
                listRequestAddFriend.clear()
                listRequestAddFriend.addAll(requestUserCurrentJob)
                val listIds = requestUserCurrentJob.map { it.id }.toArrayList()
                if(listIds.isNotEmpty())
                {
                    val users = async { UserManagerFSDB.getUsersByUserIdsAsync(listIds) }
                    val list = users.await()
                    listUser.addAll(list)
                }
                handleSuccess(0)
                setViewNoData(listUser.isNotEmpty())
                setDataListPair()
                requestAddFriendAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                // Xử lý ngoại lệ khi gặp lỗi
                e.printStackTrace()
                toast(R.string.please_try_later)
                handleSuccess(0)
                setViewNoData(false)
            }
        }
    }

    override fun initData() {
        //
    }

    override fun onClickConfirm(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val friend = Friend(listUser[position].id, Date())
                val friendEnemy = Friend(UserCache.getUser().id, Date())
                val addResult = FriendManagerFSDB.addFriendAsync(friend, UserCache.getUser().id)
                val addResultEnemy = FriendManagerFSDB.addFriendAsync(friendEnemy, listUser[position].id)
                val deleteRequest =
                    RequestAddFriendManagerFSDB.deleteRequestAddFriendAsync(listUser[position].id)
                if (addResult && addResultEnemy && deleteRequest) {
                    toast("${listUser[position].name} ${getString(R.string.and_you_is_become_friend)}")
                    listUser.removeAt(position)
                    listRequestAddFriend.removeAt(position)
                    listPair.removeAt(position)
                    requestAddFriendAdapter.notifyItemRemoved(position)
                    setViewNoData(listUser.isNotEmpty())

                } else {
                    toast(R.string.please_try_later)
                }
            }
        }
    }

    override fun onClickCancel(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val deleteRequest =
                    RequestAddFriendManagerFSDB.deleteRequestAddFriendAsync(listUser[position].id)
                if (deleteRequest) {
                    toast("${getString(R.string.you_is_denied_add_friend)}")
                    listUser.removeAt(position)
                    listRequestAddFriend.removeAt(position)
                    listPair.removeAt(position)
                    requestAddFriendAdapter.notifyItemRemoved(position)
                    setViewNoData(listUser.isNotEmpty())
                } else {
                    toast(R.string.please_try_later)
                }
            }
        }
    }

    private fun startShimmer() {
        binding.sflLoadData.startShimmer()
        binding.rvRequestAddFriends.hide()
        binding.sflLoadData.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccess(timer: Long) {
        postDelayed({
            // hide and stop simmer
            binding.sflLoadData.stopShimmer()
            binding.sflLoadData.hide()
            binding.rvRequestAddFriends.show()
        }, timer)

    }

    private fun setViewNoData(isHaveData: Boolean) {
        if (isHaveData) {
            binding.rvRequestAddFriends.show()
            binding.ltNoData.root.hide()
        } else {
            binding.rvRequestAddFriends.hide()
            binding.ltNoData.root.show()
        }
    }

    private fun setDataListPair()
    {
        listPair.clear()
        listRequestAddFriend.forEachIndexed { index, requestAddFriend ->
            val pair = Pair(listUser[index],requestAddFriend)
            listPair.add(pair)
        }
    }
}