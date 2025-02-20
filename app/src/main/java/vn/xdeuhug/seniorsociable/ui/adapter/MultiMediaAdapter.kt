package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.databinding.ItemMultimediaBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 16 / 10 / 2023
 */
class MultiMediaAdapter(context: Context) : AppAdapter<MultiMedia>(context) {
    var onListenerCLick: OnListenerCLick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemMultimediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemMultimediaBinding) : AppViewHolder(binding.root) {
        init {
            binding.imvClose.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClick(position)
                }

            }
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onShowPreview(position)
                }
            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            when(item.type)
            {
                AppConstants.UPLOAD_IMAGE->{
                    binding.imvImage.show()
                    binding.imvPlay.hide()
                    PhotoShowUtils.loadPhotoRound(item.url,binding.imvImage)
                }

                AppConstants.UPLOAD_VIDEO ->{
                    binding.imvImage.show()
                    binding.imvPlay.show()
                    PhotoShowUtils.loadPhotoRound(item.url,binding.imvImage)
                }
            }
        }

    }

    interface OnListenerCLick {
        fun onClick(position: Int)
        fun onShowPreview(position: Int)
    }
}