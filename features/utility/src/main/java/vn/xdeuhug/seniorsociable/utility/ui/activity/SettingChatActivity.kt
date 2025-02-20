package vn.xdeuhug.seniorsociable.utility.ui.activity

import android.view.View
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.utility.databinding.ActivitySettingChatBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 12 / 2023
 */
class SettingChatActivity : AppActivity() {
    private lateinit var binding: ActivitySettingChatBinding
    override fun getLayoutView(): View {
        binding = ActivitySettingChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        //
    }

    override fun initData() {
        //
    }
}