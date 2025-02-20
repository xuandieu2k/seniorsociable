package vn.xdeuhug.seniorsociable.login.ui.activity

import android.text.InputFilter
import android.view.View
import androidx.core.widget.doOnTextChanged
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.login.databinding.ActivityUpdatePasswordBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.BCryptUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 12 / 2023
 */
class UpdatePasswordActivity : AppActivity() {
    private lateinit var binding: ActivityUpdatePasswordBinding
    private var isValidPasswordIsTheSame = false
    private var isValidConfirmPassword = false
    private var isValidPassword = false
    private lateinit var fireAuth: FirebaseAuth
    override fun getLayoutView(): View {
        binding = ActivityUpdatePasswordBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        fireAuth = FirebaseAuth.getInstance()
        fireAuth.useAppLanguage()
        fireAuth.setLanguageCode("vn")
        setEditTextChange()
        setClickUpdate()
    }

    private fun setClickUpdate() {
        binding.btnComplete.clickWithDebounce {
            showDialog(getString(R.string.is_processing))
            getUser { result,user ->
                if (result) {
                    val userCurrent = user
                    userCurrent!!.password = BCryptUtils.hashPassword(binding.edtPassword.text.toString().trim())
                    UserManagerFSDB.updatePasswordUser(userCurrent,object :
                        UserManagerFSDB.Companion.FireStoreCallback<Unit>{
                        override fun onSuccess(result: Unit) {
                            toast(getString(R.string.update_password_success))
                            goToHome(userCurrent)
                        }

                        override fun onFailure(exception: Exception) {
                            exception.printStackTrace()
                            resetDataUser()
                            toast(getString(R.string.please_try_later))
                            finish()
                        }

                    })
                } else {
                    resetDataUser()
                    toast(getString(R.string.please_try_later))
                    finish()
                }
            }
        }
    }

    private fun goToHome(user: User) {
        try {
            UserCache.saveUser(user)
            hideDialog()
            startActivity<HomeActivity>()
            finish()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun initData() {
        //
    }

    private fun setEditTextChange() {
        binding.edtPassword.filters = arrayOf(
            AppUtils.EMOJI_FILTER, AppUtils.spaceCharacters, InputFilter.LengthFilter(21)
        )
        binding.edtConfirmPassword.filters = arrayOf(
            AppUtils.EMOJI_FILTER, AppUtils.spaceCharacters, InputFilter.LengthFilter(21)
        )

        binding.edtPassword.doOnTextChanged { _, _, _, _ ->
            if (binding.edtPassword.length() > 20) {
                binding.edtPassword.setText(
                    binding.edtPassword.text.toString().substring(0, 20)
                )
                binding.edtPassword.setSelection(binding.edtPassword.text!!.length)
                toast(getString(R.string.res_max_20_characters))
            } else {
                validPassword()
                validPasswordIsTheSame()
            }
            checkValidButtonUpdateInformation()
        }

        binding.edtConfirmPassword.doOnTextChanged { _, _, _, _ ->
            if (binding.edtConfirmPassword.length() > 20) {
                binding.edtConfirmPassword.setText(
                    binding.edtConfirmPassword.text.toString().substring(0, 20)
                )
                binding.edtConfirmPassword.setSelection(binding.edtConfirmPassword.text!!.length)
                toast(getString(R.string.res_max_20_characters))
            } else {
                validConfirmPassword()
                validPasswordIsTheSame()
            }
            checkValidButtonUpdateInformation()
        }
    }

    private fun validPassword() {
        val strCurrentPassword = binding.edtPassword.text.toString()
        if (strCurrentPassword.isEmpty()) {
            binding.tvErrorPassword.text = getString(R.string.res_password_empty)
            binding.tvErrorPassword.show()
            isValidPassword = false
        } else {
            if (strCurrentPassword.length in 8..20) {
                // Valid text
                binding.tvErrorPassword.text = ""
                binding.tvErrorPassword.hide()
                isValidPassword = true
            } else {
                binding.tvErrorPassword.text = getString(R.string.res_password_characters)
                binding.tvErrorPassword.show()
                isValidPassword = false
            }
        }
    }

    private fun validConfirmPassword() {
        val strCurrentPassword = binding.edtConfirmPassword.text.toString()
        if (strCurrentPassword.isEmpty()) {
            binding.tvErrorConfirmPassword.text = getString(R.string.res_password_empty)
            binding.tvErrorConfirmPassword.show()
            isValidConfirmPassword = false
        } else {
            if (strCurrentPassword.length in 8..20) {
                // Valid text
                binding.tvErrorConfirmPassword.text = ""
                binding.tvErrorConfirmPassword.hide()
                isValidConfirmPassword = true
            } else {
                binding.tvErrorConfirmPassword.text = getString(R.string.res_password_characters)
                binding.tvErrorConfirmPassword.show()
                isValidConfirmPassword = false
            }
        }
    }

    private fun validPasswordIsTheSame() {
        if (isValidPassword && isValidConfirmPassword) {
            if (binding.edtPassword.text.toString()
                    .trim() == binding.edtConfirmPassword.text.toString().trim()
            ) {
                isValidPasswordIsTheSame = true
                binding.tvErrorConfirmPassword.text = ""
                binding.tvErrorConfirmPassword.hide()
            } else {
                isValidPasswordIsTheSame = false
                binding.tvErrorConfirmPassword.text = getString(R.string.res_password_not_same)
                binding.tvErrorConfirmPassword.show()
            }
        }
    }

    private fun checkValidButtonUpdateInformation() {
        binding.btnComplete.isEnabled =
            isValidPassword && isValidConfirmPassword && isValidPasswordIsTheSame
    }

    private fun getUser(callback: (Boolean, User?) -> Unit) {
        UserManagerFSDB.getUserById(
            fireAuth.currentUser!!.uid, object : UserManagerFSDB.Companion.UserCallback {
                override fun onUserFound(user: User?) {
                    val value = user != null
                    callback(value,user) // Gọi callback để trả về giá trị
                }

                override fun onFailure(exception: Exception) {
                    // Xử lý lỗi nếu có
                    Timber.tag("Exception").e(exception)
                    callback(false,null) // Trả về giá trị false khi có lỗi
                }
            })
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        resetDataUser()
        super.onBackPressed()
    }

    private fun resetDataUser(){
        fireAuth.signOut()
        val user = UserCache.getUser()
        user.id = ""
        UserCache.saveUser(user)
    }

}