package vn.xdeuhug.seniorsociable.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import org.jetbrains.anko.textColor
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.databinding.DialogAccountBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.other.onTextChangeListener
import vn.xdeuhug.seniorsociable.ui.adapter.MediaAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.clickWithDebounce
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 29 / 12 / 2023
 */
class AccountDialog {
    @SuppressLint("SetTextI18n")
    class Builder(
        context: Context, var user: User, var typeDialog: Int
    ) : BaseDialog.Builder<Builder>(context), AdapterView.OnItemSelectedListener {
        //
        companion object {
            const val TYPE_LOCK = 1
            const val TYPE_OPEN = 2
        }

        //
        private var binding: DialogAccountBinding =
            DialogAccountBinding.inflate(LayoutInflater.from(context))

        //
        lateinit var onActionDone: OnActionDone

        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(true)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            getDialog()?.window?.setBackgroundDrawableResource(R.drawable.bg_border_dialog)
            setWidth(Resources.getSystem().displayMetrics.widthPixels * 9 / 10)
            when(typeDialog)
            {
                TYPE_LOCK ->{
                    setHeight(Resources.getSystem().displayMetrics.heightPixels * 2 / 3)
                }
                TYPE_OPEN ->{
                    setHeight(Resources.getSystem().displayMetrics.heightPixels * 1 / 2)
                }
            }
            setViewForTypeDialog()
            setDataForView()
            setEditTextReason()
            setDataSpinner()
            setDateTextView()
            binding.tvCountReason.text = "0 / 100"
            binding.btnCancel.clickWithDebounce {
                dismiss()
            }

            binding.btnConfirm.clickWithDebounce {
                dismiss()
                if(typeDialog == TYPE_OPEN)
                {
                    user.typeActive = AppConstants.ACTIVATING
                    user.reasonBlock = ""
                    user.timeStartBlock = null
                    user.timeEndBlock = null
                    onActionDone.onActionDone(
                        false, "",
                        AppConstants.ACTIVATING, null, null, user
                    )
                }else{
                    when (binding.spnFilter.selectedItemPosition) {
                        AppConstants.LOCK_FOREVER -> {
                            user.typeActive = AppConstants.BLOCKED_UN_LIMITED
                            user.reasonBlock = binding.edtReason.text.toString().trim()
                            user.timeStartBlock = null
                            user.timeEndBlock = null
                            onActionDone.onActionDone(
                                true, binding.edtReason.text.toString().trim(),
                                AppConstants.BLOCKED_UN_LIMITED, null, null, user
                            )
                        }

                        AppConstants.LOCK_ONE_WEAK -> {
                            val startDate = Date()
                            user.typeActive = AppConstants.BLOCKING
                            user.reasonBlock = binding.edtReason.text.toString().trim()
                            user.timeStartBlock = startDate
                            user.timeEndBlock = DateUtils.addDays(startDate, 7)
                            onActionDone.onActionDone(
                                true,
                                binding.edtReason.text.toString().trim(),
                                AppConstants.BLOCKING,
                                startDate,
                                DateUtils.addDays(startDate, 7),
                                user
                            )
                        }

                        AppConstants.LOCK_ONE_MONTH -> {
                            val startDate = Date()
                            val endDate = DateUtils.addDays(startDate, DateUtils.getDaysInCurrentMonth())
                            user.typeActive = AppConstants.BLOCKING
                            user.reasonBlock = binding.edtReason.text.toString().trim()
                            user.timeStartBlock = startDate
                            user.timeEndBlock = endDate
                            onActionDone.onActionDone(
                                true,
                                binding.edtReason.text.toString().trim(),
                                AppConstants.BLOCKING,
                                startDate,
                                endDate,
                                user
                            )
                        }

                        AppConstants.LOCK_ONE_YEAR -> {
                            val startDate = Date()
                            val endDate = DateUtils.addDays(startDate, 365)
                            user.typeActive = AppConstants.BLOCKING
                            user.reasonBlock = binding.edtReason.text.toString().trim()
                            user.timeStartBlock = startDate
                            user.timeEndBlock = endDate
                            onActionDone.onActionDone(
                                true,
                                binding.edtReason.text.toString().trim(),
                                AppConstants.BLOCKING,
                                startDate,
                                endDate,
                                user
                            )
                        }

                        AppConstants.LOCK_LIMITED -> {
                            val pairDate = DateUtils.getDateCurrentAndEndDate(binding.tvTimeEnd.text.toString().trim())
                            user.typeActive = AppConstants.BLOCKING
                            user.reasonBlock = binding.edtReason.text.toString().trim()
                            user.timeStartBlock = pairDate.first
                            user.timeEndBlock = pairDate.second
                            onActionDone.onActionDone(
                                true,
                                binding.edtReason.text.toString().trim(),
                                AppConstants.BLOCKING,
                                pairDate.first,
                                pairDate.second,
                                user
                            )
                        }
                    }
                }

            }
            binding.edtReason.onTextChangeListener(0) {
                binding.btnConfirm.isEnabled = it.length >= 10
            }
        }

