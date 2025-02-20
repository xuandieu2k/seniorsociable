package vn.xdeuhug.seniorsociable.news.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.news.databinding.ItemNewsTopBinding
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.News
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 10 / 2023
 */
class TopNewsAdapter(context: Context) : AppAdapter<News>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemNewsTopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    var onClickListener: OnClickListener? = null

    inner class ViewHolder(private val binding: ItemNewsTopBinding) : AppViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            //
            val item = getItem(position)
            if(item.imageUrl == null)
            {
                item.imageUrl = ""
            }
            PhotoShowUtils.loadPhotoNews(item.imageUrl, binding.imvNews)
            binding.tvTimePost.text = DateUtils.getDateByDatetimeString(item.pubDate)
            binding.tvContentNews.text = item.title
            binding.tvTopicNews.text = getString(R.string.top)
            binding.root.clickWithDebounce {
                onClickListener!!.onClickNews(item, position)
            }

        }

    }

    interface OnClickListener {
        fun onClickNews(news: News, position: Int)
    }

}