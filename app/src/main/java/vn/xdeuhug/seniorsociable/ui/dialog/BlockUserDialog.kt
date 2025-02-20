package vn.xdeuhug.seniorsociable.ui.dialog

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.databinding.DialogBlockUserBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.DateUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 30 / 12 / 2023
 */
class BlockUserDialog {
    class Builder(
        context: Context,
        var user: User
    ) :
        BaseDialog.Builder<Builder>(context) {
        private var binding: DialogBlockUserBinding =
            DialogBlockUserBinding.inflate(LayoutInflater.from(context))
        lateinit var onActionDone: OnActionDone

        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(false)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            setWidth(Resources.getSystem().displayMetrics.widthPixels* 5 / 6)
            setDataForView()

            binding.btnConfirm.setOnClickListener {
                binding.btnConfirm.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.btnConfirm.isEnabled = true
                }, 1000)
                dismiss()
                onActionDone.onActionDone(true)
            }
        }

        private fun setDataForView() {
            binding.tvReason.text = user.reasonBlock
            // User post
            when(user.typeActive)
            {
                AppConstants.BLOCKING ->{
                    val spanText = SpannableStringBuilder()
                    val textFrom = getString(R.string.from)
                    spanText.append("$textFrom ")
                    spanText.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            //
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            textPaint.color = getColor(R.color.gray_900)
                            textPaint.isUnderlineText = false
                        }
                    }, spanText.length - textFrom!!.length, spanText.length, 0)
                    //
                    val textFromDate = DateUtils.getDateByFormatTimeDateSeconds(user.timeStartBlock!!)
                    spanText.append(textFromDate)
                    spanText.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            //
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            textPaint.color = getColor(R.color.red_600)
                            textPaint.isUnderlineText = false
                        }
                    }, spanText.length - textFromDate.length, spanText.length, 0)
                    //
                    val textTo = getString(R.string.to)!!.lowercase()
                    spanText.append(" $textTo ")
                    spanText.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            //
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            textPaint.color = getColor(R.color.gray_900)
                            textPaint.isUnderlineText = false
                        }
                    }, spanText.length - textTo.length, spanText.length, 0)
                    //
                    val textDateTo = DateUtils.getDateByFormatTimeDateSeconds(user.timeStartBlock!!)
                    spanText.append(textDateTo)
                    spanText.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            //
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            textPaint.color = getColor(R.color.green_008)
                            textPaint.isUnderlineText = false
                        }
                    }, spanText.length - textDateTo.length, spanText.length, 0)
                    //
                    binding.tvTimeEnd.movementMethod = LinkMovementMethod.getInstance()
                    binding.tvTimeEnd.text = spanText
                }
                AppConstants.BLOCKED_UN_LIMITED ->{
                    binding.tvTimeEnd.text = getString(R.string.is_lock_un_limited)
                }
            }
        }

        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean)
        }
    }
}