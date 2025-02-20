package vn.xdeuhug.seniorsociable.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.databinding.HomeActivityBinding
import vn.xdeuhug.base.PagerAdapter
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.database.NotificationManagerFSDB
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.eventbus.BlockUserEventBus
import vn.xdeuhug.seniorsociable.model.eventbus.NotificationEventBus
import vn.xdeuhug.seniorsociable.model.eventbus.TabChangeEventBus
import vn.xdeuhug.seniorsociable.service.NotificationServiceNew
import vn.xdeuhug.seniorsociable.ui.dialog.BlockUserDialog
import vn.xdeuhug.seniorsociable.ui.fragment.NewsAndEventFragment
import java.util.Date
import kotlin.system.exitProcess

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 28/09/2022
 */
@Suppress("DEPRECATION")
class HomeActivity : AppActivity() {
    lateinit var binding: HomeActivityBinding


    private var currentPage = 0
    private var twice = false
    val idUser = UserCache.getUser().id

    // You can use this method to check if the activity is currently resumed or not
    override fun getLayoutView(): View {
        binding = HomeActivityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        NotificationManagerFSDB.listenToNotificationDataChange(UserCache.getUser().id,
            object : NotificationManagerFSDB.Companion.ListenerDataChange {
                override fun onAdded(notification: Notification) {
                    if (notification.firstShow) {
                        EventBus.getDefault().post(NotificationEventBus(notification, true))
                        // Kích hoạt JobIntentService để hiển thị thông báo
                        val intent = Intent(getContext(), NotificationServiceNew::class.java)
                        intent.putExtra(
                            AppConstants.OBJECT_NOTIFICATION, Gson().toJson(notification)
                        )
                        NotificationServiceNew.enqueueWork(getContext(), intent)
                    }
                }

                override fun onUpdated(notification: Notification) {
                    //
                }

                override fun onRemoved(notification: Notification) {
                    //
                }

            })

    }

    @RequiresApi(33)
    override fun initView() {
        showDialogBlock()
        updateListUserCache()
        updateUserOnline()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (twice) {
                    exitProcess(0)
                }
                twice = true
                toast(getString(R.string.back_confirm))
                postDelayed({ twice = false }, 2000)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

    }
    private var count = 0

    private fun showDialogBlock() {
        runBlocking {
            UserManagerFSDB.listenerTypeActiveChange(object :
                UserManagerFSDB.Companion.ListenerBlockChange{
                override fun onBlock(isBlock: Boolean, user: User) {
                    runOnUiThread {
                        if (!this@HomeActivity.isFinishing && !this@HomeActivity.isDestroyed) {
                            if (isBlock && count == 0) {
                                BlockUserDialog.Builder(this@HomeActivity, user).onActionDone(object :
                                    BlockUserDialog.Builder.OnActionDone {
                                    override fun onActionDone(isConfirm: Boolean) {
                                        if (isConfirm) {
                                            Firebase.auth.signOut()
                                            UserCache.saveUser(User())
                                            val intent = Intent(this@HomeActivity, Class.forName(ModuleClassConstants.LOGIN_ACTIVITY))
                                            startActivity(intent)
                                            finishAffinity()
                                        }
                                    }
                                }).create().show()
                            }
                        }
                    }
                    count++
                }

                override fun onFailure(exception: Exception) {
                    exception.printStackTrace()
                }

            })
        }
    }

    private fun updateListUserCache() {
        val listUserCache = ListUserCache.getList()
        CoroutineScope(Dispatchers.IO).launch {
            UserManagerFSDB.listenToUsersChanges(object :
                UserManagerFSDB.Companion.ListenerDataChange {
                override fun onAdded(user: User) {
                    if (listUserCache.firstOrNull { it.id == user.id } == null) {
                        listUserCache.add(user)
                        ListUserCache.saveList(listUserCache)
                    }
                }

                override fun onUpdated(user: User) {
                    val pos = listUserCache.indexOfFirst { it.id == user.id }
                    if (listUserCache.firstOrNull { it.id == user.id } != null) {
                        listUserCache[pos] = user
                        ListUserCache.saveList(listUserCache)
                    }
                }

                override fun onRemoved(user: User) {
                    //
                }

            })
        }
    }

