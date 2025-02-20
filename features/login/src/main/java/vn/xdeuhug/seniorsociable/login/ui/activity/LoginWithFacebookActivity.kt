package vn.xdeuhug.seniorsociable.login.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity
import org.json.JSONException
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.login.constants.LogInConstants
import vn.xdeuhug.seniorsociable.login.databinding.ActivityLoginWithGoogleBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.DateUtils
import java.util.Date


/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 04/09/2023
 */
@Suppress("DEPRECATION")
class LoginWithFacebookActivity : AppActivity() {
    private var callbackManager: CallbackManager? = null
    private lateinit var fireAuth: FirebaseAuth
    private lateinit var binding: ActivityLoginWithGoogleBinding
    override fun getLayoutView(): View {
        binding = ActivityLoginWithGoogleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        //
        FacebookSdk.sdkInitialize(getContext())
        callbackManager = CallbackManager.Factory.create()
        fireAuth = FirebaseAuth.getInstance()
        LoginManager.getInstance().logInWithReadPermissions(
            this, listOf("public_profile,user_hometown,user_birthday,user_age_range,user_gender,user_link,user_friends,user_location,user_likes,user_photos,user_videos,user_posts"
            )
        )
        showDialog(getString(R.string.is_authenticating))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    // App code
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    // App code
                    hideDialog()
                    finish()
                }

                override fun onError(exception: FacebookException) {
                    // App code
                    toast(getString(R.string.please_try_later))
                    hideDialog()
                    finish()
                }
            })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun initData() {
        //
    }

    private fun goToHome() {
        try {
            hideDialog()
            startActivity<HomeActivity>()
            finish()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential: AuthCredential = FacebookAuthProvider.getCredential(token.token)
        fireAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                checkExistUser(task)
            } else {
                hideDialog()
                toast(getString(R.string.please_try_later))
                finish()
            }
        }.addOnFailureListener {
            hideDialog()
            toast(getString(R.string.please_try_later))
            finish()
        }
    }

    private fun checkExistUser(task: Task<AuthResult>) {
        val us = task.result.user
        UserManagerFSDB.getUserById(us!!.uid, object : UserManagerFSDB.Companion.UserCallback {
            override fun onUserFound(user: User?) {
                if (user == null) {
                    // Get more information user ưitth GraphRequest Facebook
                    val request = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken()
                    ) { user, response -> // Application code
                        try {
                            val newUser = User(
                                us.photoUrl.toString(),
                                DateUtils.formatDateToDayMonthYear(user!!.getString("birthday")),
                                us.displayName.toString(),
                                AppUtils.getGenderFromFacebook(user.getString("gender")),
                                us.uid,
                                "",
                                Date(),
                                us.email.toString(),
                                true,
                                LogInConstants.TYPE_FACEBOOK
                            )
                            newUser.nameNormalize = AppUtils.removeVietnameseFromStringNice(newUser.name)
                            UserManagerFSDB.addUser(
                                newUser,
                                object : UserManagerFSDB.Companion.FireStoreCallback<Boolean> {
                                    override fun onSuccess(result: Boolean) {
                                        Timber.tag("Add User").d("Done !!!")
                                        UserCache.saveUser(newUser)
                                        goToHome()
                                    }

                                    override fun onFailure(exception: Exception) {
                                        // Xử lý lỗi nếu có
                                        Timber.tag("Exception").e(exception)
                                        finish()
                                    }
                                })
                            Timber.tag("Log infor User").i(response!!.toString())
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    val parameters = Bundle()
                    parameters.putString(
                        "fields", "id,name,email,gender,birthday,hometown,age_range,link,friends,location,likes,photos,videos,posts"
                    )
                    request.parameters = parameters
                    request.executeAsync()
                } else {
                    UserCache.saveUser(user)
                    goToHome()
                }
            }

            override fun onFailure(exception: Exception) {
                // Xử lý lỗi nếu có
                Timber.tag("Exception").e(exception)
                finish()
            }
        })
    }

}