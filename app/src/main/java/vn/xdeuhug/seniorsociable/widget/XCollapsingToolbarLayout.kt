package vn.xdeuhug.seniorsociable.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.CollapsingToolbarLayout

/**
 * @Author: Bùi Hửu Thắng
 * @Date: 28/09/2022
 */
class XCollapsingToolbarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) :
    CollapsingToolbarLayout(context, attrs, defStyleAttr) {

    /** 渐变监听 */
    private var listener: OnScrimsListener? = null

    /** 当前渐变状态 */
    private var scrimsShownStatus: Boolean = false

    override fun setScrimsShown(shown: Boolean, animate: Boolean) {
        super.setScrimsShown(shown, true)
        // 判断渐变状态是否改变了
        if (scrimsShownStatus == shown) {
            return
        }
        // 如果是就记录并且回调监听器
        scrimsShownStatus = shown
        listener?.onScrimsStateChange(this, scrimsShownStatus)
    }

    /**
     * 获取当前的渐变状态
     */
    fun isScrimsShown(): Boolean {
        return scrimsShownStatus
    }

    /**
     * 设置CollapsingToolbarLayout渐变监听
     */
    fun setOnScrimsListener(listener: OnScrimsListener?) {
        this.listener = listener
    }

    /**
     * CollapsingToolbarLayout渐变监听器
     */
    interface OnScrimsListener {

        /**
         * 渐变状态变化
         *
         * @param shown         渐变开关
         */
        fun onScrimsStateChange(layout: XCollapsingToolbarLayout?, shown: Boolean)
    }
}