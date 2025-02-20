package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import org.greenrobot.eventbus.Subscribe
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.databinding.ItemPreviewMediaBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.eventbus.PreviewMediaEventBus
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.VideoUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 14 / 12 / 2023
 */
class PreviewMediaAdapter(context: Context) : AppAdapter<MultiMedia>(context) {
    var onListenerCLick: OnListenerCLick? = null
    private var jzDataSource: JZDataSource? = null

    constructor(context: Context,jzDataSource: JZDataSource) : this(context) {
        this.jzDataSource = jzDataSource
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemPreviewMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemPreviewMediaBinding) :
        AppViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            if(position == 0 && item.type == AppConstants.UPLOAD_VIDEO && jzDataSource != null )
            {
                binding.plvVideo.show()
                binding.imvImage.hide()
                binding.plvVideo.jzDataSource = jzDataSource
                binding.plvVideo.fullscreenButton.hide()
                VideoUtils.loadVideoWithDataSource(jzDataSource!!, binding.plvVideo)
            }else{
                when (item.type) {
                    AppConstants.UPLOAD_IMAGE -> {

                        PhotoShowUtils.loadPhotoImagePreview(item.url, binding.imvImage){width, height ->
                            if(item.height == 0 && item.width == 0)
                            {
                                item.height = height
                                item.width = width
                            }
                            AppUtils.scaleImageFitWidthScreen(
                                binding.imvImage, getContext(), item.height, item.width
                            )
                        }
                        binding.imvImage.show()
                        binding.plvVideo.hide()
                    }

                    AppConstants.UPLOAD_VIDEO -> {
                        binding.plvVideo.show()
                        binding.imvImage.hide()
                        binding.plvVideo.fullscreenButton.hide()
                        VideoUtils.loadVideo(item.url, binding.plvVideo)
                    }
                }
            }

        }

        @Subscribe
        fun onEnd(eventBus: PreviewMediaEventBus) {
            if (eventBus.isEnd) {
                Jzvd.releaseAllVideos()
            }
        }

    }

    interface OnListenerCLick {
        fun onClick(multiMedia: MultiMedia, idPost: String)
    }
}