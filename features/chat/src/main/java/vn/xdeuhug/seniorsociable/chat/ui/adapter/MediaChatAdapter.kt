package vn.xdeuhug.seniorsociable.chat.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.dimen
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.chat.databinding.ItemMediaChatBinding
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.AppUtils.toArrayList
import vn.xdeuhug.seniorsociable.utils.GalleryViewAdapterUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 24 / 11 / 2023
 */
class MediaChatAdapter(context: Context) : AppAdapter<MultiMedia>(context) {
    var onListenerCLick: OnListenerCLick? = null

    constructor(context: Context, idChat: String, idMessage: String) : this(context) {
        this.idChat = idChat
        this.idMessage = idMessage
    }

    private var idChat = ""
    private var idMessage = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemMediaChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemMediaChatBinding) :
        AppViewHolder(binding.root) {
        init {
//            binding.imvClose.clickWithDebounce {
//                val position = bindingAdapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onListenerCLick!!.onClick(position)
//                }
//
//            }
//            binding.root.clickWithDebounce {
//                val position = bindingAdapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onListenerCLick!!.onShowPreview(position)
//                }
//            }
        }

        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClick(getData()[position],getData().toArrayList(), idMessage)
                }
            }
        }


        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
//            if (position != RecyclerView.NO_POSITION) {
//                GalleryViewAdapterUtils.setGallery(
//                    getData().size, position, binding.root, getContext()
//                )
//
//            }
            setLayout()
            when (item.type) {
                AppConstants.UPLOAD_IMAGE -> {
                    binding.imvImage.show()
                    binding.plvVideo.hide()
                    PhotoShowUtils.loadMessageImage(
                        UploadFireStorageUtils.getRootURLMessageById(idChat, idMessage),
                        item.url,
                        binding.imvImage
                    )
                }

                AppConstants.UPLOAD_VIDEO -> {
                    binding.plvVideo.show()
                    binding.imvImage.hide()
                    PhotoShowUtils.loadMessageImage(
                        UploadFireStorageUtils.getRootURLMessageById(idChat, idMessage),
                        item.url,
                        binding.imvImage
                    )
                }
            }
        }

        private fun setLayout() {
            when (getData().size) {
                1 -> {
                    binding.rlMedia.measure(
                        getContext().dimen(R.dimen.dp_160), getContext().dimen(R.dimen.dp_160)
                    )
                }

                2 -> {
                    binding.rlMedia.measure(
                        getContext().dimen(R.dimen.dp_80), getContext().dimen(R.dimen.dp_160)
                    )
                }
            }
        }

    }

    interface OnListenerCLick {
        fun onClick(multiMedia: MultiMedia, lisMultiMedia: ArrayList<MultiMedia>, idChat: String)
    }
}