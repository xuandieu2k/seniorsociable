package vn.xdeuhug.seniorsociable.other

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class SpecialCharacterTextWatcher(private val editText: EditText) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Không cần xử lý trước khi văn bản thay đổi
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Kiểm tra văn bản sau khi thay đổi
        val filteredText = s?.filter { it.isLetterOrDigit() || it.isWhitespace() }
        if (s.toString() != filteredText) {
            editText.removeTextChangedListener(this)
            editText.text.replace(0, editText.length(), filteredText)
            editText.addTextChangedListener(this)
            editText.setSelection(filteredText?.length ?: 0)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        // Không cần xử lý sau khi văn bản thay đổi
    }
}
