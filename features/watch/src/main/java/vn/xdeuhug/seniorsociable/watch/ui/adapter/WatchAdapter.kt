@file:Suppress("DEPRECATION")

package vn.xdeuhug.seniorsociable.watch.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.model.entity.modelPexels.Video
import vn.xdeuhug.seniorsociable.utils.VideoUtils
import vn.xdeuhug.seniorsociable.watch.databinding.ItemVideoBinding

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 11 / 2023
 */
@Suppress("DEPRECATION")
class WatchAdapter(context: Context) : AppAdapter<Video>(context) {
    private var currentPlayingPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemVideoBinding) : AppViewHolder(binding.root) {

        override fun onBindView(position: Int) {
            val item = getItem(position)
//            VideoUtils.loadVideo(item, binding.plvVideo, getContext())
        }
    }
}
