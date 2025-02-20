package vn.xdeuhug.seniorsociable.personalPage.ui.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.dimen
import org.jetbrains.anko.topPadding
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import vn.xdeuhug.seniorsociable.personalPage.databinding.ActivityEditInforBasicBinding
import vn.xdeuhug.seniorsociable.personalPage.ui.dialog.WorkAtDialog
import vn.xdeuhug.seniorsociable.ui.dialog.CustomDatePickerDialog
import vn.xdeuhug.seniorsociable.ui.dialog.PlaceDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 02 / 12 / 2023
 */
class EditInforBasicActivity : AppActivity() {
    private var isValidFullName = false
    private lateinit var binding: ActivityEditInforBasicBinding
    private var user = UserCache.getUser()
    private var addressUpdate = Address()
    private var birthdayUpdate = ""
    override fun getLayoutView(): View {
        binding = ActivityEditInforBasicBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setDataForView()
        setTitleBar()
        setClickBirthday()
        setClickGender()
        setClickAddress()
        setClickSave()
        setNameChange()
        setLayoutRightTitleBar()
        validUpdate()
    }

    private fun setNameChange() {
        binding.edtName.doOnTextChanged { _, _, _, _ ->
            if (binding.edtName.length() > 50) {
                binding.edtName.setText(binding.edtName.text.toString().substring(0, 50))
                binding.edtName.setSelection(binding.edtName.text!!.length)
                toast(getString(R.string.res_max_50_characters))
            } else {
                validName(binding.edtName.text.toString().trim())
            }
            validUpdate()
        }
    }

    private fun setClickSave() {
        binding.tbTitle.rightView.clickWithDebounce {
            if (binding.tbTitle.rightView.isSelected) {
                val userUpdate = user
                userUpdate.name = binding.edtName.text.toString()
                userUpdate.nameNormalize = AppUtils.removeVietnameseFromStringNice(binding.edtName.text.toString())
                userUpdate.gender = getGender()
                userUpdate.maritalStatus = getMaritalStatus()
                userUpdate.address = addressUpdate
                userUpdate.birthday = binding.tvBirthday.text.toString().trim()
                showDialog(getString(R.string.is_processing))
                UserManagerFSDB.updateUserInfo(
                    userUpdate,
                    object : UserManagerFSDB.Companion.FireStoreCallback<Unit> {
                        override fun onSuccess(result: Unit) {
                            hideDialog()
                            UserCache.saveUser(userUpdate)
                            finish()
                        }

                        override fun onFailure(exception: Exception) {
                            toast(R.string.please_try_later)
                            hideDialog()
                        }

                    })
            } else {
                toast(getString(R.string.please_update_full_information))
            }
        }
    }

    private fun setDataForView() {
        birthdayUpdate = user.birthday
        addressUpdate = user.address
        binding.edtName.setText(user.name)
        isValidFullName = user.name.isNotEmpty()
        binding.tvBirthday.text = user.birthday.ifEmpty { getString(R.string.not_update) }
        when (user.gender) {
            AppConstants.NOT_UPDATE, AppConstants.MAN -> {
                binding.rdbMan.isChecked = true
                binding.rdbWoman.isChecked = false
            }

            AppConstants.WOMAN -> {
                binding.rdbMan.isChecked = false
                binding.rdbWoman.isChecked = true
            }
        }

        when (user.maritalStatus) {
            AppConstants.MARITAL_STATUS_NOT_UPDATE, AppConstants.MARITAL_STATUS_SINGLE -> {
                binding.rdbSingle.isChecked = true
                binding.rdbMarried.isChecked = false
            }

            AppConstants.MARITAL_STATUS_MARRIED -> {
                binding.rdbSingle.isChecked = false
                binding.rdbMarried.isChecked = true
            }
        }
        binding.tvAddress.text = user.address.address.ifEmpty { getString(R.string.not_update) }
    }

    private fun setButtonUpdate(allowUpdate: Boolean) {
        binding.tbTitle.rightView.isSelected =allowUpdate
    }

