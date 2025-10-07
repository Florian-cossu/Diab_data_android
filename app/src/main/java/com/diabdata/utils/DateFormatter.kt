package com.diabdata.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.diabdata.R
import java.time.LocalDate
import java.time.Period
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

fun String.toFormatLocalDate(
    locale: Locale = Locale.getDefault()
): String {
    val date = LocalDate.parse(this)
    return formatLocalDate(date, locale = locale)
}

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

@Composable
fun periodCountToString(period: Period): String {
    val years = period.years
    val months = period.months
    val days = period.days

    return when {
        years > 0 && months > 0 ->
            pluralStringResource(R.plurals.years_and_months, years, years, months)

        years > 0 ->
            pluralStringResource(R.plurals.plurals_years, years, years)

        months > 0 ->
            pluralStringResource(R.plurals.plurals_months, months, months)

        days > 0 ->
            pluralStringResource(R.plurals.plurals_days, days, days)

        else -> stringResource(R.string.today)
    }
}

@Composable
fun periodInPeriodToString(period: Period): String {
    val years = period.years
    val months = period.months
    val days = period.days

    return when {
        years > 0 && months > 0 ->
            pluralStringResource(R.plurals.in_years_and_months, years, years, months)

        years > 0 ->
            pluralStringResource(R.plurals.in_years, years, years)

        months > 0 ->
            pluralStringResource(R.plurals.in_months, months, months)

        days > 0 ->
            pluralStringResource(R.plurals.in_days, days, days)

        else -> stringResource(R.string.today)
    }
}
