package com.diabdata.utils

data class MedicalDeviceInfo(
    val gtin: String,
    val lot: String?,
    val expiration: String?,
    val serialNumber: String?,
    val referenceNumber: String?
)

fun parseMedicalDevice(raw: String): MedicalDeviceInfo {
    val cleaned = raw.trim()

    val regex = Regex(
        "01(\\d{14})|" +               // GTIN
                "10(.*?)" + generateDataMatrixDelimiters(listOf("10", "01")) + "|" + // Batch
                "17(\\d{6})|" +                 // Expiration
                "21(.*?)\u001D" +  // Serial
                "241(.*?)\u001D" // Reference
    )

    var gtin: String? = null
    var exp: String? = null
    var lot: String? = null
    var serialNumber: String? = null
    var referenceNumber: String? = null

    regex.findAll(cleaned).forEach { match ->
        when {
            match.groups[1] != null -> gtin = match.groups[1]?.value
            match.groups[2] != null -> lot = match.groups[2]?.value
            match.groups[3] != null -> {
                val rawDate = match.groups[3]?.value ?: ""
                exp = when (rawDate.length) {
                    4 -> { // MMYY
                        val mm = rawDate.substring(0, 2)
                        val yy = rawDate.substring(2, 4)
                        "20$yy-$mm-01"
                    }

                    6 -> { // YYMMDD
                        val yy = rawDate.substring(0, 2)
                        val mm = rawDate.substring(2, 4)
                        val dd = rawDate.substring(4, 6)
                        "20$yy-$mm-$dd"
                    }

                    8 -> { // YYYYMMDD
                        val yyyy = rawDate.substring(0, 4)
                        val mm = rawDate.substring(4, 6)
                        val dd = rawDate.substring(6, 8)
                        "$yyyy-$mm-$dd"
                    }

                    else -> rawDate
                }
            }

            match.groups[4] != null -> serialNumber = match.groups[4]?.value
            match.groups[5] != null -> referenceNumber = match.groups[5]?.value
        }
    }

    return MedicalDeviceInfo(
        gtin = gtin ?: "",
        lot = lot,
        expiration = exp,
        serialNumber = serialNumber,
        referenceNumber = referenceNumber
    )
}