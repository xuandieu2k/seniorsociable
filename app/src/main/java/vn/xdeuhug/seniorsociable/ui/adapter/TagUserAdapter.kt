package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.databinding.ItemUserTagBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 10 / 2023
 */
class TagUserAdapter(context: Context) : AppAdapter<User>(context) {
    var onListenerSelected: OnListenerSelected? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemUserTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemUserTagBinding) : AppViewHolder(binding.root) {
        init {
            binding.cbSelected.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerSelected!!.onSelected(position)
                }
            }
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerSelected!!.onSelected(position)
                }
            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            PhotoShowUtils.loadAvatarImage(
                UploadFireStorageUtils.getRootURLAvatarById(item.id),
                item.avatar,
                binding.imvAvatar
            )
            binding.tvName.text = item.name
            binding.cbSelected.isChecked = item.isChecked
        }

    }

    interface OnListenerSelected {
        fun onSelected(position: Int)
    }
}