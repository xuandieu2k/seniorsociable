package vn.xdeuhug.seniorsociable.cache

import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.Restaurants

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 26/7/2023
 */
object RestaurantCache {

    private var mBranch = Restaurants()

    private val mmkv: MMKV = MMKV.mmkvWithID("caches_restaurant_senior_sociable_app")

    fun getRestaurant(): Restaurants {
        try {
            mBranch =
                Gson().fromJson(mmkv.getString(AppConstants.CACHE_RESTAURANT, ""), Restaurants::class.java)
        } catch (e: Exception) {
            e.stackTrace
        }
        return mBranch
    }

    fun saveRestaurant(restaurant: Restaurants) {
        try {
            mmkv.remove(AppConstants.CACHE_RESTAURANT)
            mmkv.putString(AppConstants.CACHE_RESTAURANT, Gson().toJson(restaurant)).commit()
        } catch (e: Exception) {
            e.stackTrace
        }
    }
}
