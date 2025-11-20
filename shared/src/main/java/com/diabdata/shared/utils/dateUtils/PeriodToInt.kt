package com.diabdata.shared.utils.dateUtils

import java.time.LocalDate
import java.time.Period

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
    val period = Period.between(date, this)
    return period.getNumberOfDays()
}

fun LocalDate.getNumberOfMonths(date: LocalDate = LocalDate.now()): Int {
    val period = Period.between(date, this)
    return period.getNumberOfMonths()
}

fun LocalDate.getNumberOfYears(date: LocalDate = LocalDate.now()): Int {
    val period = Period.between(date, this)
    return period.getNumberOfYears()
}