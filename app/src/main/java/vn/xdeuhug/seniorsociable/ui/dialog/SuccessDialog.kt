package vn.xdeuhug.seniorsociable.ui.dialog

import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.databinding.SuccessDialogBinding
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 14/07/2023
 */
class SuccessDialog {
    class Builder constructor(context: Context) : BaseDialog.Builder<Builder>(context) {
        private var binding: SuccessDialogBinding = SuccessDialogBinding.inflate(
            LayoutInflater.from(context)
        )

        init {
            setCancelable(true)
            setGravity(Gravity.CENTER)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setContentView(binding.root)
            getDialog()?.window?.setBackgroundDrawableResource(R.drawable.bg_border_dialog)
            setWidth(Resources.getSystem().displayMetrics.widthPixels * 5 / 6)
        }

    }
}