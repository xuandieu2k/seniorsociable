package vn.xdeuhug.seniorsociable.app

import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.gyf.immersionbar.ImmersionBar
import com.hjq.bar.TitleBar
import com.hjq.http.listener.OnHttpListener
import okhttp3.Call
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import vn.xdeuhug.base.BaseActivity
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.ToastAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.action.TitleBarAction
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.http.model.HttpData
import vn.xdeuhug.seniorsociable.model.eventbus.EventBusTokenExpired
import vn.xdeuhug.seniorsociable.ui.dialog.NotificationDialog
import vn.xdeuhug.seniorsociable.ui.dialog.WaitDialog

/**
 * @Author: Bùi Hửu Thắng
 * @Date: 03/10/2022
 */
abstract class AppActivity : BaseActivity(),
    ToastAction, TitleBarAction, OnHttpListener<Any> {


    /** 标题栏对象 */
    private var titleBar: TitleBar? = null

    /** 状态栏沉浸 */
    private var immersionBar: ImmersionBar? = null

    /** 加载对话框 */
    private var dialog: BaseDialog? = null

    /** 对话框数量 */
    private var dialogCount: Int = 0

    /**
     * 当前加载对话框是否在显示中
     */
    open fun isShowDialog(): Boolean {
        return dialog != null && dialog!!.isShowing
    }

    /**
     * 显示加载对话框
     */
    open fun showDialog() {
        if (isFinishing || isDestroyed) {
            return
        }
        dialogCount++
        postDelayed(Runnable {
            if ((dialogCount <= 0) || isFinishing || isDestroyed) {
                return@Runnable
            }
            if (dialog == null) {
                dialog = WaitDialog.Builder(this)
                    .setCancelable(false)
                    .create()
            }
            if (!dialog!!.isShowing) {
                dialog!!.show()
            }
        }, 100)
    }

    open fun showDialog(message:String) {
        if (isFinishing || isDestroyed) {
            return
        }
        dialogCount++
        postDelayed(Runnable {
            if ((dialogCount <= 0) || isFinishing || isDestroyed) {
                return@Runnable
            }
            if (dialog == null) {
                dialog = WaitDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(message)
                    .create()
            }
            if (!dialog!!.isShowing) {
                dialog!!.show()
            }
        }, 0)
    }

    /**
     * 隐藏加载对话框
     */
    open fun hideDialog() {
        if (isFinishing || isDestroyed) {
            return
        }
        if (dialogCount > 0) {
            dialogCount--
        }
        if ((dialogCount != 0) || (dialog == null) || !dialog!!.isShowing) {
            return
        }
        dialog?.dismiss()
        dialog = null
    }

    open fun destroyDialog()
    {
        dialog = null
    }

    override fun initLayout() {
        super.initLayout()

        val titleBar = getTitleBar()
        titleBar?.setOnTitleBarListener(this)

        if (isStatusBarEnabled()) {
            getStatusBarConfig().init()

            if (titleBar != null) {
                ImmersionBar.setTitleBar(this, titleBar)
                titleBar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white))
                titleBar.leftIcon = AppCompatResources.getDrawable(this, R.drawable.ic_back_blue)
                titleBar.setTitleColor(
                    ContextCompat.getColor(
                        getContext(),
                        R.color.text_color_blue
                    )
                )
                titleBar.setLeftTitleColor(
                    ContextCompat.getColor(
                        getContext(),
                        R.color.text_color_blue
                    )
                )
                titleBar.titleView.isAllCaps = true
            }
        }
    }

    /**
     * 是否使用沉浸式状态栏
     */
    protected open fun isStatusBarEnabled(): Boolean {
        return true
    }

    /**
     * 状态栏字体深色模式
     */
    open fun isStatusBarDarkFont(): Boolean {
        return true
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    open fun getStatusBarConfig(): ImmersionBar {
        if (immersionBar == null) {
            immersionBar = createStatusBarConfig()
        }
        return immersionBar!!
    }

    /**
     * 初始化沉浸式状态栏
     */
    protected open fun createStatusBarConfig(): ImmersionBar {
        return ImmersionBar.with(this) // 默认状态栏字体颜色为黑色
            .statusBarDarkFont(isStatusBarDarkFont()) // 指定导航栏背景颜色
            .navigationBarColor(R.color.white) // 状态栏字体和导航栏内容自动变色，必须指定状态栏颜色和导航栏颜色才可以自动变色
            .autoDarkModeEnable(true, 0.2f)
    }

    /**
     * 设置标题栏的标题
     */
    override fun setTitle(@StringRes id: Int) {
        title = getString(id)
    }

    /**
     * 设置标题栏的标题
     */
    override fun setTitle(title: CharSequence?) {
        super<BaseActivity>.setTitle(title)
        getTitleBar()?.title = title
    }

    override fun getTitleBar(): TitleBar? {
        if (titleBar == null) {
            titleBar = obtainTitleBar(getContentView())
        }
        return titleBar
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in_activity, R.anim.left_out_activity)
    }

    /**
     * [OnHttpListener]
     */
    override fun onHttpStart(call: Call) {
        //showDialog()
    }

    override fun onHttpSuccess(result: Any) {
        if (result is HttpData<*>) {
            toast(result.getMessage())
        }
    }

    override fun onHttpFail(e: Exception) {
        toast(e.message)
        hideDialog()
        dialog = null
    }

    override fun onHttpEnd(call: Call) {
        //
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isShowDialog()) {
            hideDialog()
        }
        dialog = null
    }

    override fun onLeftClick(view: View?) {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onTitleClick(view: View?) {
        //code
    }

    override fun onRightClick(view: View?) {
        //code
    }

    fun View.clickWithDebounce(debounceTime: Long = 1000L, action: () -> Unit) {
        this.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime: Long = 0

            override fun onClick(v: View) {
                if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                else action()
                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Suppress("unused")
    fun returnLogin(event: EventBusTokenExpired) {
        Timber.d(event.message)
        NotificationDialog.Builder(
            this,
            getString(R.string.notification),
            getString(R.string.token_expired),false

        ).onActionDone(object : NotificationDialog.Builder.OnActionDone {
            override fun onActionDone() {
                val user = UserCache.getUser()
                user.id = ""
                UserCache.saveUser(user)
                val intent = Intent(
                    this@AppActivity,
                    Class.forName(ModuleClassConstants.LOGIN_ACTIVITY)
                )
                startActivity(intent)
                finish()

            }
        }).show()
    }

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.hide() {
        visibility = View.GONE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }
}