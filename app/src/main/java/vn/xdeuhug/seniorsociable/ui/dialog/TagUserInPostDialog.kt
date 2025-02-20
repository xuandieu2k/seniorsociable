package vn.xdeuhug.seniorsociable.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.hjq.bar.OnTitleBarListener
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.databinding.DialogTagUserInPostBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.ui.adapter.UserInTagAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 22 / 12 / 2023
 */
class TagUserInPostDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context, private var listUserSelected: ArrayList<User>
    ) : BaseDialog.Builder<Builder>(context), UserInTagAdapter.OnListenerCLick {
        private var binding: DialogTagUserInPostBinding =
            DialogTagUserInPostBinding.inflate(LayoutInflater.from(context))
        // Adapter
        private var userInTagAdapter: UserInTagAdapter

        //
        lateinit var onActionDone: OnActionDone

        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(false)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            getDialog()?.window?.setBackgroundDrawableResource(R.drawable.bg_border_dialog)
            setWidth(Resources.getSystem().displayMetrics.widthPixels)
            setHeight(Resources.getSystem().displayMetrics.heightPixels)
            AppUtils.setFontTypeFaceTitleBar(getContext(),binding.tbTitle)
//            startShimmer()
            // set Adapter
            userInTagAdapter = UserInTagAdapter(context)
            userInTagAdapter.onListenerCLick = this
            userInTagAdapter.setData(listUserSelected)
            // Create recycleView
            AppUtils.initRecyclerViewVertical(binding.rvUserTag, userInTagAdapter)
//            handleSuccess(2000)
            binding.tbTitle.setOnTitleBarListener(object : OnTitleBarListener {
                override fun onLeftClick(view: View?) {
                    onActionDone.onActionDone(true)
                    dismiss()
                }

                override fun onTitleClick(view: View?) {
                    //
                }

                override fun onRightClick(view: View?) {
                    //
                }

            })

        }


        @SuppressLint("NotifyDataSetChanged")
        private fun handleSuccess(timer: Long) {
            postDelayed({
                // hide and stop simmer
                binding.sflLoadData.stopShimmer()
                binding.sflLoadData.hide()
                binding.rvUserTag.show()
                if (listUserSelected.isNotEmpty()) {
                    binding.rvUserTag.show()
                    binding.rlBackgroundNotFound.hide()
                } else {
                    binding.rvUserTag.hide()
                    binding.rlBackgroundNotFound.show()
                }
            }, timer)

        }

        private fun startShimmer() {
            binding.sflLoadData.startShimmer()
            binding.rvUserTag.hide()
            binding.sflLoadData.show()
        }


        override fun onClick(position: Int) {
            onActionDone.onClickUser(listUserSelected[position])
        }

        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean)
            fun onClickUser(user: User)
        }    }
}