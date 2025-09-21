package com.diabdata.utils

fun generateDataMatrixDelimiters(excludedDelimiters: List<String> = emptyList()): String {
    val delimiters = listOf(
        "01", // GTIN/CIP
        "10", // BATCH NUMBER
        "17", // EXPIRATION DATE
        "21", // SERIAL NUMBER
        "\u001D",
        "$"
    )

    val filtered = delimiters.filterNot { it in excludedDelimiters }

    return "(?:${filtered.joinToString("|")})"
}