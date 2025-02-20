package vn.xdeuhug.seniorsociable.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.text.InputFilter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.doOnTextChanged
import com.hjq.bar.OnTitleBarListener
import com.luck.picture.lib.utils.ToastUtils
import timber.log.Timber
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.database.AlbumManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.DialogAddAlbumBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.Album
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.clickWithDebounce
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 16 / 10 / 2023
 */
class AddAlbumDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context
    ) : BaseDialog.Builder<Builder>(context) {
        private var binding: DialogAddAlbumBinding =
            DialogAddAlbumBinding.inflate(LayoutInflater.from(context))
        private var isValidName = false
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
            setWidth(Resources.getSystem().displayMetrics.widthPixels * 5/6)

            binding.tbAddAlbum.setOnTitleBarListener(object : OnTitleBarListener {
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

            binding.edtName.filters = arrayOf(
                AppUtils.EMOJI_FILTER, InputFilter.LengthFilter(51)
            )

            binding.edtName.doOnTextChanged { _, _, _, _ ->
                if (binding.edtName.length() > 50) {
                    binding.edtName.setText(binding.edtName.text.toString().substring(0, 50))
                    binding.edtName.setSelection(binding.edtName.text!!.length)
                    ToastUtils.showToast(context,context.getString(R.string.res_max_50_characters))
                } else {
                    validName(binding.edtName.text.toString())
                }
            }
            binding.btnConfirm.clickWithDebounce{
                if(isValidName)
                {
                    val album = Album()
                    album.name = binding.edtName.text.toString()
                    AlbumManagerFSDB.addAlbum(
                        album,
                        object : AlbumManagerFSDB.Companion.FireStoreCallback<Boolean> {
                            override fun onSuccess(result: Boolean) {
                                ToastUtils.showToast(context,getString(R.string.create_success))
                                onActionDone.onActionDone(true)
                                dismiss()
                            }

                            override fun onFailure(exception: Exception) {
                                // Xử lý lỗi nếu có
                                Timber.tag("Exception").e(exception)
                            }
                        })
                }else{
                    validName(binding.edtName.text.toString())
                }
            }
        }

        private fun validName(name: String) {
            if (name.isEmpty()) {
                binding.tvErrorName.text = getString(R.string.res_min_characters)
                binding.tvErrorName.show()
                isValidName = false
            } else {
                if (name.length < 2) {
                    binding.tvErrorName.text = getString(R.string.res_min_characters)
                    binding.tvErrorName.show()
                    isValidName = false
                } else {
                    binding.tvErrorName.text = ""
                    binding.tvErrorName.hide()
                    isValidName = true
                }
            }
        }


        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean)
        }
    }
}