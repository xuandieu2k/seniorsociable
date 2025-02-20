package vn.xdeuhug.seniorsociable.database

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelReport.Report
import vn.xdeuhug.seniorsociable.model.entity.modelReport.ReportPostAndUser
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 12 / 2023
 */
class ReportManagerFSDB {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore

        interface ReportCallback {
            fun onReportFound(report: Report?)
            fun onFailure(exception: Exception)
        }

        interface FireStoreCallback<T> {
            fun onSuccess(result: T)
            fun onFailure(exception: Exception)
        }

        fun getReportPostAndUser(
            typeReport: Int, // Đảm bảo typeReport là Int để phù hợp với hàm generateReport
            fromDate: Date,
            toDate: Date,
            callback: FireStoreCallback<ArrayList<ReportPostAndUser>>
        ) {
            AppUtils.logJsonFromObject("$fromDate ... $toDate")
            val postCollection = db.collection("Post")
                .whereGreaterThanOrEqualTo("timeCreated", fromDate)
                .whereLessThanOrEqualTo("timeCreated", toDate)
                .orderBy("timeCreated", Query.Direction.ASCENDING)
            val userCollection = db.collection("User")
                .whereGreaterThanOrEqualTo("timeCreated", fromDate)
                .whereLessThanOrEqualTo("timeCreated", toDate)
                .orderBy("timeCreated", Query.Direction.ASCENDING)

            postCollection.get().addOnSuccessListener { postDocuments ->
                val posts = postDocuments.map { document -> document.toObject(Post::class.java) }
                userCollection.get().addOnSuccessListener { userDocuments ->
                    val users =
                        userDocuments.map { document -> document.toObject(User::class.java) }

                    val report = generateReport(ArrayList(posts), ArrayList(users), typeReport)
                    callback.onSuccess(report)
                    AppUtils.logJsonFromObject(report)
                }.addOnFailureListener {
                    callback.onFailure(it)
                }
            }.addOnFailureListener {
                callback.onFailure(it)
            }
        }

        private fun generateReport(
            posts: ArrayList<Post>,
            users: ArrayList<User>,
            typeReport: Int
        ): ArrayList<ReportPostAndUser> {
            val reports = ArrayList<ReportPostAndUser>()

            val locale = Locale("vi", "VN")
            val format: String = when (typeReport) {
                AppConstants.REPORT_TODAY, AppConstants.REPORT_YESTERDAY -> "HH:00"
                AppConstants.REPORT_THIS_WEEK -> "EEE"
                AppConstants.REPORT_THIS_MONTH, AppConstants.REPORT_LAST_MONTH -> "dd 'Tháng' MM"
                AppConstants.REPORT_THIS_YEAR, AppConstants.REPORT_LAST_YEAR -> "'Tháng' MM"
                AppConstants.REPORT_THREE_YEARS -> "yyyy" // Theo năm
                else -> "dd/MM/yyyy"
            }
            val dateFormat = SimpleDateFormat(format, locale)

            // Gom nhóm và tính toán dữ liệu dựa trên loại report
            val groupedPosts = posts.groupBy {
                val dayInEnglish = dateFormat.format(it.timeCreated)
                convertDayToVietnamese(dayInEnglish)
            }
            val groupedUsers = users.groupBy {
                val dayInEnglish = dateFormat.format(it.timeCreated)
                convertDayToVietnamese(dayInEnglish)
            }

            val allKeys = when (typeReport) {
                AppConstants.REPORT_TODAY, AppConstants.REPORT_YESTERDAY -> getAllHoursOfDay()
                AppConstants.REPORT_THIS_WEEK -> getAllDaysOfWeek()
                AppConstants.REPORT_THIS_MONTH -> getAllDaysOfMonth()
                AppConstants.REPORT_LAST_MONTH -> getAllDaysOfLastMonth()
                AppConstants.REPORT_THIS_YEAR, AppConstants.REPORT_LAST_YEAR -> getAllMonthsOfYear()
                AppConstants.REPORT_THREE_YEARS -> getLastThreeYears()
                else -> (groupedPosts.keys + groupedUsers.keys).distinct()
            }

            // Duyệt qua các khóa và tạo report
            for (key in allKeys) {
                val sumPost = groupedPosts[key]?.size ?: 0
                val sumUser = groupedUsers[key]?.size ?: 0
                val report = ReportPostAndUser().apply {
                    this.sumPost = sumPost
                    this.sumUser = sumUser
                    this.timeReport = key
                }
                reports.add(report)
            }

            return reports
        }


