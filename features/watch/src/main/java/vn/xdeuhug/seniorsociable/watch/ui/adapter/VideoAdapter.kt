package vn.xdeuhug.seniorsociable.watch.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.app.AppApplication
import vn.xdeuhug.seniorsociable.model.entity.modelPexels.Video
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.ReactObjectUtils
import vn.xdeuhug.seniorsociable.utils.VideoUtils
import vn.xdeuhug.seniorsociable.watch.databinding.ItemVideoBinding
import vn.xdeuhug.seniorsociable.widget.reactbutton.ReactButton

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 06 / 11 / 2023
 */
class VideoAdapter(context: Context) : AppAdapter<Video>(context) {
    private lateinit var app: AppApplication

    var onActionVideo: OnActionVideo? = null

    constructor(context: Context, app: AppApplication) : this(context) {
        this.app = app
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemVideoBinding) : AppViewHolder(binding.root) {
        init {
            binding.btnComment.clickWithDebounce {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION)
                {
                    onActionVideo!!.onClickComment(position,getData()[position])
                }
            }
            binding.btnShare.clickWithDebounce {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION)
                {
                    onActionVideo!!.onClickShare(position,getData()[position])
                }
            }
        }
        override fun onBindView(position: Int) {
            val item = getItem(position)
            val video = item.videoFiles.first { it.quality == "hd" }
            VideoUtils.loadVideo(video, binding.plvVideo, app)
            //
            PhotoShowUtils.loadAvatarImage("nondskdsa", binding.imvAvatar)
            binding.tvUsername.text = item.user.name
            binding.btnLike.isShowDrawableTop = true
            binding.btnLike.setReactions(*ReactObjectUtils.reactions)
            binding.btnLike.setEnableReactionTooltip(true)
            binding.btnLike.setCurrentReactionNotListener(ReactObjectUtils.defaultReactVideo)
            binding.btnLike.setDefaultReactionNotListener(ReactObjectUtils.defaultReactVideo)

            binding.btnLike.setOnReactionChangeListener { reaction ->
                Timber.tag("").d("onReactionChange: %s", reaction.reactText)
            }

            binding.btnLike.setOnReactionDialogStateListener(object :
                ReactButton.OnReactionDialogStateListener {
                override fun onDialogOpened() {
                    Timber.tag("").d("onDialogOpened")
                }

                override fun onDialogDismiss() {
                    Timber.tag("").d("onDialogDismiss")
                }
            })
        }
    }
    interface OnActionVideo{
        fun onClickComment(position: Int, video: Video)
        fun onClickReact(position: Int, video: Video)
        fun onClickShare(position: Int, video: Video)
    }
}