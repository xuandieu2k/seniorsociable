package vn.xdeuhug.seniorsociable.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import kotlin.random.Random

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 11 / 12 / 2023
 */
@Suppress("DEPRECATION")
class NotificationServiceNew : JobIntentService() {

    companion object {
        const val JOB_ID = 1001

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, NotificationServiceNew::class.java, JOB_ID, intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        // Hiển thị thông báo
        showNotification(intent)
        // Off notification
        DisableSendNotification.enqueueWork(this, intent)
    }

    private fun showNotification(intent: Intent) {
        val notification = Gson().fromJson(
            intent.getStringExtra(AppConstants.OBJECT_NOTIFICATION), Notification::class.java
        )
        try {
            notification.type
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        when (notification.type) {
            AppConstants.NOTIFICATION_FROM_POST -> {
                setNotificationFromPost(notification, AppConstants.NOTIFICATION_FROM_POST)
            }

            AppConstants.NOTIFICATION_FROM_POST_COMMENT -> {
                setNotificationFromPost(notification, AppConstants.NOTIFICATION_FROM_POST_COMMENT)
            }

            AppConstants.NOTIFICATION_FROM_POST_INTERACT -> {
                setNotificationFromPost(notification, AppConstants.NOTIFICATION_FROM_POST_INTERACT)
            }

            AppConstants.NOTIFICATION_FROM_MOVIE_SHORT_INTERACT -> {
                setNotificationFromPost(
                    notification,
                    AppConstants.NOTIFICATION_FROM_MOVIE_SHORT_INTERACT
                )
            }

            AppConstants.NOTIFICATION_FROM_MOVIE_SHORT_COMMENT -> {
                setNotificationFromPost(
                    notification,
                    AppConstants.NOTIFICATION_FROM_MOVIE_SHORT_COMMENT
                )
            }

            AppConstants.NOTIFICATION_FROM_EVENT -> {
                setNotificationFromPost(notification, AppConstants.NOTIFICATION_FROM_EVENT)
            }

            AppConstants.NOTIFICATION_FROM_EVENT_POST -> {
                setNotificationFromPost(notification, AppConstants.NOTIFICATION_FROM_EVENT_POST)
            }

            AppConstants.NOTIFICATION_FROM_REQUEST_ADD_FRIEND -> {
                setNotificationFromPost(
                    notification,
                    AppConstants.NOTIFICATION_FROM_REQUEST_ADD_FRIEND
                )
            }
        }
    }

    private fun getInteractContent(typeInteract: Int): Int {
        when (typeInteract) {
            PostConstants.INTERACT_LIKE -> {
                return R.drawable.ic_like_senior_new
            }

            PostConstants.INTERACT_LOVE -> {
                return R.drawable.ic_heart
            }

            PostConstants.INTERACT_SMILE -> {
                return R.drawable.ic_laugh
            }

            PostConstants.INTERACT_WOW -> {
                return R.drawable.ic_wow
            }

            PostConstants.INTERACT_SAD -> {
                return R.drawable.ic_sad
            }

            PostConstants.INTERACT_ANGRY -> {
                return R.drawable.ic_angry
            }
        }
        return 0
    }

    @SuppressLint("RemoteViewLayout")
    private fun setNotificationFromPost(notifications: Notification, typeNotification: Int) {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo Notification Channel cho Android Oreo trở lên
        createNotificationChannel(notifications.id, notifications.type)
        val remoteViews = RemoteViews(packageName, R.layout.item_send_notification)
        val userSendNotification =
            ListUserCache.getList().first { it.id == notifications.idUserSend }
        remoteViews.setImageViewBitmap(
            R.id.imvAvatar,
            PhotoShowUtils.getBitmap(userSendNotification.avatar, applicationContext)
        )
        setSubContentView(notifications, remoteViews)
        remoteViews.setTextViewText(
            R.id.tvContent,
            AppUtils.fromHtml("<b>${userSendNotification.name}</b> ${notifications.title}")
        )
        remoteViews.setTextViewText(
            R.id.tvDateTime,
            DateUtils.getDateByFormatTimeDate(notifications.timeCreated)
        )
        when (typeNotification) {
            AppConstants.NOTIFICATION_FROM_POST -> {
                // Tạo PendingIntent để mở ứng dụng khi người dùng nhấn vào thông báo
                val resultIntent =
                    Intent(this, Class.forName(ModuleClassConstants.DETAIL_CHAT_ACTIVITY))
                resultIntent.putExtra(AppConstants.POST_ID, notifications.idParentTypeOfNotification)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                // Tạo thông báo
                val notification = NotificationCompat.Builder(this, notifications.id)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_senior_sociable_border)
                    .setContent(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent).setAutoCancel(true).build()

                // Hiển thị thông báo
                notificationManager.notify(generateRandomNumber(notifications.id), notification)
            }

            AppConstants.NOTIFICATION_FROM_POST_COMMENT -> {
                // Tạo PendingIntent để mở ứng dụng khi người dùng nhấn vào thông báo
                val resultIntent =
                    Intent(this, Class.forName(ModuleClassConstants.POST_DETAIL_ACTIVITY))
                resultIntent.putExtra(AppConstants.POST_ID, notifications.idParentTypeOfNotification)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )


                // Tạo thông báo
                val notification = NotificationCompat.Builder(this, notifications.id)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_senior_sociable_border)
                    .setContent(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent).setAutoCancel(true).build()

                // Hiển thị thông báo
                notificationManager.notify(generateRandomNumber(notifications.id), notification)
            }

