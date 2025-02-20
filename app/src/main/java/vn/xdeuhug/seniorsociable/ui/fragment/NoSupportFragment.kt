package vn.xdeuhug.seniorsociable.ui.fragment

import android.view.View
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.databinding.FragmentNoSupportBinding
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity

/**
 * @Author: NGUYEN THE DAT
 * @Date: 4/7/2023
 */
class NoSupportFragment : AppFragment<HomeActivity>() {
    companion object {
        fun newInstance(): NoSupportFragment {
            return NoSupportFragment()
        }
    }

    private lateinit var binding: FragmentNoSupportBinding
    override fun getLayoutView(): View {
        binding = FragmentNoSupportBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initData() {

    }
}