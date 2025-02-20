@file:Suppress("DEPRECATION")

package vn.xdeuhug.seniorsociable.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 14 / 12 / 2023
 */
class UpdateOnlineService : JobIntentService() {

    companion object {
        private const val JOB_ID = 1001

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, CallRoomJobIntentService::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        // Lắng nghe call
        if (UserCache.getUser().id != "") {
            runBlocking {
                Timber.tag("Update Online").i("Oke")
                UserManagerFSDB.updateUserOnlineAsyns(UserCache.getUser().id,
                    false,
                    Date())
            }
        }
    }
}