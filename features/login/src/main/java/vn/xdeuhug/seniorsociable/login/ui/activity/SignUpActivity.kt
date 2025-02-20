package vn.xdeuhug.seniorsociable.login.ui.activity

import android.annotation.SuppressLint
import android.text.InputFilter
import android.view.View
import androidx.core.widget.doOnTextChanged
import com.google.firebase.auth.*
import org.jetbrains.anko.startActivity
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.login.constants.LogInConstants
import vn.xdeuhug.seniorsociable.login.databinding.ActivitySignupBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.PhoneUtils

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 29/08/2023
 */
class SignUpActivity : AppActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var fireAuth: FirebaseAuth
    override fun getLayoutView(): View {
        binding = ActivitySignupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setUpEditTextPhone()
        setUpClickDialog()
    }

    @SuppressLint("SetTextI18n")
    fun setUpEditTextPhone() {
        fireAuth = FirebaseAuth.getInstance()
        fireAuth.useAppLanguage()
        fireAuth.setLanguageCode("vn")
        binding.btnGetOTP.isEnabled = false
        binding.edtPhone.requestFocus()
        showKeyboard(this@SignUpActivity)
        binding.edtPhone.filters = arrayOf(
            AppUtils.EMOJI_FILTER, InputFilter.LengthFilter(15)
        )
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
                .d("${binding.countryCode.selectedCountryCodeWithPlus}${text.toString().trim()}")
        }
        binding.btnGetOTP.clickWithDebounce(1000) {
            checkPhoneExist { result ->
                if (result) {
                    toast(getString(R.string.phone_is_used))
                } else {
                    try {
                        startActivity<InputOTPActivity>(
                            LogInConstants.PHONE_NUMBER to binding.countryCode.selectedCountryCodeWithPlus + binding.edtPhone.text.toString()
                        )
                        finish()
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        binding.btnGoToLogin.clickWithDebounce(1000) {
            try {
                startActivity<LoginActivity>()
                finish()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun setUpClickDialog() {
        PhoneUtils.setUpDialogPhone(binding.countryCode, getContext(), resources)
    }

    private fun showErrorPhoneNumber(isShow: Boolean) {
        if (isShow) {
            binding.tvErrorPhone.show()
            if (binding.edtPhone.text.toString().isEmpty()) {
                binding.tvErrorPhone.text = getString(R.string.res_not_empty_character)
            } else {
                binding.tvErrorPhone.text = getString(R.string.error_phone)
            }
            binding.btnGetOTP.isEnabled = false
        } else {
            binding.tvErrorPhone.hide()
            binding.tvErrorPhone.text = ""
            binding.btnGetOTP.isEnabled = true
        }
    }

    override fun initData() {
        //
    }

    private fun checkPhoneExist(callback: (Boolean) -> Unit) {
        val phone = binding.edtPhone.text.toString().trim()
        Timber.tag("Log Phone Number")
            .d(PhoneUtils.getFormattedPhoneNumber(phone, binding.countryCode.selectedCountryNameCode))
        UserManagerFSDB.getUserByPhone(PhoneUtils.getFormattedPhoneNumber(
            phone, binding.countryCode.selectedCountryNameCode
        ), object : UserManagerFSDB.Companion.UserCallback {
            override fun onUserFound(user: User?) {
                val value = if (user == null) {
                    false
                } else {
                    UserCache.saveUser(user)
                    true
                }
                callback(value) // Gọi callback để trả về giá trị
            }

            override fun onFailure(exception: Exception) {
                // Xử lý lỗi nếu có
                Timber.tag("Exception").e(exception)
                finish()
                callback(false) // Trả về giá trị false khi có lỗi
            }
        })
    }
}