        private fun convertDayToVietnamese(day: String): String {
            return when (day) {
                "Th 2" -> "Thứ Hai"
                "Th 3" -> "Thứ Ba"
                "Th 4" -> "Thứ Tư"
                "Th 5" -> "Thứ Năm"
                "Th 6" -> "Thứ Sáu"
                "Th 7" -> "Thứ Bảy"
                "CN" -> "Chủ Nhật"
                else -> day
            }
        }

        private fun getAllDaysOfWeek(): List<String> {
            return listOf(
                "Thứ Hai",
                "Thứ Ba",
                "Thứ Tư",
                "Thứ Năm",
                "Thứ Sáu",
                "Thứ Bảy",
                "Chủ Nhật"
            )
        }

        private fun getAllDaysOfMonth(year: Int, month: Int): List<String> {
            val calendar = Calendar.getInstance()
            calendar[year, month - 1] = 1
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            return (1..daysInMonth).map { day ->
                "${day.toString().padStart(2, '0')} Tháng $month"
            }
        }

        private fun getAllMonthsOfYear(): List<String> {
            return (1..12).map { month ->
                "Tháng ${month.toString().padStart(2, '0')}"
            }
        }

        private fun getAllDaysOfMonth(): List<String> {
            val calendar = Calendar.getInstance()
//            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH] + 1 // January is 0 in Calendar

            calendar[Calendar.DAY_OF_MONTH] = 1
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            return (1..daysInMonth).map { day ->
                "${day.toString().padStart(2, '0')} Tháng ${month.toString().padStart(2, '0')}"
            }
        }

        private fun getAllDaysOfLastMonth(): List<String> {
            val calendar = Calendar.getInstance()
//            val year = calendar[Calendar.YEAR]
            var month = calendar[Calendar.MONTH] - 1 // January is 0 in Calendar
            if(month == -1)
            {
                month = 12
            }

            calendar[Calendar.DAY_OF_MONTH] = 1
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            return (1..daysInMonth).map { day ->
                "${day.toString().padStart(2, '0')} Tháng ${month.toString().padStart(2, '0')}"
            }
        }

        private fun getAllHoursOfDay(): List<String> {
            return (0..23).map { hour ->
                "${hour.toString().padStart(2, '0')}:00"
            }
        }

        private fun getLastThreeYears(): List<String> {
            val currentYear = Calendar.getInstance()[Calendar.YEAR]
            return ((currentYear - 2)..currentYear).map { year ->
                year.toString()
            }
        }


        fun getAllReport(callback: FireStoreCallback<ArrayList<Report>>) {
            val reportCollection = db.collection("Report")
            reportCollection.get().addOnSuccessListener { querySnapshot ->
                val reportList = ArrayList<Report>()

                for (document in querySnapshot.documents) {
                    val report = document.toObject(Report::class.java)
                    if (report != null) {
                        reportList.add(report)
                    }
                }

                callback.onSuccess(reportList)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        fun getAllReportByIdPost(idPost: String, callback: FireStoreCallback<ArrayList<Report>>) {
            val reportCollection = db.collection("Report").whereEqualTo("idPostReport", idPost)
            reportCollection.get().addOnSuccessListener { querySnapshot ->
                val reportList = ArrayList<Report>()

                for (document in querySnapshot.documents) {
                    val report = document.toObject(Report::class.java)
                    if (report != null) {
                        reportList.add(report)
                    }
                }

                callback.onSuccess(reportList)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        suspend fun getAllReportByIdPosts(idPost: String): ArrayList<Report> {
            val reportCollection = db.collection("Report").whereEqualTo("idPostReport", idPost)
            return try {
                val querySnapshot = reportCollection.get().await()
                val reportList = ArrayList<Report>()
                for (document in querySnapshot.documents) {
                    val report = document.toObject(Report::class.java)
                    if (report != null) {
                        reportList.add(report)
                    }
                }
                reportList
            } catch (ex: Exception) {
                ex.printStackTrace()
                ArrayList()
            }
        }

        fun addReport(report: Report, callback: FireStoreCallback<Boolean>) {
            val reportCollection = db.collection("Report")
            reportCollection.document(report.id).set(report).addOnSuccessListener { _ ->
                // Dữ liệu album đã được thêm vào với ID ngẫu nhiên
                callback.onSuccess(true)
            }.addOnFailureListener { exception ->
                logExceptionUseTimber(exception)
                callback.onFailure(exception)
            }
        }

        private fun logExceptionUseTimber(exception: Exception) {
            Timber.tag("Exception FireStore").d(exception)
        }
    }
}