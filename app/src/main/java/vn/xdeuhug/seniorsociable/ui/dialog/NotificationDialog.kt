package vn.xdeuhug.seniorsociable.ui.dialog

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.databinding.DialogNotificationBinding

/**
 * @Author: NGUYEN THE DAT
 * @Date: 4/14/2023
 */
class NotificationDialog {
    class Builder constructor(
        context: Context,
        headerText: String,
        messageText: String,isCancelable :Boolean
    ) :
        BaseDialog.Builder<Builder>(context) {
        private var binding: DialogNotificationBinding =
            DialogNotificationBinding.inflate(LayoutInflater.from(context))

        lateinit var onActionDone: OnActionDone

        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(isCancelable)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            setWidth(Resources.getSystem().displayMetrics.widthPixels* 5 / 6)
            binding.tvContent.text = messageText
            binding.tvHeader.text = headerText
            if (isCancelable)
                binding.btnConfirm.text = getString(vn.xdeuhug.seniorsociable.R.string.close)
            else
                binding.btnConfirm.text = getString(vn.xdeuhug.seniorsociable.R.string.confirm)
            binding.btnConfirm.setOnClickListener {
                binding.btnConfirm.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.btnConfirm.isEnabled = true
                }, 1000)
                dismiss()
                onActionDone.onActionDone()
            }
        }

        interface OnActionDone {
            fun onActionDone()
        }
    }
}