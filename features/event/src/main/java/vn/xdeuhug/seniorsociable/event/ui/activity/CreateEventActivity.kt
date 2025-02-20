package vn.xdeuhug.seniorsociable.event.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.Timepoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.dimen
import org.jetbrains.anko.topPadding
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.EventConstant
import vn.xdeuhug.seniorsociable.database.EventManagerFSDB
import vn.xdeuhug.seniorsociable.database.FireCloudManager
import vn.xdeuhug.seniorsociable.event.databinding.ActivityCreateEventBinding
import vn.xdeuhug.seniorsociable.model.entity.modelEvent.Event
import vn.xdeuhug.seniorsociable.model.entity.modelEvent.MemberJoinEvent
import vn.xdeuhug.seniorsociable.model.entity.modelEvent.TopicEvent
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import vn.xdeuhug.seniorsociable.ui.dialog.PlaceDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoPickerUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 12 / 2023
 */
class CreateEventActivity : AppActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityCreateEventBinding
    private var listLocalMedia = ArrayList<LocalMedia>()
    private var listMedia = ArrayList<MultiMedia>()
    private var typeDate = EventConstant.DATE_START
    private var typeHour = EventConstant.HOUR_START

    private var tpdStart: TimePickerDialog? = null
    private var tpdEnd: TimePickerDialog? = null

    private var address: Address? = null

    private var validContent = false
    private var validImage = false
    private var validAddress = false

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat(AppConstants.DATE_FORMAT)
    override fun getLayoutView(): View {
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setLauncher()
        setDataForView()
        setClickViewTime()
        setClickLocation()
        setLayoutRightTitleBar()
        setClickButtonSave()
        setListenerTextChange()
    }

    private fun validAll() {
        validContent()
        checkImage()
        checkAddress()
        updateViewSave()
    }

    private fun updateViewSave()
    {
        binding.tbTitle.rightView.isSelected = validAddress && validImage && validContent
    }

    private fun checkAddress() {
        if (validAddress) {
            binding.tvErrorAddress.hide()
            binding.tvErrorAddress.text = ""
        } else {
            binding.tvErrorAddress.show()
            binding.tvErrorAddress.text = getString(R.string.please_choose_location_organization)
        }
    }

    private fun checkImage() {
        if (validImage) {
            binding.tvErrorImage.hide()
            binding.tvErrorImage.text = ""
        } else {
            binding.tvErrorImage.show()
            binding.tvErrorImage.text = getString(R.string.please_choose_image)
        }
    }

    private fun setListenerTextChange() {
        binding.edtContent.filters = arrayOf(
            AppUtils.EMOJI_FILTER, InputFilter.LengthFilter(201)
        )

        binding.edtContent.doOnTextChanged { _, _, _, _ ->
            if (binding.edtContent.length() > 200) {
                binding.edtContent.setText(binding.edtContent.text.toString().substring(0, 200))
                binding.edtContent.setSelection(binding.edtContent.text!!.length)
                toast(getString(R.string.res_max_content_event))
                validContent = true
            } else {
                validContent()
            }
            updateViewSave()
        }
    }

    private fun setClickButtonSave() {
        binding.tbTitle.rightView.clickWithDebounce {
            validAll()
            if (binding.tbTitle.rightView.isSelected) {
                showDialog(getString(R.string.is_processing))
                val eventId = UUID.randomUUID().toString()
                val storageReference = FirebaseStorage.getInstance().reference
                val storageRef: StorageReference =
                    storageReference.child(UploadFireStorageUtils.getRootURLEventById(eventId))
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        FireCloudManager.Companion.uploadFilesToFirebaseStorage(listMedia, storageRef, object :
                            FireCloudManager.Companion.FireStoreCallback<ArrayList<MultiMedia>> {
                            override fun onSuccess(result: ArrayList<MultiMedia>) {
                                val event = createEvent(eventId,result)
                                addEventToFireStore(event)
                            }

                            override fun onFailure(result: ArrayList<MultiMedia>) {
                                toast(R.string.please_try_later)
                                hideDialog()
                            }
                        })
                    } catch (e: Exception) {
                        toast(R.string.please_try_later)
                        hideDialog()
                    }
                }
            }
        }
    }



    private fun createEvent(eventId: String, media: ArrayList<MultiMedia>): Event {
        val dateInit = Date()
        val sdf = SimpleDateFormat(DateUtils.DATE_FORMAT_1, Locale.getDefault())
        val dateStart = sdf.parse("${binding.tvTimeStart.text} ${binding.tvHourStart.text}")
        val dateEnd = sdf.parse("${binding.tvTimeEnd.text} ${binding.tvHourEnd.text}")
        val idUserCurrent = UserCache.getUser().id
        val memberJoin = MemberJoinEvent(
            idUserCurrent,
            dateInit,
            dateInit,
            "",
            EventConstant.STATUS_DEFAULT
        )
        return Event(
            eventId,
            idUserCurrent,
            dateInit,
            dateStart!!,
            dateEnd!!,
            arrayListOf(memberJoin),
            media.first().url,
            binding.edtContent.text.toString().trim(),
            EventConstant.NORMAL,
            address!!,
            TopicEvent()
        )
    }

    private fun addEventToFireStore(event: Event) {
        EventManagerFSDB.addEvent(event, object :
            EventManagerFSDB.Companion.FireStoreCallback<Boolean>{
            override fun onSuccess(result: Boolean) {
                if(result) {
                    toast(getString(R.string.create_event_success))
                } else {
                    toast(getString(R.string.please_try_later))
                }
                hideDialog()
                finish()
            }

            override fun onFailure(exception: Exception) {
                toast(getString(R.string.please_try_later))
                hideDialog()
            }
        })
    }

    private fun validContent() {
        val strContent = binding.edtContent.text.toString()
        if (strContent.isEmpty()) {
            binding.tvErrorContent.text = getString(R.string.res_not_empty)
            binding.tvErrorContent.show()
            validContent = false
        } else {
            if (strContent.length in 25..200) {
                // Valid text
                binding.tvErrorContent.text = ""
                binding.tvErrorContent.hide()
                validContent = true
            } else {
                binding.tvErrorContent.text = getString(R.string.res_min_content_event)
                binding.tvErrorContent.show()
                validContent = false
            }
        }
    }

    private fun setClickLocation() {
        binding.tvLocationEvent.clickWithDebounce {
            PlaceDialog.Builder(getContext(),getString(R.string.location_event),address)
                .onActionDone(object : PlaceDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean, address: Address?) {
                        if (isConfirm) {
                            this@CreateEventActivity.address = address
                            setViewAddress()
                            validAddress = this@CreateEventActivity.address != null
                            checkAddress()
                            updateViewSave()
                        }
                    }

                }).create().show()
        }
    }

    private fun setViewAddress() {
        binding.tvLocationEvent.text = address!!.fullAddress
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

    private fun setClickViewTime() {
        binding.tvTimeStart.clickWithDebounce {
            typeDate = EventConstant.DATE_START
            val now: Calendar = Calendar.getInstance()
            if (binding.tvTimeStart.text.toString().isNotEmpty()) {
                val date = sdf.parse(binding.tvTimeStart.text.toString())
                now.time = date!!
            }
            val dpd: DatePickerDialog = DatePickerDialog.newInstance(
                this@CreateEventActivity, now[Calendar.YEAR],  // Initial year selection
                now[Calendar.MONTH],  // Initial month selection
                now[Calendar.DAY_OF_MONTH] // Inital day selection
            )
            updateDateRangeForStartDate(dpd)
            dpd.accentColor = ContextCompat.getColor(this, R.color.blue_700)
            dpd.setOkText(getString(R.string.ok))
            dpd.setCancelText(getString(R.string.cancel))
            // If you're calling this from a support Fragment
            // If you're calling this from a support Fragment
            dpd.show(supportFragmentManager, "Datepickerdialog")
        }

        binding.tvHourStart.clickWithDebounce {
            typeHour = EventConstant.HOUR_START
            val now: Calendar = Calendar.getInstance()
            val pair = TimeUtils.extractHourMinute(binding.tvHourStart.text.toString())!!
            now[Calendar.HOUR_OF_DAY] = pair.first
            now[Calendar.MINUTE] = pair.second
            tpdStart = TimePickerDialog.newInstance(
                this@CreateEventActivity, now[Calendar.HOUR_OF_DAY],  // Initial year selection
                now[Calendar.MINUTE],  // Initial month selection
                true
            )
            tpdStart!!.accentColor = ContextCompat.getColor(this, R.color.blue_700)
            tpdStart!!.setOkText(getString(R.string.ok))
            tpdStart!!.setCancelText(getString(R.string.cancel))
            // If you're calling this from a support Fragment
            // If you're calling this from a support Fragment
            tpdStart!!.show(supportFragmentManager, "Datepickerdialog")
        }

        binding.tvTimeEnd.clickWithDebounce {
            typeDate = EventConstant.DATE_END
            val now: Calendar = Calendar.getInstance()
            if (binding.tvTimeEnd.text.toString().isNotEmpty()) {
                val date = sdf.parse(binding.tvTimeEnd.text.toString())
                now.time = date!!
            }
            val dpd: DatePickerDialog = DatePickerDialog.newInstance(
                this@CreateEventActivity, now[Calendar.YEAR],  // Initial year selection
                now[Calendar.MONTH],  // Initial month selection
                now[Calendar.DAY_OF_MONTH] // Inital day selection
            )
            dpd.accentColor = ContextCompat.getColor(this, R.color.blue_700)
            dpd.setOkText(getString(R.string.ok))
            dpd.setCancelText(getString(R.string.cancel))
            // If you're calling this from a support Fragment
            // If you're calling this from a support Fragment
            val minDate = sdf.parse(binding.tvTimeStart.text.toString())
            val calendarMin = Calendar.getInstance()
            calendarMin.time = minDate!!
            updateDateRangeForEndDate(calendarMin, dpd)
            dpd.show(supportFragmentManager, "Datepickerdialog")
        }

        binding.tvHourEnd.clickWithDebounce {
            typeHour = EventConstant.HOUR_END
            val now: Calendar = Calendar.getInstance()
            val pair = TimeUtils.extractHourMinute(binding.tvHourEnd.text.toString())!!
            now[Calendar.HOUR_OF_DAY] = pair.first
            now[Calendar.MINUTE] = pair.second
            tpdEnd = TimePickerDialog.newInstance(
                this@CreateEventActivity, now[Calendar.HOUR_OF_DAY],  // Initial year selection
                now[Calendar.MINUTE],  // Initial month selection
                true
            )

            // Check Equal than
            val dateFormat = SimpleDateFormat(DateUtils.DATE_FORMAT, Locale.getDefault())

            val date1 = dateFormat.parse(binding.tvTimeStart.text.toString())
            val date2 = dateFormat.parse(binding.tvTimeEnd.text.toString())

            // So sánh ngày
            if (date2!!.compareTo(date1!!) == 0) {
                val calenderAddOneHour = getDateAndHourPlusOneHour()
                val timePoint = Timepoint(
                    calenderAddOneHour[Calendar.HOUR_OF_DAY],
                    calenderAddOneHour[Calendar.MINUTE],
                    calenderAddOneHour[Calendar.SECOND]
                )
                tpdEnd!!.setMinTime(timePoint)
            }

            tpdEnd!!.accentColor = ContextCompat.getColor(this, R.color.blue_700)
            tpdEnd!!.setOkText(getString(R.string.ok))
            tpdEnd!!.setCancelText(getString(R.string.cancel))
            // If you're calling this from a support Fragment
            // If you're calling this from a support Fragment
            tpdEnd!!.show(supportFragmentManager, "Datepickerdialog")
        }
    }

    private fun setLauncher() {
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    listLocalMedia.clear()
                    listLocalMedia = PictureSelector.obtainSelectorList(data)
                    listMedia.clear()
                    listLocalMedia.forEach { localMedia ->
                        val media = MultiMedia()
                        val mediaId = UUID.randomUUID().toString()
                        media.id = mediaId
                        media.name = "Media $mediaId"
                        media.url = localMedia.realPath
                        media.realPath = localMedia.realPath
                        media.size = localMedia.size
                        media.height = localMedia.height
                        media.width = localMedia.width
                        if (PictureMimeType.isHasVideo(localMedia.mimeType)) {
                            media.type = AppConstants.UPLOAD_VIDEO
                        }
                        if (PictureMimeType.isHasImage(localMedia.mimeType)) {
                            media.type = AppConstants.UPLOAD_IMAGE
                        }
                        listMedia.add(media)
                    }
                    PhotoShowUtils.loadPhotoImageNormal(listLocalMedia.first().path, binding.imvBg)
                } else {
                    //
                }
                validImage = listLocalMedia.isNotEmpty()
                checkImage()
                updateViewSave()
            }
    }

    private fun setDataForView() {
        AppUtils.setFontTypeFaceTitleBar(getContext(), binding.tbTitle)
        binding.tvGallery.clickWithDebounce {
            PhotoPickerUtils.showImagePickerUploadPosterInEvent(this, launcher, listLocalMedia)
        }
        val date = Date()
        val dateAndHour = DateUtils.getAddThreeDayString(date)
        val dateAndHourMoreOneHour = DateUtils.getMoreOneHour(date)
        binding.tvTimeStart.text = dateAndHour.first
        binding.tvTimeEnd.text = dateAndHour.first
        binding.tvHourStart.text = formatHour(dateAndHour.second)
        binding.tvHourEnd.text = formatHour(dateAndHourMoreOneHour.second)
    }

    private fun formatHour(second: String): String {
        val regex = Regex("""(\d{2}):(\d{2})""")
        val matchResult = regex.find(second)
        return if (matchResult != null) {
            var (hourStr, minuteStr) = matchResult.destructured
            if (hourStr.toInt() < 10 && hourStr.length == 1) {
                hourStr = "0$hourStr"
            }
            "$hourStr:$minuteStr"
        } else {
            "00:00"
        }
    }

    override fun initData() {
        //
    }

    @SuppressLint("SetTextI18n")
    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val date = String.format("%02d", dayOfMonth) + "/" + (monthOfYear + 1) + "/" + year
        //
        val dateFormat = SimpleDateFormat(DateUtils.DATE_FORMAT, Locale.getDefault())
        when (typeDate) {
            EventConstant.DATE_START -> {
                val date1 = dateFormat.parse(date)
                val date2 = dateFormat.parse(binding.tvTimeEnd.text.toString())
                // So sánh ngày
                if (date1!!.after(date2)) {
                    binding.tvTimeEnd.text = date
                }
                if (date1.compareTo(date2!!) == 0) {
                    val calenderAddOneHour = getDateAndHourPlusOneHour()
                    val timePoint = Timepoint(
                        calenderAddOneHour[Calendar.HOUR_OF_DAY],
                        calenderAddOneHour[Calendar.MINUTE],
                        calenderAddOneHour[Calendar.SECOND]
                    )
                    binding.tvHourEnd.text = "${
                        String.format(
                            "%02d", calenderAddOneHour[Calendar.HOUR_OF_DAY]
                        )
                    }:${calenderAddOneHour[Calendar.MINUTE]}"
                }
                binding.tvTimeStart.text = date
            }

            EventConstant.DATE_END -> {
                val date1 = dateFormat.parse(date)
                val date2 = dateFormat.parse(binding.tvTimeStart.text.toString())
                if (date1!!.compareTo(date2!!) == 0) {
                    val calenderAddOneHour = getDateAndHourPlusOneHour()
                    val timePoint = Timepoint(
                        calenderAddOneHour[Calendar.HOUR_OF_DAY],
                        calenderAddOneHour[Calendar.MINUTE],
                        calenderAddOneHour[Calendar.SECOND]
                    )
                    binding.tvHourEnd.text = "${
                        String.format(
                            "%02d", calenderAddOneHour[Calendar.HOUR_OF_DAY]
                        )
                    }:${calenderAddOneHour[Calendar.MINUTE]}"
                }
                binding.tvTimeEnd.text = date
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        val hour = String.format("%02d", hourOfDay)
        val minu = String.format("%02d", minute)
        val time = "$hour:$minu"
        when (typeHour) {
            EventConstant.HOUR_START -> {
                binding.tvHourStart.text = time
                // Check Equal than
                val sdf = SimpleDateFormat(DateUtils.DATE_FORMAT_1, Locale.getDefault())

                val date1 = sdf.parse("${binding.tvTimeStart.text} $time")
                val date2 = sdf.parse("${binding.tvTimeEnd.text} ${binding.tvHourEnd.text}")

                val calendar = Calendar.getInstance()
                calendar.time = date1!!
                calendar.add(Calendar.HOUR_OF_DAY, 1)
                val oneHourLater = calendar.time

                if (date2!!.before(oneHourLater)) {
                    binding.tvHourEnd.text = "${
                        String.format(
                            "%02d", convert12HourTo24Hour(calendar)
                        )
                    }:${String.format(
                        "%02d", calendar[Calendar.MINUTE])}"
                }
            }

            EventConstant.HOUR_END -> {
                binding.tvHourEnd.text = time
            }
        }
    }

    private fun getDateAndHourPlusOneHour(): Calendar {
        val sdf = SimpleDateFormat(DateUtils.DATE_FORMAT_1, Locale.getDefault())
        val date1 = sdf.parse("${binding.tvTimeStart.text} ${binding.tvHourStart.text}")
        val calendar = Calendar.getInstance()
        calendar.time = date1!!
        calendar.add(Calendar.HOUR_OF_DAY, 1)
        return calendar
    }

    private fun convert12HourTo24Hour(calendar: Calendar): Int {
        val amPm = calendar[Calendar.AM_PM]
        var hour = calendar[Calendar.HOUR]
        if (amPm == Calendar.PM) {
            hour += 12
        }

        return hour
    }

    private fun updateDateRangeForStartDate(dpd: DatePickerDialog) {
        // Set min date to current date + 3 days
        val minDate = Calendar.getInstance()
        minDate.add(Calendar.DAY_OF_MONTH, 3)
        dpd.minDate = minDate
    }

    private fun updateDateRangeForEndDate(selectedDate: Calendar, dpd: DatePickerDialog) {
        dpd.minDate = selectedDate
    }

}