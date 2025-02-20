package vn.xdeuhug.seniorsociable.post.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.post.databinding.ItemStoryBinding
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Story
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.MultimediaUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 10 / 2023
 */
@SuppressLint("NotifyDataSetChanged")
class StoryAdapter(context: Context) : AppAdapter<Story>(context) {
    var onClickListener: OnClickListener? = null
    private var listUser = ListUserCache.getList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemStoryBinding) : AppViewHolder(binding.root) {

        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION)
                {
                    onClickListener!!.onClick(position)
                }
            }
        }
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            //
            val item = getItem(position)
            val userCreate = listUser.firstOrNull { it.id == item.idUserCreate }

            PhotoShowUtils.loadAvatarImage(
                UploadFireStorageUtils.getRootURLAvatarById(item.idUserCreate),
                userCreate!!.avatar,
                binding.imvAvatar
            )
            binding.tvUsername.text = userCreate.name
            PhotoShowUtils.loadPostImage(
                UploadFireStorageUtils.getRootURLAvatarById(UserCache.getUser().id),
                UserCache.getUser().avatar,
                binding.llFirst.imvAvatarUserCreate
            ) // Load story
            if (position == 0) {
                binding.rlStory.hide()
                binding.llFirst.root.show()
                binding.llFirst.root.clickWithDebounce {
                    onClickListener!!.onCreateStory()
                }
            } else {
                binding.rlStory.show()
                binding.llFirst.root.hide()
                PhotoShowUtils.loadPhotoImagePreview(item.multiMedia.first().url, binding.imvBackground)
            }
        }

    }

    interface OnClickListener {
        fun onCreateStory()
        fun onClick(position: Int)
    }
}