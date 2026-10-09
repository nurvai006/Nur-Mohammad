package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    /**
     * Converts standard English digits to Bengali digits.
     */
    fun toBengaliDigits(input: String): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBengaliDigits(number: Number): String {
        return toBengaliDigits(number.toString())
    }

    /**
     * Formats integer amount into Bangladeshi Taka currency format (e.g. ৳1,250 or ৳১,২৫০).
     */
    fun formatTaka(amount: Int, inBengaliNumeral: Boolean = true): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount)
        return if (inBengaliNumeral) {
            "৳" + toBengaliDigits(formatted)
        } else {
            "৳$formatted"
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        return sdf.format(Date(timestamp))
    }
}
