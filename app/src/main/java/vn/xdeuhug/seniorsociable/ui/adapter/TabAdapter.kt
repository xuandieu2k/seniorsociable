package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.backgroundColor
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.databinding.ItemTabBinding
import vn.xdeuhug.seniorsociable.model.entity.Tab
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.invisible
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 11 / 2023
 */
class TabAdapter(context: Context) : AppAdapter<Tab>(context) {
    var onListenerCLick: OnListenerCLick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemTabBinding) : AppViewHolder(binding.root) {
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
            setViewTab(item)
        }


        private fun setViewTab(item: Tab) {
            if (item.isSelected) {
                binding.vline.show()
            } else {
                binding.vline.invisible()
            }
            setTextData(item)
            setColorLine(item)
        }

        @SuppressLint("SetTextI18n")
        private fun setTextData(item: Tab) {
            binding.tvTextReact.text = AppUtils.formatFacebookLikes(item.value)
            binding.tvTextReact.setTextColor(getColor(R.color.gray_600))
            AppUtils.logJsonFromObject(item.id)
            when (item.id) {
                PostConstants.INTERACT_TAB_DEFAULT ->{
                    binding.tvTextReact.text = AppUtils.fromHtml("<b>${getString(R.string.all)}&ensp;${AppUtils.formatFacebookLikes(item.value)}</b>")
                    binding.tvTextReact.setTextColor(getColor(R.color.blue_like))
                    binding.tvTextReact.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
                PostConstants.INTERACT_LIKE -> {
                    binding.tvTextReact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_senior_new, 0, 0, 0)
                }

                PostConstants.INTERACT_LOVE -> {
                    binding.tvTextReact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart, 0, 0, 0)
                }

                PostConstants.INTERACT_SMILE -> {
                    binding.tvTextReact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_laugh, 0, 0, 0)
                }

                PostConstants.INTERACT_WOW -> {
                    binding.tvTextReact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wow, 0, 0, 0)
                }

                PostConstants.INTERACT_SAD -> {
                    binding.tvTextReact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sad, 0, 0, 0)
                }

                PostConstants.INTERACT_ANGRY -> {
                    binding.tvTextReact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_angry, 0, 0, 0)
                }

            }
        }

        private fun setColorLine(item: Tab) {
            when (item.id) {
                PostConstants.INTERACT_LIKE,PostConstants.INTERACT_TAB_DEFAULT -> {
                    binding.vline.backgroundColor = getColor(R.color.blue_like)
                }

                PostConstants.INTERACT_LOVE -> {
                    binding.vline.backgroundColor = getColor(R.color.red_love)
                }

                PostConstants.INTERACT_SMILE, PostConstants.INTERACT_WOW, PostConstants.INTERACT_SAD -> {
                    binding.vline.backgroundColor = getColor(R.color.yellow_haha)
                }

                PostConstants.INTERACT_ANGRY -> {
                    binding.vline.backgroundColor = getColor(R.color.red_angry)
                }

            }
        }

    }

    interface OnListenerCLick {
        fun onClick(position: Int)
    }
}