package vn.xdeuhug.seniorsociable.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppApplication.Companion.EMPLOYEE_NOTIFICATION_CHANNEL_ID
import vn.xdeuhug.seniorsociable.app.AppApplication.Companion.notificationManager
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

class NotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val mmkv: MMKV = MMKV.mmkvWithID("push_token")
        mmkv.putString(AppConstants.PUSH_TOKEN, token).commit()
        Timber.d("push_token_Data %s", token)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("Notification data %s", Gson().toJson(message.data))
        val title = message.data["title"].toString()
        val fullName = message.data["title"].toString()
        val avatar = message.data["avatar"].toString()
        val body = message.data["content"].toString()
        val userId = message.data["user_id"].toString()
        val objectId = message.data["object_id"].toString()
        val notificationType = message.data["notification_type"].toString()
        val data = message.data["json_addition_data"].toString()


        sendNotification(
            title, fullName, avatar, body, objectId, notificationType, userId, data
        )
    }

    private fun sendNotification(
        title: String,
        fullName: String,
        avatar: String,
        body: String,
        objectId: String,
        type: String,
        userId: String,
        jsonAdditionData: String
    ) {
        val intent = Intent(this, HomeActivity::class.java)
        val bundle = Bundle()
        if (objectId == "") bundle.putString(AppConstants.OBJECT_ID, "0")
        else bundle.putString(AppConstants.OBJECT_ID, objectId)
        bundle.putString(AppConstants.NOTIFICATION_TYPE, type)
        bundle.putString(AppConstants.JSON_ADDITION_DATA, jsonAdditionData)
        bundle.putString(AppConstants.USER_ID, userId)
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val resultPendingIntent = PendingIntent.getActivity(
            this, Random().nextInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = if (avatar != "") NotificationCompat.Builder(
            this,
            EMPLOYEE_NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.logo_senior_sociable).setLargeIcon(
            PhotoShowUtils.getBitmap(
                PhotoShowUtils.getLinkPhoto(avatar), this
            )
        ).setColor(Color.parseColor("#2A74D9")).setShortcutId(
            PhotoShowUtils.getLinkPhoto(avatar)
        ).setContentTitle(title).setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setCategory(
                NotificationCompat.CATEGORY_MESSAGE
            ).setContentIntent(resultPendingIntent).setAutoCancel(true)
            .setOnlyAlertOnce(false).setDefaults(
                Notification.DEFAULT_ALL
            ).setShowWhen(true).build()
        else NotificationCompat.Builder(this, EMPLOYEE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_senior_sociable).setColor(Color.parseColor("#2A74D9"))
            .setContentTitle(title).setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setCategory(
                NotificationCompat.CATEGORY_MESSAGE
            ).setContentIntent(resultPendingIntent).setAutoCancel(true)
            .setOnlyAlertOnce(false).setDefaults(
                Notification.DEFAULT_ALL
            ).setShowWhen(true).build()

        notificationManager.notify(
            type.toInt() + ThreadLocalRandom.current().nextInt(0, 1000), notification
        )


    }
}
