package vn.xdeuhug.seniorsociable.post.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.post.databinding.ItemPlacesBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 24 / 10 / 2023
 */
class PlacesAdapter(context: Context) : AppAdapter<AutocompletePrediction>(context){
    var onListenerCLick: OnListenerCLick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemPlacesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemPlacesBinding) : AppViewHolder(binding.root) {
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
            binding.tvShortAddress.text = item.getPrimaryText(null)
            binding.tvLongAddress.text = item.getSecondaryText(null)
        }

    }

    interface OnListenerCLick {
        fun onClick(position: Int)
    }
}