package vn.xdeuhug.seniorsociable.ui.activity

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.xdeuhug.base.PagerAdapter
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.databinding.ActivityDetailInteractBinding
import vn.xdeuhug.seniorsociable.model.entity.Tab
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.ui.adapter.TabAdapter
import vn.xdeuhug.seniorsociable.ui.fragment.TabItemInteract
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.PostUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 11 / 2023
 */
class DetailInteractActivity : AppActivity(), TabAdapter.OnListenerCLick {
    private lateinit var binding: ActivityDetailInteractBinding
    private var listInteract = ArrayList<Interact>()
    private var listTab = ArrayList<Tab>()
    private var currentPage = 0
    private lateinit var tabAdapter: TabAdapter

    private val fragments = ArrayList<Fragment>()
    override fun getLayoutView(): View {
        binding = ActivityDetailInteractBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        AppUtils.setFontTypeFaceTitleBar(this, binding.tbTitle)
        initReactTab()
    }

    private fun initTabLayout(countInteract: Int) {
        fragments.clear()
        listTab.forEach {
            if (countInteract >= 2 && it.id == 0) {
                //
                val listIds = ArrayList<String>()
                listIds.addAll(listInteract.map { item -> item.id })
                val fragment = TabItemInteract(listIds, listInteract, 0)
                fragments.add(fragment)
            } else {
                val listIds = ArrayList<String>()
                listIds.addAll(listInteract.filter { item -> item.type == it.id }
                    .map { item -> item.id })
                val fragment = TabItemInteract(listIds, it.id)
                fragments.add(fragment)
            }
        }

        val adapter = PagerAdapter(this, fragments)
        binding.vpTab.adapter = adapter
        if(countInteract >= 2){
            binding.vpTab.offscreenPageLimit = adapter.itemCount - 1
        }else{
            binding.vpTab.offscreenPageLimit = 1
        }
        binding.vpTab.post {
            binding.vpTab.setCurrentItem(currentPage, false)
            binding.vpTab.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    setAgainTab(binding.vpTab.currentItem)
                    binding.rvTab.scrollToPosition(binding.vpTab.currentItem)
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initReactTab() {
        val bundleIntent = intent.extras
        if (bundleIntent != null && bundleIntent.containsKey(PostConstants.OBJECT_DETAIL_INTERACT)) {
            val listType = object : TypeToken<ArrayList<Interact>>() {}.type
            listInteract.clear()
            listInteract.addAll(
                Gson().fromJson(
                    bundleIntent.getString(PostConstants.OBJECT_DETAIL_INTERACT), listType
                )
            )
        }
        val sort = PostUtils.sortInteract(listInteract)
        tabAdapter = TabAdapter(getContext())
        tabAdapter.onListenerCLick = this
        AppUtils.initTabReact(sort, binding.rvTab, tabAdapter)
        listTab = tabAdapter.getData() as ArrayList<Tab>
        if (sort.size >= 2) {
            listTab.forEach { it.isSelected = false }
            listTab.add(0, Tab(0, "", listInteract.size, true))
        }
        tabAdapter.notifyDataSetChanged()
        initTabLayout(sort.size)

    }

    override fun initData() {
        //
    }

    override fun onClick(position: Int) {
        binding.vpTab.setCurrentItem(position, true)
        setAgainTab(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAgainTab(position: Int) {
        // Tìm phần tử được chọn trước đó và đặt isSelected về false
        val previousSelectedIndex = listTab.indexOfFirst { it.isSelected }
        if (previousSelectedIndex != -1) {
            listTab[previousSelectedIndex].isSelected = false
        }

        // Đặt phần tử mới được chọn về true
        listTab[position].isSelected = true

        // Kiểm tra xem có cần cập nhật giao diện người dùng không
        if (previousSelectedIndex != -1 || listTab[position].isSelected) {
            tabAdapter.notifyItemChanged(previousSelectedIndex)
            tabAdapter.notifyItemChanged(position)
        }

    }
}