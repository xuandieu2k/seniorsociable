package vn.xdeuhug.seniorsociable.utility.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.gms.location.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gyf.immersionbar.ImmersionBar
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.support.v4.startActivity
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.model.eventbus.TabChangeEventBus
import vn.xdeuhug.seniorsociable.other.AppConfig
import vn.xdeuhug.seniorsociable.ui.activity.BrowserActivity
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.ui.activity.NotificationActivity
import vn.xdeuhug.seniorsociable.ui.dialog.ConfirmDialog
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utility.databinding.FragmentUtilityBinding
import vn.xdeuhug.seniorsociable.utility.ui.activity.FeedbackActivity
import vn.xdeuhug.seniorsociable.utility.ui.activity.InformationAppActivity
import vn.xdeuhug.seniorsociable.utility.ui.activity.LookUpDiseasesActivity
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.invisible
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 08/04/2023
 */
class UtilityFragment : AppFragment<HomeActivity>() {
    private lateinit var binding: FragmentUtilityBinding
    private var user = UserCache.getUser()

    override fun getLayoutView(): View {
        binding = FragmentUtilityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        PhotoShowUtils.loadAvatarImage(
            "${UploadFireStorageConstants.HEAD_UPLOAD}${UserCache.getUser().id}${UploadFireStorageConstants.BODY_UPLOAD_AVATAR}",
            UserCache.getUser().avatar,
            binding.header.imgAvatar
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initData() {
        setHead()
        binding.header.llProfile.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        context, Class.forName(ModuleClassConstants.EDIT_INFORMATION)
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }

        binding.btnLogOut.setOnClickListener {
            ConfirmDialog.Builder(
                getAttachActivity()!!,
                getString(vn.xdeuhug.seniorsociable.R.string.logout),
                "${getString(vn.xdeuhug.seniorsociable.R.string.head_confirm_logout)}" +
                        "<br/><b>${getString(vn.xdeuhug.seniorsociable.R.string.name_app_confirm_logout)}</b>?"
            ).onActionDone(object : ConfirmDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    if (isConfirm) {
                        Firebase.auth.signOut()
                        UserCache.saveUser(User())
                        hideDialog()
                        val intent = Intent(
                            getAttachActivity(), Class.forName(ModuleClassConstants.LOGIN_ACTIVITY)
                        )
                        postDelayed({
                            startActivity(intent)
                            getAttachActivity()!!.finishAffinity()
                        }, 500)
                    } else {
                        //
                    }
                }
            }).show()
        }



        binding.llFeedback.setOnClickListener {

            try {
                startActivity(
                    Intent(
                        context, FeedbackActivity::class.java
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }


        }

        binding.llTermsOfUse.setOnClickListener {
            BrowserActivity.start(
                getAttachActivity()!!,
                AppConstants.TERMS_OF_USE,
                getString(vn.xdeuhug.seniorsociable.R.string.terms_of_use)
            )

        }
        binding.llPrivacyPolicy.setOnClickListener {
            BrowserActivity.start(
                getAttachActivity()!!,
                AppConstants.PRIVACY_POLICY,
                getString(vn.xdeuhug.seniorsociable.R.string.privacy_policy)
            )

        }
        binding.llRate.setOnClickListener {

            val uri = Uri.parse("market://details?id=" + AppConfig.getPackageName())
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + AppConfig.getPackageName())
                    )
                )
            }


        }

        binding.llInfo.setOnClickListener {
//            val uri = Uri.parse("market://details?id=" + AppConfig.getPackageName())
//            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
//            goToMarket.addFlags(
//                Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
//            )
//            try {
//                startActivity(goToMarket)
//            } catch (e: ActivityNotFoundException) {
//                startActivity(
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        Uri.parse("http://play.google.com/store/apps/details?id=" + AppConfig.getPackageName())
//                    )
//                )
//            }
            try {
                startActivity(
                    Intent(
                        context, InformationAppActivity::class.java
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }

        }

        binding.llCensorship.clickWithDebounce {
            try {
                startActivity(
                    Intent(
                        context, Class.forName(ModuleClassConstants.POST_MODERATION_ACTIVITY)
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }

        binding.llEvent.clickWithDebounce {
            EventBus.getDefault()
                .post(TabChangeEventBus(AppConstants.TAB_EVENT,true))
        }
        binding.llNews.clickWithDebounce {
            EventBus.getDefault()
                .post(TabChangeEventBus(AppConstants.TAB_NEWS,true))
        }

        binding.llWatch.clickWithDebounce {
            EventBus.getDefault()
                .post(TabChangeEventBus(AppConstants.TAB_MOVIE_SHORT,true))
        }

        binding.llNotification.clickWithDebounce {
            try {
                startActivity(
                    Intent(
                        context, NotificationActivity::class.java
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }

        binding.llAccountManagement.clickWithDebounce {
            try {
                startActivity(
                    Intent(
                        context, Class.forName(ModuleClassConstants.ACCOUNT_MANAGEMENT_ACTIVITY)
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }

        binding.llSearchDiseases.clickWithDebounce {
            try {
                startActivity<LookUpDiseasesActivity>()
            } catch (e: ClassNotFoundException) {
                //code
            }
        }

        binding.llFriend.clickWithDebounce {
            try {
                startActivity(
                    Intent(
                        context, Class.forName(ModuleClassConstants.FRIENDS_ACTIVITY)
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }

        binding.llReport.clickWithDebounce {
            try {
                startActivity(
                    Intent(
                        context, Class.forName(ModuleClassConstants.REPORT_POST_ACTIVITY)
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }

        binding.llStatistical.clickWithDebounce {
            try {
                startActivity(
                    Intent(
                        context, Class.forName(ModuleClassConstants.STATISTICAL_ACTIVITY)
                    )
                )
            } catch (e: ClassNotFoundException) {
                //code
            }
        }


    }

    private fun setHead() {
        ImmersionBar.setTitleBar(this, binding.header.llProfile)
        binding.header.tvUsername.text = user.name
        PhotoShowUtils.loadAvatarImage(
            "${UploadFireStorageConstants.HEAD_UPLOAD}${UserCache.getUser().id}${UploadFireStorageConstants.BODY_UPLOAD_AVATAR}",
            UserCache.getUser().avatar,
            binding.header.imgAvatar
        )
        when(UserCache.getUser().typeAccount)
        {
            AppConstants.TYPE_FACEBOOK ->{
                binding.header.tvLoginWith.text = getString(vn.xdeuhug.seniorsociable.R.string.facebok)
                binding.header.imvLoginWith.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_logo_facebook)
            }
            AppConstants.TYPE_GOOGLE ->{
                binding.header.tvLoginWith.text = getString(vn.xdeuhug.seniorsociable.R.string.google)
                binding.header.imvLoginWith.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_logo_google)
            }
            AppConstants.TYPE_PHONE ->{
                binding.header.tvLoginWith.text = getString(vn.xdeuhug.seniorsociable.R.string.phone)
                binding.header.imvLoginWith.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_login_phone)
            }
        }
        if(user.roleAccount == AppConstants.ROLE_ADMIN)
        {
            binding.llCensorship.show()
            binding.llAccountManagement.show()
            binding.llReport.show()
            binding.header.tvRole.show()
            binding.header.tvRole.text = getString(R.string.admin)
        }else{
            binding.llCensorship.invisible()
            binding.header.tvRole.hide()
            binding.llReport.hide()
            binding.llAccountManagement.hide()
        }
    }
}