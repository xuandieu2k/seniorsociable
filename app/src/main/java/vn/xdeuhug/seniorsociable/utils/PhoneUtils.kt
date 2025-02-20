package vn.xdeuhug.seniorsociable.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Typeface
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hbb20.CountryCodePicker
import org.jetbrains.anko.textColor
import timber.log.Timber.Forest.tag
import vn.xdeuhug.seniorsociable.R

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 28/08/2023
 */
object PhoneUtils {
    fun getFormattedPhoneNumber(phoneNumber: String?, countryCode: String?): String {
        val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
        return try {
            val parsedNumber = phoneNumberUtil.parse(phoneNumber, countryCode)
            tag("Phone Current").d(
                phoneNumberUtil.format(
                    parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164
                )
            )
            val format: String =
                phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
            format
        } catch (e: NumberFormatException) {
            tag("Exception phone number").d(e)
            ""
        }
    }

    fun isPhoneNumberValid(phoneNumber: String?, countryCode: String?): Boolean {
        val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
        return try {
            val parsedNumber = phoneNumberUtil.parse(phoneNumber, countryCode)
            phoneNumberUtil.isValidNumber(parsedNumber)
        } catch (e: Exception) {
            tag("Exception phone number").d(e)
            false
        }
    }

    fun getNationalNumber(phoneNumber: String?, countryNameCode: String?,countryCode: String?): String {
        val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
        return try {
            val parsedNumber = phoneNumberUtil.parse(phoneNumber, countryNameCode)
            tag("Phone National Current").d(
                phoneNumberUtil.getNationalSignificantNumber(parsedNumber)
            )
            val nationalSignificantNumber =
                phoneNumberUtil.getNationalSignificantNumber(parsedNumber)
            countryCode+nationalSignificantNumber
        } catch (e: NumberFormatException) {
            tag("Exception phone number").d(e)
            ""
        }
    }

    fun setUpDialogPhone(countryCode: CountryCodePicker, context: Context, resources: Resources) {
        countryCode.setDialogEventsListener(object : CountryCodePicker.DialogEventsListener {
            // com.hbb20.CountryCodePicker.DialogEventsListener
            @SuppressLint("CutPasteId", "UseCompatLoadingForDrawables")
            override fun onCcpDialogOpen(dialog: Dialog) {
                // FastScroll
                val fastScroll = dialog.findViewById<View>(com.hbb20.R.id.fastscroll)
                fastScroll.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
                // Title dialog
                val tvTitle = dialog.findViewById<TextView>(com.hbb20.R.id.textView_title)
                // Relative layout is view parent search
                val rlSearch = dialog.findViewById<RelativeLayout>(com.hbb20.R.id.rl_query_holder)
                // Image clear key search
                val imgClearQuery = dialog.findViewById<ImageView>(com.hbb20.R.id.img_clear_query)
                // Editext search
                val edtSearch = dialog.findViewById<EditText>(com.hbb20.R.id.editText_search)
                // Relative layout is view title dialog
                val rlTitle = dialog.findViewById<RelativeLayout>(com.hbb20.R.id.rl_title)
                tvTitle.gravity = 1
                tvTitle.isAllCaps = true
                tvTitle.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                tvTitle.textSize = resources.getDimension(R.dimen.sp_6)
                tvTitle.textColor =
                    ContextCompat.getColor(context, R.color.white)
                edtSearch.background = ContextCompat.getDrawable(
                    context, R.drawable.bg_gray_100_stroke_gray_200_8px
                )
                val layoutParams = rlSearch.layoutParams
                val relativeParams = layoutParams as RelativeLayout.LayoutParams
                relativeParams.setMargins(0, 0, 0, 10)
                rlSearch.layoutParams = relativeParams
                rlTitle.background = context.getDrawable(R.drawable.bg_border_header_blue_seniorsociable)
                val window = dialog.window
                window?.setBackgroundDrawable(ContextCompat.getDrawable(
                        context, R.drawable.bg_border_dialog))
                imgClearQuery.setImageResource(R.drawable.icn_multisuggest_remove)
                val width = resources.displayMetrics.widthPixels * 5 / 6
                val height = resources.displayMetrics.heightPixels * 9 / 10
                window?.setLayout(width, height)
                window?.setGravity(17)
                dialog.show()
            }

            // com.hbb20.CountryCodePicker.DialogEventsListener
            override fun onCcpDialogDismiss(dialogInterface: DialogInterface) {
                //
            }

            // com.hbb20.CountryCodePicker.DialogEventsListener
            override fun onCcpDialogCancel(dialogInterface: DialogInterface) {
                //
            }
        })
        countryCode.changeDefaultLanguage(CountryCodePicker.Language.VIETNAMESE)
//        countryCode.setCustomDialogTextProvider(object :
//            CountryCodePicker.CustomDialogTextProvider {
//            override fun getCCPDialogTitle(
//                language: CountryCodePicker.Language, defaultTitle: String
//            ): String {
//                return context.getString(R.string.choose_country)
//            }
//
//            // com.hbb20.CountryCodePicker.CustomDialogTextProvider
//            override fun getCCPDialogSearchHintText(
//                language: CountryCodePicker.Language, defaultSearchHintText: String
//            ): String {
//                return context.getString(R.string.search)
//            }
//
//            // com.hbb20.CountryCodePicker.CustomDialogTextProvider
//            override fun getCCPDialogNoResultACK(
//                language: CountryCodePicker.Language, defaultNoResultACK: String
//            ): String {
//                return context.getString(R.string.not_found_data)
//            }
//        })
    }
}