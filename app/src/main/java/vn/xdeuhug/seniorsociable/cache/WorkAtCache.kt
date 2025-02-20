package vn.xdeuhug.seniorsociable.cache

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 01 / 12 / 2023
 */
object WorkAtCache {

    private var mListWorkAtSearch = ArrayList<AutocompletePrediction>()

    private val mmkv: MMKV = MMKV.mmkvWithID("caches_list_work_at_senior_sociable_app")

    fun getList(): ArrayList<AutocompletePrediction> {
        try {
            val jsonStr = mmkv.getString(AppConstants.CACHE_WORK_AT, "")
            if (!jsonStr.isNullOrEmpty()) {
                val listType = object : TypeToken<ArrayList<AutocompletePrediction>>() {}.type
                mListWorkAtSearch = Gson().fromJson(jsonStr, listType)
            }
        } catch (e: Exception) {
            e.stackTrace
        }
        return mListWorkAtSearch
    }

    fun saveList(listWorkAtSearch: ArrayList<AutocompletePrediction>) {
        try {
            mmkv.remove(AppConstants.CACHE_WORK_AT)
            mmkv.putString(AppConstants.CACHE_WORK_AT, Gson().toJson(listWorkAtSearch)).commit()
        } catch (e: Exception) {
            e.stackTrace
        }
    }
}