package vn.xdeuhug.seniorsociable.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.database.RequestAddFriendManagerFSDB
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.ItemTabBinding
import vn.xdeuhug.seniorsociable.databinding.ItemTabInteractBinding
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.ui.activity.DetailInteractActivity
import vn.xdeuhug.seniorsociable.ui.adapter.UserInteractAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.toArrayList

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 11 / 2023
 */
class TabItemInteract() : AppFragment<DetailInteractActivity>(),
    UserInteractAdapter.OnListenerCLick {
    companion object {
        fun newInstance(): TabItemInteract {
            return TabItemInteract()
        }
    }

    constructor(listIdsUser: ArrayList<String>, id: Int) : this() {
        this.listIdsUser = listIdsUser
        this.id = id
    }

    constructor(
        listIdsUser: ArrayList<String>, listInteracts: ArrayList<Interact>, id: Int
    ) : this() {
        this.listIdsUser = listIdsUser
        this.id = id
        this.listInteracts = listInteracts
    }

    private var listUser = ArrayList<User>()
    private var listInteracts = ArrayList<Interact>()
    private lateinit var userInteractAdapter: UserInteractAdapter
    private var listIdsUser = ArrayList<String>()
    private var id = 0
    private lateinit var binding: ItemTabInteractBinding
    override fun getLayoutView(): View {
        binding = ItemTabInteractBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initData() {
        initViewData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initViewData() {
        initRecycleView()
        initRecycleView()
        val users = ArrayList<User>()
        users.addAll(ListUserCache.getList().filter { it.id in listIdsUser })
        listUser.clear()
        listUser.addAll(users)
        userInteractAdapter.notifyDataSetChanged()

    }

    private fun initRecycleView() {
        userInteractAdapter = if (id == 0) {
            UserInteractAdapter(requireContext(), listInteracts, id)
        } else {
            UserInteractAdapter(requireContext(), id)
        }
        userInteractAdapter.onListenerCLick = this
        userInteractAdapter.setData(listUser)
        AppUtils.initRecyclerViewVertical(binding.rvUser, userInteractAdapter)
    }

    override fun onClick(position: Int) {
        val user = listUser[position]
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
}