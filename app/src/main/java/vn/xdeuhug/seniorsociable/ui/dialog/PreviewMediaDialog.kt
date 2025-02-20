package vn.xdeuhug.seniorsociable.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import cn.jzvd.JZDataSource
import com.hjq.bar.OnTitleBarListener
import com.zeuskartik.mediaslider.TouchImageView
import org.greenrobot.eventbus.EventBus
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.databinding.DialogPreviewMediaBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.eventbus.PreviewMediaEventBus
import vn.xdeuhug.seniorsociable.ui.adapter.PreviewMediaAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.initRecyclerViewHorizontal
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.VideoUtils
import java.util.Collections

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 10 / 2023
 */
class PreviewMediaDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context,var multiMediaCurrent: MultiMedia,var listMultiMedia: ArrayList<MultiMedia>, var type: Int, var rootURL: String,
        var jzDataSource: JZDataSource?
    ) : BaseDialog.Builder<Builder>(context) {
        private var binding: DialogPreviewMediaBinding =
            DialogPreviewMediaBinding.inflate(LayoutInflater.from(context))
        //
        private lateinit var previewMediaAdapter: PreviewMediaAdapter
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
            setWidth(Resources.getSystem().displayMetrics.widthPixels)
            setHeight(Resources.getSystem().displayMetrics.heightPixels)
            setAdapter()

            binding.tbPreview.setOnTitleBarListener(object : OnTitleBarListener {
                override fun onLeftClick(view: View?) {
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

        override fun dismiss() {
            EventBus.getDefault().post(PreviewMediaEventBus(true))
            super.dismiss()
        }

        private fun setAdapter() {
            val pos = listMultiMedia.indexOf(multiMediaCurrent)
            Collections.swap(listMultiMedia, 0, pos)
            previewMediaAdapter = if(jzDataSource != null) {
                PreviewMediaAdapter(getContext(),jzDataSource!!)
            }else{
                PreviewMediaAdapter(getContext())
            }
            previewMediaAdapter.setData(listMultiMedia)
            binding.vpMedia.adapter = previewMediaAdapter
            binding.vpMedia.currentItem = 0
        }


        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean)
        }
    }
}