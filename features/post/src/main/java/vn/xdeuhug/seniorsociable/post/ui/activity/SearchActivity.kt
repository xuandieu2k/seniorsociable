package vn.xdeuhug.seniorsociable.post.ui.activity

import android.view.View
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.post.databinding.ActivitySearchBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 10 / 12 / 2023
 */
class SearchActivity : AppActivity() {
    private lateinit var binding: ActivitySearchBinding
    override fun getLayoutView(): View {
        binding = ActivitySearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setBack()
    }

    private fun setBack() {
        binding.ivmBack.clickWithDebounce {
            finish()
        }
    }

    override fun initData() {
        //
    }
}