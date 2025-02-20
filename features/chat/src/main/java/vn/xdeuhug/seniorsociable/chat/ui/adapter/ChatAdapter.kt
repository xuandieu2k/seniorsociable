package vn.xdeuhug.seniorsociable.chat.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.chat.constants.ChatConstants
import vn.xdeuhug.seniorsociable.chat.databinding.ItemChatBinding
import vn.xdeuhug.seniorsociable.chat.entity.Chat
import vn.xdeuhug.seniorsociable.chat.entity.Message
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import java.util.Locale

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 13 / 11 / 2023
 */
class ChatAdapter(context: Context) : AppAdapter<Pair<Chat, Message>>(context) {
    var onListenerCLick: OnListenerCLick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemChatBinding) : AppViewHolder(binding.root) {

        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClickItemChat(position)
                }
            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            val chat = item.first
            val user = ListUserCache.getList()
                .firstOrNull { it.id == chat.members.firstOrNull { it != UserCache.getUser().id } }
            val message = item.second
            PhotoShowUtils.loadAvatarImage(
                UploadFireStorageUtils.getRootURLAvatarById(user!!.id),
                user.avatar,
                binding.imvAvatar
            )
            binding.tvUsername.text = user.name
            setMessage(message,user)
            binding.tvTimeSend.text = TimeUtils.timeAgoChat(getContext(),message.timeSend)
            if (message.idUserSend == UserCache.getUser().id) // Là người gửi
            {
                binding.root.isSelected = false
            } else {
                binding.root.isSelected =
                    message.isUsersRead.firstOrNull { it == UserCache.getUser().id }
                        ?.isNotEmpty() != true
            }
            AppUtils.setForeground(user, binding.imvAvatar, getContext())
        }

        private fun setMessage(message: Message, enemy: User) {
            if (message.idUserSend == UserCache.getUser().id) // Bạn là người gửi tin nhắn
            {
                when (message.messageType) {
                    ChatConstants.TYPE_MESSAGE -> {
                        binding.tvContentChat.text = "${getString(R.string.you)}: ${message.message}"
                    }

                    ChatConstants.TYPE_AUDIO -> {
                        binding.tvContentChat.text = "${getString(R.string.you)} ${ getString(R.string.sended_file_audio) }"
                    }

                    ChatConstants.TYPE_MEDIA -> {
                        when (message.multiMedia.first().type) {
                            AppConstants.UPLOAD_IMAGE -> {
                                binding.tvContentChat.text = "${getString(R.string.you)} ${ getString(R.string.sended_file_image) }"
                            }

                            AppConstants.UPLOAD_VIDEO -> {
                                binding.tvContentChat.text = "${getString(R.string.you)} ${ getString(R.string.sended_file_video) }"
                            }
                        }
                    }

                    ChatConstants.TYPE_LOCATION -> {
                        //
                    }
                }
            } else { // Đối phương gửi tin nhắn
                when (message.messageType) {
                    ChatConstants.TYPE_MESSAGE -> {
                        binding.tvContentChat.text = message.message
                    }

                    ChatConstants.TYPE_AUDIO -> {
                        binding.tvContentChat.text = getString(R.string.sended_file_audio)!!.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.ROOT
                            ) else it.toString()
                        }
                    }

                    ChatConstants.TYPE_MEDIA -> {
                        when (message.multiMedia.first().type) {
                            AppConstants.UPLOAD_IMAGE -> {
                                binding.tvContentChat.text = getString(R.string.sended_file_image)!!.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.ROOT
                                    ) else it.toString()
                                }
                            }

                            AppConstants.UPLOAD_VIDEO -> {
                                binding.tvContentChat.text = getString(R.string.sended_file_video)!!.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.ROOT
                                    ) else it.toString()
                                }
                            }
                        }
                    }

                    ChatConstants.TYPE_LOCATION -> {
                        //
                    }
                }
            }
        }

    }

    interface OnListenerCLick {
        fun onClickItemChat(position: Int)
    }
}