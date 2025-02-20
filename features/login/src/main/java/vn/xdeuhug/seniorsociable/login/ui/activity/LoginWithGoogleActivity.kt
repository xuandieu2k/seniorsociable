package vn.xdeuhug.seniorsociable.login.ui.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import org.jetbrains.anko.startActivity
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.login.constants.LogInConstants
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.login.databinding.ActivityLoginWithGoogleBinding
import vn.xdeuhug.seniorsociable.utils.AppUtils
import java.util.Date

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 30/08/2023
 */
class LoginWithGoogleActivity : AppActivity() {
    private lateinit var fireAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityLoginWithGoogleBinding

    override fun getLayoutView(): View {
        binding = ActivityLoginWithGoogleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        //
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        fireAuth = FirebaseAuth.getInstance()
        //
        //
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInGGIntent = googleSignInClient.signInIntent
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task: Task<GoogleSignInAccount> =
                        GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val ac: GoogleSignInAccount = task.getResult(ApiException::class.java)
                        showDialog(getString(R.string.is_authenticating))
                        firebaseAuthWithGoogle(ac.idToken!!)
                    } catch (e: ApiException) {
                        hideDialog()
                        toast(getString(R.string.please_try_later))
                        finish()
                    }
                } else {
                    finish()
                }
            }
        launcher.launch(signInGGIntent)
    }

    override fun initData() {
        //
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
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
                    val newUser = User(
                        us.photoUrl.toString(),
                        us.displayName.toString(),
                        us.uid,
                        "",
                        Date(),
                        us.email.toString(),
                        true,
                        LogInConstants.TYPE_GOOGLE
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

    private fun goToHome() {
        try {
            hideDialog()
            startActivity<HomeActivity>()
            finish()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

}