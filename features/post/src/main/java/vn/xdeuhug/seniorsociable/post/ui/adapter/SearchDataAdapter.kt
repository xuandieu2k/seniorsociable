package vn.xdeuhug.seniorsociable.post.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.dimen
import org.jetbrains.anko.padding
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelSearch.SearchData
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.post.databinding.ItemSearchDataBinding
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 26 / 12 / 2023
 */
class SearchDataAdapter(context: Context) : AppAdapter<SearchData>(context){
    var onListenerCLick: OnListenerCLick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemSearchDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemSearchDataBinding) : AppViewHolder(binding.root) {
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
            if(item.type == AppConstants.TYPE_KEY_SEARCH)
            {
                binding.imvAvatar.padding = getContext().dimen(R.dimen.dp_4)
                binding.tvSearch.text = item.keySearch
            }else{
                PhotoShowUtils.loadAvatarImage(item.user!!.avatar,binding.imvAvatar)
                binding.tvSearch.text = item.keySearch
            }
        }

    }

    interface OnListenerCLick {
        fun onClick(position: Int)
    }
}