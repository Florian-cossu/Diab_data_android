package com.diabdata.utils

fun linearRegression(x: List<Double>, y: List<Double>): (Double) -> Double {
    val n = x.size
    val sumX = x.sum()
    val sumY = y.sum()
    val sumXY = x.zip(y).sumOf { (xi, yi) -> xi * yi }
    val sumX2 = x.sumOf { xi -> xi * xi }

    val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
    val intercept = (sumY - slope * sumX) / n

    return { xi -> slope * xi + intercept }
}