package com.diabdata.shared.utils.dateUtils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun shortenedFormatLocalDate(
    date: LocalDate,
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)
    return date.format(formatter)
}

fun String.toShortenedFormatLocalDate(
    locale: Locale = Locale.getDefault()
): String {
    val date = LocalDate.parse(this)
    return shortenedFormatLocalDate(date, locale)
}

fun formatLocalDate(
    date: LocalDate,
    pattern: String = "dd MMM yyyy",
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, locale)
    return date.format(formatter)
}

fun String.toFormatLocalDate(
    locale: Locale = Locale.getDefault()
): String {
    val date = LocalDate.parse(this)
    return formatLocalDate(date, locale = locale)
}