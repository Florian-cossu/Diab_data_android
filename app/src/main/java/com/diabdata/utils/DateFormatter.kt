package com.diabdata.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun formatLocalDate(
    date: LocalDate,
    pattern: String = "dd MMM yyyy",
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, locale)
    return date.format(formatter)
}

fun shortenedFormatLocalDate(
    date: LocalDate,
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)
    return date.format(formatter)
}
