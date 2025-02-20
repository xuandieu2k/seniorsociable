package vn.xdeuhug.seniorsociable.login.ui.activity

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import androidx.core.view.children
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.otpview.OTPListener
import org.jetbrains.anko.startActivity
import timber.log.Timber
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.login.constants.LogInConstants
import vn.xdeuhug.seniorsociable.login.databinding.ActivityInputOtpBinding
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import java.util.concurrent.TimeUnit
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 29/08/2023
 */
class InputOTPActivity : AppActivity() {
    private var isTimeUpOTP = false
    private lateinit var binding: ActivityInputOtpBinding
    private lateinit var fireAuth: FirebaseAuth
    private var OTP = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var phoneNumber: String
    private var countErrorInputOTP = 0
    private var isResend = false
    override fun getLayoutView(): View {
        binding = ActivityInputOtpBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        binding.tvTimeEndOtp.text = ""
        binding.otvOTP.showError()
        binding.tvCountErrorInput.invisible()
        fireAuth = FirebaseAuth.getInstance()
        fireAuth.useAppLanguage()
        fireAuth.setLanguageCode("vn")
        phoneNumber = intent.getStringExtra(LogInConstants.PHONE_NUMBER)!!
        sendVerificationCode()
        setUpCompletedOTP()
        setUpClickResetOTP()
    }

    private fun setUpClickResetOTP() {
        binding.tvTimeEndOtp.clickWithDebounce(1000) {
            binding.tvTimeEndOtp.text = ""
            resendVerificationCode()
        }
    }

    private fun sendVerificationCode() { // Lấy mã OTP lần đầu
        val options = PhoneAuthOptions.newBuilder(fireAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
//        if(isShowContentCheckingInDialog)
//        {
//            isShowContentCheckingInDialog = false
//            showDialog(getString(R.string.is_checking))
//        }else{
//            showDialog(getString(R.string.is_sending_otp))
//        }
        showDialog(getString(R.string.is_processing))
        binding.tvTimeEndOtp.isEnabled = false
    }

    private fun resendVerificationCode() { // Lấy lại mã OTP
        val options = PhoneAuthOptions.newBuilder(fireAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken!!)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        isResend = true
        showDialog(getString(R.string.is_sending_otp))
        binding.tvTimeEndOtp.isEnabled = false
        isTimeUpOTP = false
    }

    private fun countErrorInputOTP(): Long {
        return when (countErrorInputOTP) {
            1 -> 3000
            2 -> 6000
            3 -> 9000
            4 -> 12000
            5 -> 15000
            else -> 15000
        }
    }

    private fun countDownTimerOneMinute() {
        val timer = object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvTimeEndOtp.text = "${getString(R.string.time_up_otp)} $secondsRemaining s"
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onFinish() {
                println("Countdown finished")
                binding.tvTimeEndOtp.text = getString(R.string.otp_is_time_up)
                binding.tvTimeEndOtp.isEnabled = true
                //
                binding.otvOTP.setOTP("")
                enableOTPView(false)
                isTimeUpOTP = true
            }
        }
        timer.start()
    }

    private fun enableOTPView(isEnable: Boolean) {
        if (!isEnable) {
            binding.otvOTP.showError()
            binding.otvOTP.children.forEach { child ->
                if (child is EditText) {
                    child.isEnabled = false
                }
            }
            hideKeyboard(binding.otvOTP)
        } else {
            binding.otvOTP.resetState()
            binding.otvOTP.children.forEach { child ->
                if (child is EditText) {
                    child.isEnabled = true
                }
            }
            binding.otvOTP.requestFocusOTP()
            showKeyboard(this@InputOTPActivity)
        }
    }

    private fun countDownPendingInputOTP(timePending: Long) {
        enableOTPView(false)
        postDelayed({
            val timer = object : CountDownTimer(timePending, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    if (isTimeUpOTP) {
                        this.onFinish()
                        this.cancel()
                    } else {
                        val secondsRemaining = millisUntilFinished / 1000
                        binding.tvCountErrorInput.text = "$secondsRemaining s"
                        binding.tvCountErrorInput.show()
                    }
                }

                @SuppressLint("ClickableViewAccessibility")
                override fun onFinish() {
                    println("Countdown finished")
                    binding.tvCountErrorInput.invisible()
                    if (!isTimeUpOTP) {
                        binding.otvOTP.requestFocusOTP()
                        binding.otvOTP.setOTP("")
                        enableOTPView(true)
                    } else {
                        enableOTPView(false)
                    }
                }
            }
            timer.start()
        }, 100)
    }

    private fun setUpCompletedOTP() {
        binding.otvOTP.requestFocusOTP()
        binding.otvOTP.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                //
            }

            override fun onOTPComplete(otp: String) {
                Timber.tag("OTP").d("The OTP is $otp")
                enableOTPView(false)
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    OTP, otp
                )
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    override fun initData() {
        //
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Timber.d("TAG", "onVerificationFailed: $e")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Timber.d("TAG", "onVerificationFailed: $e")
            }
            toast(getString(R.string.message_verification_failed))
            hideDialog()
            finish()
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String, token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            enableOTPView(true)
            hideDialog()
            binding.otvOTP.resetState()
            showKeyboard(this@InputOTPActivity)
            if (OTP == "" && resendToken == null) {
                countDownTimerOneMinute()
            }
            OTP = verificationId
            resendToken = token
            if (isResend) {
                isResend = false
                countDownTimerOneMinute()
                countErrorInputOTP = 0
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        fireAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Timber.d("TAG", "Authenticate Successfully")
                checkExistUser(task)
            } else {
                countErrorInputOTP++
                toast("${getString(R.string.time_enter_otp_fail)} $countErrorInputOTP")
                if (countErrorInputOTP > 4) {
                    hideKeyboard(binding.otvOTP)
                    startActivity<LoginActivity>()
                    finish()
                }
                countDownPendingInputOTP(countErrorInputOTP())
                // Sign in failed, display a message and update the UI
                Timber.d("TAG", "OTP not true: $countErrorInputOTP time")
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                    Timber.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                }
                // Update UI
            }
        }
    }

    private fun checkExistUser(task: Task<AuthResult>) {
        val us = task.result.user
        UserManagerFSDB.getUserById(us!!.uid, object : UserManagerFSDB.Companion.UserCallback {
            override fun onUserFound(user: User?) {
                if (user == null) {
                    try {
                        startActivity<UpdateInformationActivity>(
                            LogInConstants.USER_ID to us.uid,
                            LogInConstants.PHONE_NUMBER to us.phoneNumber.toString()
                        )
                        finish()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
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