package vn.xdeuhug.seniorsociable.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.dimen
import vn.xdeuhug.seniorsociable.R

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 21 / 10 / 2023
 */
object GalleryViewAdapterUtils {
    const val ONE = 1
    const val TWO = 2
    const val THREE = 3
    const val FOUR = 4
    fun setGallery(sumGallery: Int, position: Int, view: View,context:Context) {
        when (sumGallery) {
            ONE -> {
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            TWO -> {
                when(position)
                {
                    ONE,TWO ->{
                    view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    }
                }
            }

            THREE -> {
                when(position)
                {
                    ONE ->{
                        view.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    TWO, THREE ->{
                        view.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, context.dimen(R.dimen.dp_160)
                        )
                    }

                }
            }

            FOUR -> {
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, context.dimen(R.dimen.dp_160)
                )
            }
        }
    }
}