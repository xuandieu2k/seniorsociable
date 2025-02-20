package vn.xdeuhug.seniorsociable.other

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemSpacingDecoration(private val verticalSpacing: Int, private val horizontalSpacing: Int) :
    RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = verticalSpacing / 2
        outRect.bottom = verticalSpacing / 2
        outRect.left = horizontalSpacing
        outRect.right = horizontalSpacing
    }
}
