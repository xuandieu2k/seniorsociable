package vn.xdeuhug.seniorsociable.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.databinding.DialogCommentBinding
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.ui.adapter.CommentAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PostUtils
import vn.xdeuhug.seniorsociable.utils.ReactObjectUtils
import vn.xdeuhug.seniorsociable.widget.reactbutton.ReactButton

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 10 / 2023
 */
class CommentDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context, var postCurrent: Post
    ) : BottomSheetDialog(context) {
        private var binding: DialogCommentBinding =
            DialogCommentBinding.inflate(LayoutInflater.from(context))
        private lateinit var commentAdapter: CommentAdapter

        //
        private lateinit var onActionDone: OnActionDone
        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(true)
            setContentView(binding.root)

            // Đặt chiều cao "nửa màn hình" bằng 0
            val behavior = (binding.root.parent as View).layoutParams as CoordinatorLayout.LayoutParams
            val params = behavior.behavior as BottomSheetBehavior
            params.peekHeight = 0


            setUpEventReact()
            setDataOnTitle()
            setDataComment()
        }

        @SuppressLint("SetTextI18n")
        private fun setDataOnTitle() {
            if (postCurrent.interacts.isEmpty()) {
                binding.imvFirstReact.hide()
                binding.imvSecondReact.hide()
                binding.tvReactDetails.text =
                    context.getString(R.string.become_first_person_interact_post)
            } else {
                val listMyInteract =
                    postCurrent.interacts.filter { it.id == UserCache.getUser().id }
                if (listMyInteract.isNotEmpty()) {
                    binding.btnReact.currentReaction =
                        ReactObjectUtils.getReactionsWithType(listMyInteract[0].type)
                } else {
                    binding.btnReact.currentReaction = ReactObjectUtils.defaultReact
                }
                // Nếu size lớn hơn 1
                if (postCurrent.interacts.size > 1) {
                    if (listMyInteract.isNotEmpty()) {
                        binding.imvFirstReact.setImageDrawable(
                            ReactObjectUtils.getDrawableWithType(
                                listMyInteract[0].type, context
                            )
                        )
                        binding.tvReactDetails.text =
                            "${context.getString(R.string.you)} ${context.getString(R.string.and)} ${
                                AppUtils.formatFacebookLikes(postCurrent.interacts.size - 1)
                            } ${context.getString(R.string.other_person)}"
                    } else {
                        binding.tvReactDetails.text =
                            AppUtils.formatFacebookLikes(postCurrent.interacts.size)
                    }
                    val mostFrequentInteractions =
                        PostUtils.findMostFrequentInteractions(postCurrent.interacts)
                    val mostFrequentType1 = mostFrequentInteractions.first
                    val mostFrequentType2 = mostFrequentInteractions.second
                    if (mostFrequentType1 == mostFrequentType2) {
                        binding.imvFirstReact.show()
                        binding.imvSecondReact.hide()
                        binding.imvFirstReact.setImageDrawable(
                            ReactObjectUtils.getDrawableWithType(
                                mostFrequentType1, context
                            )
                        )
                    } else {
                        binding.imvFirstReact.show()
                        binding.imvSecondReact.show()
                        binding.imvFirstReact.setImageDrawable(
                            ReactObjectUtils.getDrawableWithType(
                                mostFrequentType1, context
                            )
                        )
                        binding.imvSecondReact.setImageDrawable(
                            ReactObjectUtils.getDrawableWithType(
                                mostFrequentType2, context
                            )
                        )
                    }


                } else { // size nhỏ hơn = 1
                    binding.imvFirstReact.show()
                    binding.imvSecondReact.hide()
                    if (listMyInteract.isNotEmpty()) {
                        binding.imvFirstReact.setImageDrawable(
                            ReactObjectUtils.getDrawableWithType(
                                listMyInteract[0].type, context
                            )
                        )
                        binding.tvReactDetails.text = context.getString(R.string.you)
                    } else {
                        binding.tvReactDetails.text =
                            AppUtils.formatFacebookLikes(postCurrent.interacts.size)
                        binding.imvFirstReact.setImageDrawable(
                            ReactObjectUtils.getDrawableWithType(
                                postCurrent.interacts[0].type, context
                            )
                        )
                    }
                }
            }
        }

        private fun setDataComment() {
            commentAdapter = CommentAdapter(context)
            // Create recycleView
            AppUtils.initRecyclerViewVertical(binding.rvComment, commentAdapter)
            commentAdapter.setData(postCurrent.comments)
            commentAdapter.notifyDataSetChanged()
            if (postCurrent.comments.isNotEmpty()) {
                binding.rvComment.show()
                binding.RlNotData.hide()
            } else {
                binding.rvComment.hide()
                binding.RlNotData.show()
            }
        }

        private fun setUpEventReact() {
            binding.btnReact.defaultReaction = ReactObjectUtils.defaultReact
            binding.btnReact.currentReaction = ReactObjectUtils.defaultReact
            binding.btnReact.text = ""
            binding.btnReact.setReactions(*ReactObjectUtils.reactions)
            binding.btnReact.setEnableReactionTooltip(true)

            binding.btnReact.setOnReactionChangeListener { reaction ->
                binding.btnReact.text = ""
                Timber.tag("").d("onReactionChange: %s", reaction.reactText)
            }

            binding.btnReact.setOnReactionDialogStateListener(object :
                ReactButton.OnReactionDialogStateListener {
                override fun onDialogOpened() {
                    binding.btnReact.text = ""
                    Timber.tag("").d("onDialogOpened")
                }

                override fun onDialogDismiss() {
                    binding.btnReact.text = ""
                    Timber.tag("").d("onDialogDismiss")
                }
            })
        }


        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean)
        }
    }
}