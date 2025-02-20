package vn.xdeuhug.seniorsociable.utility.ui.activity

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import vn.xdeuhug.seniorsociable.BuildConfig
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.utils.AppUtils.removeLinksUnderline
import vn.xdeuhug.seniorsociable.utility.databinding.ActivityInformationAppBinding
import vn.xdeuhug.seniorsociable.utility.model.UpdateVersion
import vn.xdeuhug.seniorsociable.utility.ui.adapter.UpdateVersionAdapter

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 12 / 2023
 */
class InformationAppActivity : AppActivity() {
    private lateinit var binding: ActivityInformationAppBinding
    private lateinit var updateVersionAdapter: UpdateVersionAdapter
    private var listVersion = ArrayList<UpdateVersion>()
    override fun getLayoutView(): View {
        binding = ActivityInformationAppBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setDataForView()
//        getListVersion()
    }

//    private fun getListVersion() {
//        EasyHttp.get(this).api(
//            GetHistoryVersionUpdateAPI.params()
//        ).request(object : HttpCallbackProxy<HttpData<UpdateVersionResponse>>(this) {
//            @SuppressLint("NotifyDataSetChanged")
//            override fun onHttpSuccess(result: HttpData<UpdateVersionResponse>) {
//                if (result.isRequestSucceed()) {
//                    listVersion.clear()
//                    listVersion.addAll(result.getData()!!.list)
//                    updateVersionAdapter.notifyDataSetChanged()
//                } else {
//                    toast(result.getMessage())
//                }
//            }
//        })
//    }

    @SuppressLint("SetTextI18n")
    private fun setDataForView() {
        AppUtils.setFontTypeFaceTitleBar(getContext(),binding.tbTitleApp)
        binding.itemHead.tvHeadVersion.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
        setTextWebsite()
        initRecycleView()
    }

    private fun setTextWebsite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.itemHead.tvWebsite.text = Html.fromHtml(
                "${getString(R.string.website)}<br/><a style='text-decoration:none;' href='https://seniorsociable.vn'>https://seniorsociable.vn</a>",
                Html.FROM_HTML_MODE_LEGACY
            )
        } else {
            binding.itemHead.tvWebsite.text = HtmlCompat.fromHtml(
                "${getString(R.string.website)}<br/><a style='text-decoration:none;' href='https://seniorsociable.vn'>https://seniorsociable.vn</a>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        binding.itemHead.tvWebsite.removeLinksUnderline()
        binding.itemHead.tvWebsite.movementMethod = LinkMovementMethod.getInstance()

//        val websiteText = "${getString(R.string.website)}&ensp;<a href='https://dashboard.supplier.vn'>https://dashboard.supplier.vn</a>"
//
//        val spannableString = SpannableString(websiteText)
//        val clickableSpan = object : ClickableSpan() {
//            override fun onClick(widget: View) {
//                BrowserActivity.start(
//                    getContext(), "https://dashboard.supplier.vn", ""
//                )
//            }
//        }
//
//        // Áp dụng ClickableSpan cho đoạn văn bản có liên kết
//        spannableString.setSpan(clickableSpan, websiteText.indexOf("https"), websiteText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//        // Loại bỏ đường gạch chân từ liên kết
//        spannableString.removeSpan(URLSpan::class.java)
//
//        // Thiết lập văn bản đã xử lý trong TextView
//        binding.itemHead.tvWebsite.text = spannableString
//
//        // Bật sự kiện nhấp vào liên kết
//        binding.itemHead.tvWebsite.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun initRecycleView() {
        updateVersionAdapter = UpdateVersionAdapter(getContext())
        updateVersionAdapter.setData(listVersion)
        AppUtils.initRecyclerView(binding.rvUpdateVersion, updateVersionAdapter)
    }

    override fun initData() {
        //
    }
}