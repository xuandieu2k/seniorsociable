package vn.xdeuhug.seniorsociable.cache

import com.google.gson.Gson
import com.luck.picture.lib.utils.ToastUtils
import com.tencent.mmkv.MMKV
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 03/10/2022
 */
object UserCache {

    private var mUserInfo = User()

    private val mmkv: MMKV = MMKV.mmkvWithID("caches_user_senior_sociable_app")

    fun getUser(): User {
        try {
            mUserInfo =
                Gson().fromJson(mmkv.getString(AppConstants.CACHE_USER, ""), User::class.java)
        } catch (e: Exception) {
            e.stackTrace
        }
        return mUserInfo
    }

    fun saveUser(userInfo: User) {
        try {
            mmkv.remove(AppConstants.CACHE_USER)
            mmkv.putString(AppConstants.CACHE_USER, Gson().toJson(userInfo)).commit()
        } catch (e: Exception) {
            e.stackTrace
        }
    }
    fun isLogin(): Boolean {
        val mUserInfo = getUser()
        return mUserInfo.id.isNotEmpty()
    }


}