    @SuppressLint("SimpleDateFormat")
    private fun setClickBirthday() {

        binding.tvBirthday.clickWithDebounce(1000) {
            val sdf = SimpleDateFormat(AppConstants.DATE_FORMAT)
            val calendar = Calendar.getInstance()
            val calendarMin = Calendar.getInstance()
            calendarMin.add(Calendar.YEAR, -16)
            if (binding.tvBirthday.text.toString()
                    .isNotEmpty() && binding.tvBirthday.text.toString() != getString(R.string.not_update)
            ) {
                val date = sdf.parse(binding.tvBirthday.text.toString())
                calendar.time = date!!
            }
            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH]
            val day = calendar[Calendar.DAY_OF_MONTH]

            val datePickerDialog = CustomDatePickerDialog(
                getContext(), year, month, day
            )
            datePickerDialog.datePicker.maxDate = calendarMin.time.time
            datePickerDialog.setButton(
                DialogInterface.BUTTON_POSITIVE, getString(R.string.mdtp_ok)
            ) { _, _ ->
                val datePicker = datePickerDialog.datePicker
                val dYear = datePicker.year
                val dMonth = datePicker.month
                val dayOfMonth = datePicker.dayOfMonth
                val dCalendar = Calendar.getInstance()
                dCalendar[dYear, dMonth] = dayOfMonth
                val dateFormat = SimpleDateFormat(
                    AppConstants.DATE_FORMAT, Locale.getDefault()
                )
                val selectedDate: String = dateFormat.format(dCalendar.time)
                binding.tvBirthday.text = selectedDate
                birthdayUpdate = selectedDate
                validUpdate()
            }
            datePickerDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)
            ) { _, _ ->
                // Logic xử lý khi nhấn nút "Cancel"
            }
            datePickerDialog.setOnShowListener {
                val positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                positiveButton?.setTextColor(ContextCompat.getColor(getContext(), R.color.blue_700))
                val negativeButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                negativeButton?.setTextColor(ContextCompat.getColor(getContext(), R.color.blue_700))
            }
            datePickerDialog.show()
        }
    }

    private fun setClickGender() {
        //
    }

    private fun setClickAddress() {
        binding.tvAddress.clickWithDebounce {
            PlaceDialog.Builder(getContext(), getString(R.string.address), addressUpdate)
                .onActionDone(object : PlaceDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean, address: Address?) {
                        if (isConfirm) {
                            addressUpdate = address!!
                            binding.tvAddress.text = address.address.ifEmpty { getString(R.string.not_update) }
                        }
                        validUpdate()
                    }

                }).create().show()
        }
    }

    private fun setTitleBar() {
        binding.tbTitle.rightView.allCaps = true
    }

    private fun validName(name: String) {
        if (name.isEmpty()) {
            binding.tvErrorName.text = getString(R.string.res_min_characters)
            binding.tvErrorName.show()
            isValidFullName = false
        } else {
            if (name.length < 2) {
                binding.tvErrorName.text = getString(R.string.res_min_characters)
                binding.tvErrorName.show()
                isValidFullName = false
            } else {
                binding.tvErrorName.text = ""
                binding.tvErrorName.hide()
                isValidFullName = true
            }
        }
    }

    private fun validUpdate()
    {
        if(birthdayUpdate.isNotEmpty() && birthdayUpdate != getString(R.string.not_update)
            && (addressUpdate.longitude != 0.00 && addressUpdate.latitude != 0.00 && (addressUpdate.address != "" || addressUpdate.fullAddress != "")) && isValidFullName
        )
        {
            setButtonUpdate(true)
        }else{
            setButtonUpdate(false)
        }
    }

    override fun initData() {
        //
    }

    private fun getGender(): Int {
        return if (binding.rdbMan.isChecked) {
            AppConstants.MAN
        } else {
            AppConstants.WOMAN
        }
    }

    private fun getMaritalStatus(): Int {
        return if (binding.rdbMarried.isChecked) {
            AppConstants.MARITAL_STATUS_MARRIED
        } else {
            AppConstants.MARITAL_STATUS_SINGLE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setLayoutRightTitleBar() {
        binding.tbTitle.rightView.isSelected = false
        val layout = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, // Width
            ViewGroup.LayoutParams.WRAP_CONTENT, // Height
            Gravity.CENTER_VERTICAL or Gravity.END
        )
        binding.tbTitle.rightView.layoutParams = layout
        binding.tbTitle.rightView.topPadding = getContext().dimen(R.dimen.dp_4)
        binding.tbTitle.rightView.bottomPadding = getContext().dimen(R.dimen.dp_4)
        binding.tbTitle.rightView.allCaps = true
        binding.tbTitle.rightView.background =
            getDrawable(R.drawable.button_right_titlebar_selected_dp_8)
    }
}