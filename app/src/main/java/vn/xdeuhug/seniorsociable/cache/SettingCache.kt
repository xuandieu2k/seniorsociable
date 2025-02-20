package vn.xdeuhug.seniorsociable.cache

import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.Setting

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 12/15/2022
 */
object SettingCache {

    private var mSetting = Setting()

    private val mmkv: MMKV = MMKV.mmkvWithID("caches_setting_senior_sociable_app")

    fun getSetting(): Setting {
        try {
            mSetting =
                Gson().fromJson(mmkv.getString(AppConstants.CACHE_SETTING, ""), Setting::class.java)
        } catch (e: Exception) {
            e.stackTrace
        }
        return mSetting
    }

    fun saveSetting(setting: Setting) {
        try {
            mmkv.remove(AppConstants.CACHE_SETTING)
            mmkv.putString(AppConstants.CACHE_SETTING, Gson().toJson(setting)).commit()
        } catch (e: Exception) {
            e.stackTrace
        }
    }
}
