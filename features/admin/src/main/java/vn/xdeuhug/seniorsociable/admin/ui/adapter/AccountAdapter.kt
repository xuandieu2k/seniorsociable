package vn.xdeuhug.seniorsociable.admin.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import org.jetbrains.anko.textColor
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.admin.databinding.ItemAccountBinding
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.other.handlerPostDelayed
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 29 / 12 / 2023
 */
class AccountAdapter(context: Context) : AppAdapter<User>(context) {
    var onListenerCLick: OnListenerCLick? = null
    var imageViewClickListener: OnClickImageViewListener? = null
    private var listUser = ListUserCache.getList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemAccountBinding) :
        AppViewHolder(binding.root) {
        private var isSwiping = false
        init {

            binding.splParent.addSwipeListener(object : SwipeLayout.SwipeListener {
                override fun onStartOpen(layout: SwipeLayout) {
                    isSwiping = true
                    binding.root.isClickable = false
                }

                override fun onOpen(layout: SwipeLayout) {
                    isSwiping = true
                }

                override fun onStartClose(layout: SwipeLayout) {
                    isSwiping = false
                }

                override fun onClose(layout: SwipeLayout) {
                    isSwiping = false
                    handlerPostDelayed(1000) {
                        binding.root.isClickable = true
                    }
                }

                override fun onUpdate(layout: SwipeLayout?, leftOffset: Int, topOffset: Int) {
                    // code
                }

                override fun onHandRelease(layout: SwipeLayout?, xvel: Float, yvel: Float) {
                    // code
                }
            })

            binding.llInformation.clickWithDebounce(1000) {
                val position = bindingAdapterPosition
                val item = getItem(position)
                if (position != RecyclerView.NO_POSITION) {
                    imageViewClickListener?.onClickButtonRemove(position, item)
                }
            }

            binding.llEdit.clickWithDebounce(1000) {
                val position = bindingAdapterPosition
                val item = getItem(position)
                if (position != RecyclerView.NO_POSITION) {
                    imageViewClickListener?.onClickButtonEdit(position, item)
                }
            }

            binding.root.clickWithDebounce(1000) {
                val position = bindingAdapterPosition
                onListenerCLick!!.onClick(position)
            }

        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            setLockView(item)
            binding.tvNameUser.text = item.name
            PhotoShowUtils.loadAvatarImage(item.avatar,binding.imvAvatar)
            binding.tvJoinIn.text = TimeUtils.getJoinIn(item.timeCreated, getContext())
            setActiveView(item)
            setLoginWith(item)
        }

        private fun setLockView(item: User) {
            when(item.typeActive)
            {
                AppConstants.ACTIVATING ->{
                    binding.imvAction.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_post_is_locked)
                    binding.imvAction.setColorFilter(ContextCompat.getColor(getContext(), vn.xdeuhug.seniorsociable.R.color.red_600))
                    binding.tvAction.text = getString(vn.xdeuhug.seniorsociable.R.string.lock_account)
                    binding.tvAction.textColor = getColor(vn.xdeuhug.seniorsociable.R.color.red_600)
                }
                else ->{
                    binding.imvAction.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_post_reopen)
                    binding.imvAction.setColorFilter(ContextCompat.getColor(getContext(), vn.xdeuhug.seniorsociable.R.color.green_008))
                    binding.tvAction.text = getString(vn.xdeuhug.seniorsociable.R.string.open_account)
                    binding.tvAction.textColor = getColor(vn.xdeuhug.seniorsociable.R.color.green_008)
                }
            }
        }

        private fun setActiveView(item: User) {
            when(item.typeActive)
            {
                AppConstants.ACTIVATING ->{
                    binding.imvStatus.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_post_reopen)
                    binding.imvStatus.setColorFilter(ContextCompat.getColor(getContext(), vn.xdeuhug.seniorsociable.R.color.green_008))
                    binding.tvStatus.text = getString(vn.xdeuhug.seniorsociable.R.string.is_active)
                    binding.tvStatus.textColor = getColor(vn.xdeuhug.seniorsociable.R.color.green_008)
                    binding.tvReasonLock.hide()
                }
                AppConstants.BLOCKING ->{
                    binding.imvStatus.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_post_is_locked)
                    binding.imvStatus.setColorFilter(ContextCompat.getColor(getContext(), vn.xdeuhug.seniorsociable.R.color.red_600))
                    binding.tvStatus.text = getString(vn.xdeuhug.seniorsociable.R.string.is_locked)
                    //
                    // User post
                    val spanText = SpannableStringBuilder()
                    val textLock = getString(R.string.temp_lock_to)
                    spanText.append("$textLock ")
                    spanText.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            //
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            textPaint.color = getColor(R.color.gray_900)
                            textPaint.isUnderlineText = false
                        }
                    }, spanText.length - textLock!!.length-1, spanText.length, 0)
                    //
                    val dateLock = DateUtils.getDateByFormatTimeDateSeconds(item.timeEndBlock!!)
                    spanText.append(dateLock)
                    spanText.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            //
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            textPaint.color = getColor(R.color.red_600)
                            textPaint.isUnderlineText = false
                            textPaint.isFakeBoldText = true
                        }
                    }, spanText.length - dateLock.length, spanText.length, 0)
                    //
                    binding.tvStatus.movementMethod = LinkMovementMethod.getInstance()
                    binding.tvStatus.text = spanText
                    binding.tvReasonLock.show()
                    binding.tvReasonLock.text = item.reasonBlock
                }
                AppConstants.BLOCKED_UN_LIMITED ->{
                    binding.imvStatus.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_post_is_locked)
                    binding.imvStatus.setColorFilter(ContextCompat.getColor(getContext(), vn.xdeuhug.seniorsociable.R.color.red_600))
                    binding.tvStatus.text = getString(vn.xdeuhug.seniorsociable.R.string.is_lock_un_limited)
                    binding.tvStatus.textColor = getColor(vn.xdeuhug.seniorsociable.R.color.red_600)
                    binding.tvReasonLock.show()
                    binding.tvReasonLock.text = item.reasonBlock
                }
            }
        }

        private fun setLoginWith(item: User) {
            when(item.typeAccount)
            {
                AppConstants.TYPE_FACEBOOK ->{
                    binding.tvLoginWith.text = getString(vn.xdeuhug.seniorsociable.R.string.facebok)
                    binding.imvLoginWith.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_logo_facebook)
                }
                AppConstants.TYPE_GOOGLE ->{
                    binding.tvLoginWith.text = getString(vn.xdeuhug.seniorsociable.R.string.google)
                    binding.imvLoginWith.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_logo_google)
                }
                AppConstants.TYPE_PHONE ->{
                    binding.tvLoginWith.text = getString(vn.xdeuhug.seniorsociable.R.string.phone)
                    binding.imvLoginWith.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_login_phone)
                }
            }
        }

    }

    interface OnListenerCLick {
        fun onClick(position: Int)
    }

    interface OnClickImageViewListener {
        fun onClickButtonRemove(position: Int, user: User)
        fun onClickButtonEdit(position: Int, user: User)

    }
}