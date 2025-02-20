package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.databinding.ItemAlbumBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.Album
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 16 / 10 / 2023
 */
class AlbumAdapter(context: Context) : AppAdapter<Album>(context) {
    var onListenerCLick: OnListenerCLick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemAlbumBinding) : AppViewHolder(binding.root) {
        init {
            binding.RlParent.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClick(position)
                }

            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            binding.tvNameAlbum.text = item.name
            binding.tvNumberOfAlbum.text = "${position + 1}"
            PhotoShowUtils.loadPhotoImageNormal(
                "https://images.pexels.com/photos/842711/pexels-photo-842711.jpeg?cs=srgb&dl=pexels-christian-heitz-842711.jpg&fm=jpg",
                binding.imvBackground
            )
            if (item.isChecked) {
                binding.imvChecked.show()
            } else {
                binding.imvChecked.hide()
            }
        }

    }

    interface OnListenerCLick {
        fun onClick(position: Int)
    }
}