package vn.xdeuhug.seniorsociable.utility.cache

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utility.model.Diseases

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 12 / 2023
 */
object DiseaseCache {
    private var mListDiseases = ArrayList<Diseases>()

    private val mmkv: MMKV = MMKV.mmkvWithID("caches_list_diseases_senior_sociable_app")

    fun getList(): ArrayList<Diseases> {
        try {
            val jsonStr = mmkv.getString(AppConstants.CACHE_DISEASES_LIST, "")
            if (!jsonStr.isNullOrEmpty()) {
                val listType = object : TypeToken<ArrayList<Diseases>>() {}.type
                mListDiseases = Gson().fromJson(jsonStr, listType)
            }
        } catch (e: Exception) {
            e.stackTrace
        }
        return mListDiseases
    }

    fun saveList(diseasesList: ArrayList<Diseases>) {
        try {
            mmkv.remove(AppConstants.CACHE_DISEASES_LIST)
            mmkv.putString(AppConstants.CACHE_DISEASES_LIST, Gson().toJson(diseasesList)).commit()
        } catch (e: Exception) {
            e.stackTrace
        }
    }
}