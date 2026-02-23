package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.StringRes
import com.diabdata.shared.R as R


enum class BloodType(
    @param:StringRes
    val displayNameRes: Int,
) {
    A_POSITIVE(R.string.blood_type_a_positive),
    A_NEGATIVE(R.string.blood_type_a_negative),
    B_POSITIVE(R.string.blood_type_b_positive),
    B_NEGATIVE(R.string.blood_type_b_negative),
    AB_POSITIVE(R.string.blood_type_ab_positive),
    AB_NEGATIVE(R.string.blood_type_ab_negative),
    O_POSITIVE(R.string.blood_type_o_positive),
    O_NEGATIVE(R.string.blood_type_o_negative);

    fun displayName(context: Context): String = context.getString(displayNameRes)
}