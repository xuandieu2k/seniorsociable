@file:Suppress("DEPRECATION")

package vn.xdeuhug.seniorsociable.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.database.NewsManagerFSDB
import vn.xdeuhug.seniorsociable.database.NotificationManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.News
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 26 / 12 / 2023
 */
class UpdateNewsService : JobIntentService() {
    companion object {
        private const val JOB_ID = 1001

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, UpdateNewsService::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        val listNews = ArrayList<News>()
        NewsManagerFSDB.getAllNews(object :
            NewsManagerFSDB.Companion.FireStoreCallback<ArrayList<News>> {
            override fun onSuccess(result: ArrayList<News>) {
                listNews.addAll(result)
                val listNewsIntent = ArrayList<News>()
                val bundleIntent = intent.extras
                if (bundleIntent != null && bundleIntent.containsKey(AppConstants.LIST_NEWS)) {
                    val listType = object : TypeToken<ArrayList<News>>() {}.type
                    listNewsIntent.clear()
                    listNewsIntent.addAll(
                        Gson().fromJson(
                            bundleIntent.getString(AppConstants.LIST_NEWS), listType
                        )
                    )
                }
                val listMapIds = listNews.map { it.articleId }
                listNewsIntent.forEach {
                    if (it.articleId !in listMapIds) {
                        NewsManagerFSDB.addNews(it, object :
                            NewsManagerFSDB.Companion.FireStoreCallback<Boolean> {
                            override fun onSuccess(result: Boolean) {
                                if (result) {
                                    Timber.tag("Add news Success").e("Thêm Oke đc nha m")
                                } else {
                                    Timber.tag("Exception add news").e("Thêm k đc nha m")
                                }
                            }

                            override fun onFailure(exception: Exception) {
                                Timber.tag("Exception add news").e(exception)
                            }

                        })
                    }
                }
            }

            override fun onFailure(exception: Exception) {
                Timber.tag("Exception get list").e(exception)
            }

        })
    }
}