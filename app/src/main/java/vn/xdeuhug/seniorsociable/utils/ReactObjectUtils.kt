package vn.xdeuhug.seniorsociable.utils


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.constants.ReactConstants
import vn.xdeuhug.seniorsociable.widget.reactbutton.Reaction


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 26 / 10 / 2023
 */
object ReactObjectUtils {

    /**
     *  default of React
     */
    var defaultReact = Reaction(
        ReactConstants.LIKE, ReactConstants.DEFAULT, ReactConstants.GRAY, R.drawable.ic_like_news
    )

    /**
     *  default of React In Watch
     */
    var defaultReactVideo = Reaction(
        ReactConstants.LIKE, ReactConstants.DEFAULT, ReactConstants.WHITE, R.drawable.ic_like_news_white
    )

    /**
     *  default of React
     */
    var defaultReactNoIcon = Reaction(
        ReactConstants.LIKE, ReactConstants.DEFAULT, ReactConstants.GRAY, 0
    )


    /**
     *  List react
     */
    var reactionsNoIcon = arrayOf(
        Reaction(ReactConstants.LIKE, ReactConstants.BLUE, 0),
        Reaction(ReactConstants.LOVE, ReactConstants.RED_LOVE, 0),
        Reaction(ReactConstants.SMILE, ReactConstants.YELLOW_HAHA, 0),
        Reaction(ReactConstants.WOW, ReactConstants.YELLOW_HAHA, 0),
        Reaction(ReactConstants.SAD, ReactConstants.YELLOW_HAHA, 0),
        Reaction(ReactConstants.ANGRY, ReactConstants.RED_ANGRY, 0)
    )

    /**
     *  List react
     */
    var reactions = arrayOf(
        Reaction(ReactConstants.LIKE, ReactConstants.BLUE, R.drawable.ic_like_senior_new),
        Reaction(ReactConstants.LOVE, ReactConstants.RED_LOVE, R.drawable.ic_heart),
        Reaction(ReactConstants.SMILE, ReactConstants.YELLOW_HAHA, R.drawable.ic_laugh),
        Reaction(ReactConstants.WOW, ReactConstants.YELLOW_HAHA, R.drawable.ic_wow),
        Reaction(ReactConstants.SAD, ReactConstants.YELLOW_HAHA, R.drawable.ic_sad),
        Reaction(ReactConstants.ANGRY, ReactConstants.RED_ANGRY, R.drawable.ic_angry)
    )

    /**
     *  @param Type is type post
     */
    fun getReactionsWithType(type: Int): Reaction {
        when (type) {
            PostConstants.INTERACT_LIKE -> {
                return Reaction(
                    ReactConstants.LIKE, ReactConstants.BLUE, R.drawable.ic_like_senior_new
                )
            }

            PostConstants.INTERACT_LOVE -> {
                return Reaction(ReactConstants.LOVE, ReactConstants.RED_LOVE, R.drawable.ic_heart)
            }

            PostConstants.INTERACT_SMILE -> {
                return Reaction(
                    ReactConstants.SMILE, ReactConstants.YELLOW_HAHA, R.drawable.ic_laugh
                )
            }

            PostConstants.INTERACT_WOW -> {
                return Reaction(ReactConstants.WOW, ReactConstants.YELLOW_HAHA, R.drawable.ic_wow)
            }

            PostConstants.INTERACT_SAD -> {
                return Reaction(ReactConstants.SAD, ReactConstants.YELLOW_HAHA, R.drawable.ic_sad)
            }

            PostConstants.INTERACT_ANGRY -> {
                return Reaction(ReactConstants.ANGRY, ReactConstants.RED_ANGRY, R.drawable.ic_angry)
            }

            else -> return Reaction(
                ReactConstants.LIKE, ReactConstants.BLUE, R.drawable.ic_like_senior_new
            )
        }
    }


    fun getTypeByReact(reaction: Reaction): Int {
        when {
            reaction.reactTextColor == ReactConstants.BLUE && reaction.reactIconId == R.drawable.ic_like -> {
                return PostConstants.INTERACT_LIKE
            }

            reaction.reactTextColor == ReactConstants.RED_LOVE && reaction.reactIconId == R.drawable.ic_heart -> {
                return PostConstants.INTERACT_LOVE
            }

            reaction.reactTextColor == ReactConstants.YELLOW_HAHA && reaction.reactIconId == R.drawable.ic_laugh -> {
                return PostConstants.INTERACT_SMILE
            }

            reaction.reactTextColor == ReactConstants.YELLOW_HAHA && reaction.reactIconId == R.drawable.ic_wow -> {
                return PostConstants.INTERACT_WOW
            }

            reaction.reactTextColor == ReactConstants.YELLOW_HAHA && reaction.reactIconId == R.drawable.ic_sad -> {
                return PostConstants.INTERACT_SAD
            }

            reaction.reactTextColor == ReactConstants.RED_ANGRY && reaction.reactIconId == R.drawable.ic_angry -> {
                return PostConstants.INTERACT_ANGRY
            }

            else -> return PostConstants.INTERACT_LIKE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getDrawableWithType(type: Int, context: Context): Drawable? {
        when (type) {
            PostConstants.INTERACT_LIKE -> {
                return context.getDrawable(R.drawable.ic_like_senior_new)
            }

            PostConstants.INTERACT_LOVE -> {
                return context.getDrawable(R.drawable.ic_heart)
            }

            PostConstants.INTERACT_SMILE -> {
                return context.getDrawable(R.drawable.ic_laugh)
            }

            PostConstants.INTERACT_WOW -> {
                return context.getDrawable(R.drawable.ic_wow)
            }

            PostConstants.INTERACT_SAD -> {
                return context.getDrawable(R.drawable.ic_sad)
            }

            PostConstants.INTERACT_ANGRY -> {
                return context.getDrawable(R.drawable.ic_angry)
            }

            else -> return context.getDrawable(R.drawable.ic_like_senior_new)
        }
    }

}