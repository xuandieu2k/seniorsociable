package vn.xdeuhug.seniorsociable.personalPage.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.text.InputFilter
import android.view.Gravity
import android.view.LayoutInflater
import androidx.core.widget.doOnTextChanged
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.Album
import vn.xdeuhug.seniorsociable.personalPage.databinding.DialogEditDescriptionBinding
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.clickWithDebounce
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 15 / 11 / 2023
 */
class EditDescriptionDialog {
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    class Builder constructor(
        context: Context, description: String
    ) : BaseDialog.Builder<Builder>(context) {
        private var binding: DialogEditDescriptionBinding =
            DialogEditDescriptionBinding.inflate(LayoutInflater.from(context))

        //
        private lateinit var onActionDone: OnActionDone
        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(true)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            getDialog()?.window?.setBackgroundDrawableResource(R.drawable.bg_border_dialog)
            setWidth(Resources.getSystem().displayMetrics.widthPixels)
            setHeight(Resources.getSystem().displayMetrics.heightPixels)
            val user = UserCache.getUser()
            PhotoShowUtils.loadAvatarImage(
                UploadFireStorageUtils.getRootURLAvatarById(user.id), user.avatar, binding.imvAvatar
            )
            binding.tbDescription.leftView.clickWithDebounce {
                dismiss()
            }
            binding.edtDescription.setText(description)
            binding.edtDescription.filters = arrayOf(InputFilter.LengthFilter(150))
            binding.tvUsername.text = user.name
            binding.edtDescription.doOnTextChanged { text, _, _, _ ->
                binding.tvCountChar.text = "${text!!.length} / 150"
            }
            binding.tbDescription.rightView.clickWithDebounce {
                onActionDone.onActionDone(true, binding.edtDescription.text.toString().trim())
            }
        }

        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean, description: String)
        }

    }
}