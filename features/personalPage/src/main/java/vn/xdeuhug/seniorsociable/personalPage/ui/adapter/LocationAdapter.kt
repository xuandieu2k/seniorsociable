package vn.xdeuhug.seniorsociable.personalPage.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.personalPage.databinding.ItemLocationBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 01 / 12 / 2023
 */
class LocationAdapter(context: Context) : AppAdapter<AutocompletePrediction>(context) {
    var onListenerCLick: OnListenerCLick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemLocationBinding) : AppViewHolder(binding.root) {

        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick?.onClick(position)
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