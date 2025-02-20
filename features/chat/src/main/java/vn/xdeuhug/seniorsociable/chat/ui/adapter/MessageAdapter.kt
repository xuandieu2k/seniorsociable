package vn.xdeuhug.seniorsociable.chat.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.aghajari.emojiview.view.AXEmojiLayout.LayoutParams
import org.jetbrains.anko.dimen
import org.jetbrains.anko.textColor
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.chat.databinding.ItemMessageBinding
import vn.xdeuhug.seniorsociable.chat.constants.ChatConstants
import vn.xdeuhug.seniorsociable.chat.entity.Message
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.GalleryViewAdapterUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 22 / 11 / 2023
 */
class MessageAdapter(context: Context) : AppAdapter<Message>(context),
    MediaChatAdapter.OnListenerCLick {
    var onListenerCLick: OnListenerCLick? = null

    constructor(context: Context, idChat: String) : this(context) {
        this.idChat = idChat
    }

    private var idChat = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemMessageBinding) : AppViewHolder(binding.root) {

        init {
//            binding.root.clickWithDebounce {
//                val position = bindingAdapterPosition
//                if(position != RecyclerView.NO_POSITION)
//                {
//                    onListenerCLick!!.onClickItemChat(position)
//                }
//            }
        }

        private lateinit var mediaAdapter: MediaChatAdapter

        @SuppressLint("SetTextI18n", "SimpleDateFormat", "NotifyDataSetChanged")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            if (item.idUserSend == UserCache.getUser().id) {
                binding.rlRoot.gravity = Gravity.END
                binding.llParent.gravity = Gravity.END

                val params = binding.tvTimeChat.layoutParams as RelativeLayout.LayoutParams
                val params1 = binding.llParent.layoutParams as RelativeLayout.LayoutParams

                params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                params1.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)

                binding.tvTimeChat.layoutParams = params
                binding.llParent.layoutParams = params1
            } else {
                binding.rlRoot.gravity = Gravity.START
                binding.llParent.gravity = Gravity.START

                val params = binding.tvTimeChat.layoutParams as RelativeLayout.LayoutParams
                val params1 = binding.llParent.layoutParams as RelativeLayout.LayoutParams

                params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                params1.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)

                // Thiết lập params cho tvTimeChat
                binding.tvTimeChat.layoutParams = params
                binding.llParent.layoutParams = params1
            }
            checkView(item)
            setColorForView((item.idUserSend == UserCache.getUser().id))
        }

        private fun checkView(item: Message) {
            when (item.messageType) {
                ChatConstants.TYPE_MESSAGE -> {
                    binding.tvContent.show()
                    binding.vpvVoice.hide()
                    binding.rvImage.hide()
                    setMessage(item)
                }

                ChatConstants.TYPE_AUDIO -> {
                    binding.tvContent.hide()
                    binding.vpvVoice.show()
                    binding.rvImage.hide()
                    setAudio(item)
                }

                ChatConstants.TYPE_MEDIA -> {
                    binding.tvContent.hide()
                    binding.vpvVoice.hide()
                    binding.rvImage.show()
                    setMultimedia(item)
                }

                ChatConstants.TYPE_LOCATION -> {
                    binding.tvContent.hide()
                    binding.vpvVoice.hide()
                    binding.rvImage.hide()
                    setLocation(item)
                }
            }
        }

        private fun setMessage(item: Message) {
            binding.tvContent.text = item.message
            binding.tvTimeChat.text = DateUtils.getDateByFormatTimeDateSeconds(item.timeSend)
        }

        private fun setMultimedia(item: Message) {
            binding.llParent.background = null
            binding.rvImage.measure(LayoutParams.WRAP_CONTENT, getContext().dimen(R.dimen.dp_120))
            mediaAdapter = MediaChatAdapter(getContext(), idChat, item.id)
            mediaAdapter.onListenerCLick = this@MessageAdapter
            mediaAdapter.setData(item.multiMedia)
            setMedia(binding.rvImage, item.multiMedia, mediaAdapter)
        }

        private fun setLocation(item: Message) {
            //
        }

        private fun setAudio(item: Message) {
            binding.llParent.backgroundTintList =
                ColorStateList.valueOf(getColor(R.color.transparent))
            binding.vpvVoice.show()
            binding.vpvVoice.setAudio(item.multiMedia.first().url)
        }

        private fun setColorForView(isUserCurrent: Boolean) {
            if (isUserCurrent) {
                // là tin nhắn của người dùng hiện tại
                binding.llParent.backgroundTintList =
                    ColorStateList.valueOf(getColor(R.color.blue_700))
                binding.tvContent.textColor = getColor(R.color.white)
            } else {
                binding.llParent.backgroundTintList =
                    ColorStateList.valueOf(getColor(R.color.gray_200))
                binding.tvContent.textColor = getColor(R.color.gray_600)
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setMedia(
            reCycle: RecyclerView, multiMedia: ArrayList<MultiMedia>, mediaAdapter: MediaChatAdapter
        ) {
            when (multiMedia.size) {
                GalleryViewAdapterUtils.ONE -> {
                    AppUtils.initRecyclerViewVertical(reCycle, mediaAdapter)
                }

                GalleryViewAdapterUtils.TWO, GalleryViewAdapterUtils.THREE, GalleryViewAdapterUtils.FOUR -> {
                    AppUtils.initRecyclerViewVertical(reCycle, mediaAdapter, 2)
                }
            }
            mediaAdapter.setData(multiMedia)
            mediaAdapter.notifyDataSetChanged()
        }

    }

    interface OnListenerCLick {
        fun onClickLongItemChat(position: Int)
        fun onClickImage(
            multiMedia: MultiMedia, lisMultiMedia: ArrayList<MultiMedia>, idChat: String
        )
    }

    override fun onClick(
        multiMedia: MultiMedia, lisMultiMedia: ArrayList<MultiMedia>, idChat: String
    ) {
        onListenerCLick!!.onClickImage(multiMedia, lisMultiMedia, idChat)
    }
}