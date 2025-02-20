package vn.xdeuhug.seniorsociable.app.helper

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import vn.xdeuhug.seniorsociable.utils.AppUtils

open class MyAxisValueFormatter : ValueFormatter() {
    var s: String? = null

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return AppUtils.getDecimalFormattedString(value.toLong().toString())
    }

    override fun getFormattedValue(value: Float): String {
        return AppUtils.getDecimalFormattedString(value.toLong().toString())
    }
}