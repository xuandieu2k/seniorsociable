package vn.xdeuhug.seniorsociable.chat.ui.activity

import android.view.View
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.chat.databinding.ActivityVideoCallBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 11 / 12 / 2023
 */
class VideoCallActivity : AppActivity() {
    private lateinit var binding: ActivityVideoCallBinding
    override fun getLayoutView(): View {
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        //
    }

    override fun initData() {
        //
    }
}