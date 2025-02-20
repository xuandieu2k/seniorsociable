package vn.xdeuhug.seniorsociable.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import vn.xdeuhug.seniorsociable.R

/**
 * @Author: Bùi Hửu Thắng
 * @Date: 28/09/2022
 */
class AppTextViewBold : AppCompatTextView {
    var typeFont = ResourcesCompat.getFont(context, R.font.roboto_medium)

    constructor(context: Context?) : super(context!!) {
        setFontsTextView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        setFontsTextView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        setFontsTextView()
    }

    private fun setFontsTextView() {
        typeface = typeFont
    }
}