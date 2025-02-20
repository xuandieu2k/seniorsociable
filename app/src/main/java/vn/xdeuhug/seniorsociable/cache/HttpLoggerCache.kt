package vn.xdeuhug.seniorsociable.cache

import com.tencent.mmkv.MMKV
import vn.xdeuhug.seniorsociable.constants.AppConstants


/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 05/08/2023
 */
object HttpLoggerCache {

    private var logEnable = false

    private val mmkv: MMKV = MMKV.mmkvWithID("caches_http_log_senior_sociable_app")

    fun getHttpLogEnable(): Boolean {
        try {
            logEnable = mmkv.getBoolean(AppConstants.CACHE_HTTP_LOG, false)
        } catch (e: Exception) {
            e.stackTrace
        }
        return logEnable
    }

    fun saveHttpLogEnable(enable: Boolean) {
        try {
            mmkv.remove(AppConstants.CACHE_HTTP_LOG)
            mmkv.putBoolean(AppConstants.CACHE_HTTP_LOG, enable).commit()
        } catch (e: Exception) {
            e.stackTrace
        }
    }
}
