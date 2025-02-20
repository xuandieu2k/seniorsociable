package vn.xdeuhug.seniorsociable.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author: NGUYEN THE DAT
 * @Date: 3/20/2023
 */
class CustomRecyclerView : RecyclerView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var heightSpec = heightSpec
        heightSpec = MeasureSpec.makeMeasureSpec(300, MeasureSpec.AT_MOST)
        super.onMeasure(widthSpec, heightSpec)
    }
}