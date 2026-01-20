package com.planzy.app.data.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    @RequiresApi(Build.VERSION_CODES.O)
    private val shortFormatter = DateTimeFormatter.ofPattern(
        "dd MMM yyyy",
        Locale.ENGLISH
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatToShort(dateString: String): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(
                dateString,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
            zonedDateTime.format(shortFormatter)
        } catch (e: Exception) {
            try {
                val localDateTime = java.time.LocalDateTime.parse(
                    dateString,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                localDateTime.format(shortFormatter)
            } catch (e2: Exception) {
                dateString
            }
        }
    }
}