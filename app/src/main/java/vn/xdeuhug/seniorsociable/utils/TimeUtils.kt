package vn.xdeuhug.seniorsociable.utils

import android.annotation.SuppressLint
import android.content.Context
import com.hjq.toast.ToastUtils
import vn.xdeuhug.seniorsociable.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.Objects
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object TimeUtils {
    fun timeAgoChat(context: Context, date: String): String {
        val past = getDateFromString(date)
        val now = Date()
        val calendar: Calendar = GregorianCalendar()
        calendar.time = past!!
        try {
            val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time) //giây
            val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time) //phút
            val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time) //giờ
            val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time) // ngày
            val year = days / 365 //năm
            return if (seconds < 60) {
                context.getString(vn.xdeuhug.seniorsociable.R.string.finish_now)
            } else if (minutes < 60) {
                String.format(
                    context.getString(vn.xdeuhug.seniorsociable.R.string.minute_ago), minutes
                )
            } else if (hours < 24) {
                String.format(context.getString(vn.xdeuhug.seniorsociable.R.string.hour_ago), hours)
            } else if (days < 8) {
                String.format(context.getString(vn.xdeuhug.seniorsociable.R.string.day_ago), days)
            } else if (days in 8..364) {
                String.format(
                    "%s/%s lúc %s:%s",
                    String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)),
                    String.format("%02d", calendar.get(Calendar.MONTH) + 1),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    String.format("%02d", calendar.get(Calendar.MINUTE))
                )
            } else if (year > 0) {
                String.format(
                    "%s lúc %s", date, calendar.get(Calendar.HOUR_OF_DAY), String.format(
                        "%02d", calendar.get(
                            Calendar.MINUTE
                        )
                    )
                )
            } else {
                String.format("%s", date)
            }
        } catch (j: Exception) {
            j.printStackTrace()
        }
        return String.format("%s", date)
    }

    fun timeAgoChat(context: Context, past: Date): String {
        val now = Date()
        val calendar: Calendar = GregorianCalendar()
        calendar.time = past
        try {
            val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
            val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
            val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time)
            val year = days / 365
            return when {
                seconds < 60 -> context.getString(R.string.finish_now)
                minutes < 60 -> String.format(context.getString(R.string.minute_ago), minutes)
                hours < 24 -> String.format(context.getString(R.string.hour_ago), hours)
                days < 8 -> String.format(context.getString(R.string.day_ago), days)
                days in 8..364 -> String.format(
                    "%s/%s lúc %s:%s",
                    String.format("%02d", calendar[Calendar.DAY_OF_MONTH]),
                    String.format("%02d", calendar[Calendar.MONTH] + 1),
                    calendar[Calendar.HOUR_OF_DAY],
                    String.format("%02d", calendar[Calendar.MINUTE])
                )

                year > 0 -> String.format(
                    "%s lúc %s:%s",
                    calendar[Calendar.YEAR],
                    calendar[Calendar.HOUR_OF_DAY],
                    String.format("%02d", calendar[Calendar.MINUTE])
                )

                else -> String.format("%s", past)
            }
        } catch (j: Exception) {
            j.printStackTrace()
        }
        return String.format("%s", past)
    }


    private fun getDateFromString(strDate: String): Date? {
        val old = "MM-dd-yyyy HH:mm:ss"
        val new = "HH:mm dd-MM-yyyy"
        @SuppressLint("SimpleDateFormat") val df = SimpleDateFormat(old)
        df.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date = try {
            df.parse(strDate)!!
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
        df.timeZone = TimeZone.getDefault()
        val formattedDate = df.format(Objects.requireNonNull(date))
        var newDateString = ""
        try {
            @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat(old)
            val d = sdf.parse(formattedDate)
            sdf.applyPattern(new)
            newDateString = sdf.format(Objects.requireNonNull(d))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val dateFormat = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault())
        var dateData: Date? = null
        try {
            dateData = dateFormat.parse(newDateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return dateData
    }

    fun timeAgoStatus(date: String): String {
        val past = getDateFromString(date)
        val now = Date()
        val calendar: Calendar = GregorianCalendar()
        calendar.time = past!!
        try {
            val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time) //giây
            val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time) //phút
            val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time) //giờ
            val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time) // ngày
            val year = days / 365 //năm
            return when {
                seconds < 60 -> {
                    "Vừa truy cập"
                }

                minutes < 60 -> {
                    String.format("%s %s phút trước", "Hoạt động", minutes)
                }

                hours < 24 -> {
                    String.format("%s %s giờ trước", "Hoạt động", hours)
                }

                days < 8 -> {
                    String.format("%s %s ngày trước", "Hoạt động", days)
                }

                days in 8..365 -> {
                    String.format("%s %s tuần trước", "Hoạt động", days / 7)
                }

                year > 365 -> {
                    String.format("%s %s năm trước", "Hoạt động", calendar[Calendar.YEAR])
                }

                else -> {
                    String.format("%s", date)
                }
            }
        } catch (j: Exception) {
            j.printStackTrace()
        }
        return String.format("%s", date)
    }

    fun changeFormatTimeMessageChat(strDate: String): String {
        val old = "MM-dd-yyyy HH:mm:ss"
        val new = "dd/MM/yyyy HH:mm:ss"
        @SuppressLint("SimpleDateFormat") val df = SimpleDateFormat(old)
        df.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date = try {
            df.parse(strDate)!!
        } catch (e: ParseException) {
            throw java.lang.RuntimeException(e)
        }
        df.timeZone = TimeZone.getDefault()
        val formattedDate = df.format(Objects.requireNonNull(date))
        var newDateString = ""
        try {
            @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat(old)
            val d = sdf.parse(formattedDate)
            sdf.applyPattern(new)
            newDateString = sdf.format(Objects.requireNonNull(d))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return newDateString
    }

    @Suppress("DEPRECATION")
    fun getJoinIn(dateJoin: Date, context: Context): String {
        val c = Calendar.getInstance()
        c.time = dateJoin
        when (dateJoin.month) {
            0 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.january)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            1 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.february)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            2 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.march)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            3 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.april)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            4 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.may)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            5 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.june)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            6 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.july)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            7 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.august)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            8 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.september)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            9 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.october)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            10 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.november)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            11 -> {
                return "${context.getString(R.string.joined)} ${context.getString(R.string.december)} ${
                    context.getString(
                        R.string.year
                    )
                } ${c[Calendar.YEAR]}"
            }

            else -> return ""
        }
    }

    fun formatTimeAgo(pastDate: Date, context: Context): String {
        val currentTime = Date().time
        val pastTime = pastDate.time
        val difference = currentTime - pastTime

        val seconds = difference / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            years > 0 -> context.getString(R.string.year_ago_notify, years)
            months > 0 -> context.getString(R.string.month_ago_notify, months)
            weeks > 0 -> context.getString(R.string.week_ago_notify, weeks)
            days > 0 -> context.getString(R.string.day_ago_notify, days)
            hours > 0 -> context.getString(R.string.hour_ago_notify, hours)
            minutes > 0 -> context.getString(R.string.minute_ago_notify, minutes)
            else -> context.getString(R.string.finish_now)
        }
    }

    fun extractHourMinute(input: String): Pair<Int, Int>? {
        val regex = Regex("""(\d{2}):(\d{2})""")
        val matchResult = regex.find(input)

        return if (matchResult != null) {
            val (hourStr, minuteStr) = matchResult.destructured
            Pair(hourStr.toInt(), minuteStr.toInt())
        } else {
            null
        }
    }



}
