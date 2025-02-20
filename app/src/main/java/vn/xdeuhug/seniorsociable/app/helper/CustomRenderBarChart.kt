package vn.xdeuhug.seniorsociable.app.helper

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.highlight.Range
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler


/**
 * @Author: 阮仲伦 Nguyễn Trọng Luân
 * @Date: 12/26/22
 */
class CustomRenderBarChart(
    chart: BarDataProvider?,
    animator: ChartAnimator?,
    viewPortHandler: ViewPortHandler?
) :
    BarChartRenderer(chart, animator, viewPortHandler) {
    private var mRadius = 16f
    fun setmRadius(mRadius: Float) {
        this.mRadius = mRadius
    }

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)
        mShadowPaint.color = dataSet.barShadowColor
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY


        if (mBarBuffers != null) {
            // initialize the buffer
            val buffer = mBarBuffers[index]
            buffer.setPhases(phaseX, phaseY)
            buffer.setDataSet(index)
            buffer.setBarWidth(mChart.barData.barWidth)
            buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
            buffer.feed(dataSet)
            trans.pointValuesToPixel(buffer.buffer)

            // if multiple colors
            if (dataSet.colors.size > 1) {
                var j = 0

                while (j < buffer.size()) {

                    if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                        j += 4
                        continue
                    }
                    if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break
                    if (mChart.isDrawBarShadowEnabled) {
                        if (mRadius > 0) c.drawRoundRect(
                            RectF(
                                buffer.buffer[j], mViewPortHandler.contentTop(),
                                buffer.buffer[j + 2], mViewPortHandler.contentBottom()
                            ), mRadius, mRadius, mShadowPaint
                        ) else c.drawRect(
                            buffer.buffer[j], mViewPortHandler.contentTop(),
                            buffer.buffer[j + 2], mViewPortHandler.contentBottom(), mShadowPaint
                        )
                    }

                    // Set the color for the currently drawn value. If the index
                    // is
                    // out of bounds, reuse colors.
                    mRenderPaint.color = dataSet.getColor(j / 4)
                    if (mRadius > 0) {
                        for (i in 0 until dataSet.entryCount) {
                            val e = dataSet.getEntryForIndex(i) ?: continue
                            e.x
                            val y = e.y
                            val path: Path = if (y < 0)
                                roundedRect(
                                    buffer.buffer[j],
                                    buffer.buffer[j + 1],
                                    buffer.buffer[j + 2],
                                    buffer.buffer[j + 3], mRadius, mRadius,
                                    tl = false,
                                    tr = false,
                                    br = true,
                                    bl = true
                                )
                            else
                                roundedRect(
                                    buffer.buffer[j],
                                    buffer.buffer[j + 1],
                                    buffer.buffer[j + 2],
                                    buffer.buffer[j + 3], mRadius, mRadius,
                                    tl = true,
                                    tr = true,
                                    br = false,
                                    bl = false
                                )
                            c.drawPath(path, mRenderPaint)
                        }

                    } else c.drawRect(
                        buffer.buffer[j],
                        buffer.buffer[j + 1],
                        buffer.buffer[j + 2], buffer.buffer[j + 3], mRenderPaint
                    )
                    j += 4
                }
            } else {
                mRenderPaint.color = dataSet.color
                var j = 0

                while (j < buffer.size()) {
                    if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                        j += 4
                        continue
                    }
                    if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break
                    if (mChart.isDrawBarShadowEnabled) {
                        if (mRadius > 0) c.drawRoundRect(
                            RectF(
                                buffer.buffer[j], mViewPortHandler.contentTop(),
                                buffer.buffer[j + 2],
                                mViewPortHandler.contentBottom()
                            ), mRadius, mRadius, mShadowPaint
                        ) else c.drawRect(
                            buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                            buffer.buffer[j + 3], mRenderPaint
                        )
                    }
                    if (mRadius > 0) {
                        for (i in 0 until dataSet.entryCount) {
                            val e = dataSet.getEntryForIndex(i) ?: continue
                            e.x
                            val y = e.y
                            val path: Path
                            if (y < 0)
                                path = roundedRect(
                                    buffer.buffer[j],
                                    buffer.buffer[j + 1],
                                    buffer.buffer[j + 2],
                                    buffer.buffer[j + 3], mRadius, mRadius,
                                    tl = false,
                                    tr = false,
                                    br = true,
                                    bl = true
                                )
                            else
                                path = roundedRect(
                                    buffer.buffer[j],
                                    buffer.buffer[j + 1],
                                    buffer.buffer[j + 2],
                                    buffer.buffer[j + 3], mRadius, mRadius,
                                    tl = true,
                                    tr = true,
                                    br = false,
                                    bl = false
                                )
                            c.drawPath(path, mRenderPaint)
                        }


                    } else c.drawRect(
                        buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], mRenderPaint
                    )
                    j += 4
                }
            }
        }
    }

    override fun drawHighlighted(c: Canvas, indices: Array<Highlight>) {
        val barData = mChart.barData
        for (high in indices) {
            val set = barData.getDataSetByIndex(high.dataSetIndex)
            if (set == null || !set.isHighlightEnabled) {
                continue
            }
            val e = set.getEntryForXValue(high.x, high.y)
            if (!isInBoundsX(e, set)) {
                continue
            }
            val trans: Transformer = mChart.getTransformer(set.axisDependency)
            mHighlightPaint.color = set.highLightColor
            mHighlightPaint.alpha = set.highLightAlpha
            val isStack = high.stackIndex >= 0 && e.isStacked
            val y1: Float
            val y2: Float
            if (isStack) {
                if (mChart.isHighlightFullBarEnabled) {
                    y1 = e.positiveSum
                    y2 = -e.negativeSum
                } else {
                    val range: Range = e.ranges[high.stackIndex]
                    y1 = range.from
                    y2 = range.to
                }
            } else {
                y1 = e.y
                y2 = 0f
            }

            prepareBarHighlight(e.x, y1, y2, barData.barWidth / 2f, trans)
            setHighlightDrawPos(high, mBarRect)
            val path2: Path = if (y1 > 0)
                roundedRect(
                    mBarRect.left, mBarRect.top, mBarRect.right,
                    mBarRect.bottom, mRadius, mRadius, tl = true, tr = true, br = false, bl = false
                )
            else {
                roundedRect(
                    mBarRect.left, mBarRect.top, mBarRect.right,
                    mBarRect.bottom, mRadius, mRadius, tl = false, tr = false, br = true, bl = true
                )
            }
            c.drawPath(path2, mHighlightPaint)
        }
    }

    companion object {
        fun roundedRect(
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            rx: Float,
            ry: Float,
            tl: Boolean,
            tr: Boolean,
            br: Boolean,
            bl: Boolean
        ): Path {
            var radiusX = rx
            var radiusY = ry

            val path = Path()

            if (radiusX < 0) radiusX = 0f
            if (radiusY < 0) radiusY = 0f

            val width = right - left
            val height = bottom - top

            if (radiusX > width / 2) radiusX = width / 2
            if (radiusY > height / 2) radiusY = height / 2

            val widthMinusCorners = width - 2 * radiusX
            val heightMinusCorners = height - 2 * radiusY

            path.moveTo(right, top + radiusY)

            if (tr) {
                path.rQuadTo(0f, -radiusY, -radiusX, -radiusY) //top-right corner
            } else {
                path.rLineTo(0f, -radiusY)
                path.rLineTo(-radiusX, 0f)
            }

            path.rLineTo(-widthMinusCorners, 0f)

            if (tl) {
                path.rQuadTo(-radiusX, 0f, -radiusX, radiusY) //top-left corner
            } else {
                path.rLineTo(-radiusX, 0f)
                path.rLineTo(0f, radiusY)
            }

            path.rLineTo(0f, heightMinusCorners)

            if (bl) {
                path.rQuadTo(0f, radiusY, radiusX, radiusY) //bottom-left corner
            } else {
                path.rLineTo(0f, radiusY)
                path.rLineTo(radiusX, 0f)
            }

            path.rLineTo(widthMinusCorners, 0f)

            if (br) {
                path.rQuadTo(radiusX, 0f, radiusX, -radiusY) //bottom-right corner
            } else {
                path.rLineTo(radiusX, 0f)
                path.rLineTo(0f, -radiusY)
            }

            path.rLineTo(0f, -heightMinusCorners)
            path.close() //Given close, last lineto can be removed.

            return path
        }
    }
}