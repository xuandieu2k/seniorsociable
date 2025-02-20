package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.databinding.ItemTopicBinding
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.Topic

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 07 / 10 / 2023
 */
class TopicAdapter(context: Context) : AppAdapter<Topic>(context) {
    var onListenerSelected: OnListenerSelected? = null
    private var isTopInWatchTab = false

    constructor(context: Context, isTopInWatchTab: Boolean) : this(context) {
        this.isTopInWatchTab = isTopInWatchTab
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemTopicBinding) : AppViewHolder(binding.root) {
        init {
            if(!isTopInWatchTab)
            {
                binding.tvTopic.background = getDrawable(R.drawable.bg_button_filter_selector)
            }else{
                binding.tvTopic.background = getDrawable(R.drawable.bg_button_filter_video)
            }
            binding.tvTopic.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerSelected!!.onSelected(position)
                }

            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            binding.tvTopic.text = item.name
            binding.tvTopic.isSelected = item.isSelected
        }

    }

    interface OnListenerSelected {
        fun onSelected(position: Int)
    }
}