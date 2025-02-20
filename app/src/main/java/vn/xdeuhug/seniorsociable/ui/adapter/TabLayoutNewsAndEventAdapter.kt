package vn.xdeuhug.seniorsociable.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 10 / 2023
 */
@Suppress("DEPRECATION")
class TabLayoutNewsAndEventAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            Class.forName(ModuleClassConstants.NEWS_FRAGMENT).newInstance() as AppFragment<*>
        } else {
            Class.forName(ModuleClassConstants.EVENT_FRAGMENT).newInstance() as AppFragment<*>
        }
    }
}