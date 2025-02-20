package vn.xdeuhug.seniorsociable.post.ui.activity

import android.view.View
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.post.databinding.ActivityCreatePostBinding
import vn.xdeuhug.seniorsociable.post.databinding.ActivityStoriesBinding
import vn.xdeuhug.seniorsociable.ui.adapter.MultiMediaAdapter

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 06 / 12 / 2023
 */
class StoriesActivity: AppActivity() {
    private lateinit var binding: ActivityStoriesBinding
    override fun getLayoutView(): View {
        binding = ActivityStoriesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        //
    }

    override fun initData() {
        //
    }
}