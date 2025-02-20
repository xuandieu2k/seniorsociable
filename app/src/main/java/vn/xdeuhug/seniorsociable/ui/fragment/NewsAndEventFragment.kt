package vn.xdeuhug.seniorsociable.ui.fragment

import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import org.greenrobot.eventbus.Subscribe
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.databinding.FragmentNewsAndEventBinding
import vn.xdeuhug.seniorsociable.model.eventbus.TabChangeEventBus
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.ui.adapter.TabLayoutNewsAndEventAdapter

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 10 / 2023
 */
class NewsAndEventFragment : AppFragment<HomeActivity>() {

    private lateinit var binding: FragmentNewsAndEventBinding
    private lateinit var tabLayout: Array<String>
    override fun getLayoutView(): View {
        binding = FragmentNewsAndEventBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initData() {
        tabLayout = arrayOf(getString(R.string.news), getString(R.string.event))
        binding.vp2ViewTab.adapter = TabLayoutNewsAndEventAdapter(childFragmentManager, lifecycle)
        binding.vp2ViewTab.isUserInputEnabled = false
        TabLayoutMediator(binding.tlTab, binding.vp2ViewTab) { tab, position ->
            tab.text = tabLayout[position]
        }.attach()

    }

    @Subscribe
    fun onTabChange(event:TabChangeEventBus){
        if(event.isChange)
        {
            when(event.tabCurrent)
            {
                AppConstants.TAB_EVENT ->{
                    binding.tlTab.getTabAt(1)?.select()
                }
                AppConstants.TAB_NEWS ->{
                    binding.tlTab.getTabAt(0)?.select()
                }
            }
        }
    }
}