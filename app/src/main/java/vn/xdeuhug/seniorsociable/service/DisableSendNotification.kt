package vn.xdeuhug.seniorsociable.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.database.NotificationManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 25 / 12 / 2023
 */
@Suppress("DEPRECATION")
class DisableSendNotification : JobIntentService() {

    companion object {
        private const val JOB_ID = 1001

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, DisableSendNotification::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        Timber.d("NotificationDebug --- Intent data: ${intent.extras}")
        val notification = Gson().fromJson(
            intent.getStringExtra(AppConstants.OBJECT_NOTIFICATION), Notification::class.java
        )
        Timber.d("NotificationDebug --- Received Notification ID: ${notification.id}")
        // Lắng nghe call
        if (UserCache.getUser().id != "") {
            runBlocking {
                NotificationManagerFSDB.updateFirstSendNotification(
                    notification.id, UserCache.getUser().id
                )
            }
        }
    }
}