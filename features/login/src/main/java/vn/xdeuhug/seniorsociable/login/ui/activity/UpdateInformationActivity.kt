package vn.xdeuhug.seniorsociable.login.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.jetbrains.anko.startActivity
import pyxis.uzuki.live.mediaresizer.MediaResizerGlobal
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.login.constants.LogInConstants
import vn.xdeuhug.seniorsociable.login.databinding.ActivityUpdateInformationBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.ui.activity.BrowserActivity
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.ui.dialog.CustomDatePickerDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.BCryptUtils
import vn.xdeuhug.seniorsociable.utils.PhotoPickerUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.system.exitProcess


/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 09/09/2023
 */
@Suppress("DEPRECATION")
class UpdateInformationActivity : AppActivity() {
    private var twice = false
    private var isValidPasswordIsTheSame = false
    private var isValidConfirmPassword = false
    private var isValidPassword = false
    private var isValidFullName = false
    private var isValidBirthday = false
    private lateinit var binding: ActivityUpdateInformationBinding
    private var mediaUrl = ""
    private var localMedia = ArrayList<LocalMedia>()
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var isChoosePicture = false
    private var phoneNumber = ""
    private var idUser = ""
    private lateinit var storageReference: StorageReference
    override fun getLayoutView(): View {
        binding = ActivityUpdateInformationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        MediaResizerGlobal.initializeApplication(this)
        storageReference = FirebaseStorage.getInstance().reference
        setBackButton()
        phoneNumber = intent.getStringExtra(LogInConstants.PHONE_NUMBER).toString()
        idUser = intent.getStringExtra(LogInConstants.USER_ID).toString()
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    localMedia = PictureSelector.obtainSelectorList(data) as ArrayList<LocalMedia>
                    PhotoShowUtils.loadAvatarImage(
                        "",PictureSelector.obtainSelectorList(data).first().realPath, binding.imvAvatar
                    )
                    validChooseImage()
                } else {
                    // Error and not choose
                    validChooseImage()
                }
            }
        setTextParseHTML()
        setClickButton()
        setEditTextChange()
        setClickButtonComplete()
    }

    private fun validChooseImage() {
        if (localMedia.isEmpty()) {
            isChoosePicture = false
            binding.tvErrorImage.show()
            binding.tvErrorImage.text = getString(R.string.please_choose_avatar)
        } else {
            isChoosePicture = true
            binding.tvErrorImage.hide()
            binding.tvErrorImage.text = ""
        }
    }

    private fun setClickButtonComplete() {
        binding.btnComplete.clickWithDebounce {
//            val imageUri = Uri.fromFile(File(localMedia.first().realPath))
//            val resizeOption = ImageResizeOption.Builder()
//                .setImageProcessMode(ImageMode.ResizeAndCompress)
//                .setImageResolution(1280, 720)
//                .setBitmapFilter(false)
//                .setCompressFormat(Bitmap.CompressFormat.JPEG)
//                .setCompressQuality(75)
//                .setScanRequest(ScanRequest.TRUE)
//                .build()
//
//            val option = ResizeOption.Builder()
//                .setMediaType(MediaType.IMAGE)
//                .setImageResizeOption(resizeOption)
//                .setTargetPath(imageUri.path!!)
////                .setOutputPath(imageFile.absolutePath)
//                .setCallback { code, output ->  // doesn't require when using ```processSynchronously```
//                    when (code) {
//                        MediaResizer.RESIZE_SUCCESS -> {
//                            // Xử lý thành công, output là đường dẫn đến hình ảnh đã xử lý
//                            // Hiển thị hình ảnh đã xử lý trong ImageView hoặc làm bất kỳ việc gì bạn muốn.
//                            val imageUri = Uri.fromFile(File(output))
//                            val imageId = UUID.randomUUID().toString()
//                            // Defining the child of storageReference
//                            val ref: StorageReference = storageReference.child("upload/$idUser/avatar/$imageId")
//                            ref.putFile(imageUri).addOnCompleteListener {
//                                //
//                                Timber.tag("Đã thêm hình").d("id: $imageId")
//                                val newUser = User(
//                                    imageId,
//                                    binding.edtBirthday.text.toString(),
//                                    binding.edtName.text.toString().trim(),
//                                    getGender(),
//                                    idUser,
//                                    phoneNumber,
//                                    Date(),
//                                    "",
//                                    true,
//                                    LogInConstants.TYPE_PHONE
//                                )
//                                UserManagerFSDB.addUser(newUser,
//                                    object : UserManagerFSDB.Companion.FireStoreCallback<Boolean> {
//                                        override fun onSuccess(result: Boolean) {
//                                            Timber.tag("Add User").d("Done !!!")
//                                            UserCache.saveUser(newUser)
//                                            goToHome()
//                                        }
//
//                                        override fun onFailure(exception: Exception) {
//                                            // Xử lý lỗi nếu có
//                                            Timber.tag("Exception").e(exception)
//                                            finish()
//                                        }
//                                    })
//                            }.addOnCanceledListener {
//                                //
//                                toast(R.string.please_try_later)
//                            }.addOnProgressListener {
//                                // Đang upload
//                                showDialog(R.string.is_processing)
//                            }
//                        }
//
//                        MediaResizer.RESIZE_FAILED -> {
//                            // Xử lý lỗi, output có thể chứa thông tin về lỗi
//                            // Hiển thị thông báo lỗi cho người dùng hoặc xử lý lỗi theo cách khác.
//                        }
//                    }
//                }
//                .build()
//            MediaResizer.process(option)
            val imageUri = Uri.fromFile(File(localMedia.first().realPath))
            val imageId = UUID.randomUUID().toString()
            // Defining the child of storageReference
            val ref: StorageReference =
                storageReference.child("${UploadFireStorageUtils.getRootURLAvatarById(idUser)}$imageId")

            showDialog(R.string.is_processing)
            ref.putFile(imageUri).addOnCompleteListener {
                //
                CoroutineScope(Dispatchers.Main).launch {
                    val downloadUrl = ref.downloadUrl.await()
                    Timber.tag("Đã thêm hình").d("id: $imageId")
                    val newUser = User(
                        downloadUrl.toString(),
                        binding.edtBirthday.text.toString(),
                        binding.edtName.text.toString().trim(),
                        getGender(),
                        idUser,
                        BCryptUtils.hashPassword(binding.edtPassword.text.toString().trim()),
                        phoneNumber,
                        Date(),
                        "",
                        true,
                        LogInConstants.TYPE_PHONE
                    )
                    newUser.nameNormalize = AppUtils.removeVietnameseFromStringNice(newUser.name)
                    UserManagerFSDB.addUser(newUser,
                        object : UserManagerFSDB.Companion.FireStoreCallback<Boolean> {
                            override fun onSuccess(result: Boolean) {
                                Timber.tag("Add User").d("Done !!!")
                                UserCache.saveUser(newUser)
                                hideDialog()
                                goToHome()
                            }

                            override fun onFailure(exception: Exception) {
                                // Xử lý lỗi nếu có
                                Timber.tag("Exception").e(exception)
                                hideDialog()
                                finish()
                            }
                        })
                }
            }.addOnCanceledListener {
                //
                toast(R.string.please_try_later)
                hideDialog()
            }.addOnProgressListener {
                // Đang upload
            }
        }
    }

    private fun setBackButton() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (twice) {
                    exitProcess(0)
                }
                twice = true
                toast(getString(R.string.back))
                postDelayed({ twice = false }, 2000)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setEditTextChange() {
        binding.edtPassword.filters = arrayOf(
            AppUtils.EMOJI_FILTER, AppUtils.spaceCharacters, InputFilter.LengthFilter(21)
        )
        binding.edtConfirmPassword.filters = arrayOf(
            AppUtils.EMOJI_FILTER, AppUtils.spaceCharacters, InputFilter.LengthFilter(21)
        )

        binding.edtName.filters = arrayOf(
            AppUtils.EMOJI_FILTER, InputFilter.LengthFilter(51)
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

        binding.edtName.doOnTextChanged { _, _, _, _ ->
            if (binding.edtName.length() > 50) {
                binding.edtName.setText(binding.edtName.text.toString().substring(0, 50))
                binding.edtName.setSelection(binding.edtName.text!!.length)
                toast(getString(R.string.res_max_50_characters))
            } else {
                validName(binding.edtName.text.toString())
            }
            checkValidButtonUpdateInformation()
        }

        binding.cbSelected.setOnClickListener {
            checkValidButtonUpdateInformation()
        }
    }

    private fun checkValidButtonUpdateInformation() {
        binding.btnComplete.isEnabled =
            isValidPassword && isValidConfirmPassword && isValidPasswordIsTheSame && binding.cbSelected.isChecked && isValidBirthday && isValidFullName && isChoosePicture
    }

    @SuppressLint("SimpleDateFormat")
    private fun setClickButton() {
        binding.edtBirthday.clickWithDebounce(1000) {
            val sdf = SimpleDateFormat(AppConstants.DATE_FORMAT)
            val calendar = Calendar.getInstance()
            val calendarMin = Calendar.getInstance()
            calendarMin.add(Calendar.YEAR, -16)
            if (binding.edtBirthday.text.toString()
                    .isNotEmpty() && binding.edtBirthday.hint.toString() != getString(R.string.birthday)
            ) {
                val date = sdf.parse(binding.edtBirthday.text.toString())
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
                binding.edtBirthday.text = selectedDate
                binding.edtBirthday.hint = ""
                validBirthday()
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

        //
        binding.imvAvatar.setOnClickListener {
            PhotoPickerUtils.showImagePickerChooseAvatarNotGif(this, launcher)
        }
    }

    private fun setTextParseHTML() {
        //
        val htmlText = getString(R.string.url_policy)
        binding.tvUrlPolicy.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
        binding.tvUrlPolicy.movementMethod = LinkMovementMethod.getInstance()

        val spans = binding.tvUrlPolicy.text as Spannable
        val clickableSpanPolicy = object : ClickableSpan() {
            override fun onClick(widget: View) {
                BrowserActivity.start(
                    getContext(), AppConstants.PRIVACY_POLICY, getString(R.string.privacy_policy)
                )
            }
        }

        val clickableSpanTerms = object : ClickableSpan() {
            override fun onClick(widget: View) {
                BrowserActivity.start(
                    getContext(), AppConstants.TERMS_OF_USE, getString(R.string.terms_of_use)
                )
            }
        }

        // Tìm vị trí của các liên kết trong chuỗi HTML
        val startPolicy = htmlText.indexOf("<b>Chính sách bảo mật</b>")
        val endPolicy = (startPolicy + "<b>Chính sách bảo mật</b>".length) - 7
        val startTerms = htmlText.indexOf("<b>Điều khoản sử dụng</b>") - 7
        val endTerms = startTerms + "<b>Điều khoản sử dụng</b>".length - 7

        // Áp dụng sự kiện click cho các liên kết
        spans.setSpan(
            clickableSpanPolicy,
            startPolicy,
            endPolicy,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spans.setSpan(
            clickableSpanTerms,
            startTerms,
            endTerms,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spans.setSpan(
            ForegroundColorSpan(getColor(R.color.blue_app_senior_sociable)),
            endTerms + 5,
            endTerms + 20,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
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

    private fun validBirthday() {
        if (binding.edtBirthday.hint.toString() == getString(R.string.birthday)) {
            binding.tvErrorBirthday.text = getString(R.string.please_choose_birthday)
            binding.tvErrorBirthday.show()
            isValidBirthday = false
        } else {
            binding.tvErrorBirthday.text = ""
            binding.tvErrorBirthday.hide()
            isValidBirthday = true
        }
        checkValidButtonUpdateInformation()
    }

    private fun getGender(): Int {
        return if (binding.rdbMan.isChecked) {
            AppConstants.MAN
        } else {
            AppConstants.WOMAN
        }
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

}