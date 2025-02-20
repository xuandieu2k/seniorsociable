package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.NotificationConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.ItemNotificationBinding
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 29 / 11 / 2023
 */
class NotificationAdapter(context: Context) : AppAdapter<Notification>(context) {
    var onListenerCLick: OnListenerCLick? = null
    private var listUser = ListUserCache.getList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) : AppViewHolder(binding.root) {
        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClick(position)
                }

            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            val userSend = listUser.firstOrNull { it.id == item.idUserSend }
            setData(userSend , item)
            binding.tvDateTime.text = DateUtils.getDateByFormatTimeDate(item.timeCreated)
            setBackground(item)
        }

        private fun setBackground(item: Notification) {
            binding.root.isSelected = item.read != AppConstants.IS_READ
        }

        private fun setData(user: User?, item: Notification) {
            user.let {
                PhotoShowUtils.loadAvatarImage(UploadFireStorageUtils.getRootURLAvatarById(it!!.id),it.avatar,binding.imvAvatar)
                binding.tvContent.text = AppUtils.fromHtml("<b>${user!!.name}</b> ${item.title}")
            }
            setSubContent(item)
        }

        private fun setSubContent(item: Notification) {
            when(item.type)
            {
                AppConstants.NOTIFICATION_FROM_POST_INTERACT ->{
                    setInteractContent(item.typeInteract)
                }
                AppConstants.NOTIFICATION_FROM_POST_COMMENT ->{
                    binding.imvSubContent.setImageResource(R.drawable.ic_notification_comment)
                }
                AppConstants.NOTIFICATION_FROM_POST ->{
                    binding.imvSubContent.setImageResource(R.drawable.ic_notification_post)
                }
            }
        }

        private fun setInteractContent(typeInteract: Int) {
            when(typeInteract)
            {
                PostConstants.INTERACT_LIKE -> {
                    binding.imvSubContent.setImageResource(R.drawable.ic_like_senior_new)
                }

                PostConstants.INTERACT_LOVE -> {
                    binding.imvSubContent.setImageResource(R.drawable.ic_heart)
                }

                PostConstants.INTERACT_SMILE -> {
                    binding.imvSubContent.setImageResource(R.drawable.ic_laugh)
                }

                PostConstants.INTERACT_WOW -> {
                    binding.imvSubContent.setImageResource(R.drawable.ic_wow)
                }

                PostConstants.INTERACT_SAD -> {
                    binding.imvSubContent.setImageResource(R.drawable.ic_sad)
                }

                PostConstants.INTERACT_ANGRY -> {
                    binding.imvSubContent.setImageResource(R.drawable.ic_angry)
                }
            }
        }

    }

    interface OnListenerCLick {
        fun onClick(position: Int)
    }
}