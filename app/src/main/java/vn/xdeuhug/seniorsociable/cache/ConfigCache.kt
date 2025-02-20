package vn.xdeuhug.seniorsociable.cache

import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.Config

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 03/10/2022
 */
object ConfigCache {

    var mConfig: Config = Config()

    private val mmkv: MMKV = MMKV.mmkvWithID("cache_config_senior_sociable_app")

    fun saveConfig(config: Config?) {
        try {
            mmkv.remove(AppConstants.CACHE_CONFIG)
            mmkv.putString(AppConstants.CACHE_CONFIG, Gson().toJson(config)).commit()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getConfig(): Config {
        try {
            mConfig =
                Gson().fromJson(mmkv.getString(AppConstants.CACHE_CONFIG, ""), Config::class.java)
        } catch (e: Exception) {
            e.stackTrace
        }
        return mConfig
    }
    fun getSessionToken(): String {
        val config = getConfig()
        return "${config.type} ${config.sessionToken}:${config.apiKey}"
    }
}