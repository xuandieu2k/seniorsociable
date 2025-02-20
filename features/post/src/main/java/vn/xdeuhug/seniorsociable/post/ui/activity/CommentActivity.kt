package vn.xdeuhug.seniorsociable.post.ui.activity

import android.view.View
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.post.databinding.ActivityCommentBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 11 / 12 / 2023
 */
class CommentActivity:AppActivity() {
    private lateinit var binding: ActivityCommentBinding
    private var postID = ""
    override fun getLayoutView(): View {
        binding = ActivityCommentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        //
    }

    override fun initData() {
        //
    }
}