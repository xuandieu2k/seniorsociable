package vn.xdeuhug.seniorsociable.admin.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import org.jetbrains.anko.textColor
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.admin.databinding.ItemAccountBinding
import vn.xdeuhug.seniorsociable.admin.databinding.ItemReportDetailsBinding
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelReport.Report
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.other.handlerPostDelayed
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 03 / 01 / 2024
 */
class ReportDetailsAdapter(context: Context) : AppAdapter<Report>(context) {
    private var listUser = ListUserCache.getList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemReportDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemReportDetailsBinding) :
        AppViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            val userRP = listUser.first { it.id == item.idUser }
            PhotoShowUtils.loadAvatarImage("",userRP.avatar,binding.imvAvatar)
            binding.tvUsername.text = userRP.name
            binding.tvContentReason.text = item.contentReport
            binding.tvNameReport.text = item.reason!!.name
        }

        private fun setReasonTitle(item: Report) {
            //
        }
    }
}