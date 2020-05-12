@file:JvmName("DateManager")

package iam.thevoid.mediapicker.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by iam on 22/08/2017.
 */
fun formatDateToString(outputPattern: String, sDate: Date? = Date()): String {
    return SimpleDateFormat(outputPattern, Locale.getDefault()).format(sDate)
}