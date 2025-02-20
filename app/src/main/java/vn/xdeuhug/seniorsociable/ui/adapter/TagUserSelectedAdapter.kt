package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.databinding.ItemUserTagSelectedBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 10 / 2023
 */
class TagUserSelectedAdapter(context: Context) : AppAdapter<User>(context) {
    var onListenerRemove: OnListenerRemove? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemUserTagSelectedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemUserTagSelectedBinding) : AppViewHolder(binding.root) {
        init {
            binding.imvClose.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerRemove!!.onRemove(position)
                }
            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            PhotoShowUtils.loadAvatarImage(
                "${UploadFireStorageConstants.HEAD_UPLOAD}${item.id}${UploadFireStorageConstants.BODY_UPLOAD_AVATAR}",
                item.avatar,
                binding.imvAvatar)
            binding.tvName.text = item.name
        }

    }

    interface OnListenerRemove {
        fun onRemove(position: Int)
    }
}