package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.StringRes
import com.diabdata.shared.R as R

enum class GlucoseUnit (
    @param:StringRes
    val displayNameRes: Int,
) {
    MG_DL (
        displayNameRes = R.string.glucose_unit_mg_dl
    ),
    MMOL_L (
        displayNameRes = R.string.glucose_unit_mmol_l
    );

    fun displayName(context: Context): String = context.getString(displayNameRes)
}