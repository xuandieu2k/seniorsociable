package vn.xdeuhug.seniorsociable.friends.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.friends.databinding.ItemRequestAddFriendsBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 28 / 11 / 2023
 */
class SendingInviteAdapter(context: Context) : AppAdapter<User>(context) {
    var onListenerCLick: OnListenerCLick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemRequestAddFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemRequestAddFriendsBinding) :
        AppViewHolder(binding.root) {
        init {
            binding.btnConfirm.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClickConfirm(position)
                }
            }
            binding.btnDelete.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClickCancel(position)
                }
            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            PhotoShowUtils.loadAvatarImage(
                UploadFireStorageUtils.getRootURLAvatarById(item.id), item.avatar, binding.imvAvatar
            )
            binding.tvNameUser.text = item.name
            binding.tvTimeRequest.text = "23 ngày"
        }

    }

    interface OnListenerCLick {
        fun onClickConfirm(position: Int)
        fun onClickCancel(position: Int)
    }
}