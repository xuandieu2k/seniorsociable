package vn.xdeuhug.seniorsociable.other

import android.annotation.SuppressLint
import android.content.Context
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.model.entity.MarkerChart
import vn.xdeuhug.seniorsociable.utils.AppUtils

/**
 * @Author: NGUYEN THE DAT
 * @Date: 4/29/2023
 */
class CustomMarkerView(
    context: Context,
    layoutResource: Int

) :
    MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)
    private val tvContentTwo: TextView = findViewById(R.id.tvContentTwo)
    private val rlTag: RelativeLayout = findViewById(R.id.tvBgTag)

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun refreshContent(e: Entry, highlight: Highlight) {
        val obj = e.data as MarkerChart
        tvContent.text = "Số lượng: ${obj.amount}"
        tvContentTwo.text = "Tổng tiền: ${AppUtils.getMoneyFormatted(obj.total.toLong().toBigDecimal())}"
        // set the entry-value as the display text

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(
            -(width / 2).toFloat(),
            -height.toFloat()
        )
    }

    init {
        rlTag.background =
            ContextCompat.getDrawable(
                context, R.drawable.bg_blue_transparent
            )

    }
}