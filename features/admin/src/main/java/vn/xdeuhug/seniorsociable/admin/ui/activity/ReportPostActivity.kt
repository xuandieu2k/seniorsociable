package vn.xdeuhug.seniorsociable.admin.ui.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.admin.databinding.ActivityReportPostBinding
import vn.xdeuhug.seniorsociable.admin.ui.adapter.ReportAdapter
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.database.PostManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.other.doAfterTextChanged
import vn.xdeuhug.seniorsociable.ui.dialog.CustomDatePickerDialog
import vn.xdeuhug.seniorsociable.ui.dialog.PostModerationDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 30 / 12 / 2023
 */
class ReportPostActivity : AppActivity(), ReportAdapter.OnListenerCLick,
    ReportAdapter.OnClickImageViewListener, AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityReportPostBinding

    /* Data and Adapter for Post */
    private lateinit var postRPAdapter: ReportAdapter
    private var listPost = ArrayList<Post>()

    /* paging*/
    private var limit = AppConstants.PAGE_SIZE
    private var currentPage = 1
    private var lastVisible: DocumentSnapshot? = null
    private var loading = false
    private var lastPage = false
    /* end */

    override fun getLayoutView(): View {
        binding = ActivityReportPostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        AppUtils.setFontTypeFaceTitleBar(getContext(), binding.tbTitle)
        startShimmer()
        setDateTextView()
        initStatusFilter()
        setDataPost()
        postDelayed({
            getDataPost()
        }, 1000)
        customPaginate()
        setQuerySearchChange()
    }

    private fun setQuerySearchChange() {
        binding.svSearch.doAfterTextChanged{
            resetDataPost()
        }
    }

    /*
   * Init Data For Post
   * */
    @SuppressLint("NotifyDataSetChanged")
    private fun setDataPost() {
        postRPAdapter = ReportAdapter(this)
        postRPAdapter.onListenerCLick = this
        postRPAdapter.imageViewClickListener = this
        postRPAdapter.setData(listPost)
        // Create recycleView
        binding.rvPost.show()
        AppUtils.initRecyclerViewVertical(binding.rvPost, postRPAdapter)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataPost() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val listTriple = async {
                    PostManagerFSDB.getAllPostRPWithStatus(
                        getStatusFilter(),
                        AppUtils.removeVietnameseFromStringNice(binding.svSearch.query.toString().trim()),
                        DateUtils.getDateByString(binding.tvFromDate.text.toString().trim()),
                        DateUtils.getDateByString(binding.tvToDate.text.toString().trim()),
                        limit,
                        lastVisible
                    )
                }.await()
                if (currentPage == 1) {
                    handleSuccess(0)
                    listPost.clear()
                    binding.splReset.finishRefresh()
                }
                // UI update code here
                listPost.addAll(listTriple.first)
                postRPAdapter.notifyDataSetChanged()
                lastVisible = listTriple.second
                lastPage = listTriple.third
                loading = false
                binding.loadMore.hide()
                showViewNoData()
            } catch (e: Exception) {
                // Xử lý ngoại lệ khi gặp lỗi
                e.printStackTrace()
                toast(R.string.please_try_later)
                loading = false
                binding.loadMore.hide()
                binding.splReset.finishRefresh()
                showViewNoData()
            }
        }
    }

    private fun customPaginate() {
        binding.splReset.setOnRefreshListener {
            postDelayed({
                currentPage = 1
                lastPage = false
                lastVisible = null
                getDataPost()
            }, 1000)
        }
        binding.nsvData.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            var recyclerViewBottom = binding.rvPost.bottom
            if (binding.loadMore.visibility == View.VISIBLE) {
                recyclerViewBottom = binding.rvPost.bottom + binding.loadMore.height
            }
            val nestedScrollViewHeight = binding.nsvData.height

            if (binding.rvPost.visibility == View.VISIBLE && binding.sflLoadData.visibility == View.GONE) {
                if (scrollY + nestedScrollViewHeight >= recyclerViewBottom) {
                    if (!loading && !lastPage) {
                        binding.loadMore.show()
                        binding.nsvData.post {
                            binding.nsvData.smoothScrollTo(
                                0,
                                binding.rvPost.bottom + binding.loadMore.height
                            )
                        }
                        loading = true
                        currentPage += 1
                        getDataPost()
                    }
                }
            }
        })
    }

    private fun initStatusFilter() {
        val arrayText = arrayListOf(
            getString(R.string.all),
            getString(R.string.is_passed),
            getString(R.string.is_not_passed),
            getString(R.string.is_pending_passed),
            getString(R.string.is_locked)
        )

        val adapterFilter =
            ArrayAdapter(this, R.layout.simple_spinner_item_custom, arrayText)
        adapterFilter.setDropDownViewResource(R.layout.simple_list_item_activated_custom)
        binding.spnFilter.adapter = adapterFilter
        binding.spnFilter.onItemSelectedListener = this
        binding.spnFilter.setSelection(0)
        binding.imvDrownList.clickWithDebounce {
            binding.spnFilter.performClick()
        }
    }

    private fun getStatusFilter(): Int {
        return binding.spnFilter.selectedItemPosition
    }

    @SuppressLint("SimpleDateFormat")
    private fun setDateTextView() {
        val sdf = SimpleDateFormat(DateUtils.DATE_FORMAT)
        val currentDate = sdf.format(Date())
        val calendarD = Calendar.getInstance()
        calendarD.add(Calendar.MONTH, 0)
        calendarD[Calendar.DATE] = calendarD.getActualMinimum(Calendar.DAY_OF_MONTH)
        val monthFirstDay = calendarD.time
        val firstDateToMonth = sdf.format(monthFirstDay)
        binding.tvFromDate.text = firstDateToMonth
        binding.tvToDate.text = currentDate
        binding.tvToDate.clickWithDebounce(1000) {
            val date = sdf.parse(binding.tvToDate.text.toString())
            val calendar = Calendar.getInstance()
            calendar.time = date!!
            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH]
            val day = calendar[Calendar.DAY_OF_MONTH]

            val datePickerDialog = CustomDatePickerDialog(
                getContext(), year, month, day
            )
            val dateMin = sdf.parse(binding.tvFromDate.text.toString())
            datePickerDialog.datePicker.minDate = dateMin!!.time
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
                binding.tvToDate.text = selectedDate
                resetDataPost()
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
        binding.tvFromDate.clickWithDebounce(1000) {
            val date = sdf.parse(binding.tvFromDate.text.toString())
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
                binding.tvFromDate.text = selectedDate
                val fromDate = sdf.parse(selectedDate)
                val toDate = sdf.parse(binding.tvToDate.text.toString())
                if (fromDate!! > toDate) {
                    binding.tvToDate.text = selectedDate
                }
                resetDataPost()
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

    private fun resetDataPost() {
        currentPage = 1
        lastVisible = null
        startShimmer()
        postDelayed({
            getDataPost()
        }, 1000)
    }

    private fun startShimmer() {
        binding.sflLoadData.startShimmer()
        binding.rvPost.hide()
        binding.rlBackgroundNotFound.hide()
        binding.sflLoadData.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccess(timer: Long) {
        postDelayed({
            binding.rvPost.show()
            binding.sflLoadData.stopShimmer()
            binding.sflLoadData.hide()
        }, timer)

    }

    private fun showViewNoData() {
        if (listPost.isNotEmpty()) {
            binding.rlBackgroundNotFound.hide()
            binding.rvPost.show()
        } else {
            binding.rlBackgroundNotFound.show()
            binding.rvPost.hide()
        }
    }


    override fun initData() {
        //
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        resetDataPost()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //
    }

    override fun onClick(position: Int) {
        //
    }

    override fun onClickButtonRemove(position: Int, post: Post) {
        try {
            startActivity<DetailsReportActivity>(AppConstants.OBJECT_POST to Gson().toJson(post))
        }catch (ex:Exception)
        {
            ex.printStackTrace()
        }
    }

    override fun onClickButtonEdit(position: Int, post: Post) {
        //
    }
}