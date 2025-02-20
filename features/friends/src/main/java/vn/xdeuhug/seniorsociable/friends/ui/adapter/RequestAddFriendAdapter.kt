package vn.xdeuhug.seniorsociable.friends.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.friends.databinding.ItemRequestAddFriendsBinding
import vn.xdeuhug.seniorsociable.model.entity.modelFriend.RequestAddFriend
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 09 / 11 / 2023
 */
class RequestAddFriendAdapter(context: Context) :
    AppAdapter<Pair<User, RequestAddFriend>>(context) {
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
            val itemFirst = getItem(position).first
            val itemSecond = getItem(position).second
            PhotoShowUtils.loadAvatarImage(
                UploadFireStorageUtils.getRootURLAvatarById(itemFirst.id),
                itemFirst.avatar,
                binding.imvAvatar
            )
            binding.tvNameUser.text = itemFirst.name
            binding.tvTimeRequest.text =
                TimeUtils.formatTimeAgo(itemSecond.timeCreated, getContext())
        }

    }

    interface OnListenerCLick {
        fun onClickConfirm(position: Int)
        fun onClickCancel(position: Int)
    }
}