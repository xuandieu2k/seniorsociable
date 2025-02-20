package vn.xdeuhug.seniorsociable.friends.ui.fragment

import android.view.View
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.friends.databinding.FragmentSearchFriendBinding
import vn.xdeuhug.seniorsociable.friends.ui.activity.FriendsActivity

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 09 / 11 / 2023
 */
class SearchFriendFragment : AppFragment<FriendsActivity>() {
    private lateinit var binding: FragmentSearchFriendBinding
    override fun getLayoutView(): View {
        binding = FragmentSearchFriendBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initData() {
        //
    }
}