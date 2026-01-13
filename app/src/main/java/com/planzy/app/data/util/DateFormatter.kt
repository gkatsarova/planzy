package com.planzy.app.data.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
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
            val instant = Instant.parse(
                if (dateString.endsWith("Z")) dateString
                else "${dateString}Z"
            )

            val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

            zonedDateTime.format(shortFormatter)
        } catch (e: Exception) {
            dateString
        }
    }
}