package vn.xdeuhug.seniorsociable.admin.ui.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import vn.xdeuhug.seemt.app.helper.MyValueFormatter
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.admin.databinding.ActivityStatisticalBinding
import vn.xdeuhug.seniorsociable.model.entity.modelReport.ReportPostAndUser
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.app.helper.MyAxisValueFormatter
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.database.ReportManagerFSDB
import vn.xdeuhug.seniorsociable.model.chart.MarkerChart
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.Topic
import vn.xdeuhug.seniorsociable.other.CustomLineMarkerView
import vn.xdeuhug.seniorsociable.ui.adapter.TopicAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.TypeFaceUtils
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 01 / 01 / 2024
 */
class StatisticalActivity : AppActivity(), TopicAdapter.OnListenerSelected {
    private lateinit var binding: ActivityStatisticalBinding

    // Set data for Filter topic
    private lateinit var topicAdapter: TopicAdapter
    private var listTopic = ArrayList<Topic>()
    override fun getLayoutView(): View {
        binding = ActivityStatisticalBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setDataTopic()
        getDataReport()
    }

    override fun initData() {
        //
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataTopic() {
        val array = arrayListOf(
            Topic(AppConstants.REPORT_TODAY.toString(), getString(R.string.today), true),
            Topic(AppConstants.REPORT_YESTERDAY.toString(), getString(R.string.yesterday), false),
            Topic(AppConstants.REPORT_THIS_WEEK.toString(), getString(R.string.this_week), false),
            Topic(AppConstants.REPORT_THIS_MONTH.toString(), getString(R.string.this_month), false),
            Topic(AppConstants.REPORT_LAST_MONTH.toString(), getString(R.string.last_month), false),
            Topic(AppConstants.REPORT_THIS_YEAR.toString(), getString(R.string.this_year), false),
            Topic(AppConstants.REPORT_LAST_YEAR.toString(), getString(R.string.last_year), false),
            Topic(
                AppConstants.REPORT_THREE_YEARS.toString(),
                getString(R.string.three_years),
                false
            ),
        )
        listTopic.addAll(array)
        topicAdapter = TopicAdapter(this)
        topicAdapter.onListenerSelected = this
        // Create recycleView
        AppUtils.initRecyclerViewHorizontal(binding.itemChart.rvTopic, topicAdapter)
        topicAdapter.setData(listTopic)
        topicAdapter.notifyDataSetChanged()
    }

    private fun setDataReport(
        data: ArrayList<ReportPostAndUser>, numMapQuarter: HashMap<Int, String>
    ) {
        initLineChart(binding.itemChart.lcReport, numMapQuarter)
        if (data.size < 1) binding.itemChart.lcReport.data = null
        else binding.itemChart.lcReport.data =
            generateLineData(data)

    }

    private fun generateLineData(data: List<ReportPostAndUser>): LineData {
        val line = ArrayList<ILineDataSet>()
        val entriesPost = ArrayList<Entry>()
        val entriesUser = ArrayList<Entry>()
        for (i in data.indices) {
            //
            val post = BarEntry(i.toFloat(), data[i].sumPost.toFloat())
            post.data = MarkerChart(
                data[i].sumPost,
                data[i].sumUser
            )
            entriesPost.add(post)
            //
            val user = BarEntry(i.toFloat(), data[i].sumUser.toFloat())
            user.data = MarkerChart(
                data[i].sumPost,
                data[i].sumUser
            )

            entriesUser.add(user)
        }
        val custom: ValueFormatter
        custom = MyValueFormatter()
        binding.itemChart.lcReport.axisLeft.valueFormatter = custom

        val linePost = LineDataSet(entriesPost, "")
        linePost.color =
            getColor(R.color.green_008)
        linePost.setCircleColor(getColor(R.color.green_008))
        linePost.lineWidth = 1f
        linePost.mode = LineDataSet.Mode.CUBIC_BEZIER
        linePost.circleRadius = 1.5f
        linePost.fillAlpha = 65
        linePost.fillColor = ColorTemplate.colorWithAlpha(
            getColor(R.color.green_008), 200
        )
        linePost.setDrawCircleHole(false)
        linePost.setDrawIcons(false)
        linePost.setDrawValues(false)
        linePost.setDrawFilled(false)

        val lineUser = LineDataSet(entriesUser, "")
        lineUser.color = getColor(R.color.red_600)
        lineUser.setCircleColor(getColor(R.color.red_600))
        lineUser.lineWidth = 1f
        lineUser.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineUser.circleRadius = 1.5f
        lineUser.fillAlpha = 65
        lineUser.fillColor = ColorTemplate.colorWithAlpha(
            getColor(R.color.red_600), 200
        )
        lineUser.setDrawCircleHole(false)
        lineUser.setDrawIcons(false)
        lineUser.setDrawValues(false)
        lineUser.setDrawFilled(false)
        // Nhớ thêm vô k quên
        line.add(linePost)
        line.add(lineUser)

        return LineData(line)
    }

    private fun initLineChart(chart: LineChart, numMapQuarter: HashMap<Int, String>) {

        chart.animate()
        chart.description.isEnabled = false
        // enable touch gestures
        chart.setTouchEnabled(true)
        chart.dragDecelerationFrictionCoef = 0.9f
        // enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        chart.isHighlightPerDragEnabled = true
        // if disabled, scaling can be done on x- and y-axis separately
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true)
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
        // set an alternative background color
        // set an alternative background color
        chart.setBackgroundColor(Color.WHITE)
        chart.isDoubleTapToZoomEnabled = true
        val custom: ValueFormatter = MyAxisValueFormatter()
        val leftAxis: YAxis = chart.axisLeft
        leftAxis.typeface = TypeFaceUtils.getRobotoRegularTypeface(this)
        leftAxis.setLabelCount(10, false)
        leftAxis.spaceTop = 15f
        leftAxis.valueFormatter = custom
        val xAxis: XAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.typeface = TypeFaceUtils.getRobotoRegularTypeface(this)
        xAxis.setDrawGridLines(true)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.labelCount = 7
        xAxis.textSize = 10f
        xAxis.labelRotationAngle = -60f
        if (numMapQuarter.size > 0) xAxis.valueFormatter = object : MyAxisValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {

                if (value.toInt() > -1 && value.toInt() < numMapQuarter.size) return numMapQuarter[value.toInt()].toString()
                return ""
            }
        }
        val customMarkerView = CustomLineMarkerView(
            this, R.layout.layout_line_marker
        )
        chart.marker = customMarkerView
        chart.animateY(500)
        chart.setNoDataTextColor(getColor(R.color.text_color_blue))
        chart.setNoDataText(getString(R.string.no_data))
        chart.setPinchZoom(true)
        chart.fitScreen()
    }

    private fun getDataReport() {
        val pairDate = getDate()
        ReportManagerFSDB.getReportPostAndUser(listTopic.first { it.isSelected }.id.toInt(),
            pairDate.first, pairDate.second,object :
                ReportManagerFSDB.Companion.FireStoreCallback<ArrayList<ReportPostAndUser>> {
                override fun onSuccess(result: ArrayList<ReportPostAndUser>) {
                    val numMapQuarter = HashMap<Int, String>()
                    result.mapIndexed { i, data ->
                        numMapQuarter[i] = data.timeReport
                    }
//                    result.mapIndexed { i, data ->
//                        numMapQuarter[i] = DateUtils.formatReportDateTime(
//                            getContext(), listTopic.first { it.isSelected }.id.toInt(), data.timeReport, i
//                        )
//                    }
                    if (result.isNotEmpty()) {
                        setDataReport(
                            result, numMapQuarter
                        )
                    } else {
                        binding.itemChart.lcReport.clear()
                        binding.itemChart.lcReport.setNoDataText(getString(R.string.no_data))

                    }
                }

                override fun onFailure(exception: Exception) {
                    exception.printStackTrace()
                    binding.itemChart.lcReport.clear()
                    binding.itemChart.lcReport.setNoDataText(getString(R.string.no_data))
                }

            })
    }

    private fun getDate(): Pair<Date, Date> {
        when (listTopic.first { it.isSelected }.id.toInt()) {
            AppConstants.REPORT_TODAY -> {
                return Pair(DateUtils.atStartOfDay(Date()), DateUtils.atEndOfDay(Date()))
            }

            AppConstants.REPORT_YESTERDAY -> {
                val yesterday = DateUtils.getYesterday()
                return Pair(DateUtils.atStartOfDay(yesterday), DateUtils.atEndOfDay(yesterday))
            }

            AppConstants.REPORT_THIS_WEEK -> {
                return Pair(DateUtils.getStartOfWeek(), DateUtils.getEndOfWeek())
            }

            AppConstants.REPORT_THIS_MONTH -> {
                val thisMonth = DateUtils.getDateRangeThisMonth()
                return Pair(thisMonth.first, thisMonth.second)
            }

            AppConstants.REPORT_LAST_MONTH -> {
                val lastMonth = DateUtils.getDateRangeLastMonth()
                return Pair(lastMonth.first, lastMonth.second)
            }

            AppConstants.REPORT_THIS_YEAR -> {
                return Pair(
                    DateUtils.getStartDateInYear(DateUtils.getThisYear()),
                    DateUtils.getEndDateInYear(DateUtils.getThisYear())
                )
            }

            AppConstants.REPORT_LAST_YEAR -> {
                return Pair(
                    DateUtils.getStartDateInYear(DateUtils.getLastYears()),
                    DateUtils.getEndDateInYear(DateUtils.getLastYears())
                )
            }

            AppConstants.REPORT_THREE_YEARS -> {
                val lastMonth = DateUtils.getDateRangeThreeMonthNearest()
                return Pair(lastMonth.first, lastMonth.second)
            }

            else -> {
                return Pair(Date(), Date())
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSelected(position: Int) {
        listTopic.forEachIndexed { _, item ->
            item.isSelected = false
        }
        listTopic[position].isSelected = true
        topicAdapter.notifyDataSetChanged()
        getDataReport()
    }
}