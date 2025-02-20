package vn.xdeuhug.seniorsociable.friends.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.friends.constants.FriendsConstants
import vn.xdeuhug.seniorsociable.friends.databinding.FragmentSearchFriendBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.other.doOnQueryTextListener
import vn.xdeuhug.seniorsociable.ui.adapter.SearchUserAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import java.io.Serializable

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 09 / 11 / 2023
 */
class SearchFriendActivity : AppActivity(), SearchUserAdapter.OnListenerCLick {
    private lateinit var binding: FragmentSearchFriendBinding

    /* *
    *  init data
    * */
    private lateinit var searchUserAdapter: SearchUserAdapter
    private var listUser = ArrayList<User>()
    override fun getLayoutView(): View {
        binding = FragmentSearchFriendBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        initFriend()
        setClickButton()
        setUpDoOnSearch()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpDoOnSearch() {
        binding.svSearch.doOnQueryTextListener(1000) {
            if (it.isNotEmpty()) {
                getUserByKeyword(it)
            } else {
                listUser.clear()
                searchUserAdapter.notifyDataSetChanged()
                binding.rvUser.hide()
                binding.llNoData.root.hide()
                binding.llFindFriends.show()
            }
        }
    }

    private fun getUserByKeyword(key: String) {
        UserManagerFSDB.searchUsersByName(key,
            object : UserManagerFSDB.Companion.FireStoreCallback<ArrayList<User>> {

                @SuppressLint("NotifyDataSetChanged")
                override fun onSuccess(result: ArrayList<User>) {
                    listUser.clear()
                    listUser.addAll(result)
                    searchUserAdapter.notifyDataSetChanged()
                    if (listUser.isEmpty()) {
                        binding.rvUser.hide()
                        binding.llNoData.root.show()
                        binding.llFindFriends.hide()
                    } else {
                        binding.rvUser.show()
                        binding.llNoData.root.hide()
                        binding.llFindFriends.hide()
                    }
                }

                override fun onFailure(exception: Exception) {
                    toast(R.string.please_try_later)
                }

            })
    }

    private fun setClickButton() {
        binding.ivmBack.clickWithDebounce {
            finish()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initFriend() {
        searchUserAdapter = SearchUserAdapter(this)
        searchUserAdapter.onListenerCLick = this
        searchUserAdapter.setData(listUser)
        searchUserAdapter.notifyDataSetChanged()
        AppUtils.initRecyclerViewVertical(binding.rvUser, searchUserAdapter)
    }

    override fun initData() {
        //
    }

    override fun onClick(position: Int) {
        if(listUser[position].id == UserCache.getUser().id)
        {
            try {
                intent = Intent(this, Class.forName(ModuleClassConstants.PERSONAL_ACTIVITY))
                intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(listUser[position]))
                startActivity(intent)
            } catch (e: ClassNotFoundException) {
                //code
            }
        }else{
            try {
                intent = Intent(this, Class.forName(ModuleClassConstants.PERSONAL_MEMBER_ACTIVITY))
                intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(listUser[position]))
                startActivity(intent)
            } catch (e: ClassNotFoundException) {
                //code
            }
        }
    }
}