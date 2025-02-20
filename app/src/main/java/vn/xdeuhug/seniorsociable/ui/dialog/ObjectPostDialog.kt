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
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.databinding.DialogObjectPostBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 14 / 10 / 2023
 */
class ObjectPostDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context,
        var currentStatus:Int
    ) : BaseDialog.Builder<Builder>(context) {
        private var binding: DialogObjectPostBinding =
            DialogObjectPostBinding.inflate(LayoutInflater.from(context))

        //
        private lateinit var onActionDone: OnActionDone

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
            setStatusPost()
//            binding.rbPublic.isEnabled = false
//            binding.rbPrivate.isEnabled = false
//            binding.rbFriend.isEnabled = false
            // Nút công khai
            binding.llPublic.setOnClickListener {
                binding.rbPublic.isChecked = true
                binding.rbPrivate.isChecked = false
                binding.rbFriend.isChecked = false
            }
            // Nút chỉ mình tôi
            binding.llPrivate.setOnClickListener {
                binding.rbPrivate.isChecked = true
                binding.rbPublic.isChecked = false
                binding.rbFriend.isChecked = false
            }
            // Nút bạn bè
            binding.llFriend.setOnClickListener {
                binding.rbFriend.isChecked = true
                binding.rbPrivate.isChecked = false
                binding.rbPublic.isChecked = false
            }
            // Nút công khai
            binding.rbPublic.setOnClickListener {
                binding.rbPublic.isChecked = true
                binding.rbPrivate.isChecked = false
                binding.rbFriend.isChecked = false
            }
            // Nút chỉ mình tôi
            binding.rbPrivate.setOnClickListener {
                binding.rbPrivate.isChecked = true
                binding.rbPublic.isChecked = false
                binding.rbFriend.isChecked = false
            }
            // Nút bạn bè
            binding.rbFriend.setOnClickListener {
                binding.rbFriend.isChecked = true
                binding.rbPrivate.isChecked = false
                binding.rbPublic.isChecked = false
            }
            binding.tbObjectPost.setOnTitleBarListener(object : OnTitleBarListener {
                override fun onLeftClick(view: View?) {
                    onActionDone.onActionDone(true,getStatusPost())
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

        private fun setStatusPost() {
            when(currentStatus)
            {
                PostConstants.TYPE_PUBLIC ->{
                    binding.rbPublic.isChecked = true
                    binding.rbPrivate.isChecked = false
                    binding.rbFriend.isChecked = false
                }
                PostConstants.TYPE_PRIVATE ->{
                    binding.rbPublic.isChecked = false
                    binding.rbPrivate.isChecked = true
                    binding.rbFriend.isChecked = false
                }

                PostConstants.TYPE_FRIEND -> {
                    binding.rbPublic.isChecked = false
                    binding.rbPrivate.isChecked = false
                    binding.rbFriend.isChecked = true
                }
            }
        }

        private fun getStatusPost():Int
        {
            if(binding.rbPublic.isChecked)
            {
                return PostConstants.TYPE_PUBLIC
            }
            if(binding.rbPrivate.isChecked)
            {
                return PostConstants.TYPE_PRIVATE
            }
            if(binding.rbFriend.isChecked)
            {
                return PostConstants.TYPE_FRIEND
            }
            return -1
        }

        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean, statusPost: Int)
        }
    }
}