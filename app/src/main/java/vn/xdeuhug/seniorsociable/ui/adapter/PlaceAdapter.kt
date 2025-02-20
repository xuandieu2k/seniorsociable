package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomtom.sdk.location.Place
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.databinding.ItemPlaceBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2023
 */
class PlaceAdapter(context: Context) : AppAdapter<Place>(context) {
    var onListenerSelected: OnListenerSelected? = null
    private var isTopInWatchTab = false

    constructor(context: Context, isTopInWatchTab: Boolean) : this(context) {
        this.isTopInWatchTab = isTopInWatchTab
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemPlaceBinding) : AppViewHolder(binding.root) {
        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerSelected?.onSelected(position)
                }
            }
        }
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val place = getItem(position)
            val shortAddress= place.address!!.freeformAddress
            val fullAddress = "${place.address!!.streetNumber} ${place.address!!.streetName} ${place.address!!.municipalitySubdivision} ${place.address!!.municipality}"
            binding.tvShortAddress.text = shortAddress
            binding.tvLongAddress.text = fullAddress.trimStart()
        }

    }

    interface OnListenerSelected {
        fun onSelected(position: Int)
    }
}