package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.JZDataSource
import com.devs.readmoreoption.ReadMoreOption
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.ItemPostBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.eventbus.ReloadDataInPostEventBus
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.removeLinksUnderline
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.AppUtils.toArrayList
import vn.xdeuhug.seniorsociable.utils.GalleryViewAdapterUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.PostUtils
import vn.xdeuhug.seniorsociable.utils.ReactObjectUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import vn.xdeuhug.seniorsociable.widget.reactbutton.ReactButton
import vn.xdeuhug.seniorsociable.widget.reactbutton.Reaction


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 10 / 2023
 */
class PostAdapter(context: Context) : AppAdapter<Post>(context), MediaAdapter.OnListenerCLick {
    var onListenerCLick: OnListenerCLick? = null
    var onListenerClickTagUser: AppUtils.OnListenerClickTagUser? = null
    private var listUser = ListUserCache.getList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemPostBinding) : AppViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            val list = listUser.filter { it.id in item.idUserTags }.toArrayList() // List gán thẻ
            val userPost = listUser.firstOrNull { it.id == item.idUserPost }
            val mediaAdapter = MediaAdapter(getContext(), userPost!!, item.typePost, item.id)
            mediaAdapter.onListenerCLick = this@PostAdapter
            userPost.let {
                PhotoShowUtils.loadAvatarImage(
                    UploadFireStorageUtils.getRootURLAvatarById(it.id),
                    it.avatar,
                    binding.imvAuthor
                )
                binding.tvAuthor.text = AppUtils.fromHtml("<b>${it.name}</b>")
                AppUtils.singleTextView(
                    item.typePost,
                    getContext(),
                    binding.tvAuthor,
                    userPost,
                    list,
                    item.address,
                    object :
                        AppUtils.OnListenerClickTagUser {
                        override fun clickNameUserInPost(user: User) {
                            onListenerClickTagUser!!.clickNameUserInPost(user)
                        }

                        override fun clickListUser(listUserTag: ArrayList<User>) {
                            onListenerClickTagUser!!.clickListUser(listUserTag)
                        }

                        override fun clickAddress(address: Address) {
                            onListenerClickTagUser!!.clickAddress(address)
                        }

                    })
            }
            AppUtils.setMedia(binding.rvMedia, item.multiMedia, mediaAdapter)
            PostUtils.setViewTypePost(item.type, binding.imvTypePost)
            if (item.multiMedia.isNotEmpty()) {
                binding.rvMedia.show()
            } else {
                binding.rvMedia.hide()
            }
            binding.tvTimePost.text = TimeUtils.timeAgoChat(getContext(), item.timeCreated)
            // OR using options to customize
            val readMoreOption: ReadMoreOption =
                ReadMoreOption.Builder(getContext()).textLength(300) // OR
                    .moreLabel(getString(R.string.see_more)!!)
                    .lessLabel(getString(R.string.hide_less)!!)
                    .moreLabelColor(getColor(R.color.blue_700))
                    .lessLabelColor(getColor(R.color.red_app_senior_sociable))
                    .labelUnderLine(false).expandAnimation(true).build()

            readMoreOption.addReadMoreTo(binding.tvContent, item.content)
            binding.btnLike.setReactions(*ReactObjectUtils.reactions)
            binding.btnLike.setEnableReactionTooltip(true)
            item.isReloadDataNotPic = true
            setTypePost(item)
            //
            setDataLayoutInteract(item)
            // set React
            val reactOfUser = item.interacts.filter { it.id == UserCache.getUser().id }
            binding.btnLike.setDefaultReactionNotListener(ReactObjectUtils.defaultReact)
            if (reactOfUser.isNotEmpty()) {
                binding.btnLike.setCurrentReactionNotListener(
                    ReactObjectUtils.getReactionsWithType(
                        reactOfUser[0].type
                    )
                )

            } else {
                binding.btnLike.setCurrentReactionNotListener(ReactObjectUtils.defaultReact)
            }

            binding.btnLike.setOnReactionChangeListener { reaction ->
                Timber.tag("").d("onReactionChange: %s", reaction.reactText)
                onListenerCLick!!.onClickReact(
                    position, item, reaction, binding.btnLike.defaultReaction
                )
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

            AppUtils.setChildListener(binding.llComment) {
                onListenerCLick!!.onShowDialogComment(item, position)
            }

            AppUtils.setChildListener(binding.llShare) {
                onListenerCLick!!.onClickShare(position, item)
            }

            AppUtils.setChildListener(binding.llDetailsInteract) {
                onListenerCLick!!.onShowDialogComment(item, position)
            }

            binding.imvSeeMore.clickWithDebounce {
                onListenerCLick!!.onSeeMore(item, position)
            }
            binding.imvAuthor.clickWithDebounce {
                onListenerCLick!!.onClickAvatar(userPost)
            }

            binding.tvAuthor.clickWithDebounce {
                onListenerCLick!!.onClickTitleTag(position, item)
            }
        }

        private fun setTypePost(item: Post) {
            when (item.type) {
                PostConstants.TYPE_PRIVATE -> {
                    binding.imvTypePost.setImageResource(R.drawable.ic_private)
                }

                PostConstants.TYPE_FRIEND -> {
                    binding.imvTypePost.setImageResource(R.drawable.ic_friend)
                }

                PostConstants.TYPE_PUBLIC -> {
                    binding.imvTypePost.setImageResource(R.drawable.ic_public)
                }
            }
        }

        @Subscribe
        fun onReloadDataInteractCommentAndShare(eventBus: ReloadDataInPostEventBus) {
            if (eventBus.isReload) {
                setDataLayoutInteract(getItem(eventBus.position))
            }
        }


        @SuppressLint("SetTextI18n")
        private fun setDataLayoutInteract(item: Post) {
            if (item.comments.isEmpty() && item.interacts.isEmpty()) {
                binding.llInteract.hide()
                binding.tvNumberOfComment.hide()
            } else {
                binding.llInteract.show()
                // Tương tác các icon
                if (item.interacts.isEmpty()) {
                    binding.imvFirstReact.hide()
                    binding.imvSecondReact.hide()
                    binding.tvReactDetails.hide()
                } else {
                    val listMyInteract = item.interacts.filter { it.id == UserCache.getUser().id }
                    // Nếu size lớn hơn 1
                    if (item.interacts.size > 1) {
                        if (listMyInteract.isNotEmpty()) {
                            binding.imvFirstReact.setImageDrawable(
                                ReactObjectUtils.getDrawableWithType(
                                    listMyInteract[0].type, getContext()
                                )
                            )
                            binding.tvReactDetails.text =
                                "${getString(R.string.you)} ${getString(R.string.and)} ${
                                    AppUtils.formatFacebookLikes(item.interacts.size - 1)
                                } ${getString(R.string.other_person)}"
                        } else {
                            binding.tvReactDetails.text =
                                AppUtils.formatFacebookLikes(item.interacts.size)
                        }
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
                        if (listMyInteract.isNotEmpty()) {
                            binding.imvFirstReact.setImageDrawable(
                                ReactObjectUtils.getDrawableWithType(
                                    listMyInteract[0].type, getContext()
                                )
                            )
                            binding.tvReactDetails.text = getString(R.string.you)
                        } else {
                            binding.tvReactDetails.text =
                                AppUtils.formatFacebookLikes(item.interacts.size)
                            binding.imvFirstReact.setImageDrawable(
                                ReactObjectUtils.getDrawableWithType(
                                    item.interacts[0].type, getContext()
                                )
                            )
                        }
                    }
                }
            }
            if (item.comments.isNotEmpty()) {
                binding.tvNumberOfComment.show()
                binding.tvNumberOfComment.text =
                    "${item.comments.size} ${getString(R.string.comment)}"
            } else {
                binding.tvNumberOfComment.hide()
                binding.tvNumberOfComment.text = ""
            }
            if (item.idsUserShare.isNotEmpty()) {
                binding.tvNumberOfShare.show()
                binding.tvNumberOfShare.text =
                    "${item.idsUserShare.size} ${getString(R.string.share)}"
            } else {
                binding.tvNumberOfShare.hide()
                binding.tvNumberOfShare.text = ""
            }
            if (item.idsUserShare.isNotEmpty() && item.comments.isNotEmpty()) {
                binding.tvBigDot.show()
            } else {
                binding.tvBigDot.hide()
            }
        }

    }

    override fun onClick(
        jzDataSource: JZDataSource?,
        multiMedia: MultiMedia,
        lisMultiMedia: ArrayList<MultiMedia>,
        idPost: String
    ) {
        onListenerCLick!!.onClick(jzDataSource, multiMedia, lisMultiMedia, idPost)
    }

    interface OnListenerCLick {
        fun onClick(
            jzDataSource: JZDataSource?,
            multiMedia: MultiMedia,
            lisMultiMedia: ArrayList<MultiMedia>,
            idPost: String
        )

        fun onShowDialogComment(post: Post, position: Int)
        fun onClickReact(
            position: Int, postUpdate: Post, reaction: Reaction, defaultReaction: Reaction
        )

        fun onSeeMore(post: Post, position: Int)

        fun onClickShare(position: Int, post: Post)
        fun onClickTitleTag(position: Int, post: Post)
        fun onClickAvatar(user: User)
    }
}