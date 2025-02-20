package vn.xdeuhug.seniorsociable.ui.dialog

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.appcompat.view.ContextThemeWrapper
import vn.xdeuhug.seniorsociable.R
import java.util.Locale




/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 24/06/2023
 */
class CustomDatePickerDialog(
    context: Context,year: Int,
    month: Int,
    dayOfMonth: Int
) : DatePickerDialog(
    ContextThemeWrapper(context, R.style.DatePickerDialogTheme),
    null,
    year,
    month,
    dayOfMonth
) {
    init {
        Locale.setDefault(Locale.getDefault())
    }

    override fun onDateChanged(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        //
    }

}

