package vn.xdeuhug.seniorsociable.utility.ui.activity

import android.text.InputFilter
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.utility.databinding.ActivityFeedbackBinding
import vn.xdeuhug.seniorsociable.utils.AppUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 20/06/2023
 */
class FeedbackActivity : AppActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityFeedbackBinding
    private var isValidEmail = false
    private var isValidContent = false

    override fun getLayoutView(): View {
        //
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        //
        setEnableButtonConfirm((isValidContent && isValidEmail))
        binding.tvSender.text = UserCache.getUser().name
        binding.tvPhoneNumber.text = UserCache.getUser().phone
        binding.edtEmail.setText(UserCache.getUser().email)
        val listTopicFeedback = listOf(
            getString(R.string.feed_back_type),
            getString(R.string.requires_improvement),
            getString(R.string.suggest_to_do_more)
        )
        binding.spnTopicFeedback.onItemSelectedListener = this
        val adapterTopicFeedback =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, listTopicFeedback)
        adapterTopicFeedback.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnTopicFeedback.adapter = adapterTopicFeedback
        binding.edtEmail.filters = arrayOf(
            AppUtils.EMOJI_FILTER
        )
        binding.edtContent.filters = arrayOf(
            AppUtils.EMOJI_FILTER, InputFilter.LengthFilter(2000)
        )
        binding.edtEmail.doOnTextChanged { _, _, _, _ ->
            validEmail(binding.edtEmail.text.toString())
            setEnableButtonConfirm((isValidContent && isValidEmail))
        }

        binding.edtContent.doOnTextChanged { _, _, _, _ ->
            validContent(binding.edtContent.text.toString())
            setEnableButtonConfirm((isValidContent && isValidEmail))
        }
        clickListener()
    }

    private fun clickListener() {
        binding.btnCancel.clickWithDebounce(1000) {
            try {
                finish()
            } catch (ex: Exception) {
                //
            }
        }
        binding.btnConfirm.clickWithDebounce(1000) {
//            sendFeedback(getPositionId())
        }
    }

    private fun getPositionId(): Int {
        when (binding.spnTopicFeedback.selectedItemPosition) {
            0, 1 -> return 0
            2 -> return 1
        }
        return -1
    }

    override fun initData() {
        //
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        //
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        //
    }

//    private fun sendFeedback(type: Int) {
//        EasyHttp.post(this).api(
//            FeedbackAPI.params(
//                binding.edtEmail.text.toString(), binding.edtContent.text.toString(), type
//            )
//        ).request(object : HttpCallback<HttpData<String?>>(this) {
//            override fun onSucceed(result: HttpData<String?>) {
//                if (!result.isRequestSucceed()) {
//                    toast(result.getMessage())
//                    hideDialog()
//                    binding.btnConfirm.isEnabled = true
//                } else {
//                    if (result.isRequestSucceed()) {
//                        toast(getString(R.string.text_success_feedback))
//                    } else {
//                        toast(result.getMessage())
//                    }
//                    finish()
//                    hideDialog()
//                }
//            }
//        })
//    }

    private fun validEmail(email: String) {
        val content = binding.edtContent.text.toString()
        if (email.isEmpty()) {
            binding.tvErrorEmail.text = getString(R.string.empty_mail)
            binding.tvErrorEmail.visibility = View.VISIBLE
            isValidEmail = false
        } else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (content.isNotEmpty() && content.length <= 2000) {
                binding.tvErrorContent.text = ""
                binding.tvErrorContent.visibility = View.GONE
                binding.tvErrorEmail.text = ""
                binding.tvErrorEmail.visibility = View.GONE
                isValidEmail = true
                isValidContent = true
            } else {
                binding.tvErrorEmail.text = ""
                binding.tvErrorEmail.visibility = View.GONE
                isValidEmail = true
            }
        } else {
            if (email.length > 50) {
                binding.tvErrorEmail.text = getString(R.string.email_invalid_length)
                binding.tvErrorEmail.visibility = View.VISIBLE
                isValidEmail = false
            } else {
                binding.tvErrorEmail.text = getString(R.string.invalid_email)
                binding.tvErrorEmail.visibility = View.VISIBLE
                isValidEmail = false
            }
        }
    }

    private fun validContent(content: String) {
        val email = binding.edtEmail.text.toString()
        if (content.isEmpty()) {
            binding.tvErrorContent.text = getString(R.string.empty_feedback)
            binding.tvErrorContent.visibility = View.VISIBLE
            isValidContent = false
        } else if (content.length <= 2000) {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tvErrorContent.text = ""
                binding.tvErrorContent.visibility = View.GONE
                binding.tvErrorEmail.text = ""
                binding.tvErrorEmail.visibility = View.GONE
                isValidContent = true
                isValidEmail = true
            } else {
                binding.tvErrorContent.text = ""
                binding.tvErrorContent.visibility = View.GONE
                isValidContent = true
            }
        } else {
            binding.tvErrorContent.text = getString(R.string.res_content_feedback_characters)
            binding.tvErrorContent.visibility = View.VISIBLE
            isValidContent = false
        }
    }

    fun setEnableButtonConfirm(isEnable: Boolean) {
        if(isEnable)
        {
            binding.btnConfirm.isEnabled = true
            binding.tvCreate.isEnabled = true
        }else{
            binding.btnConfirm.isEnabled = false
            binding.tvCreate.isEnabled = false
        }
    }


}