    private fun updateUserOnline() {
        UserManagerFSDB.updateUserOnline(
            UserCache.getUser().id,
            true,
            Date(),
            object : UserManagerFSDB.Companion.FireStoreCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    Timber.tag("Update Online").i("Oke")
                }

                override fun onFailure(exception: Exception) {
                    exception.printStackTrace()
                }

            })
    }

    private fun setListUser() {
        val listUserCache = ListUserCache.getList()
        UserManagerFSDB.getAllUserChange(object :
            UserManagerFSDB.Companion.FireStoreCallback<ArrayList<User>> {
            override fun onSuccess(result: ArrayList<User>) {
                if (result.size > listUserCache.size) {
                    ListUserCache.saveList(result)
                }
            }

            override fun onFailure(exception: Exception) {
                exception.printStackTrace()
            }

        })

    }

    override fun initData() {
        val fragments: List<Fragment>
        fragments = listOf(
            Class.forName(ModuleClassConstants.HOME_FRAGMENT).newInstance() as AppFragment<*>,
            NewsAndEventFragment(),
            Class.forName(ModuleClassConstants.WATCH_FRAGMENT).newInstance() as AppFragment<*>,
            Class.forName(ModuleClassConstants.CHAT_FRAGMENT).newInstance() as AppFragment<*>,
            Class.forName(ModuleClassConstants.UTILITY_FRAGMENT).newInstance() as AppFragment<*>
        )


        val adapter = PagerAdapter(this, fragments)

        //Setup menu
        binding.mBottomNavigationView.itemIconTintList = null
        val menu = binding.mBottomNavigationView.menu
        for (i in 0 until menu.size()) {
            binding.mBottomNavigationView.findViewById<BottomNavigationItemView>(menu.getItem(i).itemId)
                .setOnLongClickListener { true }
        }
        binding.contentView.adapter = adapter
        binding.contentView.offscreenPageLimit = adapter.itemCount - 1
        binding.contentView.setCurrentItem(currentPage, false)
        binding.mBottomNavigationView.selectedItemId = R.id.menu_home
        binding.mBottomNavigationView.setOnItemSelectedListener { item ->
            // Handle item selection here
            when (item.itemId) {
                R.id.menu_home -> {
                    currentPage = 0
                    binding.contentView.setCurrentItem(currentPage, false)
                    return@setOnItemSelectedListener true
                }

                R.id.menu_news -> {
                    currentPage = 1
                    binding.contentView.setCurrentItem(currentPage, false)

                    return@setOnItemSelectedListener true
                }

                R.id.menu_watch -> {
                    currentPage = 2
                    binding.contentView.setCurrentItem(currentPage, false)
                    return@setOnItemSelectedListener true
                }

                R.id.menu_message -> {
                    currentPage = 3
                    binding.contentView.setCurrentItem(currentPage, false)
                    return@setOnItemSelectedListener true
                }

                R.id.menu_information -> {
                    currentPage = 4
                    binding.contentView.setCurrentItem(currentPage, false)
                    return@setOnItemSelectedListener true
                }

                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }

        //Chặn viewpager2 vuốt
        binding.contentView.isUserInputEnabled = false

    }

    /**
     * @param event tab là tab cần chuyển đến
     */
    @Subscribe
    fun onTabChange(event: TabChangeEventBus) {
        if (event.isChange) {
            when (event.tabCurrent) {
                AppConstants.TAB_EVENT -> {
                    binding.mBottomNavigationView.selectedItemId = R.id.menu_news
                }

                AppConstants.TAB_NEWS -> {
                    binding.mBottomNavigationView.selectedItemId = R.id.menu_news
                }

                AppConstants.TAB_MOVIE_SHORT -> {
                    binding.mBottomNavigationView.selectedItemId = R.id.menu_watch
                }
            }
        }
    }

    override fun onDestroy() {
        GlobalScope.launch {
            Timber.tag("Update Off").i("Oke")
            UserManagerFSDB.updateUserOnlineAsyns(idUser, false, Date())
        }
        super.onDestroy()
    }
}