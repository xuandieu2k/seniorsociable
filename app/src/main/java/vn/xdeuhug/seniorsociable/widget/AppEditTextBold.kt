package vn.xdeuhug.seniorsociable.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import vn.xdeuhug.widget.view.RegexEditText
import vn.xdeuhug.seniorsociable.R

/**
 * @Author: Bùi Hửu Thắng
 * @Date: 28/09/2022
 */
class AppEditTextBold : RegexEditText {
    private var typeFont = ResourcesCompat.getFont(context, R.font.roboto_medium)

    constructor(context: Context?) : super(context!!) {
        setFonts()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        setFonts()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        setFonts()
    }

    private fun setFonts() {
        typeface = typeFont
    }
}