package com.diabdata.shared.utils.dateUtils

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

fun Period.getNumberOfDays(): Int {
    return this.days
}

fun Period.getNumberOfMonths(): Int {
    return this.months
}

fun Period.getNumberOfYears(): Int {
    return this.years
}

fun LocalDate.getNumberOfDaysUntil(date: LocalDate = LocalDate.now()): Int {
    return ChronoUnit.DAYS.between(date, this).toInt()
}

fun LocalDate.getNumberOfMonths(date: LocalDate = LocalDate.now()): Int {
    return ChronoUnit.MONTHS.between(date, this).toInt()
}

fun LocalDate.getNumberOfYears(date: LocalDate = LocalDate.now()): Int {
    return ChronoUnit.YEARS.between(date, this).toInt()
}