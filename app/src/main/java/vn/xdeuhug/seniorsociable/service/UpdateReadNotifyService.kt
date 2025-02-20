package vn.xdeuhug.seniorsociable.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.toast
import timber.log.Timber
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.database.NotificationManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 16 / 12 / 2023
 */
@Suppress("DEPRECATION")
class UpdateReadNotifyService : JobIntentService() {

    companion object {
        private const val JOB_ID = 1001

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, UpdateReadNotifyService::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        // Lắng nghe call
        if (UserCache.getUser().id != "") {
            val notification = Gson().fromJson(
                intent.getStringExtra(AppConstants.OBJECT_NOTIFICATION), Notification::class.java
            )
            NotificationManagerFSDB.updateReadNotification(userId = UserCache.getUser().id,
                notification.id,
                object : NotificationManagerFSDB.Companion.FireStoreCallback<Boolean> {
                    override fun onSuccess(result: Boolean) {
                        if (result) {
                            Timber.tag("Update notification").i("Done")
                        } else {
                            Timber.tag("Update notification").i("Failure")
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        Timber.tag("Update notification").i("Failure: $exception")
                    }

                })
        }
    }
}