            AppConstants.NOTIFICATION_FROM_POST_INTERACT -> {
                // Tạo PendingIntent để mở ứng dụng khi người dùng nhấn vào thông báo
                val resultIntent =
                    Intent(this, Class.forName(ModuleClassConstants.DETAIL_CHAT_ACTIVITY))
                resultIntent.putExtra(AppConstants.POST_ID, notifications.idParentTypeOfNotification)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )


                // Tạo thông báo
                val notification = NotificationCompat.Builder(this, notifications.id)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_senior_sociable_border)
                    .setContent(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent).setAutoCancel(true).build()

                // Hiển thị thông báo
                notificationManager.notify(generateRandomNumber(notifications.id), notification)
            }

            AppConstants.NOTIFICATION_FROM_MOVIE_SHORT_INTERACT -> {
                // Tạo PendingIntent để mở ứng dụng khi người dùng nhấn vào thông báo
                val resultIntent =
                    Intent(this, Class.forName(ModuleClassConstants.DETAIL_CHAT_ACTIVITY))
                resultIntent.putExtra(AppConstants.POST_ID, notifications.idTypeOfNotification)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )


                // Tạo thông báo
                val notification = NotificationCompat.Builder(this, notifications.id)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_senior_sociable_border)
                    .setContent(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent).setAutoCancel(true).build()

                // Hiển thị thông báo
                notificationManager.notify(generateRandomNumber(notifications.id), notification)
            }

            AppConstants.NOTIFICATION_FROM_MOVIE_SHORT_COMMENT -> {
                // Tạo PendingIntent để mở ứng dụng khi người dùng nhấn vào thông báo
                val resultIntent =
                    Intent(this, Class.forName(ModuleClassConstants.DETAIL_CHAT_ACTIVITY))
                resultIntent.putExtra(AppConstants.POST_ID, notifications.idTypeOfNotification)
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
                )

                // Tạo thông báo
                val notification = NotificationCompat.Builder(this, notifications.id)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_senior_sociable_border)
                    .setContent(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent).setAutoCancel(true).build()

                // Hiển thị thông báo
                notificationManager.notify(generateRandomNumber(notifications.id), notification)
            }

            AppConstants.NOTIFICATION_FROM_EVENT -> {
                // Tạo PendingIntent để mở ứng dụng khi người dùng nhấn vào thông báo
                val resultIntent =
                    Intent(this, Class.forName(ModuleClassConstants.DETAIL_CHAT_ACTIVITY))
                resultIntent.putExtra(AppConstants.POST_ID, notifications.idTypeOfNotification)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )


                // Tạo thông báo
                val notification = NotificationCompat.Builder(this, notifications.id)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_senior_sociable_border)
                    .setContent(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent).setAutoCancel(true).build()

                // Hiển thị thông báo
                notificationManager.notify(generateRandomNumber(notifications.id), notification)
            }

            AppConstants.NOTIFICATION_FROM_EVENT_POST -> {
                // Tạo PendingIntent để mở ứng dụng khi người dùng nhấn vào thông báo
                val resultIntent =
                    Intent(this, Class.forName(ModuleClassConstants.DETAIL_CHAT_ACTIVITY))
                resultIntent.putExtra(AppConstants.POST_ID, notifications.idTypeOfNotification)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )


                // Tạo thông báo
                val notification = NotificationCompat.Builder(this, notifications.id)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_senior_sociable_border)
                    .setContent(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent).setAutoCancel(true).build()

                // Hiển thị thông báo
                notificationManager.notify(generateRandomNumber(notifications.id), notification)
            }

            AppConstants.NOTIFICATION_FROM_REQUEST_ADD_FRIEND -> {
                // Tạo PendingIntent để mở ứng dụng khi người dùng nhấn vào thông báo
                val resultIntent =
                    Intent(this, Class.forName(ModuleClassConstants.DETAIL_CHAT_ACTIVITY))
                resultIntent.putExtra(AppConstants.POST_ID, notifications.idTypeOfNotification)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )


                // Tạo thông báo
                val notification = NotificationCompat.Builder(this, notifications.id)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_senior_sociable_border)
                    .setContent(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent).setAutoCancel(true).build()

                // Hiển thị thông báo
                notificationManager.notify(generateRandomNumber(notifications.id), notification)
            }
        }
    }

    private fun setSubContentView(notifications: Notification, remoteViews: RemoteViews) {
        when (notifications.type) {
            AppConstants.NOTIFICATION_FROM_POST_INTERACT -> {
                remoteViews.setImageViewResource(
                    R.id.imvSubContent,
                    getInteractContent(notifications.typeInteract)
                )
            }

            AppConstants.NOTIFICATION_FROM_POST_COMMENT -> {
                remoteViews.setImageViewResource(
                    R.id.imvSubContent,
                    R.drawable.ic_notification_comment
                )
            }

            AppConstants.NOTIFICATION_FROM_POST -> {
                remoteViews.setImageViewResource(
                    R.id.imvSubContent,
                    R.drawable.ic_notification_post
                )
            }
        }
    }

    private fun getNameChanel(typeNotification: Int): String {
        return when (typeNotification) {
            AppConstants.NOTIFICATION_FROM_POST -> "Thông báo từ bài viết"

            AppConstants.NOTIFICATION_FROM_POST_COMMENT -> "Thông báo từ bình luận bài viết"

            AppConstants.NOTIFICATION_FROM_POST_INTERACT -> "Thông báo từ cảm xúc bài viết"

            AppConstants.NOTIFICATION_FROM_MOVIE_SHORT_INTERACT -> "Thông báo từ cảm xúc thước phim"

            AppConstants.NOTIFICATION_FROM_MOVIE_SHORT_COMMENT -> "Thông báo từ bình luận thước phim"

            AppConstants.NOTIFICATION_FROM_EVENT -> "Thông báo từ sự kiện"

            AppConstants.NOTIFICATION_FROM_EVENT_POST -> "Thông báo từ bài viết sự kiện"

            AppConstants.NOTIFICATION_FROM_REQUEST_ADD_FRIEND -> "Thông báo từ lời mời kết bạn"
            else -> "Thông báo từ không xác định"
        }
    }

    private fun createNotificationChannel(id: String, type: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                id, getNameChanel(type), NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun generateRandomNumber(input: String): Int {
        val hash = input.hashCode()
        val random = Random(hash)
        return random.nextInt()
    }

}