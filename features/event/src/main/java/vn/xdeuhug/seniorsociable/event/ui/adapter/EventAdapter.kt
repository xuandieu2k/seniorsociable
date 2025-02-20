package vn.xdeuhug.seniorsociable.event.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.event.databinding.ItemEventsBinding
import vn.xdeuhug.seniorsociable.model.entity.modelEvent.Event
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.News
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 12 / 2023
 */
class EventAdapter(context: Context) : AppAdapter<Event>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemEventsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    var onClickListener: OnClickListener? = null

    inner class ViewHolder(private val binding: ItemEventsBinding) : AppViewHolder(binding.root) {
        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION)
                {
                    onClickListener!!.onClickEvent(getItem(position),position)
                }
            }
        }
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            //
            val item = getItem(position)
            setDataForView(item)

        }

        private fun setDataForView(item: Event) {
            PhotoShowUtils.loadPosterImageCenterCrop(
                UploadFireStorageUtils.getRootURLEventById(
                    item.id
                ), item.poster, binding.imvBg
            )
            setStatus(item.timeStart)
            binding.tvContent.text = item.content
            binding.tvMember.text = AppUtils.fromHtml("<b>Số lượng:</b> ${item.membersJoin.size}")
            binding.tvTimeStart.text = AppUtils.fromHtml("${DateUtils.getDateByFormatTimeDate(item.timeStart)} <b>&ensp;-&ensp;</b> ${DateUtils.getDateByFormatTimeDate(item.timeEnd)}")
        }

        private fun setStatus(timeStart: Date) {
            val today = Date()
            if(today.time < timeStart.time)
            {
                binding.tvStatus.text = AppUtils.fromHtml("<font color='#2F9672'>${getString(R.string.coming_soon)}</font>")
                return
            }
            if(today.time > timeStart.time)
            {
                binding.tvStatus.text = AppUtils.fromHtml("<font color='#E96012'>${getString(R.string.finish)}</font>")
                return
            }

            if(today.time == timeStart.time)
            {
                binding.tvStatus.text = AppUtils.fromHtml("<font color='#1462B0'>${getString(R.string.starting)}</font>")
                return
            }

        }

    }

    interface OnClickListener {
        fun onClickEvent(event: Event, position: Int)
    }
}