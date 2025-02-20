package vn.xdeuhug.seniorsociable.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.core.text.HtmlCompat
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.databinding.DialogExitBinding
import vn.xdeuhug.seniorsociable.utils.AppUtils.clickWithDebounce

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 21 / 12 / 2023
 */
class ExitDialog(context: Context,var content: String) :
    Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen){

    private var binding: DialogExitBinding = DialogExitBinding.inflate(LayoutInflater.from(context))

    //
    private lateinit var onActionDone: OnActionDone

    fun onActionDone(onActionDone: OnActionDone) {
        this.onActionDone = onActionDone
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setCancelable(false)
        this.window!!.setLayout(
            Resources.getSystem().displayMetrics.widthPixels * 5 / 6, WindowManager.LayoutParams.WRAP_CONTENT
        )
        setContentView(binding.root)
        binding.btnCancel.setOnClickListener {
            binding.btnCancel.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnCancel.isEnabled = true
            }, 1000)
            dismiss()
            onActionDone.onActionDone(AppConstants.BUTTON_CANCEL)
        }
        binding.tvContent.text = HtmlCompat.fromHtml(
            content, HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.btnConfirm.setOnClickListener {
            binding.btnConfirm.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnConfirm.isEnabled = true
            }, 1000)
            dismiss()
            onActionDone.onActionDone(AppConstants.BUTTON_CONFIRM)
        }
        binding.btnExit.clickWithDebounce{
            dismiss()
            onActionDone.onActionDone(AppConstants.BUTTON_EXIT)
        }
    }


    interface OnActionDone {
        fun onActionDone(isConfirm: Int)
    }
}