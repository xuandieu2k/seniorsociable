package vn.xdeuhug.seniorsociable.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.InputFilter
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Base64
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.*
import cn.jzvd.JzvdStd
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.luck.picture.lib.utils.ToastUtils
import org.jetbrains.anko.support.v4.toast
import pyxis.uzuki.live.richutilskt.utils.getRealPath
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppApplication
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.model.entity.Tab
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Hobby
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.other.CenterLayoutManager
import vn.xdeuhug.seniorsociable.other.PreCachingLayoutManager
import vn.xdeuhug.seniorsociable.ui.adapter.MediaAdapter
import vn.xdeuhug.seniorsociable.ui.adapter.TabAdapter
import vn.xdeuhug.seniorsociable.widget.AppEditText
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.NetworkInterface
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object AppUtils {
    /** Trả về tên thiết bị  */
    fun getDeviceName(): String {
        return Build.MANUFACTURER + " - " + Build.MODEL
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (into in interfaces) {
                val addresses = into.inetAddresses
                for (address in addresses) {
                    if (!address.isLoopbackAddress) {
                        val ip = address.hostAddress
                        if (ip.contains(":")) // Kiểm tra xem địa chỉ IP có chứa dấu hai chấm (:) không (IPv6)
                            continue
                        return ip
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    fun setFileToStorageDir(fileName: String): File {
        return File(
            Environment.getExternalStorageDirectory(),
            Environment.DIRECTORY_DOWNLOADS + File.separator + AppConstants.FOLDER_APP + File.separator + fileName.replace(
                "%20", ""
            )
        )
    }

    fun fromHtml(string: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(string)
        }
    }

    fun getDecimalFormattedString(value: BigDecimal): String {
        val formatter = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.US))
        return formatter.format(value)
    }

    fun getMoneyFormatted(value: BigDecimal): String {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        return formatter.format(value)
    }

    fun getDecimalFormattedString(value: String): String {
        var value = value
        if (value.contains("-")) {
            value = value.substring(1)
            val lst = StringTokenizer(value, ".")
            var str1 = value
            var str2 = ""
            if (lst.countTokens() > 1) {
                str1 = lst.nextToken()
                str2 = lst.nextToken()
            }
            var str3 = StringBuilder()
            var i = 0
            var j = -1 + str1.length
            if (str1[-1 + str1.length] == '.') {
                j--
                str3 = StringBuilder(".")
            }
            var k = j
            while (true) {
                if (k < 0) {
                    if (str2.length > 0) {
                        str3.append(".").append(str2)
                    }
                    return String.format("-%s", str3)
                }
                if (i == 3) {
                    str3.insert(0, ",")
                    i = 0
                }
                str3.insert(0, str1[k])
                i++
                k--
            }
        } else {
            val lst = StringTokenizer(value, ".")
            var str1 = value
            var str2 = ""
            if (lst.countTokens() > 1) {
                str1 = lst.nextToken()
                str2 = lst.nextToken()
            }
            var str3 = StringBuilder()
            var i = 0
            var j = -1 + str1.length
            if (str1[-1 + str1.length] == '.') {
                j--
                str3 = StringBuilder(".")
            }
            var k = j
            while (true) {
                if (k < 0) {
                    if (str2.length > 0) {
                        str3.append(".").append(str2)
                    }
                    return str3.toString()
                }
                if (i == 3) {
                    str3.insert(0, ",")
                    i = 0
                }
                str3.insert(0, str1[k])
                i++
                k--
            }
        }
    }

    fun roundDouble(numberF: Double?, roundTo: Int): Double {
        val mF: Double
        val num = java.lang.StringBuilder("#########.")
        for (count in 0 until roundTo) {
            num.append("0")
        }
        val df = DecimalFormat(num.toString())
        df.roundingMode = RoundingMode.HALF_UP
        val mS = df.format(numberF).replace(",", ".")
        mF = mS.toDouble()
        return mF
    }

    fun initRecyclerView(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        configRecyclerView(
            view, LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)
        )
        view.adapter = adapter
    }

    fun configRecyclerView(
        recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager?
    ) {
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        (recyclerView.itemAnimator)!!.changeDuration = 0
        ((recyclerView.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.isNestedScrollingEnabled = false
    }

    fun initRecyclerViewVertical(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        configRecyclerView(
            view, PreCachingLayoutManager(
                view.context, RecyclerView.VERTICAL, false
            )
        )
        view.adapter = adapter
    }

    fun initRecyclerViewVertical(
        view: RecyclerView, adapter: RecyclerView.Adapter<*>?, count: Int
    ) {
        configRecyclerView(view, GridLayoutManager(view.context, count))
        view.adapter = adapter
    }

    fun initRecyclerViewVerticalWithStaggeredGridLayoutManager(
        view: RecyclerView, adapter: RecyclerView.Adapter<*>?, count: Int
    ) {
        configRecyclerView(
            view, StaggeredGridLayoutManager(
                count, StaggeredGridLayoutManager.VERTICAL
            )
        )
        view.adapter = adapter
    }


    fun initRecyclerViewHorizontal(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        configRecyclerView(
            view, PreCachingLayoutManager(
                view.context, RecyclerView.HORIZONTAL, false
            )
        )
        view.adapter = adapter
    }

    fun initRecyclerViewHorizontal(
        view: RecyclerView, adapter: RecyclerView.Adapter<*>?, count: Int
    ) {
        configRecyclerView(view, GridLayoutManager(view.context, count))
        view.adapter = adapter
    }

    fun initRecyclerViewReverse(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        val preCachingLayoutManager = CenterLayoutManager(
            view.context, RecyclerView.VERTICAL, true
        )
        configRecyclerView(view, preCachingLayoutManager)
        view.adapter = adapter
    }

    fun initRecyclerViewVerticalWithFlexBoxLayout(
        view: RecyclerView, adapter: RecyclerView.Adapter<*>?
    ) {
        configRecyclerViewWithFlexBoxLayout(view, FlexboxLayoutManager(view.context))
        view.adapter = adapter
    }

    private fun configRecyclerViewWithFlexBoxLayout(
        recyclerView: RecyclerView, layoutManager: FlexboxLayoutManager?
    ) {
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        (recyclerView.itemAnimator)!!.changeDuration = 0
        ((recyclerView.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.isNestedScrollingEnabled = false
        layoutManager!!.flexWrap = FlexWrap.WRAP
    }

    // Layout post
    fun initRecyclerViewHorizontalInPost(
        view: RecyclerView, adapter: RecyclerView.Adapter<*>?, count: Int
    ) {
        val layoutManager = GridLayoutManager(view.context, count)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) {
                    2 // Chiếm 2 cột cho item đầu tiên
                } else {
                    1 // Chiếm 1 cột cho các item khác
                }
            }
        }
        configRecyclerView(view, GridLayoutManager(view.context, count))
        view.adapter = adapter
    }

    //format điểm đánh giá nhà hàng
    //ví dụ: 5.0 ---> 5, 5.1, 5.2 ---> 5.1/5.2
    fun formatDoubleToString(value: Double): String {
        val s: String = if (value.toInt().toDouble().compareTo(value) == 0) {
            java.lang.String.format(Locale.getDefault(), "%s", value.toInt())
        } else {
            java.lang.String.format(Locale.getDefault(), "%s", value)
        }
        return s
    }

    //Round double to 2 decimal
    fun roundOffDecimal(numInDouble: Double): Double {
        return BigDecimal(numInDouble.toString()).setScale(2, RoundingMode.HALF_UP).toDouble()
    }


    fun encodeBase64(string: String): String? {
        val data: ByteArray = string.toByteArray(StandardCharsets.UTF_8)
        return Base64.encodeToString(data, Base64.NO_WRAP or Base64.URL_SAFE)
    }

    @SuppressLint("HardwareIds")
    fun generateID(context: Context): String {
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return if (androidId != null && androidId != "9774d56d682e549c") {
            androidId
        } else {
            UUID.randomUUID().toString()
        }
    }

    fun initRecyclerViewGrid(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        configRecyclerView(view, GridLayoutManager(AppApplication.instance?.applicationContext, 3))
        view.adapter = adapter
    }

    fun getxdeuhugColorList(): List<Int> {
        return arrayListOf(
            Color.rgb(187, 0, 20),
            Color.rgb(255, 139, 0),
            Color.rgb(56, 192, 93),
            Color.rgb(102, 170, 214),
            Color.rgb(0, 98, 5),
            Color.rgb(0, 113, 187),
            Color.rgb(217, 80, 138),
            Color.rgb(254, 149, 7),
            Color.rgb(254, 247, 120),
            Color.rgb(106, 167, 134),
            Color.rgb(53, 194, 209),
            Color.rgb(193, 37, 82),
            Color.rgb(255, 102, 0),
            Color.rgb(245, 199, 0),
            Color.rgb(106, 150, 31),
            Color.rgb(179, 100, 53),
            Color.rgb(207, 248, 246),
            Color.rgb(148, 212, 212),
            Color.rgb(136, 180, 187),
            Color.rgb(118, 174, 175),
            Color.rgb(42, 109, 130)
        )
    }

    var EMOJI_FILTER = InputFilter { source, start, end, _, _, _ ->
        for (index in start until end) {
            val type = Character.getType(source[index])
            if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                return@InputFilter ""
            }
        }
        null
    }

    val specialCharacters = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) {
            if (source[i].toString() == " " || source[i].toString() == "," || source[i].toString() == "." || source[i].toString() == "\n") {
                // Do nothing
            } else if (!Character.isLetterOrDigit(source[i])) {
                return@InputFilter ""
            }
        }
        null
    }

    val dotAndCommaCharacters = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) {
            if (source[i].toString() == " ") {
                // Do nothing
            } else if (!Character.isLetterOrDigit(source[i])) {
                return@InputFilter ""
            }
        }
        null
    }

    val spaceCharacters = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) {
            if (Character.isWhitespace(source[i])) {
                return@InputFilter ""
            }
        }
        return@InputFilter null
    }


    fun getCompleteAddressString(context: Context, latitude: Double, longitude: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            @Suppress("DEPRECATION") val addresses =
                geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return strAdd
    }


    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable: Drawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(
            0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    fun calculateTotalPage(totalRecord: Int, limit: Int): Int {
        return if (totalRecord % limit == 0) {
            (totalRecord / limit)
        } else {
            (totalRecord / limit) + 1
        }
    }

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.hide() {
        visibility = View.GONE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }

    fun formatTwoInt(number: Int): String {
        return if (number <= 0) "00" else if (number < 10) "0$number" else number.toString()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun AppEditText.enableScrollText() {
        overScrollMode = View.OVER_SCROLL_ALWAYS
        scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
        isVerticalScrollBarEnabled = true
        setOnTouchListener { _, event ->
            if (!text.isNullOrEmpty()) {
                parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    fun View.clickWithDebounce(debounceTime: Long = 1000L, action: () -> Unit) {
        this.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime: Long = 0

            override fun onClick(v: View) {
                if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                else action()
                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }

    fun checkMimeTypeVideo(type: Int): Boolean {
        return type == 1
    }

    fun getMimeType(url: String): String {
        try {
            return url.substring(url.lastIndexOf(".") + 1)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getRandomString(len: Int): String {
        val AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val rnd = SecureRandom()
        val sb = java.lang.StringBuilder(len)
        for (i in 0 until len) sb.append(AB[rnd.nextInt(AB.length)])
        return sb.toString()
    }

    fun getNameFileFormatTime(path: String): String {
        return String.format(
            "%s.%s", System.currentTimeMillis().toString() + getRandomString(24), getMimeType(path)
        )
    }

    fun dpToPx(context: Context, valueInDp: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
    }

    fun getVideo(url: String, context: Context): String? {
        var link: String? = ""
        val a: Int
        val b: Int
        if (url.contains(context.getString(vn.xdeuhug.seniorsociable.R.string.link_youtube_1))) {
            a = url.indexOf(".be/")
            link = url.substring(a + 4)
        } else if (url.contains(context.getString(vn.xdeuhug.seniorsociable.R.string.link_youtube_4))) {
            a = url.indexOf("?v=")
            if (url.contains("&")) {
                b = url.indexOf("&")
                link = url.substring(a + 3, b)
            } else link = url.substring(a + 3)
        } else if (url.contains(context.getString(vn.xdeuhug.seniorsociable.R.string.link_youtube_3))) {
            a = url.indexOf("?")
            b = url.indexOf("shorts/")
            link = url.substring(b + 7, a)
        }
        Timber.d("load link id : ")
        Timber.d(link)
        return link
    }

    fun generateQRCode(idGroup: String): Bitmap? {
        return try {
            val result: BitMatrix = QRCodeWriter().encode(
                String.format(
                    "%s", idGroup
                ), BarcodeFormat.QR_CODE, 1024, 1024
            )
            val bitmap: Bitmap = Bitmap.createBitmap(
                result.width, result.height, Bitmap.Config.ARGB_8888
            )
            for (y in 0 until result.height) {
                for (x in 0 until result.width) {
                    if (result.get(x, y)) {
                        bitmap.setPixel(x, y, Color.BLACK)
                    }
                }
            }
            bitmap
        } catch (e: WriterException) {
            Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        }
    }

    @Suppress("DEPRECATION")
    fun removeVietnameseFromString(str: String): String {
        val slug: String = try {
            val temp: String = Normalizer.normalize(str, Normalizer.Form.NFD)
            val pattern: Pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            pattern.matcher(temp).replaceAll("")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
        return slug.toLowerCase(Locale.ROOT)
    }

    fun get(string: String): String {
        var result = ""
        val stringText = string.split(" ")
        for (i in stringText.indices) {
            if (stringText[i].isNotEmpty()) { // Kiểm tra độ dài của chuỗi
                result += stringText[i].substring(0, 1).lowercase()
            }
        }
        return result
    }

    fun getXdeuhugColorListPie(): List<Int> {
        return arrayListOf(
            Color.rgb(84, 112, 198),
            Color.rgb(194, 53, 49),
            Color.rgb(98, 200, 127),
            Color.rgb(231, 111, 0),
            Color.rgb(145, 199, 174),
            Color.rgb(154, 96, 180),
            Color.rgb(250, 200, 88),
            Color.rgb(234, 124, 204),
            Color.rgb(0, 162, 174),
            Color.rgb(189, 188, 187)
        )
    }

    fun getGenderFromFacebook(gender: String): Int {
        return if (gender.lowercase() == "male") {
            1
        } else {
            0
        }
    }

    fun setChildListener(parent: View, listener: View.OnClickListener) {
        parent.setOnClickListener(listener)
        if (parent !is ViewGroup) {
            return
        }
        for (i in 0 until parent.childCount) {
            setChildListener(parent.getChildAt(i), listener)
        }
    }

    fun formatInteract(interact: Long): String {
        return String.format("%.3f", interact / 1000.0)
    }

    fun formatFacebookLikes(likes: Int): String {
        return when {
            likes >= 1_000_000_000 -> String.format("%.1fB", likes / 1_000_000_000.0)
            likes >= 1_000_000 -> String.format("%.1fM", likes / 1_000_000.0)
            likes >= 1_000 -> String.format("%.1fK", likes / 1_000.0)
            else -> likes.toString()
        }
    }

    fun logJsonFromObject(ob: Any) {
        Timber.tag("Log Object -" + ob.javaClass)
            .i(GsonBuilder().setPrettyPrinting().create().toJson(ob))
    }


    fun removeVietnameseFromStringNice(str: String): String {
        try {
            val temp = Normalizer.normalize(str, Normalizer.Form.NFD)
            val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            return pattern.matcher(temp).replaceAll("").lowercase(Locale.getDefault())
                .replace("đ".toRegex(), "d")
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    // Extension function để chuyển đổi List thành ArrayList
    fun <T> List<T>.toArrayList(): ArrayList<T> {
        return ArrayList(this)
    }

    fun getLastName(fullName: String): String {
        return if (!fullName.contains(" ")) {
            fullName
        } else {
            var lastPosition = 0
            for (position in fullName.indices) {
                if (fullName[position] == ' ') {
                    if (position != fullName.length - 1) {
                        lastPosition = position + 1
                    }
                }
            }
            if (lastPosition == 0) {
                fullName
            } else {
                fullName.substring(lastPosition, fullName.length)
            }
        }
    }

    fun combineAndHashIds(id1: String, id2: String): String {
        // Sắp xếp theo thứ tự từ điển
        val sortedIds = listOf(id1, id2).sorted()

        //         Ghép chuỗi theo thứ tự
        val combinedString = sortedIds.joinToString("")

        // Sử dụng hàm hash (ở đây là SHA-256) để tạo một chuỗi duy nhất
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(combinedString.toByteArray())

        // Chuyển đổi mảng byte thành chuỗi hex
        val hashedString = StringBuilder()
        for (byte in hashedBytes) {
            hashedString.append(String.format("%02x", byte))
        }

        return hashedString.toString()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMedia(
        reCycle: RecyclerView, multiMedia: ArrayList<MultiMedia>, mediaAdapter: MediaAdapter
    ) {
        when (multiMedia.size) {
            GalleryViewAdapterUtils.ONE -> {
                initRecyclerView(reCycle, mediaAdapter)
            }

            GalleryViewAdapterUtils.TWO -> {
                initRecyclerViewHorizontal(reCycle, mediaAdapter, 2)
            }

            GalleryViewAdapterUtils.THREE, GalleryViewAdapterUtils.FOUR -> {
                initRecyclerViewVerticalWithStaggeredGridLayoutManager(reCycle, mediaAdapter, 2)
            }
        }
        mediaAdapter.setData(multiMedia)
        mediaAdapter.notifyDataSetChanged()
    }

    /**
     * @param sumTab Truyền vào
     *
     * */
    @SuppressLint("NotifyDataSetChanged")
    fun initTabReact(
        listIdsReact: List<MutableMap.MutableEntry<Int, Int>>,
        reCycle: RecyclerView,
        tabAdapter: TabAdapter
    ) {
        val listTab = getListReact(listIdsReact.map { it.key }.toArrayList(), listIdsReact)
        tabAdapter.setData(listTab)
        initRecyclerViewHorizontal(reCycle, tabAdapter)
        tabAdapter.notifyDataSetChanged()
    }

    private fun getListReact(
        listIdsReact: ArrayList<Int>, mapInteract: List<MutableMap.MutableEntry<Int, Int>>
    ): ArrayList<Tab> {
        when (listIdsReact.size) {
            1 -> {
                return arrayListOf(Tab(listIdsReact[0], "", mapInteract[0].value, true))
            }

            2 -> {
                return arrayListOf(
                    Tab(listIdsReact[0], "", mapInteract[0].value, true),
                    Tab(listIdsReact[1], "", mapInteract[1].value, false)
                )
            }

            3 -> {
                return arrayListOf(
                    Tab(listIdsReact[0], "", mapInteract[0].value, true),
                    Tab(listIdsReact[1], "", mapInteract[1].value, false),
                    Tab(listIdsReact[2], "", mapInteract[2].value, false)
                )
            }

            4 -> {
                return arrayListOf(
                    Tab(listIdsReact[0], "", mapInteract[0].value, true),
                    Tab(listIdsReact[1], "", mapInteract[1].value, false),
                    Tab(listIdsReact[2], "", mapInteract[2].value, false),
                    Tab(listIdsReact[3], "", mapInteract[3].value, false)
                )
            }

            5 -> {
                return arrayListOf(
                    Tab(listIdsReact[0], "", mapInteract[0].value, true),
                    Tab(listIdsReact[1], "", mapInteract[1].value, false),
                    Tab(listIdsReact[2], "", mapInteract[2].value, false),
                    Tab(listIdsReact[3], "", mapInteract[3].value, false),
                    Tab(listIdsReact[4], "", mapInteract[4].value, false)
                )
            }

            6 -> {
                return arrayListOf(
                    Tab(listIdsReact[0], "", mapInteract[0].value, true),
                    Tab(listIdsReact[1], "", mapInteract[1].value, false),
                    Tab(listIdsReact[2], "", mapInteract[2].value, false),
                    Tab(listIdsReact[3], "", mapInteract[3].value, false),
                    Tab(listIdsReact[4], "", mapInteract[4].value, false),
                    Tab(listIdsReact[5], "", mapInteract[5].value, false)
                )
            }

            else -> return arrayListOf()
        }
    }

    fun setFontTypeFaceTitleBar(context: Context, titleBar: com.hjq.bar.TitleBar) {
        val typeface =
            ResourcesCompat.getFont(context, vn.xdeuhug.seniorsociable.R.font.roboto_medium)
        titleBar.titleView.typeface = typeface
        titleBar.leftView.typeface = typeface
        titleBar.rightView.typeface = typeface
    }

    @SuppressLint("DiscouragedApi")
    fun createHobbiesList(context: Context, resources: Resources): ArrayList<Hobby> {
        val hobbiesList = ArrayList<Hobby>()

        val hobbyNames = listOf(
            "play_soccer",
            "play_piano",
            "read_books",
            "travel",
            "cook",
            "paint",
            "play_video_games",
            "photography",
            "hiking",
            "bike_riding",
            "gardening",
            "watch_movies",
            "listen_to_music",
            "dance",
            "yoga",
            "play_chess",
            "coding",
            "write",
            "play_board_games",
            "swimming",
            "play_guitar",
            "volunteer",
            "play_basketball",
            "camping",
            "draw",
            "meditate",
            "play_tennis",
            "bird_watching",
            "learn_languages",
            "play_volleyball",
            "surfing",
            "singing",
            "knitting",
            "astronomy",
            "fishing",
            "play_cricket",
            "play_golf",
            "calligraphy",
            "skateboarding",
            "sudoku",
            "magic_tricks",
            "play_rugby",
            "sculpture",
            "play_table_tennis",
            "origami",
            "collect_stamps",
            "play_badminton",
            "horseback_riding"
        )

        for ((index, hobbyName) in hobbyNames.withIndex()) {
            val nameRes = resources.getIdentifier(
                hobbyName,
                "string",
                resources.getResourcePackageName(vn.xdeuhug.seniorsociable.R.string.app_name)
            )
            val imageRes = resources.getIdentifier(
                "ic_hobby_$hobbyName",
                "drawable",
                resources.getResourcePackageName(vn.xdeuhug.seniorsociable.R.drawable.ic_launcher_foreground)
            )
            val hobby = Hobby(index + 1, context.getString(nameRes), imageRes)
            hobbiesList.add(hobby)
        }
        AppUtils.logJsonFromObject(hobbiesList)
        return hobbiesList
    }

    fun getListHobbyIds(listIds: ArrayList<Int>, listHobby: ArrayList<Hobby>): ArrayList<Hobby> {
        val listHobbyNews = ArrayList<Hobby>()
        listHobby.forEach {
            if (it.id in listIds) {
                listHobbyNews.add(it)
            }
        }
        return listHobbyNews
    }

    fun getListHobby(context: Context): ArrayList<Hobby> {
        val listHobby = arrayListOf(
            Hobby(
                1,
                context.getString(vn.xdeuhug.seniorsociable.R.string.play_soccer),
                vn.xdeuhug.seniorsociable.R.drawable.ic_hobby_play_soccer
            ), Hobby(
                2,
                context.getString(vn.xdeuhug.seniorsociable.R.string.play_piano),
                vn.xdeuhug.seniorsociable.R.drawable.ic_hobby_play_piano
            )
        )
        return listHobby
    }

    /**
     * Provide the list of emoji in form of unicode string
     *
     * @param context context
     * @return list of emoji unicode
     */
    fun getEmojis(context: Context?): java.util.ArrayList<String> {
        val convertedEmojiList = java.util.ArrayList<String>()
        val emojiList =
            context!!.resources.getStringArray(vn.xdeuhug.seniorsociable.R.array.photo_editor_emoji)
        for (emojiUnicode in emojiList) {
            convertedEmojiList.add(convertEmoji(emojiUnicode))
        }
        return convertedEmojiList
    }

    private fun convertEmoji(emoji: String): String {
        return try {
            val convertEmojiToInt = emoji.substring(2).toInt(16)
            String(Character.toChars(convertEmojiToInt))
        } catch (e: NumberFormatException) {
            ""
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setForeground(item: User, imageView: ImageView, context: Context) {
        if (item.online) {
            imageView.foreground = context.getDrawable(R.drawable.bg_avatar_online)
        } else {
            imageView.foreground = context.getDrawable(R.drawable.bg_avatar_offline)
        }
    }

    fun scaleImageFitWidthScreen(imvImage: ImageView, context: Context, height: Int, width: Int) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        imvImage.layoutParams.height = scaledHeight
        imvImage.layoutParams.width = screenWidth
        imvImage.requestLayout()
    }

    fun scaleImageThreePartFourWidthScreen(
        imvImage: ImageView, context: Context, height: Int, width: Int
    ) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        val maxHeight = context.resources.getDimension(R.dimen.dp_280)
        val finalHeight = if (scaledHeight > maxHeight) maxHeight else scaledHeight
        imvImage.layoutParams.height = finalHeight.toInt()
        imvImage.layoutParams.width = screenWidth
        imvImage.requestLayout()
    }

    fun scaleVideoThreePartFourWidthScreen(
        videoView: JzvdStd, context: Context, height: Int, width: Int
    ) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        val maxHeight = context.resources.getDimension(R.dimen.dp_280)
        val finalHeight = if (scaledHeight > maxHeight) maxHeight else scaledHeight
        videoView.layoutParams.height = finalHeight.toInt()
        videoView.layoutParams.width = screenWidth
        videoView.requestLayout()
    }

    fun scaleImageThreePartFourWidthScreenWithFourItem(
        imvImage: ImageView, context: Context, height: Int, width: Int
    ) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        val maxHeight = context.resources.getDimension(R.dimen.dp_300)
        val finalHeight = if (scaledHeight > maxHeight) maxHeight else scaledHeight
        imvImage.layoutParams.height = finalHeight.toInt()
        imvImage.layoutParams.width = screenWidth
        imvImage.requestLayout()
    }

    fun scaleVideoThreePartFourWidthScreenWithFourItem(
        videoView: JzvdStd, context: Context, height: Int, width: Int
    ) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        val maxHeight = context.resources.getDimension(R.dimen.dp_300)
        val finalHeight = if (scaledHeight > maxHeight) maxHeight else scaledHeight
        videoView.layoutParams.height = finalHeight.toInt()
        videoView.layoutParams.width = screenWidth
        videoView.requestLayout()
    }

    fun scaleImageOnePartFourWidthScreen(
        imvImage: ImageView, context: Context, height: Int, width: Int, total: Int
    ) {
        var screenWidth = context.resources.displayMetrics.widthPixels
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        var maxHeight = 0f
        when (total) {
            2 -> {
                maxHeight = context.resources.getDimension(R.dimen.dp_134)
            }

            3 -> {
                screenWidth = context.resources.displayMetrics.widthPixels * 2 / 3
                maxHeight = context.resources.getDimension(R.dimen.dp_97)
            }
        }
        val finalHeight = if (scaledHeight > maxHeight) maxHeight else scaledHeight
        imvImage.layoutParams.height = finalHeight.toInt()
        imvImage.layoutParams.width = screenWidth
        imvImage.requestLayout()
    }

    fun scaleVideoOnePartFourWidthScreen(
        videoView: JzvdStd, context: Context, height: Int, width: Int, total: Int
    ) {
        var screenWidth = context.resources.displayMetrics.widthPixels
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        var maxHeight = 0f
        when (total) {
            2 -> {
                maxHeight = context.resources.getDimension(R.dimen.dp_134)
            }

            3 -> {
                screenWidth = context.resources.displayMetrics.widthPixels * 2 / 3
                maxHeight = context.resources.getDimension(R.dimen.dp_97)
            }
        }
        val finalHeight = if (scaledHeight > maxHeight) maxHeight else scaledHeight
        videoView.layoutParams.height = finalHeight.toInt()
        videoView.layoutParams.width = screenWidth
        videoView.requestLayout()
    }

    fun scaleImageHaftWidthScreen(imvImage: ImageView, context: Context, height: Int, width: Int) {
        val screenWidth = context.resources.displayMetrics.widthPixels / 2
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        imvImage.layoutParams.height = (scaledHeight * 1.15).toInt()
        imvImage.layoutParams.width = screenWidth
        imvImage.requestLayout()
    }

    fun scaleVideoHaftWidthScreen(videoView: JzvdStd, context: Context, height: Int, width: Int) {
        val screenWidth = context.resources.displayMetrics.widthPixels / 2
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        videoView.layoutParams.height = (scaledHeight * 1.15).toInt()
        videoView.layoutParams.width = screenWidth
        videoView.requestLayout()
    }

    fun scaleImageInComment(
        imvImage: ImageView, context: Context, height: Int, width: Int
    ) {
        val screenWidth = context.resources.getDimension(R.dimen.dp_80)
        val scaleRatio = screenWidth / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        val maxHeight = context.resources.getDimension(R.dimen.dp_120)
        val finalHeight = if (scaledHeight > maxHeight) maxHeight else scaledHeight
        imvImage.layoutParams.height = finalHeight.toInt()
        imvImage.requestLayout()
    }

    fun scaleImageFitWidthScreenPost(
        imvImage: ImageView, context: Context, height: Int, width: Int
    ) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        val maxHeight = context.resources.getDimension(R.dimen.dp_280)
        val finalHeight = if (scaledHeight > maxHeight) maxHeight else scaledHeight
        imvImage.layoutParams.height = finalHeight.toInt()
        imvImage.layoutParams.width = screenWidth
        imvImage.requestLayout()
    }

    fun scaleVideoFitWidthScreenPost(
        videoView: JzvdStd, context: Context, height: Int, width: Int
    ) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        val maxHeight = context.resources.getDimension(R.dimen.dp_280)
        val finalHeight = if (scaledHeight > maxHeight) maxHeight else scaledHeight
        videoView.layoutParams.height = finalHeight.toInt()
        videoView.layoutParams.width = screenWidth
        videoView.requestLayout()
    }

    fun scaleImageFitWidthParentInStory(
        imvImage: ImageView, context: Context, height: Int, width: Int
    ) {
        val screenWidth = context.resources.getDimension(R.dimen.dp_104)
        val scaleRatio = screenWidth.toFloat() / width.toFloat()
        val scaledHeight = (height.toFloat() * scaleRatio).toInt()
        imvImage.layoutParams.height = scaledHeight
        imvImage.layoutParams.width = screenWidth.toInt()
        imvImage.requestLayout()
    }

    fun convertDpToPixel(resources: Resources, dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale).toInt()
    }

    // Hàm chuyển đổi pixel thành dp
    fun convertPixelToDp(resources: Resources, px: Int): Int {
        val density = resources.displayMetrics.density
        return (px / density).toInt()
    }

    fun getDropboxIMGSize(uri: Uri): Pair<Int, Int> {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.path!!).absolutePath, options)
        return Pair(options.outHeight, options.outWidth)
    }

    fun RecyclerView?.getCurrentPosition(): Int {
        return (this?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    fun RecyclerView.addScrollListener(onScroll: (position: Int) -> Unit) {
        var lastPosition = 0
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (layoutManager is LinearLayoutManager) {
                    val currentVisibleItemPosition =
                        (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                    if (lastPosition != currentVisibleItemPosition && currentVisibleItemPosition != RecyclerView.NO_POSITION) {
                        onScroll.invoke(currentVisibleItemPosition)
                        lastPosition = currentVisibleItemPosition
                    }
                }
            }
        })
    }

    fun RecyclerView.onScrollDoneGetPosition(onScrollUpdate: (Int) -> Unit) {
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_FLING -> {
                    }

                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {
                        print("When User Done it's Scroll")
                        val currentPosition =
                            (this@onScrollDoneGetPosition.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        onScrollUpdate.invoke(currentPosition)
                    }

                    AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL -> {
                    }
                }
            }
        })
    }

    fun TextView.removeLinksUnderline() {
        val spannable = SpannableString(text)
        for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
            spannable.setSpan(object : URLSpan(u.url) {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }, spannable.getSpanStart(u), spannable.getSpanEnd(u), 0)
        }
        text = spannable
    }

    fun singleTextView(
        typePost:Int,
        context: Context,
        textView: TextView,
        userPost: User,
        listUserTag: ArrayList<User>,
        address: vn.xdeuhug.seniorsociable.model.entity.modelUser.Address?,
        onListenerClickTagUser: OnListenerClickTagUser
    ) {
        // User post
        val spanText = SpannableStringBuilder()

        // Thêm userName với ClickableSpan
        spanText.append(userPost.name)
        spanText.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                onListenerClickTagUser.clickNameUserInPost(userPost)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color =
                    context.getColor(R.color.gray_900)
                textPaint.isUnderlineText = false
                textPaint.isFakeBoldText = true
            }
        }, spanText.length - userPost.name.length, spanText.length, 0)

        // First tag user
        // User post

        // Thêm userName với ClickableSpan
        when (listUserTag.size) {
            0 -> {
                // Không làm gì
            }

            1 -> {
                spanText.append(" ${context.getString(R.string.with_of)} ")
                //
                spanText.append(listUserTag[0].name)
                spanText.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onListenerClickTagUser.clickNameUserInPost(listUserTag[0])
                    }

                    override fun updateDrawState(textPaint: TextPaint) {
                        textPaint.color =
                            context.getColor(R.color.gray_900)
                        textPaint.isUnderlineText = false
                        textPaint.isFakeBoldText = true
                    }
                }, spanText.length - listUserTag[0].name.length, spanText.length, 0)
            }

            2 -> {
                spanText.append(" ${context.getString(R.string.with_of)} ")
                //
                spanText.append(listUserTag[0].name)
                spanText.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onListenerClickTagUser.clickNameUserInPost(listUserTag[0])
                    }

                    override fun updateDrawState(textPaint: TextPaint) {
                        textPaint.color =
                            context.getColor(R.color.gray_900)
                        textPaint.isUnderlineText = false
                        textPaint.isFakeBoldText = true
                    }
                }, spanText.length - listUserTag[0].name.length, spanText.length, 0)
                //
                spanText.append(" ${context.getString(R.string.and)} ")
                //
                spanText.append(listUserTag[1].name)
                spanText.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onListenerClickTagUser.clickNameUserInPost(listUserTag[1])
                    }

                    override fun updateDrawState(textPaint: TextPaint) {
                        textPaint.color =
                            context.getColor(R.color.gray_900)
                        textPaint.isUnderlineText = false
                        textPaint.isFakeBoldText = true
                    }
                }, spanText.length - listUserTag[1].name.length, spanText.length, 0)
            }

            else -> {
                spanText.append(" ${context.getString(R.string.with_of)} ")
                //
                spanText.append(listUserTag[0].name)
                spanText.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onListenerClickTagUser.clickNameUserInPost(listUserTag[0])
                    }

                    override fun updateDrawState(textPaint: TextPaint) {
                        textPaint.color =
                            context.getColor(R.color.gray_900)
                        textPaint.isUnderlineText = false
                        textPaint.isFakeBoldText = true
                    }
                }, spanText.length - listUserTag[0].name.length, spanText.length, 0)
                //
                spanText.append(" ${context.getString(R.string.and)} ")
                //
                spanText.append("${listUserTag.size - 1} ${context.getString(R.string.other_people)}")
                spanText.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            onListenerClickTagUser.clickListUser(listUserTag)
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            textPaint.color =
                                context.getColor(R.color.gray_900)
                            textPaint.isUnderlineText = false
                            textPaint.isFakeBoldText = true
                        }
                    },
                    spanText.length - "${listUserTag.size - 1} ${context.getString(R.string.other_people)}".length,
                    spanText.length,
                    0
                )
            }
        }

        if (address != null) {
            spanText.append(" ${context.getString(R.string.at)} ")
            //
            // Thêm userName với ClickableSpan
            spanText.append(address.address)
            spanText.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onListenerClickTagUser.clickAddress(address)
                }

                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.color =
                        context.getColor(R.color.gray_900)
                    textPaint.isUnderlineText = false
                    textPaint.isFakeBoldText = true
                }
            }, spanText.length - address.address.length, spanText.length, 0)
        }
        when(typePost)
        {
            PostConstants.POST_UPDATE_AVATAR ->{
                val textAvatar = " ${context.getString(R.string.is_updated_avatar)}"
                spanText.append(textAvatar)
                spanText.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onListenerClickTagUser.clickNameUserInPost(userPost)
                    }

                    override fun updateDrawState(textPaint: TextPaint) {
                        textPaint.color =
                            context.getColor(R.color.gray_900)
                        textPaint.isUnderlineText = false
                        textPaint.isFakeBoldText = false
                    }
                }, spanText.length - textAvatar.length, spanText.length, 0)
            }
            PostConstants.POST_UPDATE_BACKGROUND ->{
                val textBG = " ${context.getString(R.string.is_updated_background)}"
                spanText.append(textBG)
                spanText.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onListenerClickTagUser.clickNameUserInPost(userPost)
                    }

                    override fun updateDrawState(textPaint: TextPaint) {
                        textPaint.color =
                            context.getColor(R.color.gray_900)
                        textPaint.isUnderlineText = false
                        textPaint.isFakeBoldText = false
                    }
                }, spanText.length - textBG.length, spanText.length, 0)
            }
        }

        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = spanText
    }

    fun setTextTitleReply(
        context: Context,
        textView: TextView,
        userReply: User
    )
    {
        // User post
        val spanText = SpannableStringBuilder()
        spanText.append("${context.getString(R.string.is_repling)} - ")
        if(userReply.id == UserCache.getUser().id)
        {
            // Thêm userName với ClickableSpan
            spanText.append(context.getString(R.string.you))
            spanText.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    //
                }

                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.color =
                        context.getColor(R.color.gray_900)
                    textPaint.isUnderlineText = false
                    textPaint.isFakeBoldText = true
                }
            }, spanText.length - context.getString(R.string.you).length, spanText.length, 0)
        }else{
            // Thêm userName với ClickableSpan
            spanText.append(userReply.name)
            spanText.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    //
                }

                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.color =
                        context.getColor(R.color.gray_900)
                    textPaint.isUnderlineText = false
                    textPaint.isFakeBoldText = true
                }
            }, spanText.length - userReply.name.length, spanText.length, 0)
        }
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = spanText
    }

    fun mapClickableSpansToIds(
        text: Editable,
        clickableSpans: List<ClickableSpan>,
        nameToIdMap: Map<String, String>
    ): String {
        val result = StringBuilder()
        var startIndex = 0

        for (clickableSpan in clickableSpans) {
            val start = text.getSpanStart(clickableSpan)
            val end = text.getSpanEnd(clickableSpan)

            // Thêm phần không chứa span vào kết quả
            result.append(text.subSequence(startIndex, start))

            // Lấy tên người dùng từ văn bản
            val userName = text.subSequence(start, end).toString()

            // Kiểm tra và thêm userId hoặc tên người dùng nếu không có
            val userId = nameToIdMap[userName]
            if (userId != null) {
                result.append("@[$userId]")
            } else {
                result.append(userName)
            }

            startIndex = end
        }

        // Thêm phần cuối cùng của văn bản vào kết quả
        result.append(text.subSequence(startIndex, text.length))

        return result.toString()
    }


    fun findClickableSpans(text: Editable): List<ClickableSpan> {
        val clickableSpans = text.getSpans(0, text.length, ClickableSpan::class.java)
        return clickableSpans.toList()
    }




    fun setTextUserTagByIdUserInTextView(
        context: Context,
        textView: TextView,
        textComment: String,
        userIdToNameMap: HashMap<String, String>,
        onClickName: OnListenerClickNameTagUser
    ) {
        val spannableStringBuilder = SpannableStringBuilder(textComment)
        val pattern = Regex("@\\[(\\w+)]")
        val matches = pattern.findAll(textComment)
        for (match in matches) {
            val userId = match.groupValues[1]
            val user = ListUserCache.getList().first { it.id == userId }
            val userName = userIdToNameMap[userId]

            if (userName != null) {
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onClickName.clickNameUserInComment(user)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = context.getColor(R.color.gray_900)
                        ds.isUnderlineText = false
                        ds.isFakeBoldText = true
                    }
                }

                spannableStringBuilder.setSpan(
                    clickableSpan, match.range.first, match.range.last + 1, 0
                )
                spannableStringBuilder.replace(match.range.first, match.range.last + 1, userName)
            }
        }

        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = spannableStringBuilder
    }

    fun startDetailUserPage(user: User,activity:Activity?) {
        if (user.id == UserCache.getUser().id) {
            activity?.let {
                val intent = Intent(it, Class.forName(ModuleClassConstants.PERSONAL_ACTIVITY))
                intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                it.startActivity(intent)
            }
        } else {
            activity?.let {
                val intent = Intent(
                    it, Class.forName(ModuleClassConstants.PERSONAL_MEMBER_ACTIVITY)
                )
                intent.putExtra(AppConstants.PERSONAL_USER, Gson().toJson(user))
                it.startActivity(intent)
            }
        }
    }

    interface OnListenerClickTagUser {
        fun clickNameUserInPost(user: User)
        fun clickListUser(listUserTag: ArrayList<User>)
        fun clickAddress(address: vn.xdeuhug.seniorsociable.model.entity.modelUser.Address)
    }

    interface OnListenerClickNameTagUser {
        fun clickNameUserInComment(user: User)
    }


}