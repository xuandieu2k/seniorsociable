package vn.xdeuhug.seniorsociable.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import vn.xdeuhug.seniorsociable.R

/**
 * @Author: Bùi Hửu Thắng
 * @Date: 28/09/2022
 */
open class AppButton : AppCompatButton {
    var typeFont = ResourcesCompat.getFont(context, R.font.roboto_regular)

    constructor(context: Context?) : super(context!!) {
        typeface = typeFont
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        typeface = typeFont
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        typeface = typeFont
    }
}