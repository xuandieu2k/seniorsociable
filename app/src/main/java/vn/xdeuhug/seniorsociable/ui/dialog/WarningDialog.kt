package vn.xdeuhug.seniorsociable.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.view.Gravity
import android.view.LayoutInflater
import androidx.core.text.HtmlCompat
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.databinding.DialogWarningBinding
import vn.xdeuhug.seniorsociable.other.onTextChangeListener
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.utils.AppUtils.clickWithDebounce
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 31/07/2023
 */
class WarningDialog {
    class Builder constructor(
        context: Context,
        headerText: String,
        messageText: String,
        type: Int,
    ) : BaseDialog.Builder<Builder>(context) {
        var type = 0
        private var binding: DialogWarningBinding =
            DialogWarningBinding.inflate(LayoutInflater.from(context))
        lateinit var onActionDone: OnActionDone

        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(false)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            setWidth(Resources.getSystem().displayMetrics.widthPixels * 5 / 6)
            lockEmoji()
            this.type = type
            when (type) {
                AppConstants.TYPE_WARNING -> {
                    binding.llContent.show()
                    binding.llNote.hide()
                    binding.llResetPassword.hide()
                    binding.tvHeader.setTextColor(getColor(R.color.red_600))
                    binding.llInforEmployee.hide()
                }
                AppConstants.TYPE_NOTE -> {
                    binding.llContent.hide()
                    binding.llNote.show()
                    binding.llResetPassword.hide()
                    binding.tvHeader.setTextColor(getColor(R.color.blue_700))
                    binding.llInforEmployee.hide()
                }
                AppConstants.TYPE_RESET_PASSWORD ->{
                    binding.llContent.hide()
                    binding.llNote.hide()
                    binding.llResetPassword.show()
                    binding.tvHeader.setTextColor(getColor(R.color.red_600))
                    binding.btnCancel.hide()
                    binding.llInforEmployee.hide()
                    binding.btnConfirm.background = getDrawable(R.drawable.bg_border_right_left_button)
                }
                AppConstants.TYPE_NOTIFICATION -> {
                    binding.llContent.show()
                    binding.llNote.hide()
                    binding.llResetPassword.hide()
                    binding.llInforEmployee.hide()
                    binding.tvHeader.setTextColor(getColor(R.color.blue_700))
                }
                AppConstants.TYPE_EMPLOYEE_CREATED -> {
                    binding.llInforEmployee.show()
                    binding.llContent.hide()
                    binding.llNote.hide()
                    binding.llResetPassword.hide()
                    binding.btnCancel.hide()
                    binding.tvHeader.setTextColor(getColor(R.color.blue_700))
                    binding.btnConfirm.background = getDrawable(R.drawable.bg_border_right_left_button)
                }

                AppConstants.TYPE_THREE_OPTION -> {
                    binding.llContent.show()
                    binding.llNote.hide()
                    binding.llResetPassword.hide()
                    binding.tvHeader.setTextColor(getColor(R.color.red_600))
                    binding.llInforEmployee.hide()
                    binding.btnExit.show()
                }
            }
            binding.tvHeader.text = headerText
            binding.tvContent.text = HtmlCompat.fromHtml(
                messageText, HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            binding.btnCancel.setOnClickListener {
                binding.btnCancel.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.btnCancel.isEnabled = true
                }, 1000)
                dismiss()
                onActionDone.onActionDone(AppConstants.BUTTON_CANCEL)
            }
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
            setEditTextNote()


        }
        private fun lockEmoji() {
            binding.edtNote.filters = arrayOf(
                AppUtils.EMOJI_FILTER, InputFilter.LengthFilter(255)
            )
        }
        fun setTextTvAccount(text:String)
        {
            binding.tvAccount.text = text
        }
        fun setTextTvPassword(text:String)
        {
            binding.tvPassword.text = text
        }

        fun setTextTvAccountE(text:String)
        {
            binding.tvAccountE.text = text
        }
        fun setTextTvPasswordE(text:String)
        {
            binding.tvPasswordE.text = text
        }
        fun setTextTvSupplierE(text:String)
        {
            binding.tvSupplierE.text = text
        }

        fun getTextNote():String
        {
            return binding.edtNote.text.toString()
        }

        @SuppressLint("SetTextI18n")
        private fun setEditTextNote() {
            binding.edtNote.onTextChangeListener(0) {
                binding.tvCountChar.text = "${it.length} / 255"
            }
        }



        interface OnActionDone {
            fun onActionDone(isConfirm: Int)
        }
    }
}