package vn.xdeuhug.seniorsociable.cache

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelSearch.SearchData

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 26 / 12 / 2023
 */
object ListSearchDataCache {
    private var mListSearchData = ArrayList<SearchData>()

    private val mmkv: MMKV = MMKV.mmkvWithID("caches_list_search_data_senior_sociable_app")

    fun getList(): ArrayList<SearchData> {
        try {
            val jsonStr = mmkv.getString(AppConstants.CACHE_SEARCH_DATA_LIST, "")
            if (!jsonStr.isNullOrEmpty()) {
                val listType = object : TypeToken<ArrayList<SearchData>>() {}.type
                mListSearchData = Gson().fromJson(jsonStr, listType)
            }
        } catch (e: Exception) {
            e.stackTrace
        }
        return mListSearchData
    }

    fun saveList(listSearch: ArrayList<SearchData>) {
        try {
            mmkv.remove(AppConstants.CACHE_SEARCH_DATA_LIST)
            mmkv.putString(AppConstants.CACHE_SEARCH_DATA_LIST, Gson().toJson(listSearch)).commit()
        } catch (e: Exception) {
            e.stackTrace
        }
    }
}