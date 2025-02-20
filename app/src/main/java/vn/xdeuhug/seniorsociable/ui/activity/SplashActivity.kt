package vn.xdeuhug.seniorsociable.ui.activity

import android.content.Intent
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.tencent.mmkv.MMKV
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.databinding.SplashActivityBinding
import vn.xdeuhug.seniorsociable.manager.ActivityManager
import vn.xdeuhug.seniorsociable.other.AppConfig
import java.util.Locale


/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 28/09/2022
 */
class SplashActivity : AppActivity() {
    private lateinit var binding: SplashActivityBinding

    override fun getLayoutView(): View {
        binding = SplashActivityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        //
    }

    override fun onBackPressed() {
        ActivityManager.getInstance().finishAllActivities()
    }

    override fun initData() {
        binding.ivSplashDebug.let {
            it.setText(getString(R.string.beta).uppercase(Locale.getDefault()))
            if (AppConfig.isDebug()) {
                it.show()
            } else {
                it.invisible()
            }
        }
        registrationToken()
        goToHome()
    }


    //Di chuyển đến màn hình sau khi đăng nhập thành công
    private fun goToHome() {
        Timber.tag("Log User").e("${FirebaseAuth.getInstance().currentUser} ${UserCache.isLogin()}")
        postDelayed({
            if (FirebaseAuth.getInstance().currentUser != null && UserCache.isLogin()) {
                try {
                    val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    val intent = Intent(
                        this@SplashActivity,
                        Class.forName(ModuleClassConstants.LOGIN_ACTIVITY)
                    )
                    startActivity(intent)
                    finish()
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
            }
        }, 1000)
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig()
            // Ẩn thanh trạng thái và thanh điều hướng
            .hideBar(BarHide.FLAG_HIDE_BAR)
    }

    override fun initActivity() {
        // Nếu Hoạt động hiện tại không phải là Hoạt động đầu tiên trong ngăn xếp tác vụ
        if (!isTaskRoot) {
            val intent: Intent? = intent
            // Nếu Hoạt động hiện tại được bắt đầu từ biểu tượng màn hình
            if (((intent != null) && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && (Intent.ACTION_MAIN == intent.action))) {
                // Hủy Hoạt động hiện tại để tránh lặp lại việc khởi tạo mục nhập
                finish()
                return
            }
        }
        super.initActivity()
    }

    private fun registrationToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.d("Fetching FCM registration token failed %s", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Timber.d("push_token_Data %s", token)
            val mmkv: MMKV = MMKV.mmkvWithID("push_token")
            mmkv.putString(AppConstants.PUSH_TOKEN, token).commit()
        })

    }
}