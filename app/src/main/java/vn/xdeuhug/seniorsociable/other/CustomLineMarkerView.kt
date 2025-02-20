package vn.xdeuhug.seniorsociable.other

import android.annotation.SuppressLint
import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.utils.AppUtils

/**
 * @Author: NGUYỄN XUÂN DIỆU
 * @Date: 04/29/2023
 */
class CustomLineMarkerView(
    context: Context,
    layoutResource: Int

) :
    MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)
    private val tvContentTwo: TextView = findViewById(R.id.tvContentTwo)
    private val rlTag: LinearLayout = findViewById(R.id.tvBgTag)

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun refreshContent(e: Entry, highlight: Highlight) {
        val obj = e.data as vn.xdeuhug.seniorsociable.model.chart.MarkerChart
        tvContent.text = "${context.getString(R.string.sum_of_post)}: ${AppUtils.getMoneyFormatted(obj.sumPost.toBigDecimal())}"
        tvContentTwo.text = "${context.getString(R.string.sum_of_user)}: ${AppUtils.getMoneyFormatted(obj.sumUser.toBigDecimal())}"
        // set the entry-value as the display text

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(
            -(width / 2).toFloat(),
            -height.toFloat()
        )
    }

    private val totalWidth = resources.displayMetrics.widthPixels

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        val supposedX = posX + width
        val mpPointF = MPPointF()

        mpPointF.x = when {
            supposedX > totalWidth -> -width.toFloat()
            posX - width < 0 -> 0f
            else -> -(width / 2).toFloat()
        }

        mpPointF.y = if (posY > height) -height.toFloat()
        else 0f
        return mpPointF
    }

    init {
        rlTag.background =
            ContextCompat.getDrawable(
                context, R.drawable.bg_blue_transparent
            )

    }
}