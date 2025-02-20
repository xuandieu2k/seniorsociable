package vn.xdeuhug.seniorsociable.chat.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.chat.databinding.ItemUserOnlineBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 13 / 11 / 2023
 */
class UserOnlineAdapter(context: Context) : AppAdapter<User>(context) {
    var onListenerCLick: OnListenerCLick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemUserOnlineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemUserOnlineBinding) :
        AppViewHolder(binding.root) {
        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClickItem(position)
                }
            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            PhotoShowUtils.loadAvatarImage(
                UploadFireStorageUtils.getRootURLAvatarById(item.id), item.avatar, binding.imvAvatar
            )
            AppUtils.setForeground(item, binding.imvAvatar, getContext())
            binding.tvUsername.text = AppUtils.getLastName(item.name)
        }

    }

    interface OnListenerCLick {
        fun onClickItem(position: Int)
    }
}