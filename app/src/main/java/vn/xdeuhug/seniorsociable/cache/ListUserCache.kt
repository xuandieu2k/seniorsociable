package vn.xdeuhug.seniorsociable.cache

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 02 / 12 / 2023
 */
object ListUserCache {
    private var mListUser = ArrayList<User>()

    private val mmkv: MMKV = MMKV.mmkvWithID("caches_list_user_senior_sociable_app")

    fun getList(): ArrayList<User> {
        try {
            val jsonStr = mmkv.getString(AppConstants.CACHE_USER_LIST, "")
            if (!jsonStr.isNullOrEmpty()) {
                val listType = object : TypeToken<ArrayList<User>>() {}.type
                mListUser = Gson().fromJson(jsonStr, listType)
            }
        } catch (e: Exception) {
            e.stackTrace
        }
        return mListUser
    }

    fun saveList(userList: ArrayList<User>) {
        try {
            mmkv.remove(AppConstants.CACHE_USER_LIST)
            mmkv.putString(AppConstants.CACHE_USER_LIST, Gson().toJson(userList)).commit()
        } catch (e: Exception) {
            e.stackTrace
        }
    }
}