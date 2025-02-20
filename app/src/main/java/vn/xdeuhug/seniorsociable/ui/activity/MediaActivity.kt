package vn.xdeuhug.seniorsociable.ui.activity

import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zeuskartik.mediaslider.MediaSliderActivity
import vn.xdeuhug.seniorsociable.constants.AppConstants
import java.lang.reflect.Type

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 03/10/2022
 */
class MediaActivity : MediaSliderActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent: Intent = intent
        val position = intent.getIntExtra(AppConstants.MEDIA_POSITION, 0)
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<String>>() {}.type
        val listMedia: ArrayList<String> = gson.fromJson(
            intent.getStringExtra(AppConstants.MEDIA_DATA),
            type
        )
        var listType = ArrayList<String>()
        if (intent.extras!!.containsKey(AppConstants.MEDIA_DATA_TYPE))
            listType = gson.fromJson(
                intent.getStringExtra(AppConstants.MEDIA_DATA_TYPE),
                type
            )
        if (listType.size == 0)
            loadMediaSliderView(
                listMedia,
                true,
                position
            )
        else loadMediaSliderView(
            listMedia, listType,
            true,
            position
        )

    }
}