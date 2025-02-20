package vn.xdeuhug.seniorsociable.ui.dialog

import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import cn.jzvd.JZDataSource
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.databinding.DialogPostModerationBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.ui.adapter.MediaAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.clickWithDebounce
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 12 / 2023
 */
class PostModerationDialog {
    class Builder constructor(
        context: Context, var post: Post
    ) : BaseDialog.Builder<Builder>(context), MediaAdapter.OnListenerCLick {
        private var binding: DialogPostModerationBinding =
            DialogPostModerationBinding.inflate(LayoutInflater.from(context))

        val listUser = ListUserCache.getList()

        //
        lateinit var onActionDone: OnActionDone

        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(true)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            getDialog()?.window?.setBackgroundDrawableResource(R.drawable.bg_border_dialog)
            setWidth(Resources.getSystem().displayMetrics.widthPixels * 5 / 6)
            setHeight(Resources.getSystem().displayMetrics.heightPixels * 8 / 10)
            setDataForView()
            binding.btnCancel.clickWithDebounce {
                dismiss()
            }

            binding.btnConfirm.clickWithDebounce {
                val oldStatus = post.status
                setStatusPost()
                dismiss()
                onActionDone.onActionDone(true, post.status,oldStatus, post)
            }
            setViewRadioButton()
            setUpClickView()
        }

        private fun setStatusPost() {
            if (binding.rbPassed.isChecked) {
                post.status = PostConstants.IS_PASSED
            }
            if (binding.rbNotPassed.isChecked) {
                post.status = PostConstants.IS_NOT_PASSED
            }
            if (binding.rbPendingPassed.isChecked) {
                post.status = PostConstants.IS_PENDING_PASSED
            }
            if (binding.rbLocked.isChecked) {
                post.status = PostConstants.IS_LOCKED
            }
            if (binding.rbReOpen.isChecked) // mở lại thì thành đã duyệt
            {
                post.status = PostConstants.IS_PASSED
            }
        }

        private fun setViewRadioButton() {
            when (post.status) {
                PostConstants.IS_PASSED -> { // Đã duyệt -> Chỉ hiển thị Đã kiểm duyệt và khóa bài
                    binding.rbPassed.isChecked = true
                    //
                    binding.llPassed.show()
                    binding.llLocked.show()
                    binding.llNotPassed.hide()
                    binding.llReOpen.hide()
                    binding.llPendingPassed.hide()
                }

                PostConstants.IS_NOT_PASSED -> { // Không duyệt -> Chỉ hiển thị không duyệt
                    binding.rbNotPassed.isChecked = true
                    //
                    binding.llNotPassed.show()
                    binding.llPassed.hide()
                    binding.llLocked.hide()
                    binding.llReOpen.hide()
                    binding.llPendingPassed.hide()
                }

                PostConstants.IS_PENDING_PASSED -> { // Chờ duyệt -> Hiển thị Kiểm duyệt, không duyệt, khóa bài
                    binding.rbPendingPassed.isChecked = true
                    //
                    binding.llPendingPassed.show()
                    binding.llPassed.show()
                    binding.llLocked.hide()
                    binding.llNotPassed.show()
                    binding.llReOpen.hide()
                }

                PostConstants.IS_LOCKED -> { // Đã khóa -> Hiển thị Mở lại và khóa
                    binding.rbLocked.isChecked = true
                    //
                    binding.llPassed.hide()
                    binding.llLocked.show()
                    binding.llNotPassed.hide()
                    binding.llReOpen.show()
                    binding.llPendingPassed.hide()
                }
            }
        }

        private fun setUpClickView() {
            // Nút công khai
            AppUtils.setChildListener(binding.llPassed) {
                binding.rbPassed.isChecked = true
                binding.rbNotPassed.isChecked = false
                binding.rbLocked.isChecked = false
                binding.rbReOpen.isChecked = false
                binding.rbPendingPassed.isChecked = false
            }
            AppUtils.setChildListener(binding.llNotPassed) {
                binding.rbPassed.isChecked = false
                binding.rbNotPassed.isChecked = true
                binding.rbLocked.isChecked = false
                binding.rbReOpen.isChecked = false
                binding.rbPendingPassed.isChecked = false
            }
            AppUtils.setChildListener(binding.llLocked) {
                binding.rbPassed.isChecked = false
                binding.rbNotPassed.isChecked = false
                binding.rbLocked.isChecked = true
                binding.rbReOpen.isChecked = false
                binding.rbPendingPassed.isChecked = false
            }
            AppUtils.setChildListener(binding.llReOpen) {
                binding.rbReOpen.isChecked = true
                binding.rbPassed.isChecked = false
                binding.rbNotPassed.isChecked = false
                binding.rbLocked.isChecked = false
                binding.rbPendingPassed.isChecked = false
            }

            AppUtils.setChildListener(binding.llPendingPassed) {
                binding.rbPendingPassed.isChecked = true
                binding.rbReOpen.isChecked = false
                binding.rbPassed.isChecked = false
                binding.rbNotPassed.isChecked = false
                binding.rbLocked.isChecked = false
            }
        }

        private fun setDataForView() {
            val userPost = listUser.firstOrNull { it.id == post.idUserPost }
            val mediaAdapter = MediaAdapter(getContext(), userPost!!, post.typePost, post.id)
            mediaAdapter.onListenerCLick = this@Builder
            userPost.let {
                PhotoShowUtils.loadAvatarImage(
                    UploadFireStorageUtils.getRootURLAvatarById(it.id), it.avatar, binding.imvAuthor
                )
                binding.tvAuthor.text = AppUtils.fromHtml("<b>${it.name}</b>")
            }
            binding.tvContent.text = post.content
            AppUtils.setMedia(binding.rvMedia, post.multiMedia, mediaAdapter)
            if (post.multiMedia.isNotEmpty()) {
                binding.rvMedia.show()
            } else {
                binding.rvMedia.hide()
            }
        }


        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean, statusCurrent: Int, oldStatus: Int, postNew: Post)
        }

        override fun onClick(
            jzDataSource: JZDataSource?,
            multiMedia: MultiMedia,
            lisMultiMedia: ArrayList<MultiMedia>,
            idPost: String
        ) {
            if (jzDataSource == null) {
                PreviewMediaDialog.Builder(
                    getContext(),
                    multiMedia,
                    lisMultiMedia,
                    UploadFireStorageConstants.TYPE_POST,
                    UploadFireStorageUtils.getRootURLPostById(idPost),
                    null
                ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean) {
                        //
                    }
                }).create().show()
            } else {
                val dialog = PreviewMediaDialog.Builder(
                    getContext(),
                    multiMedia,
                    lisMultiMedia,
                    UploadFireStorageConstants.TYPE_POST,
                    UploadFireStorageUtils.getRootURLPostById(idPost),
                    jzDataSource
                ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean) {
                        //
                    }
                })
                dialog.create().show()
            }
        }
    }
}