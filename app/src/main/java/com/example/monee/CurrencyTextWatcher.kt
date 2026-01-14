package com.example.monee

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat

class CurrencyTextWatcher(private val editText: EditText) : TextWatcher {
    private val decimalFormat = DecimalFormat("#,###")
    init {
        decimalFormat.isGroupingUsed = true
        decimalFormat.maximumFractionDigits = 0
        val symbols = decimalFormat.decimalFormatSymbols
        symbols.groupingSeparator = '.'
        decimalFormat.decimalFormatSymbols = symbols
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable?) {
        editText.removeTextChangedListener(this)
        try {
            var originalString = s.toString()
            originalString = originalString.replace("\\D".toRegex(), "")
            if (originalString.isNotEmpty()) {
                val longval = originalString.toLong()
                val formattedString = decimalFormat.format(longval)
                editText.setText(formattedString)
                editText.setSelection(editText.text.length)
            } else {
                editText.setText("")
            }
        } catch (nfe: NumberFormatException) {
            nfe.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        editText.addTextChangedListener(this)
    }
}
