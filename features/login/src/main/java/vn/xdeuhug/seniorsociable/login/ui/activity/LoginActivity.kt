package vn.xdeuhug.seniorsociable.login.ui.activity

import android.content.Intent
import android.text.InputFilter
import android.view.View
import androidx.core.widget.doOnTextChanged
import com.google.gson.Gson
import org.jetbrains.anko.startActivity
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.login.databinding.ActivityLoginBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.BCryptUtils
import vn.xdeuhug.seniorsociable.utils.PhoneUtils


/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 28/08/2023
 */
class LoginActivity : AppActivity() {
    private var isValidPhone = false
    private var isValidPassword = false
    private lateinit var binding: ActivityLoginBinding
    override fun getLayoutView(): View {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        CheckValidButtonLogin()
        setUpClickButton()
        setUpClickDialog()
        listenerEditTextChange()
    }

    private fun setUpClickDialog() {
        PhoneUtils.setUpDialogPhone(binding.countryCode, this, resources)
    }

    private fun listenerEditTextChange() {
        binding.edtPhone.filters = arrayOf(
            AppUtils.EMOJI_FILTER, InputFilter.LengthFilter(11)
        )
        binding.edtPassword.filters = arrayOf(
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
            }
            CheckValidButtonLogin()
        }
        binding.edtPhone.doOnTextChanged { text, _, _, _ ->
            if (PhoneUtils.isPhoneNumberValid(
                    binding.countryCode.selectedCountryCodeWithPlus + text.toString().trim(),
                    binding.countryCode.selectedCountryCode
                )
            ) {
                showErrorPhoneNumber(false)
            } else {
                showErrorPhoneNumber(true)
            }
            Timber.tag("Log Phone")
                .d("${binding.countryCode.selectedCountryCodeWithPlus}${text.toString()}")
            CheckValidButtonLogin()
        }
    }

    private fun setUpClickButton() {
        binding.btnCreateAccount.clickWithDebounce(1000) {
            try {
                startActivity<SignUpActivity>()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        binding.btnForgetPassword.clickWithDebounce(1000) {
            try {
                startActivity<ForgetPasswordActivity>()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        binding.btnLogin.clickWithDebounce(1000) {
            showDialog(getString(R.string.is_authenticating))
            postDelayed({
                checkPhoneExist { result,user ->
                    hideDialog()
                    if (result) {
                        if(BCryptUtils.verifyPassword(binding.edtPassword.text.toString().trim(),user!!.password))
                        {
                            try {
                                startActivity<InputOTPLoginActivity>(AppConstants.PERSONAL_USER to Gson().toJson(user))
                                finish()
                            } catch (e: ClassNotFoundException) {
                                //code
                            }
                        }else{
                            toast(getString(R.string.phone_or_password_not_true))
                        }
                    } else {
                        toast(getString(R.string.phone_is_not_used))
                    }
                }
            },2000)
        }
        binding.btnGoogle.clickWithDebounce(1000) {
            try {
                showDialog(getString(R.string.is_processing))
                postDelayed({
                    hideDialog()
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity<LoginWithGoogleActivity>()
                }, 1000)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        binding.btnFacebook.clickWithDebounce(1000) {
            try {
                showDialog(getString(R.string.is_processing))
                postDelayed({
                    hideDialog()
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity<LoginWithFacebookActivity>()
                }, 1000)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun initData() {
        //
    }

    private fun checkPhoneExist(callback: (Boolean,User?) -> Unit) {
        val phone = binding.edtPhone.text.toString().trim()
        Timber.tag("Log Phone Number")
            .d(PhoneUtils.getFormattedPhoneNumber(phone, binding.countryCode.selectedCountryNameCode))
        UserManagerFSDB.getUserByPhone(PhoneUtils.getFormattedPhoneNumber(
            phone, binding.countryCode.selectedCountryNameCode
        ), object : UserManagerFSDB.Companion.UserCallback {
            override fun onUserFound(user: User?) {
                val value = user != null
                callback(value,user) // Gọi callback để trả về giá trị
            }

            override fun onFailure(exception: Exception) {
                // Xử lý lỗi nếu có
                Timber.tag("Exception").e(exception)
                finish()
                callback(false,null) // Trả về giá trị false khi có lỗi
            }
        })
    }

    private fun showErrorPhoneNumber(isShow: Boolean) {
        if (isShow) {
            binding.tvErrorPhone.show()
            if (binding.edtPhone.text.toString().isEmpty()) {
                binding.tvErrorPhone.text = getString(R.string.res_not_empty_character)
            } else {
                binding.tvErrorPhone.text = getString(R.string.error_phone)
            }
            isValidPhone = false
        } else {
            binding.tvErrorPhone.hide()
            binding.tvErrorPhone.text = ""
            isValidPhone = true
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

    private fun CheckValidButtonLogin()
    {
        binding.btnLogin.isEnabled = isValidPhone && isValidPassword
    }
}