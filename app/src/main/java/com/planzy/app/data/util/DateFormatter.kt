package com.planzy.app.data.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    private val shortFormatter = DateTimeFormatter.ofPattern(
        "dd MMM yyyy",
        Locale.ENGLISH
    )

    fun formatToShort(dateString: String): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(
                dateString,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
            zonedDateTime.format(shortFormatter)
        } catch (_: Exception) {
            try {
                val localDateTime = java.time.LocalDateTime.parse(
                    dateString,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                localDateTime.format(shortFormatter)
            } catch (_: Exception) {
                dateString
            }
        }
    }
}