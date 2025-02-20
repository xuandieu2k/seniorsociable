package vn.xdeuhug.seniorsociable.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.database.NotificationManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.ActivityNotificationBinding
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification
import vn.xdeuhug.seniorsociable.model.eventbus.NotificationEventBus
import vn.xdeuhug.seniorsociable.service.UpdateReadNotifyService
import vn.xdeuhug.seniorsociable.ui.adapter.NotificationAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 29 / 11 / 2023
 */
class NotificationActivity : AppActivity(), NotificationAdapter.OnListenerCLick {
    private lateinit var binding: ActivityNotificationBinding
    private var listNotification = ArrayList<Notification>()
    private lateinit var notificationAdapter: NotificationAdapter
    override fun getLayoutView(): View {
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setDataForView()
    }

    private fun setDataForView() {
        notificationAdapter = NotificationAdapter(getContext())
        notificationAdapter.setData(listNotification)
        notificationAdapter.onListenerCLick = this
        AppUtils.initRecyclerViewVertical(binding.rvNotification, notificationAdapter)
//        setDataNotification()
        listenerDataChange()
    }

    private fun listenerDataChange() {
        setViewNoData(listNotification.isNotEmpty())
        NotificationManagerFSDB.listenToNotificationChanges(UserCache.getUser().id,
            object : NotificationManagerFSDB.Companion.ListenerDataChange {
                @SuppressLint("NotifyDataSetChanged")
                override fun onAdded(notification: Notification) {
                    listNotification.add(notification)
                    notificationAdapter.notifyDataSetChanged()
                    setViewNoData(listNotification.isNotEmpty())
                }

                override fun onUpdated(notification: Notification) {
                    //
                }

                override fun onRemoved(notification: Notification) {
                    //
                }

            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataNotification() {
        runBlocking {
            val list =
                NotificationManagerFSDB.getAllNotificationByUserIdAsync(UserCache.getUser().id)
            listNotification.clear()
            listNotification.addAll(list)
            notificationAdapter.notifyDataSetChanged()
//            listenerDataChange()
            setViewNoData(listNotification.isNotEmpty())
        }
    }

    override fun initData() {
        //
    }

    private fun setViewNoData(isHaveData: Boolean) {
        if (isHaveData) {
            binding.nsvScroll.show()
            binding.rlNoData.hide()
        } else {
            binding.nsvScroll.hide()
            binding.rlNoData.show()
        }
    }

    override fun onClick(position: Int) {
        val intentService = Intent()
        val notification =  listNotification[position]
        val intent = Intent(this, Class.forName(ModuleClassConstants.POST_DETAIL_ACTIVITY))
        intent.putExtra(AppConstants.POST_ID, notification.idParentTypeOfNotification)
        startActivity(intent)
        notification.read = AppConstants.IS_READ
        notificationAdapter.notifyItemChanged(position,notification)
        intentService.putExtra(AppConstants.OBJECT_NOTIFICATION, Gson().toJson(notification))
        UpdateReadNotifyService.enqueueWork(getContext(), intentService)
        EventBus.getDefault().post(NotificationEventBus(notification, false))
    }
}