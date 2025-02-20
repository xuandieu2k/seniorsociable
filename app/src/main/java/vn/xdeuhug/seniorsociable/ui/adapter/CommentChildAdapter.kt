package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.databinding.ItemCommentChildBinding
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Comment
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.PostUtils
import vn.xdeuhug.seniorsociable.utils.ReactObjectUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import vn.xdeuhug.seniorsociable.utils.VideoUtils
import vn.xdeuhug.seniorsociable.widget.reactbutton.ReactButton
import vn.xdeuhug.seniorsociable.widget.reactbutton.Reaction

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 24 / 12 / 2023
 */
class CommentChildAdapter(context: Context) : AppAdapter<Comment>(context) {
    var onListenerCLick: OnListenerCLick? = null
    private var listUser = ListUserCache.getList()
    private var idParentComment = ""
    private var userIdToNameMap = hashMapOf<String, String>()
    private var positionParent = -1

    constructor(context: Context, idParentComment: String, positionParent: Int) : this(context) {
        this.idParentComment = idParentComment
        this.positionParent = positionParent
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemCommentChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemCommentChildBinding) :
        AppViewHolder(binding.root) {
        init {
            binding.rlMedia.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClickInChild(position, getData()[position])
                }
            }
            ListUserCache.getList().forEach {
                userIdToNameMap[it.id] = it.name
            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            //
            val userComment = listUser.firstOrNull { it.id == item.idUserComment }
            // **
            if (item.multiMedia.isNotEmpty()) {
                binding.rlMedia.show()
                when (item.multiMedia[0].type) {
                    AppConstants.UPLOAD_VIDEO -> {
                        binding.plvVideo.show()
                        binding.imvImage.hide()
                        VideoUtils.loadVideoFromFirebase(
                            UploadFireStorageUtils.getRootURLCommentById(
                                item.id
                            ), item.multiMedia[0].url, binding.plvVideo
                        )
                    }

                    AppConstants.UPLOAD_IMAGE -> {
                        AppUtils.scaleImageInComment(
                            binding.imvImage,
                            getContext(),
                            AppUtils.convertPixelToDp(
                                getResources(), item.multiMedia[0].height
                            ),
                            AppUtils.convertPixelToDp(getResources(), item.multiMedia[0].width)
                        )
                        binding.imvImage.show()
                        binding.plvVideo.hide()
                        PhotoShowUtils.loadCommentImage(
                            UploadFireStorageUtils.getRootURLCommentById(
                                item.id
                            ), item.multiMedia[0].url, binding.imvImage
                        )
                    }
                }
            } else {
                binding.rlMedia.hide()
            }
            AppUtils.setTextUserTagByIdUserInTextView(getContext(),
                binding.tvComment,
                item.content,
                userIdToNameMap,
                object : AppUtils.OnListenerClickNameTagUser {
                    override fun clickNameUserInComment(user: User) {
                        onListenerCLick!!.clickNameUserInCommentInChild(user)
                    }

                })
            binding.tvUsername.text = userComment?.name
            PhotoShowUtils.loadAvatarImage(
                UploadFireStorageUtils.getRootURLAvatarById(userComment!!.id),
                userComment.avatar,
                binding.imvAvatar
            )
            setDataLayoutInteract(item)
            // **
            setDataReact(item, position)
            setClickReply(position)
            AppUtils.setChildListener(binding.llInteract) {
                onListenerCLick!!.onClickDetailReactInChild(position, item)
            }
            setClickNameAndAvatar(userComment!!)
        }

        private fun setClickNameAndAvatar(userComment: User) {
            binding.imvAvatar.clickWithDebounce {
                onListenerCLick!!.clickNameUserInCommentInChild(userComment)
            }
            binding.tvUsername.clickWithDebounce {
                onListenerCLick!!.clickNameUserInCommentInChild(userComment)
            }
        }

        private fun setClickReply(position: Int) {
            binding.tvReply.clickWithDebounce {
                onListenerCLick!!.onReplyInChild(positionParent ,position, getData()[position])
            }
        }

