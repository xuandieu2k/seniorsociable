package vn.xdeuhug.seniorsociable.admin.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import org.jetbrains.anko.textColor
import vn.xdeuhug.seniorsociable.admin.databinding.ItemPostModerationBinding
import vn.xdeuhug.seniorsociable.admin.databinding.ItemReportBinding
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.other.handlerPostDelayed
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 03 / 01 / 2024
 */
class ReportAdapter(context: Context) : AppAdapter<Post>(context) {
    var onListenerCLick: OnListenerCLick? = null
    var imageViewClickListener: OnClickImageViewListener? = null
    private var listUser = ListUserCache.getList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemReportBinding) :
        AppViewHolder(binding.root) {
        private var isSwiping = false
        init {

            binding.splParent.addSwipeListener(object : SwipeLayout.SwipeListener {
                override fun onStartOpen(layout: SwipeLayout) {
                    isSwiping = true
                    binding.root.isClickable = false
                }

                override fun onOpen(layout: SwipeLayout) {
                    isSwiping = true
                }

                override fun onStartClose(layout: SwipeLayout) {
                    isSwiping = false
                }

                override fun onClose(layout: SwipeLayout) {
                    isSwiping = false
                    handlerPostDelayed(1000) {
                        binding.root.isClickable = true
                    }
                }

                override fun onUpdate(layout: SwipeLayout?, leftOffset: Int, topOffset: Int) {
                    // code
                }

                override fun onHandRelease(layout: SwipeLayout?, xvel: Float, yvel: Float) {
                    // code
                }
            })

            binding.llInformation.clickWithDebounce(1000) {
                val position = bindingAdapterPosition
                val item = getItem(position)
                if (position != RecyclerView.NO_POSITION) {
                    imageViewClickListener?.onClickButtonRemove(position, item)
                }
            }

            binding.llEdit.clickWithDebounce(1000) {
                val position = bindingAdapterPosition
                val item = getItem(position)
                if (position != RecyclerView.NO_POSITION) {
                    imageViewClickListener?.onClickButtonEdit(position, item)
                }
            }

            binding.root.clickWithDebounce(1000) {
                val position = bindingAdapterPosition
                onListenerCLick!!.onClick(position)
            }

        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            val userPost = listUser.first { it.id == item.idUserPost }
            if (item.multiMedia.isNotEmpty()) {
                PhotoShowUtils.loadPhotoImageNormal(item.multiMedia.first().url, binding.imvPost)
            }else{
                PhotoShowUtils.loadPhotoImageNormal("/sdsad/sad", binding.imvPost)
            }
            PhotoShowUtils.loadAvatarImage(userPost.avatar,binding.imvAvatar)
            binding.tvContent.text = item.content
            binding.tvNameUser.text = userPost.name
            binding.tvDate.text = DateUtils.getDateByFormatTimeDate(item.timeCreated)
            if(item.reports.size > 99)
            {
                binding.tvCountRP.text = "99+"
            }else{
                binding.tvCountRP.text = item.reports.size.toString()
            }
        }

    }

    interface OnListenerCLick {
        fun onClick(position: Int)
    }

    interface OnClickImageViewListener {
        fun onClickButtonRemove(position: Int, post: Post)
        fun onClickButtonEdit(position: Int, post: Post)

    }
}