        @SuppressLint("SimpleDateFormat")
        private fun setDateTextView() {
            val sdf = SimpleDateFormat(DateUtils.DATE_FORMAT)
            val currentDate = sdf.format(Date())
            val calendarD = Calendar.getInstance()
            calendarD.add(Calendar.MONTH, 0)
            calendarD[Calendar.DATE] = calendarD.getActualMinimum(Calendar.DAY_OF_MONTH)
            binding.tvTimeStart.text = currentDate
            binding.tvTimeEnd.text = currentDate
            binding.tvTimeStart.isEnabled = false
            binding.tvTimeEnd.clickWithDebounce {
                val date = sdf.parse(binding.tvTimeEnd.text.toString())
                val calendar = Calendar.getInstance()
                calendar.time = date!!
                val year = calendar[Calendar.YEAR]
                val month = calendar[Calendar.MONTH]
                val day = calendar[Calendar.DAY_OF_MONTH]

                val datePickerDialog = CustomDatePickerDialog(
                    getContext(), year, month, day
                )
                val dateMin = sdf.parse(binding.tvTimeStart.text.toString())
                datePickerDialog.datePicker.minDate = dateMin!!.time
                datePickerDialog.setButton(
                    DialogInterface.BUTTON_POSITIVE, getString(R.string.mdtp_ok)
                ) { _, _ ->
                    val datePicker = datePickerDialog.datePicker
                    val dYear = datePicker.year
                    val dMonth = datePicker.month
                    val dayOfMonth = datePicker.dayOfMonth
                    val dCalendar = Calendar.getInstance()
                    dCalendar[dYear, dMonth] = dayOfMonth
                    val dateFormat =
                        SimpleDateFormat(DateUtils.DATE_FORMAT, Locale.getDefault())
                    val selectedDate: String = dateFormat.format(dCalendar.time)
                    binding.tvTimeEnd.text = selectedDate
                }
                datePickerDialog.setButton(
                    DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)
                ) { _, _ ->
                    // Logic xử lý khi nhấn nút "Cancel"
                }
                datePickerDialog.setOnShowListener {
                    val positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    positiveButton?.setTextColor(
                        ContextCompat.getColor(
                            getContext(),
                            R.color.blue_700
                        )
                    )
                    val negativeButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    negativeButton?.setTextColor(
                        ContextCompat.getColor(
                            getContext(),
                            R.color.blue_700
                        )
                    )
                }
                datePickerDialog.show()
            }
            binding.tvTimeStart.clickWithDebounce(1000) {
                val date = sdf.parse(binding.tvTimeStart.text.toString())
                val calendar = Calendar.getInstance()
                calendar.time = date!!
                val year = calendar[Calendar.YEAR]
                val month = calendar[Calendar.MONTH]
                val day = calendar[Calendar.DAY_OF_MONTH]

                val datePickerDialog = CustomDatePickerDialog(
                    getContext(), year, month, day
                )
                datePickerDialog.datePicker.maxDate = Date().time
                datePickerDialog.setButton(
                    DialogInterface.BUTTON_POSITIVE, getString(R.string.mdtp_ok)
                ) { _, _ ->
                    val datePicker = datePickerDialog.datePicker
                    val dYear = datePicker.year
                    val dMonth = datePicker.month
                    val dayOfMonth = datePicker.dayOfMonth
                    val dCalendar = Calendar.getInstance()
                    dCalendar[dYear, dMonth] = dayOfMonth
                    val dateFormat =
                        SimpleDateFormat(DateUtils.DATE_FORMAT, Locale.getDefault())
                    val selectedDate: String = dateFormat.format(dCalendar.time)
                    binding.tvTimeStart.text = selectedDate
                    val fromDate = sdf.parse(selectedDate)
                    val toDate = sdf.parse(binding.tvTimeEnd.text.toString())
                    if (fromDate!! > toDate) {
                        binding.tvTimeEnd.text = selectedDate
                    }
                }
                datePickerDialog.setButton(
                    DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)
                ) { _, _ ->
                    // Logic xử lý khi nhấn nút "Cancel"
                }
                datePickerDialog.setOnShowListener {
                    val positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    positiveButton?.setTextColor(
                        ContextCompat.getColor(
                            getContext(),
                            R.color.blue_700
                        )
                    )
                    val negativeButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    negativeButton?.setTextColor(
                        ContextCompat.getColor(
                            getContext(),
                            R.color.blue_700
                        )
                    )
                }
                datePickerDialog.show()
            }
        }


        private fun setDataSpinner() {
            val arrayText = arrayListOf(
                getString(R.string.lock_one_weak),
                getString(R.string.lock_one_month),
                getString(R.string.lock_one_year),
                getString(R.string.is_lock_un_limited),
                getString(R.string.have_time_end),
            )

            val adapterFilter =
                ArrayAdapter(getContext(), R.layout.simple_spinner_item_custom, arrayText)
            adapterFilter.setDropDownViewResource(R.layout.simple_list_item_activated_custom)
            binding.spnFilter.adapter = adapterFilter
            binding.spnFilter.onItemSelectedListener = this
            binding.spnFilter.setSelection(0)
            binding.imvDrownList.clickWithDebounce {
                binding.spnFilter.performClick()
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setViewForTypeDialog() {
            when (typeDialog) {
                TYPE_LOCK -> {
                    binding.llLockAccount.show()
                    binding.tvContent.text =
                        "${getString(R.string.confirm_lock_account)} ${user.name} ?"
                    binding.btnConfirm.isEnabled = false
                }

                TYPE_OPEN -> {
                    binding.llLockAccount.hide()
                    binding.btnConfirm.isEnabled = true
                    binding.tvContent.text =
                        "${getString(R.string.confirm_open_account)} ${user.name} ?"
                }
            }
        }

        private fun setDataForView() {
            setLockView(user)
            binding.tvNameUser.text = user.name
            PhotoShowUtils.loadAvatarImage(user.avatar, binding.imvAvatar)
            binding.tvJoinIn.text = TimeUtils.getJoinIn(user.timeCreated, getContext())
            setActiveView(user)
            setLoginWith(user)
        }

        @SuppressLint("SetTextI18n")
        private fun setEditTextReason() {
            binding.edtReason.doOnTextChanged { text, start, before, count ->
                binding.tvCountReason.text = "${binding.edtReason.length()} / 100"
            }
        }

        private fun setLockView(item: User) {
            when (item.typeActive) {
                AppConstants.ACTIVATING -> {
                    binding.imvStatus.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_post_is_locked)
                    binding.imvStatus.setColorFilter(
                        ContextCompat.getColor(
                            getContext(),
                            vn.xdeuhug.seniorsociable.R.color.red_600
                        )
                    )
                    binding.tvStatus.text =
                        getString(vn.xdeuhug.seniorsociable.R.string.lock_account)
                    binding.tvStatus.textColor = getColor(vn.xdeuhug.seniorsociable.R.color.red_600)
                }

                else -> {
                    binding.imvStatus.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_post_reopen)
                    binding.imvStatus.setColorFilter(
                        ContextCompat.getColor(
                            getContext(),
                            vn.xdeuhug.seniorsociable.R.color.green_008
                        )
                    )
                    binding.tvStatus.text =
                        getString(vn.xdeuhug.seniorsociable.R.string.open_account)
                    binding.tvStatus.textColor =
                        getColor(vn.xdeuhug.seniorsociable.R.color.green_008)
                }
            }
        }

        private fun setActiveView(item: User) {
            when (item.typeActive) {
                AppConstants.ACTIVATING -> {
                    binding.imvStatus.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_post_reopen)
                    binding.imvStatus.setColorFilter(
                        ContextCompat.getColor(
                            getContext(),
                            vn.xdeuhug.seniorsociable.R.color.green_008
                        )
                    )
                    binding.tvStatus.text = getString(vn.xdeuhug.seniorsociable.R.string.is_active)
                    binding.tvStatus.textColor =
                        getColor(vn.xdeuhug.seniorsociable.R.color.green_008)
                    binding.tvReasonLock.hide()
                }

                AppConstants.BLOCKING -> {
                    binding.imvStatus.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_post_is_locked)
                    binding.imvStatus.setColorFilter(
                        ContextCompat.getColor(
                            getContext(),
                            vn.xdeuhug.seniorsociable.R.color.red_600
                        )
                    )
                    binding.tvStatus.text = getString(vn.xdeuhug.seniorsociable.R.string.is_locked)
                    //
                    // User post
                    val spanText = SpannableStringBuilder()
                    val textLock = getString(R.string.temp_lock_to)
                    spanText.append("$textLock ")
                    spanText.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            //
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            textPaint.color = getColor(R.color.gray_900)
                            textPaint.isUnderlineText = false
                        }
                    }, spanText.length - textLock!!.length-1, spanText.length, 0)
                    //
                    val dateLock = DateUtils.getDateByFormatTimeDateSeconds(item.timeEndBlock!!)
                    spanText.append(dateLock)
                    spanText.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            //
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            textPaint.color = getColor(R.color.red_600)
                            textPaint.isUnderlineText = false
                            textPaint.isFakeBoldText = true
                        }
                    }, spanText.length - dateLock.length, spanText.length, 0)
                    //
                    binding.tvStatus.movementMethod = LinkMovementMethod.getInstance()
                    binding.tvStatus.text = spanText
                    binding.tvReasonLock.show()
                    binding.tvReasonLock.text = item.reasonBlock
                }

                AppConstants.BLOCKED_UN_LIMITED -> {
                    binding.imvStatus.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_post_is_locked)
                    binding.imvStatus.setColorFilter(
                        ContextCompat.getColor(
                            getContext(),
                            vn.xdeuhug.seniorsociable.R.color.red_600
                        )
                    )
                    binding.tvStatus.text =
                        getString(vn.xdeuhug.seniorsociable.R.string.is_lock_un_limited)
                    binding.tvStatus.textColor = getColor(vn.xdeuhug.seniorsociable.R.color.red_600)
                    binding.tvReasonLock.show()
                    binding.tvReasonLock.text = item.reasonBlock
                }
            }
        }

        private fun setLoginWith(item: User) {
            when (item.typeAccount) {
                AppConstants.TYPE_FACEBOOK -> {
                    binding.tvLoginWith.text = getString(vn.xdeuhug.seniorsociable.R.string.facebok)
                    binding.imvLoginWith.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_logo_facebook)
                }

                AppConstants.TYPE_GOOGLE -> {
                    binding.tvLoginWith.text = getString(vn.xdeuhug.seniorsociable.R.string.google)
                    binding.imvLoginWith.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_logo_google)
                }

                AppConstants.TYPE_PHONE -> {
                    binding.tvLoginWith.text = getString(vn.xdeuhug.seniorsociable.R.string.phone)
                    binding.imvLoginWith.setImageResource(vn.xdeuhug.seniorsociable.R.drawable.ic_login_phone)
                }
            }
        }


        interface OnActionDone {
            fun onActionDone(
                isBlock: Boolean,
                reasonBlock: String,
                typeActive: Int,
                timeStart: Date?,
                timeEnd: Date?,
                userNew: User
            )
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            when (binding.spnFilter.selectedItemPosition) {
                AppConstants.LOCK_LIMITED -> {
                    binding.llTimeUpLock.show()
                }

                else -> {
                    binding.llTimeUpLock.hide()
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            //
        }
    }
}