        private fun setDataReact(item: Comment, position: Int) {
            val reactOfUser = item.interacts.filter { it.id == UserCache.getUser().id }
            binding.btnInteract.setDefaultReactionNotListener(ReactObjectUtils.defaultReact)
            if (reactOfUser.isNotEmpty()) {
                binding.btnInteract.setCurrentReactionNotListener(
                    ReactObjectUtils.getReactionsWithType(
                        reactOfUser[0].type
                    )
                )
            } else {
                binding.btnInteract.setCurrentReactionNotListener(ReactObjectUtils.defaultReact)
            }
            binding.btnInteract.setCompoundDrawables(null, null, null, null)
            binding.btnInteract.setReactions(*ReactObjectUtils.reactions)
            binding.btnInteract.setEnableReactionTooltip(true)

            binding.btnInteract.setOnReactionChangeListener { reaction ->
                binding.btnInteract.setCompoundDrawables(null, null, null, null)
                onListenerCLick!!.onReactInChild(
                    positionParent, item, reaction, binding.btnInteract.defaultReaction
                )
                Timber.tag("").d("onReactionChange: %s", reaction.reactText)
            }

            binding.btnInteract.setOnReactionDialogStateListener(object :
                ReactButton.OnReactionDialogStateListener {
                override fun onDialogOpened() {
                    Timber.tag("").d("onDialogOpened")
                }

                override fun onDialogDismiss() {
                    Timber.tag("").d("onDialogDismiss")
                }
            })
            //
            binding.tvTimeAgo.text = TimeUtils.formatTimeAgo(item.timeCreated, getContext())
        }

        @SuppressLint("SetTextI18n")
        private fun setDataLayoutInteract(item: Comment) {
            if (item.interacts.isEmpty()) {
                binding.llInteract.hide()
            } else {
                binding.llInteract.show()
                // Tương tác các icon
                if (item.interacts.isEmpty()) {
                    binding.imvFirstReact.hide()
                    binding.imvSecondReact.hide()
                    binding.tvReactDetails.hide()
                } else {
                    // Nếu size lớn hơn 1
                    if (item.interacts.size > 1) {
                        binding.tvReactDetails.text =
                            AppUtils.formatFacebookLikes(item.interacts.size)
                        val mostFrequentInteractions =
                            PostUtils.findMostFrequentInteractions(item.interacts)
                        val mostFrequentType1 = mostFrequentInteractions.first
                        val mostFrequentType2 = mostFrequentInteractions.second
                        if (mostFrequentType1 == mostFrequentType2) {
                            binding.imvFirstReact.show()
                            binding.imvSecondReact.hide()
                            binding.imvFirstReact.setImageDrawable(
                                ReactObjectUtils.getDrawableWithType(
                                    mostFrequentType1, getContext()
                                )
                            )
                        } else {
                            binding.imvFirstReact.show()
                            binding.imvSecondReact.show()
                            binding.imvFirstReact.setImageDrawable(
                                ReactObjectUtils.getDrawableWithType(
                                    mostFrequentType1, getContext()
                                )
                            )
                            binding.imvSecondReact.setImageDrawable(
                                ReactObjectUtils.getDrawableWithType(
                                    mostFrequentType2, getContext()
                                )
                            )
                        }

                    } else { // size nhỏ hơn = 1
                        binding.imvFirstReact.show()
                        binding.imvSecondReact.hide()
                        binding.imvFirstReact.setImageDrawable(
                            ReactObjectUtils.getDrawableWithType(
                                item.interacts[0].type, getContext()
                            )
                        )
                        binding.tvReactDetails.text =
                            AppUtils.formatFacebookLikes(item.interacts.size)
                    }
                }
            }
        }

    }

    interface OnListenerCLick {
        fun onClickInChild(position: Int, comment: Comment)
        fun onReplyInChild(positionParent: Int, position: Int, comment: Comment)
        fun onReactInChild(
            position: Int, comment: Comment, reactionCurrent: Reaction, defaultReaction: Reaction
        )

        fun onClickDetailReactInChild(position: Int, comment: Comment)
        fun clickNameUserInCommentInChild(user: User)
    }
}